package ic2.verification;

import static ic2.verification.AllMachinesVerification.*;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.recipe.*;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.*;
import ic2.core.block.machine.tileentity.*;
import ic2.core.block.storage.box.TileEntityStorageBox;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.ref.Ic2Items;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Behavior assertions are separate from placement/menu/snapshot smoke checks. */
final class AllMachineBehaviors {
   private final AllMachinesVerification suite;
   private final Set<String> generic = new LinkedHashSet<>();
   AllMachineBehaviors(AllMachinesVerification suite) { this.suite = suite; }

   void setup() {
      new AllGenerationCases(suite).setup();
      new AllFluidCases(suite).setup();
      new AllUuCases(suite).setup();
      new AllTransportCases(suite).setup();
      new AllReactorCases(suite).setup();
      new AllUtilityCases(suite).setup();
      new AllPowerControlCases(suite).setup();
      for (String id : suite.machines.keySet()) suite.guard(id, "configure scenario", () -> {
         var tile = suite.tile(id);
         if (tile instanceof TileEntityStandardMachine<?,?,?> machine
            && (machine.inputSlot instanceof InvSlotProcessableGeneric || id.equals("electric_furnace") || id.equals("recycler"))) {
            generic.add(id); genericMachine(id, machine);
         }
         if (tile instanceof TileEntityStorageBox box) storageBox(id, box);
         if (tile instanceof ic2.core.block.storage.tank.TileEntityTank) storageTank(id);
         if (id.equals("tank")) storageTank(id);
         if (id.equals("metal_former")) {
            suite.at(1000, () -> { suite.event(id, 0); });
            suite.at(1020, () -> prepareGeneric(id));
            suite.at(1600, () -> assertGenericOutput(id, "rolling mode"));
            suite.at(1700, () -> { suite.event(id, 0); });
            suite.at(1720, () -> prepareGeneric(id));
            suite.at(2400, () -> assertGenericOutput(id, "cutting mode"));
         }
         if (tile instanceof TileEntityInduction induction) {
            induction.inputSlotA.put(new ItemStack(Items.IRON_ORE));
            induction.inputSlotB.put(new ItemStack(Items.GOLD_ORE)); tile.setChanged();
            suite.at(2000, () -> {
               suite.behave(id, "both induction input slots smelt to matching outputs",
                  induction.outputSlotA.get().is(Items.IRON_INGOT) && induction.outputSlotB.get().is(Items.GOLD_INGOT));
               suite.observe(id, "outputs", List.of(induction.outputSlotA.get().toString(), induction.outputSlotB.get().toString()));
            });
         }
         if (id.equals("iron_furnace")) {
            suite.slot(id,"input").put(new ItemStack(Items.IRON_ORE));
            suite.slot(id,"fuel").put(new ItemStack(Items.COAL)); tile.setChanged();
            suite.at(700, () -> suite.behave(id, "coal smelts iron ore and consumes input",
               suite.slot(id,"output").get().is(Items.IRON_INGOT) && suite.slot(id,"input").isEmpty()));
         }
         if (tile instanceof TileEntityBatchCrafter batch && id.equals("batch_crafter")) {
            batch.craftingGrid[4]=new ItemStack(Items.OAK_LOG);
            batch.ingredientsRow[4].put(new ItemStack(Items.OAK_LOG)); batch.matrixChange(-1); batch.setChanged();
            suite.at(300,()->suite.behave(id,"offset 1x1 ghost recipe crafts four planks",batch.craftingOutput.get().is(Items.OAK_PLANKS)&&batch.craftingOutput.get().getCount()==4&&batch.ingredientsRow[4].isEmpty()));
            suite.at(400,()->{
               batch.craftingOutput.clear(); java.util.Arrays.fill(batch.craftingGrid,ItemStack.EMPTY);
               batch.craftingGrid[4]=new ItemStack(Ic2Items.FORGE_HAMMER); batch.craftingGrid[5]=new ItemStack(Items.IRON_INGOT);
               batch.ingredientsRow[4].put(new ItemStack(Ic2Items.FORGE_HAMMER)); batch.ingredientsRow[5].put(new ItemStack(Items.IRON_INGOT));
               batch.matrixChange(-1); batch.setChanged();
            });
            suite.at(650,()->{
               suite.behave(id,"tool recipe makes iron plate",batch.craftingOutput.get().is(Ic2Items.IRON_PLATE));
               suite.behave(id,"offset tool remainder stays in matching ingredient slot",batch.ingredientsRow[4].get().is(Ic2Items.FORGE_HAMMER)&&batch.ingredientsRow[4].get().getDamageValue()==1);
            });
         }
         if(tile instanceof TileEntityChunkloader loader)suite.at(500,()->{
            long own=net.minecraft.world.level.ChunkPos.pack(loader.getBlockPos());
            suite.behave(id,"powered loader contains its own chunk and is active",loader.getLoadedChunks().contains(own)&&loader.getActive());
            var ownChunk=net.minecraft.world.level.ChunkPos.unpack(own);
            loader.removeChunkFromLoaded(ownChunk);
            suite.behave(id,"own chunk cannot be deselected",loader.getLoadedChunks().contains(own));
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)loader.addChunkToLoaded(new net.minecraft.world.level.ChunkPos(ownChunk.x()+x,ownChunk.z()+z));
            suite.behave(id,"selection is capped at nine chunks",loader.getLoadedChunks().size()==loader.getMaxChunks());
         });
         if (Set.of("batbox","cesu","mfe","mfsu","classic_mfe","classic_mfsu",
            "batbox_chargepad","cesu_chargepad","mfe_chargepad","mfsu_chargepad").contains(id)) {
            InvSlot charge = (InvSlot)get(tile,"chargeSlot"); charge.put(stack("re_battery",1)); tile.setChanged();
            suite.at(900, () -> {
               double amount = ic2.api.item.ElectricItem.manager.getCharge(charge.get());
               suite.behave(id, "receives real EU network power and charges battery", amount > 0);
               suite.observe(id,"chargedBatteryEU",amount);
            });
         }
         if (id.endsWith("_transformer")) suite.at(200, () -> {
            var energy = tile.getComponent(Energy.class);
            suite.behave(id,"network fills buffer with configured voltage/direction",energy.getEnergy()>0);
            suite.observe(id,"sinkTier",energy.getSinkTier()); suite.observe(id,"sourceTier",energy.getSourceTier());
            suite.observe(id,"sourceDirections",energy.getSourceDirs().toString());
         });
      });
   }

   private void genericMachine(String id, TileEntityStandardMachine<?,?,?> machine) {
      suite.at(20, () -> suite.guard(id,"blocked output setup", () -> {
         prepareGeneric(id);
         for(int i=0;i<machine.outputSlot.size();i++) machine.outputSlot.put(i,new ItemStack(Items.BEDROCK,64));
         machine.setChanged();
      }));
      suite.at(100, () -> suite.guard(id,"output full check", () -> {
         suite.behave(id,"full output prevents processing and input loss", !machine.getActive() && !machine.inputSlot.isEmpty() && machine.getProgress()==0);
         machine.outputSlot.clear(); machine.setChanged();
      }));
      suite.at(id.equals("centrifuge")?6500:950, () -> assertGenericOutput(id,"normal processing"));
   }

   @SuppressWarnings({"rawtypes","unchecked"}) private void prepareGeneric(String id) {
      var machine=(TileEntityStandardMachine<?,?,?>)suite.tile(id);
      machine.outputSlot.clear();
      if(id.equals("electric_furnace")) machine.inputSlot.put(new ItemStack(Items.IRON_ORE));
      else if(id.equals("recycler")) {
         machine.inputSlot.put(new ItemStack(Items.COBBLESTONE,64));
         machine.upgradeSlot.put(0,new ItemStack(Ic2Items.OVERCLOCKER_UPGRADE,2));
      } else {
         Recipes.IGetter getter=(Recipes.IGetter)get(machine.inputSlot,"recipeManager");
         IMachineRecipeManager manager=(IMachineRecipeManager)getter.get(suite.level);
         MachineRecipe recipe=null;
         for(Object candidate:manager.getRecipes()) {
            var current=(MachineRecipe)candidate;
            if(current.getInput() instanceof IRecipeInput input && !input.getInputs().isEmpty()) {recipe=current;break;}
         }
         if(recipe==null)throw new IllegalStateException("No iterable recipe to exercise "+id);
         var input=(IRecipeInput)recipe.getInput(); var stack=input.getInputs().getFirst().copy(); stack.setCount(input.getAmount());
         machine.inputSlot.put(stack); suite.observe(id,"recipeInput",stack.toString());
      }
      if(id.equals("block_cutter")) ((TileEntityBlockCutter)machine).cutterSlot.put(new ItemStack(Ic2Items.DIAMOND_CUTTING_BLADE));
      if(id.equals("ore_washing_plant") && suite.tank(id,"fluid").isEmpty()) suite.fill(id,"fluid","minecraft:water",8000);
      machine.setChanged();
   }
   private void assertGenericOutput(String id,String stage) {
      suite.guard(id,stage,()->{
         var machine=(TileEntityStandardMachine<?,?,?>)suite.tile(id);
         suite.behave(id,stage+" creates output using actual world ticks",!machine.outputSlot.isEmpty());
         suite.observe(id,stage+" outputs",java.util.stream.IntStream.range(0,machine.outputSlot.size()).mapToObj(i->machine.outputSlot.get(i).toString()).toList());
         if(!id.equals("recycler")) suite.behave(id,stage+" consumes the recipe input",machine.inputSlot.isEmpty());
      });
   }

   private void storageBox(String id,TileEntityStorageBox box) {
      InvSlot inventory=(InvSlot)get(box,"inventory"); inventory.put(new ItemStack(Items.IRON_INGOT,13)); box.setChanged();
      suite.at(200,()->{
         ItemStack drop=box.adjustDrop(new ItemStack(block(id)),true);
         var restored=suite.helper(id,box.getBlockPos());
         suite.machines.put(id,restored);
         restored.onPlaced(drop,suite.player,net.minecraft.core.Direction.NORTH);
         InvSlot copy=(InvSlot)get(restored,"inventory");
         suite.behave(id,"break/place drop preserves 13 stored ingots",copy.get().is(Items.IRON_INGOT)&&copy.get().getCount()==13);
      });
   }
   private void storageTank(String id) {
      var tile=suite.tile(id);
      if(!tile.hasComponent(Fluids.class))throw new IllegalStateException("Tank has no fluids component");
      var tank=tile.getComponent(Fluids.class).getAllTanks().iterator().next();
      tank.fillMbUnchecked(Ic2FluidStack.create(fluid("coolant"),2250),false); tile.setChanged();
      suite.at(150,()->{
         var simulated=tank.drainMbUnchecked(250,true);
         suite.behave(id,"simulated extraction preserves fluid",simulated.getAmountMb()==250&&tank.getFluidAmount()==2250);
         var drained=tank.drainMbUnchecked(250,false);
         suite.behave(id,"actual extraction respects requested amount",drained.getAmountMb()==250&&tank.getFluidAmount()==2000);
      });
      if(tile instanceof ic2.core.block.storage.tank.TileEntityTank) suite.at(200,()->{
         ItemStack drop=tile.adjustDrop(new ItemStack(block(id)),true);
         var restored=suite.helper(id,tile.getBlockPos()); suite.machines.put(id,restored); restored.onPlaced(drop,suite.player,net.minecraft.core.Direction.NORTH);
         var copy=restored.getComponent(Fluids.class).getAllTanks().iterator().next();
         suite.behave(id,"break/place drop preserves 2000mB coolant",copy.getFluidAmount()==2000&&copy.getFluidStack().getFluid()==fluid("coolant"));
      });
   }
   void finish() { }
}
