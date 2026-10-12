package ic2.verification;

import static ic2.verification.AllMachinesVerification.*;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.*;
import ic2.core.block.storage.box.TileEntityStorageBox;
import ic2.core.fluid.*;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;

final class AllTransportCases {
   private final AllMachinesVerification s;
   AllTransportCases(AllMachinesVerification suite){s=suite;}
   private InvSlot chest(String id,Direction side){return (InvSlot)get(s.helper("iron_storage_box",s.tile(id).getBlockPos().relative(side)),"inventory");}
   private Ic2FluidTank tank(String id,Direction side){return s.helper("tank",s.tile(id).getBlockPos().relative(side)).getComponent(Fluids.class).getAllTanks().iterator().next();}
   void setup(){
      s.guard("weighted_item_distributor","item priority setup",()->{
         var tile=(TileEntityWeightedItemDistributor)s.tile("weighted_item_distributor");var first=chest("weighted_item_distributor",Direction.EAST);var second=chest("weighted_item_distributor",Direction.SOUTH);
         tile.getPriority().addAll(List.of(Direction.EAST,Direction.SOUTH));tile.updatePriority(true);tile.buffer.put(new ItemStack(Items.IRON_INGOT,23));tile.setChanged();
         s.at(200,()->s.behave("weighted_item_distributor","first-priority inventory receives exactly 23 items",first.get().getCount()==23&&second.isEmpty()&&tile.buffer.isEmpty()));
         s.at(250,()->{for(int i=0;i<first.size();i++)first.put(i,new ItemStack(Items.BEDROCK,64));tile.buffer.put(new ItemStack(Items.GOLD_INGOT,11));tile.setChanged();});
         s.at(400,()->s.behave("weighted_item_distributor","full first target falls back to second without loss",second.get().is(Items.GOLD_INGOT)&&second.get().getCount()==11&&tile.buffer.isEmpty()));
      });
      s.guard("sorting_machine","filtered routing setup",()->{
         var tile=(TileEntitySortingMachine)s.tile("sorting_machine");var match=chest("sorting_machine",Direction.EAST);var fallback=chest("sorting_machine",Direction.SOUTH);
         tile.defaultRoute=Direction.SOUTH;tile.getFilterSlots(Direction.EAST)[0]=new ItemStack(Items.IRON_INGOT,2);tile.buffer.put(0,new ItemStack(Items.IRON_INGOT,8));tile.buffer.put(1,new ItemStack(Items.GOLD_INGOT,7));tile.setChanged();
         s.at(300,()->s.behave("sorting_machine","matched batch routes east and unmatched items use default south",match.get().is(Items.IRON_INGOT)&&match.get().getCount()==8&&fallback.get().is(Items.GOLD_INGOT)&&fallback.get().getCount()==7&&tile.buffer.isEmpty()));
      });
      s.guard("item_buffer","buffer ejection setup",()->{
         var tile=(TileEntityItemBuffer)s.tile("item_buffer");var recipient=chest("item_buffer",Direction.EAST);tile.rightcontentSlot.put(new ItemStack(Items.IRON_INGOT,19));tile.leftcontentSlot.put(new ItemStack(Items.GOLD_INGOT,17));tile.upgradeSlot.put(0,stack("ejector_upgrade",1));tile.setChanged();
         s.at(500,()->{int total=0;for(var item:recipient)total+=item.getCount();s.behave("item_buffer","ejector transfers buffered inventories without duplication",total==36&&tile.rightcontentSlot.isEmpty()&&tile.leftcontentSlot.isEmpty());s.observe("item_buffer","deliveredItems",total);});
      });
      for(String id:List.of("fluid_distributor","weighted_fluid_distributor"))s.guard(id,"fluid routing setup",()->{
         var tile=(TileEntityFluidDistributor)s.tile(id);var east=tank(id,Direction.EAST);var south=tank(id,Direction.SOUTH);
         if(tile instanceof TileEntityWeightedFluidDistributor priority){priority.getPriority().addAll(List.of(Direction.EAST,Direction.SOUTH));priority.updatePriority(true);}
         s.fill(id,"fluidTank","coolant",1000);
         s.at(200,()->{
            s.behave(id,"distributes 1000mB to connected tanks with volume conservation",east.getFluidAmount()+south.getFluidAmount()==1000&&tile.fluidTank.isEmpty());
            s.behave(id,tile instanceof TileEntityWeightedFluidDistributor?"highest-priority tank receives all fluid":"two equal receivers share fluid equally",tile instanceof TileEntityWeightedFluidDistributor?east.getFluidAmount()==1000:east.getFluidAmount()==500&&south.getFluidAmount()==500);
         });
         if(id.equals("fluid_distributor")){
            var front=tank(id,Direction.NORTH);s.at(300,()->s.event(id,0));s.at(330,()->s.fill(id,"fluidTank","coolant",1000));
            s.at(450,()->s.behave(id,"inverted mode consolidates fluid into front tank",tile.getActive()&&front.getFluidAmount()==1000&&tile.fluidTank.isEmpty()));
         }
      });
      s.guard("fluid_regulator","metered fluid setup",()->{
         var tile=(TileEntityFluidRegulator)s.tile("fluid_regulator");var recipient=tank("fluid_regulator",Direction.NORTH);s.fill("fluid_regulator","fluidTank","coolant",1000);s.at(20,()->s.event("fluid_regulator",100));
         s.at(250,()->{s.behave("fluid_regulator","per-second mode transfers exact configured volume",recipient.getFluidAmount()==1000&&s.tank("fluid_regulator","fluidTank").isEmpty());s.event("fluid_regulator",1001);s.fill("fluid_regulator","fluidTank","coolant",1000);});
         s.at(300,()->s.behave("fluid_regulator","per-tick mode transfers same volume faster",recipient.getFluidAmount()==2000&&s.tank("fluid_regulator","fluidTank").isEmpty()));
         s.at(350,()->{recipient.fillMbUnchecked(Ic2FluidStack.create(fluid("coolant"),recipient.getCapacity()-recipient.getFluidAmount()-37),false);s.fill("fluid_regulator","fluidTank","coolant",100);});
         s.at(400,()->s.behave("fluid_regulator","partial receiver capacity preserves untransferred fluid",recipient.getFluidAmount()==recipient.getCapacity()&&s.tank("fluid_regulator","fluidTank").getFluidAmount()==63));
      });
   }
}
