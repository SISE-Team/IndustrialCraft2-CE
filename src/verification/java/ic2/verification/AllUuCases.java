package ic2.verification;

import static ic2.verification.AllMachinesVerification.*;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.*;
import ic2.core.item.ItemCrystalMemory;
import ic2.core.ref.Ic2Items;
import ic2.core.uu.UuIndex;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

final class AllUuCases {
   private final AllMachinesVerification s;
   AllUuCases(AllMachinesVerification suite){s=suite;}
   void setup(){
      for(String id:List.of("mass_fabricator","classic_mass_fabricator","matter_generator"))s.guard(id,"UU generation setup",()->{
         ((InvSlot)get(s.tile(id),"amplifierSlot")).put(stack("scrap",64));s.tile(id).setChanged();
         s.at(7500,()->{
            boolean produced=id.equals("matter_generator")?s.tank(id,"fluidTank").getFluidAmount()>0:s.slot(id,"output").get().is(Ic2Items.UU_MATTER);
            s.behave(id,"actual EU and scrap produce UU matter",produced);
            s.observe(id,"scrapRemaining",s.slot(id,"scrap").get().getCount());
            s.level.setBlockAndUpdate(s.tile(id).getBlockPos().east(),Blocks.REDSTONE_BLOCK.defaultBlockState());
         });
         s.at(7550,()->s.observe(id,"pausedProduction",id.equals("matter_generator")?s.tank(id,"fluidTank").getFluidAmount():s.slot(id,"output").get().getCount()));
         s.at(7800,()->{s.behave(id,"redstone pause stops UU production",((Number)s.rows.get(id).get("pausedProduction")).intValue()==(id.equals("matter_generator")?s.tank(id,"fluidTank").getFluidAmount():s.slot(id,"output").get().getCount()));s.level.setBlockAndUpdate(s.tile(id).getBlockPos().east(),Blocks.AIR.defaultBlockState());});
      });
      s.guard("uu_scanner","scanner setup",()->{
         var scanner=(TileEntityScanner)s.tile("uu_scanner");scanner.inputSlot.put(new ItemStack(Items.IRON_ORE));scanner.diskSlot.put(stack("crystal_memory",1));scanner.setChanged();
         s.at(4500,()->{
            s.behave("uu_scanner","3300 powered ticks complete scanning and consume source item",scanner.isDone()&&scanner.inputSlot.isEmpty());
            s.event("uu_scanner",1);
         });
         s.at(4530,()->{
            var memory=(ItemCrystalMemory)scanner.diskSlot.get().getItem();var recorded=memory.readItemStack(scanner.diskSlot.get());
            s.behave("uu_scanner","record button writes completed pattern onto crystal memory",recorded!=null&&recorded.is(Items.IRON_ORE));
            var storage=(TileEntityPatternStorage)s.tile("pattern_storage");storage.diskSlot.put(scanner.diskSlot.get().copy());s.event("pattern_storage",3);
         });
      });
      s.guard("pattern_storage","storage events setup",()->{
         var storage=(TileEntityPatternStorage)s.tile("pattern_storage");
         s.at(4560,()->{
            s.behave("pattern_storage","imports scanned memory and rejects duplicate pattern",storage.getPatterns().size()==1&&storage.getPatterns().getFirst().is(Items.IRON_ORE)&&!storage.addPattern(new ItemStack(Items.IRON_ORE)));
            storage.addPattern(new ItemStack(Items.STONE));s.event("pattern_storage",1);
         });
         s.at(4590,()->{s.behave("pattern_storage","next-pattern event selects second entry",storage.index==1&&storage.pattern.is(Items.STONE));s.event("pattern_storage",2);});
         s.at(4620,()->s.behave("pattern_storage","exports selected pattern to crystal",((ItemCrystalMemory)storage.diskSlot.get().getItem()).readItemStack(storage.diskSlot.get()).is(Items.STONE)));
      });
      s.guard("replicator","replication setup",()->{
         var replicator=(TileEntityReplicator)s.tile("replicator");s.fill("replicator","fluidTank","uu_matter",1000);
         s.at(100,()->s.event("replicator",4));
         s.at(200,()->s.behave("replicator","single run with no pattern stays stopped and preserves UU",!replicator.getActive()&&s.tank("replicator","fluidTank").getFluidAmount()==1000));
         s.at(4700,()->{
            var storage=(TileEntityPatternStorage)s.helper("pattern_storage",replicator.getBlockPos().east());
            storage.diskSlot.put(((TileEntityScanner)s.tile("uu_scanner")).diskSlot.get().copy());
            storage.onNetworkEvent(s.player,3);replicator.refreshInfo();
            s.observe("replicator","patternBucketCost",UuIndex.instance.getInBuckets(new ItemStack(Items.IRON_ORE)));s.event("replicator",4);
         });
         s.at(4900,()->{s.behave("replicator","single run duplicates scanned item and automatically stops",replicator.outputSlot.get().is(Items.IRON_ORE)&&replicator.outputSlot.get().getCount()==1&&!replicator.getActive()&&s.tank("replicator","fluidTank").getFluidAmount()<1000);s.event("replicator",5);});
         s.at(5100,()->{s.behave("replicator","continuous mode makes further output",replicator.outputSlot.get().getCount()>1);s.event("replicator",3);});
         s.at(5130,()->s.observe("replicator","stopCount",replicator.outputSlot.get().getCount()));
         s.at(5300,()->s.behave("replicator","stop button halts replication",!replicator.getActive()&&replicator.outputSlot.get().getCount()==((Number)s.rows.get("replicator").get("stopCount")).intValue()));
      });
      s.guard("uu_assembly_bench","UU assembly setup",()->{
         var bench=(TileEntityAssemblyBench)s.tile("uu_assembly_bench");
         java.util.Arrays.fill(bench.craftingGrid,ItemStack.EMPTY);bench.craftingGrid[1]=stack("uu_matter",1);bench.ingredientsRow[1].put(stack("uu_matter",1));bench.matrixChange(-1);bench.setChanged();
         s.at(300,()->{s.behave("uu_assembly_bench","original top-center UU layout produces eight oak logs",bench.craftingOutput.get().is(Items.OAK_LOG)&&bench.craftingOutput.get().getCount()==8);bench.craftingOutput.clear();java.util.Arrays.fill(bench.craftingGrid,ItemStack.EMPTY);bench.craftingGrid[4]=stack("uu_matter",1);bench.ingredientsRow[4].put(stack("uu_matter",1));bench.matrixChange(-1);bench.setChanged();});
         s.at(600,()->s.behave("uu_assembly_bench","original center UU layout distinguishes stone from wood",bench.craftingOutput.get().is(Items.STONE)&&bench.craftingOutput.get().getCount()==16&&bench.ingredientsRow[4].isEmpty()));
      });
   }
}
