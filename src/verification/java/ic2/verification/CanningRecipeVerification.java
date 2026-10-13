package ic2.verification;

import com.google.gson.GsonBuilder;
import ic2.api.recipe.*;
import ic2.core.block.machine.tileentity.TileEntitySolidCanner;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Every loaded solid-canning recipe runs through real placed canners and normal world ticks. */
public final class CanningRecipeVerification {
 public static volatile boolean finished;private record Job(String id,ItemStack container,ItemStack fill,ItemStack output){}private static final class Node{TileEntitySolidCanner machine;int index,age;}
 private final List<Job> jobs=new ArrayList<>();private final List<Node> nodes=new ArrayList<>();private final List<Map<String,Object>> cases=new ArrayList<>();private final List<String> failures=new ArrayList<>();private int checks;
 @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event){if(!nodes.isEmpty()||!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player))return;var s=AllMachinesVerification.ACTIVE;int n=0;
  for(var recipe:Recipes.cannerBottle.get(player.level()).getRecipes()){var input=recipe.getInput();if(input.container.getInputs().isEmpty()||input.fill.getInputs().isEmpty())continue;var container=input.container.getInputs().getFirst().copyWithCount(input.container.getAmount());var fill=input.fill.getInputs().getFirst().copyWithCount(input.fill.getAmount());jobs.add(new Job("canner_bottle/"+(n++),container,fill,recipe.getOutput().copy()));}
  for(int i=0;i<8;i++){var p=new BlockPos(512+i*16,181,1424);s.level.setChunkForced(p.getX()>>4,p.getZ()>>4,true);var node=new Node();node.machine=(TileEntitySolidCanner)s.helper("solid_canner",p);s.helper("creative_generator",p.west());node.index=i;nodes.add(node);feed(node);}save();
 }
 private void feed(Node n){n.age=0;n.machine.inputSlot.clear();n.machine.canInputSlot.clear();n.machine.outputSlot.clear();if(n.index<jobs.size()){var j=jobs.get(n.index);n.machine.inputSlot.put(j.fill.copy());n.machine.canInputSlot.put(j.container.copy());n.machine.setChanged();}}
 @SubscribeEvent public void tick(ServerTickEvent.Post event){if(finished||nodes.isEmpty())return;boolean done=true;for(var n:nodes){if(n.index>=jobs.size())continue;done=false;n.age++;var j=jobs.get(n.index);if(n.machine.outputSlot.isEmpty()&&n.age<2000)continue;var out=n.machine.outputSlot.get();boolean pass=ItemStack.matches(out,j.output)&&n.machine.inputSlot.isEmpty()&&n.machine.canInputSlot.isEmpty();checks++;if(!pass)failures.add(j.id+" incorrect canning result "+out);cases.add(Map.of("recipe",j.id,"container",j.container.toString(),"containerId",BuiltInRegistries.ITEM.getKey(j.container.getItem()).toString(),"fill",j.fill.toString(),"fillId",BuiltInRegistries.ITEM.getKey(j.fill.getItem()).toString(),"output",out.toString(),"ticks",n.age,"passed",pass));n.index+=nodes.size();feed(n);save();}if(done){finished=true;save();}}
 private void save(){try{Files.writeString(Path.of("ic2-canning-recipes-verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("checks",checks,"cases",cases,"failures",failures,"finished",finished,"complete",finished&&failures.isEmpty())));}catch(Exception e){throw new RuntimeException(e);}}
}
