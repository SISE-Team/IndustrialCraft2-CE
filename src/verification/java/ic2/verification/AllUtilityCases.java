package ic2.verification;

import static ic2.verification.AllMachinesVerification.*;
import ic2.api.item.ElectricItem;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.*;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.block.personal.*;
import ic2.core.crop.TileEntityCrop;
import ic2.core.ref.Ic2Items;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import com.mojang.authlib.GameProfile;

final class AllUtilityCases {
   private final AllMachinesVerification s;
   AllUtilityCases(AllMachinesVerification suite){s=suite;}
   private InvSlot chest(String id,Direction side){return (InvSlot)get(s.helper("iron_storage_box",s.tile(id).getBlockPos().relative(side)),"inventory");}
   void setup(){
      s.guard("miner","miner input setup",()->{
         var miner=(TileEntityMiner)s.tile("miner");var recipient=chest("miner",Direction.EAST);
         miner.drillSlot.put(stack("diamond_drill",1));ElectricItem.manager.charge(miner.drillSlot.get(),100000,99,true,false);miner.pipeSlot.put(stack("mining_pipe",8));miner.setChanged();s.level.setBlockAndUpdate(miner.getBlockPos().below(),Blocks.IRON_ORE.defaultBlockState());
         s.at(1500,()->{int count=0;for(var item:miner.buffer)if(item.is(Items.RAW_IRON))count+=item.getCount();s.behave("miner","charged drill mines ore and stores raw iron in internal buffer",count>0&&!s.level.getBlockState(miner.getBlockPos().below()).is(Blocks.IRON_ORE));s.observe("miner","rawIronCount",count);s.observe("miner","blockBelow",s.level.getBlockState(miner.getBlockPos().below()).toString());miner.drillSlot.clear();miner.setChanged();});
         s.at(2500,()->s.behave("miner","removing drill retracts mining pipe into inventory",!s.level.getBlockState(miner.getBlockPos().below()).is(block("mining_pipe"))&&!s.level.getBlockState(miner.getBlockPos().below()).is(block("mining_pipe_tip"))));
         s.at(3000,()->{
            var pump=(TileEntityPump)s.helper("pump",miner.getBlockPos().east());s.helper("creative_generator",pump.getBlockPos().above());
            s.level.setBlockAndUpdate(miner.getBlockPos().below(),Blocks.WATER.defaultBlockState());s.level.setBlockAndUpdate(miner.getBlockPos().below(2),Blocks.IRON_ORE.defaultBlockState());s.level.setBlockAndUpdate(miner.getBlockPos().below(3),Blocks.BEDROCK.defaultBlockState());
            miner.drillSlot.put(stack("diamond_drill",1));ElectricItem.manager.charge(miner.drillSlot.get(),100000,99,true,false);miner.pipeSlot.put(stack("mining_pipe",8));miner.setChanged();
            for(int probe:List.of(3050,3500,4000))s.at(probe,()->{int iron=0;for(var item:miner.buffer)if(item.is(Items.RAW_IRON))iron+=item.getCount();s.observe("miner","linkedPumpAt"+probe,List.of(pump.getComponent(ic2.core.block.comp.Fluids.class).getFluidTank("fluid").getFluidAmount(),pump.getEnergy(),pump.progress,miner.getEnergy(),miner.progress,miner.canProvideLiquid,iron,s.level.getBlockState(miner.getBlockPos().below()).toString(),s.level.getBlockState(miner.getBlockPos().below(2)).toString()));});
            s.at(4000,()->{int iron=0;for(var item:miner.buffer)if(item.is(Items.RAW_IRON))iron+=item.getCount();s.behave("miner","connected pump removes intervening water and mining continues into ore",pump.getComponent(ic2.core.block.comp.Fluids.class).getFluidTank("fluid").getFluidAmount()==1000&&iron>=2);miner.drillSlot.clear();miner.setChanged();});
         });
      });
      s.guard("advanced_miner","advanced mining setup",()->{
         var miner=(TileEntityAdvMiner)s.tile("advanced_miner");var recipient=chest("advanced_miner",Direction.EAST);miner.scannerSlot.put(stack("scanner",1));ElectricItem.manager.charge(miner.scannerSlot.get(),100000,99,true,false);miner.filterSlot.put(new ItemStack(Items.RAW_IRON));miner.setChanged();
         if((boolean)get(miner,"blacklist"))miner.onNetworkEvent(s.player,1);
         var ore=miner.getBlockPos().offset(-16,-1,-16);s.level.setBlockAndUpdate(ore,Blocks.IRON_ORE.defaultBlockState());
         s.at(2500,()->{int count=0;for(var item:recipient)if(item.is(Items.RAW_IRON))count+=item.getCount();s.behave("advanced_miner","powered scanner and whitelist mine matching ore and route drops",count>0&&s.level.getBlockState(ore).isAir());s.observe("advanced_miner","rawIronCount",count);s.level.setBlockAndUpdate(miner.getBlockPos().south(),Blocks.REDSTONE_BLOCK.defaultBlockState());});
         s.at(2530,()->{s.event("advanced_miner",1);s.event("advanced_miner",2);});
         s.at(2550,()->s.behave("advanced_miner","network buttons toggle blacklist and silk touch modes while stopped",(boolean)get(miner,"blacklist")&&(boolean)get(miner,"silkTouch")));
      });
      s.guard("pump","world-source pump setup",()->{
         var pump=(TileEntityPump)s.tile("pump");var water=pump.getBlockPos().north();s.level.setBlockAndUpdate(water,Blocks.WATER.defaultBlockState());
         s.at(490,()->s.observe("pump","pumpSnapshot",List.of(pump.outputSlot.get().toString(),pump.containerSlot.get().toString(),s.tank("pump","fluid").getFluidAmount(),pump.progress,s.level.getBlockState(water).toString())));
         for(Direction dir:List.of(Direction.NORTH,Direction.EAST,Direction.WEST,Direction.DOWN))s.level.setBlockAndUpdate(water.relative(dir),Blocks.GLASS.defaultBlockState());
         pump.containerSlot.put(new ItemStack(Items.BUCKET));pump.setChanged();
         s.at(500,()->s.behave("pump","normal world ticks pump source water into a bucket",pump.outputSlot.get().is(Items.WATER_BUCKET)&&pump.containerSlot.isEmpty()&&!s.level.getBlockState(water).is(Blocks.WATER)));
         s.at(600,()->{pump.outputSlot.clear();pump.containerSlot.put(new ItemStack(Items.BUCKET));s.level.setBlockAndUpdate(water,Blocks.LAVA.defaultBlockState());pump.setChanged();});
         s.at(1100,()->s.behave("pump","lava source pumps into lava bucket without duplicating source",pump.outputSlot.get().is(Items.LAVA_BUCKET)&&pump.containerSlot.isEmpty()&&s.tank("pump","fluid").isEmpty()&&s.level.getBlockState(water).isAir()));
         s.at(1200,()->{s.fill("pump","fluid","minecraft:water",8000);s.level.setBlockAndUpdate(water,Blocks.WATER.defaultBlockState());});
         s.at(1400,()->{s.behave("pump","full output tank stops pumping without deleting source",s.tank("pump","fluid").getFluidAmount()==8000&&s.level.getBlockState(water).is(Blocks.WATER)&&!pump.getActive());s.tank("pump","fluid").drainMbUnchecked(Integer.MAX_VALUE,false);s.level.setBlockAndUpdate(water,Blocks.AIR.defaultBlockState());});
      });
      for(String id:List.of("crop_harvester","cropmatron","classic_cropmatron"))s.guard(id,"crop machine setup",()->{
         var pos=s.tile(id).getBlockPos().east(2);s.level.setBlockAndUpdate(pos.below(),Blocks.FARMLAND.defaultBlockState());var crop=(TileEntityCrop)s.helper("wheat_crop",pos);
         if(id.equals("crop_harvester")){
            crop.setCurrentAge(crop.getCrop().getMaxAge());
            crop.setStatGain(31);
            for(int x=2;x<=3;x++)for(int z=-1;z<=1;z++)if(x!=2||z!=0){var extra=s.tile(id).getBlockPos().offset(x,0,z);s.level.setBlockAndUpdate(extra.below(),Blocks.FARMLAND.defaultBlockState());var mature=(TileEntityCrop)s.helper("wheat_crop",extra);mature.setStatGain(31);mature.setCurrentAge(mature.getCrop().getMaxAge());mature.setChanged();}
            s.at(4500,()->{var contents=s.slot(id,"content");int count=0;for(var item:contents)count+=item.getCount();s.behave(id,"ripe IC2 wheat is harvested into machine inventory",count>0);s.observe(id,"harvestedItemCount",count);s.observe(id,"cropBlock",s.level.getBlockState(pos).toString());s.observe(id,"cropAge",crop.getCurrentAge());s.observe(id,"scan",List.of(get(s.tile(id),"scanX"),get(s.tile(id),"scanY"),get(s.tile(id),"scanZ")));});
         }else{
            crop.setStorageWater(0);crop.setStorageNutrients(0);crop.setStorageWeedEX(0);s.slot(id,"fertilizer").put(stack("fertilizer",3));
            if(id.equals("cropmatron")){s.fill(id,"waterTank","minecraft:water",1000);s.fill(id,"exTank","weed_ex",1000);}
            else{s.slot(id,"hydration").put(stack("hydration_cell",1));s.slot(id,"weedEx").put(stack("weed_ex_cell",1));}
            s.at(4500,()->{s.behave(id,"automatic scan waters and fertilizes nearby IC2 crop",crop.getStorageWater()>0&&s.slot(id,"fertilizer").get().getCount()<3);s.observe(id,"cropWater",crop.getStorageWater());s.observe(id,"cropNutrients",crop.getStorageNutrients());s.observe(id,"cropWeedEx",crop.getStorageWeedEX());s.observe(id,"cropBlock",s.level.getBlockState(pos).toString());s.observe(id,"scan",List.of(get(s.tile(id),"scanX"),get(s.tile(id),"scanY"),get(s.tile(id),"scanZ")));});
         }
         crop.setChanged();s.tile(id).setChanged();
      });
      s.guard("terraformer","terraform blueprint setup",()->{
         var tile=(TileEntityTerra)s.tile("terraformer");for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++){s.level.setBlockAndUpdate(tile.getBlockPos().offset(x,-2,z),Blocks.BEDROCK.defaultBlockState());s.level.setBlockAndUpdate(tile.getBlockPos().offset(x,-1,z),Blocks.DIRT.defaultBlockState());}tile.tfbpSlot.put(stack("desertification_tfbp",1));tile.setChanged();
         s.at(1500,()->{int sand=0;for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)if(s.level.getBlockState(tile.getBlockPos().offset(x,-1,z)).is(Blocks.SAND))sand++;s.behave("terraformer","powered desertification blueprint converts real dirt into sand",sand>0&&tile.getActive());s.observe("terraformer","convertedSandBlocks",sand);tile.tfbpSlot.clear();tile.setChanged();});
         s.at(1600,()->s.behave("terraformer","removing blueprint stops operation after idle delay",!tile.getActive()));
      });
      s.guard("tesla_coil","Tesla target setup",()->{
         var tile=s.tile("tesla_coil");var cow=EntityType.COW.create(s.level,EntitySpawnReason.COMMAND);cow.setPos(tile.getBlockPos().getX()+2.5,tile.getBlockPos().getY(),tile.getBlockPos().getZ()+0.5);cow.setNoAi(true);s.level.addFreshEntity(cow);
         s.at(100,()->{s.behave("tesla_coil","unpowered redstone control leaves nearby target unharmed",cow.getHealth()==cow.getMaxHealth());s.level.setBlockAndUpdate(tile.getBlockPos().south(),Blocks.REDSTONE_BLOCK.defaultBlockState());});
         s.at(250,()->{s.behave("tesla_coil","redstone-enabled coil shocks actual nearby living entity",cow.getHealth()<cow.getMaxHealth()||!cow.isAlive());s.level.setBlockAndUpdate(tile.getBlockPos().south(),Blocks.AIR.defaultBlockState());cow.discard();});
      });
      s.guard("teleporter","teleporter pair setup",()->{
         var tile=(TileEntityTeleporter)s.tile("teleporter");var target=s.helper("teleporter",tile.getBlockPos().north(5));var bank=s.helper("mfsu",tile.getBlockPos().west());bank.getComponent(Energy.class).addEnergy(1000000);tile.setTarget(target.getBlockPos());
         var item=new ItemEntity(s.level,tile.getBlockPos().getX()+0.5,tile.getBlockPos().getY()+1.1,tile.getBlockPos().getZ()+0.5,new ItemStack(Items.IRON_INGOT,16));item.setNoGravity(true);item.setDeltaMovement(0,0,0);item.setPickUpDelay(32767);s.level.addFreshEntity(item);s.level.setBlockAndUpdate(tile.getBlockPos().south(),Blocks.REDSTONE_BLOCK.defaultBlockState());
         s.at(150,()->{s.behave("teleporter","redstone teleports actual entity to linked pad and consumes bank EU",item.blockPosition().distSqr(target.getBlockPos().above())<4&&item.getItem().getCount()==16&&bank.getComponent(Energy.class).getEnergy()<1000000);s.observe("teleporter","remainingBankEU",bank.getComponent(Energy.class).getEnergy());item.discard();s.level.setBlockAndUpdate(tile.getBlockPos().south(),Blocks.AIR.defaultBlockState());});
      });
      s.guard("personal_chest","owner permissions setup",()->{
         var tile=(TileEntityPersonalChest)s.tile("personal_chest");s.at(100,()->{
            s.behave("personal_chest","first owner can access and different player is denied",tile.permitsAccess(s.player.getGameProfile())&&!tile.permitsAccess(new GameProfile(UUID.fromString("bd0d9232-7075-4e72-b084-9dcb81ad577a"),"IC2Visitor")));
            var inventory=tile.getPrivilegedInventory(s.player.getGameProfile());inventory.setItem(0,new ItemStack(Items.DIAMOND,13));
            s.behave("personal_chest","owner inventory stores and extracts exact requested count",inventory.removeItem(0,3).getCount()==3&&inventory.getItem(0).getCount()==10);
         });
      });
      s.guard("trade_o_mat","stock-backed trading setup",()->{
         var tile=(TileEntityTradeOMat)s.tile("trade_o_mat");var stock=chest("trade_o_mat",Direction.EAST);stock.put(new ItemStack(Items.GOLD_INGOT,9));tile.demandSlot.put(new ItemStack(Items.IRON_INGOT,2));tile.offerSlot.put(new ItemStack(Items.GOLD_INGOT,3));tile.inputSlot.put(new ItemStack(Items.IRON_INGOT,4));tile.setChanged();
         s.at(400,()->{int gold=0,iron=0;for(var item:stock){if(item.is(Items.GOLD_INGOT))gold+=item.getCount();if(item.is(Items.IRON_INGOT))iron+=item.getCount();}s.behave("trade_o_mat","two trades conserve paid items and stock-backed offered output",tile.outputSlot.get().is(Items.GOLD_INGOT)&&tile.outputSlot.get().getCount()==6&&tile.inputSlot.isEmpty()&&gold==3&&iron==4&&tile.totalTradeCount==2);s.observe("trade_o_mat","tradeCount",tile.totalTradeCount);});
      });
      s.guard("energy_o_mat","paid energy setup",()->{
         var tile=(TileEntityEnergyOMat)s.tile("energy_o_mat");var income=chest("energy_o_mat",Direction.EAST);s.helper("creative_generator",tile.getBlockPos().west());tile.demandSlot.put(new ItemStack(Items.IRON_INGOT));tile.inputSlot.put(new ItemStack(Items.IRON_INGOT));tile.chargeSlot.put(stack("re_battery",1));tile.setChanged();
         s.at(400,()->{double charged=ElectricItem.manager.getCharge(tile.chargeSlot.get());s.behave("energy_o_mat","one payment buys configured EU without excess charging",income.get().is(Items.IRON_INGOT)&&income.get().getCount()==1&&tile.inputSlot.isEmpty()&&charged==tile.euOffer&&tile.paidFor==0&&tile.euBuffer==0);s.observe("energy_o_mat","paidBatteryEU",charged);});
      });
      s.guard("industrial_workbench","real crafting menu setup",()->{
         var tile=(TileEntityIndustrialWorkbench)s.tile("industrial_workbench");tile.leftCrafting.input.put(new ItemStack(Items.IRON_INGOT));tile.rightCrafting.input.put(stack("copper_plate",1));tile.craftingGrid.put(4,new ItemStack(Items.OAK_LOG));
         s.at(200,()->{var menu=(ContainerIndustrialWorkbench)tile.createServerScreenHandler(99,s.player);menu.broadcastChanges();
            s.behave("industrial_workbench","general grid and hammer/cutter previews resolve real recipes",menu.getSlot(menu.indexOutput).getItem().is(Items.OAK_PLANKS)&&menu.getSlot(menu.indexOutputHammer).getItem().is(Ic2Items.IRON_PLATE)&&!menu.getSlot(menu.indexOutputCutter).getItem().isEmpty());
            menu.clicked(menu.indexOutputHammer,0,ContainerInput.PICKUP,s.player);s.behave("industrial_workbench","taking hammer result consumes ingot and retains damaged tool",menu.getCarried().is(Ic2Items.IRON_PLATE)&&tile.leftCrafting.input.isEmpty()&&tile.leftCrafting.tool.get().getDamageValue()==1);menu.setCarried(ItemStack.EMPTY);menu.removed(s.player);
         });
      });
   }
}
