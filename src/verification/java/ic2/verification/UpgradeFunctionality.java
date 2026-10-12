package ic2.verification;

import static ic2.verification.AllMachinesVerification.*;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.util.StackTag;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** Observes real upgrade transfers and processing, independently of the rates matrix. */
final class UpgradeFunctionality {
   private final ExtendedValidation e;
   private final AllMachinesVerification s;
   private final Map<String,Ic2TileEntity> tiles=new LinkedHashMap<>();
   private final Map<String,InvSlot> sinks=new LinkedHashMap<>();
   private final Map<String,Ic2FluidTank> fluidSinks=new LinkedHashMap<>();
   private final Map<Integer,List<Runnable>> jobs=new TreeMap<>();
   private final List<Map<String,Object>> results=new ArrayList<>();
   private int age,number;
   UpgradeFunctionality(ExtendedValidation owner){e=owner;s=owner.s;}
   private Ic2TileEntity machine(String label,String id){
      var pos=new BlockPos(1536+(number%8)*16,201,176+(number/8)*16);number++;
      for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)s.level.setChunkForced((pos.getX()>>4)+x,(pos.getZ()>>4)+z,true);
      var tile=s.helper(id,pos);tiles.put(label,tile);return tile;
   }
   private InvSlotUpgrade upgrades(Ic2TileEntity tile){return (InvSlotUpgrade)((TileEntityInventory)tile).getInventorySlot("upgrade");}
   private void install(Ic2TileEntity tile,ItemStack stack){var slot=upgrades(tile);e.check("functional fixture accepts "+stack,slot.accepts(stack));slot.put(0,stack);slot.onChanged();tile.setChanged();}
   private void at(int tick,Runnable job){jobs.computeIfAbsent(tick,k->new ArrayList<>()).add(job);}
   private void record(String test,Object actual,boolean passed){e.check(test,passed);results.add(Map.of("test",test,"actual",actual,"passed",passed));}
   private InvSlot box(Ic2TileEntity tile){var sink=s.helper("iron_storage_box",tile.getBlockPos().east());return (InvSlot)get(sink,"inventory");}
   private ItemStack filtered(String upgrade,ItemStack filter){
      var result=stack(upgrade,1);var tag=new CompoundTag();var list=new ListTag();var item=new CompoundTag();
      StackTag.save(s.level.registryAccess(),filter,item);item.putByte("Slot",(byte)0);list.add(item);tag.put("Items",list);
      tag.putByte("dir",(byte)(Direction.EAST.ordinal()+1));StackTag.set(result,tag);return result;
   }
   void prepare(){
      for(String label:List.of("control","overclock","transformer","storage")){
         var tile=(TileEntityStandardMachine<?,?,?>)machine(label,"electric_furnace");
         s.helper("creative_generator",tile.getBlockPos().west());
         if(label.equals("overclock"))install(tile,stack("overclocker_upgrade",2));
         if(label.equals("transformer"))install(tile,stack("transformer_upgrade",2));
         if(label.equals("storage"))install(tile,stack("energy_storage_upgrade",64));
      }
      at(20,()->{
         for(String label:List.of("control","overclock")){var tile=(TileEntityStandardMachine<?,?,?>)tiles.get(label);tile.inputSlot.put(new ItemStack(Items.IRON_ORE,8));tile.setChanged();}
         var trans=(TileEntityStandardMachine<?,?,?>)tiles.get("transformer");record("transformer stack raises actual sink tier by two",trans.getComponent(Energy.class).getSinkTier(),trans.getComponent(Energy.class).getSinkTier()==trans.defaultTier+2);
         var storage=(TileEntityStandardMachine<?,?,?>)tiles.get("storage");record("storage upgrade increases actual machine buffer",storage.getComponent(Energy.class).getCapacity(),storage.getComponent(Energy.class).getCapacity()>=storage.defaultEnergyStorage+640000);
      });
      at(110,()->{var normal=(TileEntityStandardMachine<?,?,?>)tiles.get("control");var fast=(TileEntityStandardMachine<?,?,?>)tiles.get("overclock");record("overclocked furnace completes real recipes before control",fast.outputSlot.get().getCount(),fast.outputSlot.get().getCount()>normal.outputSlot.get().getCount());});
      at(600,()->{for(String label:List.of("control","overclock")){var tile=(TileEntityStandardMachine<?,?,?>)tiles.get(label);int total=tile.inputSlot.get().getCount()+tile.outputSlot.get().getCount();record(label+" actual furnace conserves all eight inputs",total,total==8&&tile.outputSlot.get().is(Items.IRON_INGOT));}});
      for(String upgrade:List.of("ejector_upgrade","advanced_ejector_upgrade")){
         var tile=machine(upgrade,"item_buffer");var iron=(InvSlot)get(tile,"rightcontentSlot");var gold=(InvSlot)get(tile,"leftcontentSlot");
         iron.put(new ItemStack(Items.IRON_INGOT,13));gold.put(new ItemStack(Items.GOLD_INGOT,11));sinks.put(upgrade,box(tile));
         install(tile,upgrade.startsWith("advanced")?filtered(upgrade,new ItemStack(Items.IRON_INGOT)):stack(upgrade,1));
         at(200,()->{int n=0,g=0;for(var item:sinks.get(upgrade)){if(item.is(Items.IRON_INGOT))n+=item.getCount();if(item.is(Items.GOLD_INGOT))g+=item.getCount();}record(upgrade+" transfers iron exactly once",n,n==13&&iron.isEmpty());record(upgrade+" applies its real whitelist",g,upgrade.startsWith("advanced")?g==0&&gold.get().getCount()==11:g==11&&gold.isEmpty());});
      }
      for(String upgrade:List.of("pulling_upgrade","advanced_pulling_upgrade")){
         var tile=(TileEntityStandardMachine<?,?,?>)machine(upgrade,"electric_furnace");var source=box(tile);sinks.put(upgrade,source);
         if(upgrade.startsWith("advanced")){source.put(0,new ItemStack(Items.GOLD_ORE,11));source.put(1,new ItemStack(Items.IRON_ORE,13));}
         else source.put(0,new ItemStack(Items.IRON_ORE,13));
         install(tile,upgrade.startsWith("advanced")?filtered(upgrade,new ItemStack(Items.IRON_ORE)):stack(upgrade,1));
         at(200,()->{record(upgrade+" actually pulls thirteen iron ores",tile.inputSlot.get().getCount(),tile.inputSlot.get().is(Items.IRON_ORE)&&tile.inputSlot.get().getCount()==13);if(upgrade.startsWith("advanced"))record("advanced pull leaves excluded gold in source",source.get().getCount(),source.get().is(Items.GOLD_ORE)&&source.get().getCount()==11);});
      }
      for(String upgrade:List.of("fluid_ejector_upgrade","fluid_pulling_upgrade")){
         var tile=machine(upgrade,"fermenter");var adjacent=s.helper("iron_tank",tile.getBlockPos().east());var tank=(Ic2FluidTank)get(adjacent,"contents");fluidSinks.put(upgrade,tank);
         if(upgrade.equals("fluid_ejector_upgrade"))((Ic2FluidTank)get(tile,"outputTank")).fillMbUnchecked(Ic2FluidStack.create(fluid("biogas"),1000),false);
         else tank.fillMbUnchecked(Ic2FluidStack.create(fluid("biomass"),1000),false);
         install(tile,stack(upgrade,1));
         at(200,()->{var inside=(Ic2FluidTank)get(tile,upgrade.equals("fluid_ejector_upgrade")?"outputTank":"inputTank");record(upgrade+" transfers real 1000mB with conservation",List.of(inside.getFluidAmount(),tank.getFluidAmount()),inside.getFluidAmount()+tank.getFluidAmount()==1000&&(upgrade.equals("fluid_ejector_upgrade")?tank.getFluidAmount()==1000:inside.getFluidAmount()==1000));});
      }
      var inverter=machine("inverter","induction_furnace");install(inverter,stack("redstone_inverter_upgrade",1));
      at(20,()->record("redstone inverter produces signal without external source",inverter.getComponent(Redstone.class).getRedstoneInput(),inverter.getComponent(Redstone.class).getRedstoneInput()==15));
      at(40,()->s.level.setBlockAndUpdate(inverter.getBlockPos().south(),Blocks.REDSTONE_BLOCK.defaultBlockState()));
      at(60,()->record("redstone inverter suppresses an actual neighboring signal",inverter.getComponent(Redstone.class).getRedstoneInput(),inverter.getComponent(Redstone.class).getRedstoneInput()==0));
   }
   boolean tick(){age++;for(var job:jobs.getOrDefault(age,List.of()))job.run();if(age<600)return false;
      e.details.put("upgradeFunctionalCases",results);
      for(var tile:tiles.values()){s.level.setBlockAndUpdate(tile.getBlockPos(),Blocks.AIR.defaultBlockState());s.level.setBlockAndUpdate(tile.getBlockPos().east(),Blocks.AIR.defaultBlockState());s.level.setBlockAndUpdate(tile.getBlockPos().west(),Blocks.AIR.defaultBlockState());s.level.setBlockAndUpdate(tile.getBlockPos().south(),Blocks.AIR.defaultBlockState());}
      return true;
   }
}
