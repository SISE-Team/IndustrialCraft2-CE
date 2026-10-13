package ic2.verification;

import com.google.gson.GsonBuilder;
import ic2.api.recipe.*;
import ic2.core.block.comp.*;
import ic2.core.block.machine.tileentity.*;
import ic2.core.fluid.Ic2FluidStack;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Runs every loaded standard item-processing recipe through normal placed-machine ticks. */
public final class ProcessingRecipeVerification {
    public static volatile boolean finished;
    private record Job(String id,ItemStack input,List<ItemStack> output,boolean weighted){}
    private static final Map<String,String> MACHINES=Map.of("macerator","macerator","extractor","extractor","compressor","compressor","centrifuge","centrifuge","ore_washing","ore_washing_plant","metal_former_extruding","metal_former","metal_former_rolling","metal_former","metal_former_cutting","metal_former","block_cutter","block_cutter");
    private static final class Program {String type,name;TileEntityStandardMachine<?,?,?> machine;final List<Job> jobs=new ArrayList<>();int index=-1,age;}
    private final Map<String,Program> programs=new LinkedHashMap<>();private final List<Map<String,Object>> cases=new ArrayList<>();private final List<String> failures=new ArrayList<>();private final List<Map<String,Object>> unavailable=new ArrayList<>();private int checks;private boolean started;
    private void check(boolean ok,String name){checks++;if(!ok)failures.add(name);}
    private void save(){var report=Map.of("checks",checks,"cases",cases,"failures",failures,"finished",finished,"complete",finished&&failures.isEmpty(),"execution","server_placed_machines_normal_ticks","unavailableRecipes",unavailable);try{Files.writeString(Path.of("ic2-processing-recipes-verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception e){throw new RuntimeException(e);}}
    private void prepare(AllMachinesVerification s){
        for(var holder:s.level.getServer().getRecipeManager().getRecipes())if(holder.value() instanceof ic2.core.recipe.v2.RecipeHolder<?,?> wrapped){
            String type=BuiltInRegistries.RECIPE_TYPE.getKey(wrapped.getType()).getPath();if(!MACHINES.containsKey(type)||!(wrapped.recipe().getInput() instanceof IRecipeInput input)||!(wrapped.recipe().getOutput() instanceof Collection<?> outputs))continue;
            boolean weighted=wrapped.recipe() instanceof MachineRecipeWeighted;var offered=input.getInputs();if(offered.isEmpty()){unavailable.add(Map.of("recipe",holder.id().identifier().toString(),"input",input.toString(),"reason","ingredient has no registered item in this isolated mod set"));continue;}var sample=offered.getFirst().copyWithCount(input.getAmount());if(weighted)outputs=((MachineRecipeWeighted<?>)wrapped.recipe()).getOutputWeighted().getOutputs();var expected=new ArrayList<ItemStack>();for(var value:outputs)if(value instanceof ItemStack stack&&!stack.isEmpty())expected.add(stack.copy());
            var program=programs.computeIfAbsent(type,k->{var p=new Program();p.type=k;p.name=MACHINES.get(k);return p;});program.jobs.add(new Job(holder.id().identifier().toString(),sample,expected,weighted));
            // Exercise every IC2 item alternative in tagged machine ingredients as well.
            for(var variant:offered)if(BuiltInRegistries.ITEM.getKey(variant.getItem()).getNamespace().equals("ic2")&&variant.getItem()!=sample.getItem())program.jobs.add(new Job(holder.id().identifier()+" ["+variant.getItem()+"]",variant.copyWithCount(input.getAmount()),expected,weighted));
        }
        int index=0;for(var p:programs.values()){
            p.jobs.sort(Comparator.comparing(Job::id));var pos=new BlockPos(520+(index++%5)*16,181,520+(index/5)*16);s.level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);s.level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);
            p.machine=(TileEntityStandardMachine<?,?,?>)s.helper(p.name,pos);s.helper("creative_generator",pos.west());
            if(p.machine instanceof TileEntityMetalFormer former)former.setMode(p.type.endsWith("rolling")?1:p.type.endsWith("cutting")?2:0);
            if(p.machine instanceof TileEntityBlockCutter cutter)((ic2.core.block.invslot.InvSlot)AllMachinesVerification.get(cutter,"cutterSlot")).put(AllMachinesVerification.stack("diamond_cutting_blade",1));
            next(p);
        }
        check(programs.keySet().containsAll(MACHINES.keySet()),"all standard processing manager categories are loaded");s.level.getServer().tickRateManager().requestGameToSprint(2_000_000);started=true;save();
    }
    private void next(Program p){p.index++;p.age=0;if(p.index>=p.jobs.size())return;var job=p.jobs.get(p.index);p.machine.inputSlot.clear();p.machine.outputSlot.clear();p.machine.inputSlot.put(job.input.copy());
        if(p.machine instanceof TileEntityOreWashing){var tank=p.machine.getComponent(ic2.core.block.comp.Fluids.class).getAllTanks().iterator().next();tank.drainMbUnchecked(Integer.MAX_VALUE,false);tank.fillMbUnchecked(Ic2FluidStack.create(Fluids.WATER,tank.getCapacity()),false);}
        p.machine.setChanged();
    }
    @SubscribeEvent public void tick(ServerTickEvent.Post event){if(finished)return;var s=AllMachinesVerification.ACTIVE;if(s==null||!s.baseFinished())return;
        try{if(!started){prepare(s);return;}boolean all=true;for(var p:programs.values()){
            if(p.index>=p.jobs.size())continue;all=false;p.age++;var job=p.jobs.get(p.index);boolean consumed=p.machine.inputSlot.isEmpty()||p.machine.inputSlot.get().getItem()!=job.input.getItem();if(!consumed&&p.age<30000)continue;
            var actual=new ArrayList<ItemStack>();for(var stack:p.machine.outputSlot)if(!stack.isEmpty())actual.add(stack.copy());boolean outputOk=!actual.isEmpty();
            if(job.weighted)outputOk=actual.size()==1&&job.output.stream().anyMatch(expected->ItemStack.matches(expected,actual.getFirst()));
            else for(var expected:job.output){int total=actual.stream().filter(stack->ItemStack.isSameItemSameComponents(stack,expected)).mapToInt(ItemStack::getCount).sum();outputOk&=total>=expected.getCount();}
            boolean pass=consumed&&outputOk;check(pass,job.id+" consumes input and produces expected output");cases.add(Map.of("recipe",job.id,"machine",p.name,"input",job.input.toString(),"usedItem",BuiltInRegistries.ITEM.getKey(job.input.getItem()).toString(),"output",actual.stream().map(Object::toString).toList(),"ticks",p.age,"passed",pass));next(p);
        }if(all){finished=true;s.level.getServer().tickRateManager().stopSprinting();}if(cases.size()%10==0||finished)save();}
        catch(Throwable e){failures.add("suite: "+e);e.printStackTrace();finished=true;s.level.getServer().tickRateManager().stopSprinting();save();}
    }
}
