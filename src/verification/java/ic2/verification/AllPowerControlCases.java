package ic2.verification;

import static ic2.verification.AllMachinesVerification.*;
import ic2.api.item.ElectricItem;
import ic2.core.block.comp.Energy;
import ic2.core.block.wiring.tileentity.TileEntityTransformer;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

final class AllPowerControlCases {
   private final AllMachinesVerification s;
   private final Map<String,ic2.core.block.tileentity.Ic2TileEntity> receivers=new HashMap<>();
   AllPowerControlCases(AllMachinesVerification suite){s=suite;}
   void setup(){
      s.at(8000,()->s.level.getServer().tickRateManager().stopSprinting());
      s.at(9200,()->s.level.getServer().tickRateManager().requestGameToSprint(12000-s.ticks));
      for(String id:List.of("lv_transformer","mv_transformer","hv_transformer","ev_transformer"))s.guard(id,"voltage modes setup",()->{
         var tile=(TileEntityTransformer)s.tile(id);var energy=tile.getComponent(Energy.class);
         var recipient=s.helper("mfsu",tile.getBlockPos().east());
         s.at(350,()->{s.behave(id,"step-down sends actual converted EU into output bank",recipient.getComponent(Energy.class).getEnergy()>0&&energy.getSinkTier()==energy.getSourceTier()+1);s.observe(id,"stepDownEU",recipient.getComponent(Energy.class).getEnergy());s.event(id,2);});
         s.at(400,()->{
            s.behave(id,"step-up button reverses sink/source tiers and directions",tile.getMode()==TileEntityTransformer.Mode.stepup&&energy.getSourceTier()==energy.getSinkTier()+1&&energy.getSourceDirs().equals(EnumSet.of(Direction.NORTH)));
            s.helper("creative_generator",tile.getBlockPos().west());
            if(id.equals("ev_transformer")){
               var stage=(TileEntityTransformer)s.helper("ev_transformer",tile.getBlockPos().north());s.facing(stage,Direction.SOUTH);
               var bank=s.helper("mfsu",stage.getBlockPos().east());receivers.put(id,bank);
            }else receivers.put(id,s.helper("mfsu",tile.getBlockPos().north()));
         });
         s.at(850,()->{var bank=receivers.get(id);s.behave(id,"step-up transmits real EU through front output",bank.getComponent(Energy.class).getEnergy()>0);s.observe(id,"stepUpEU",bank.getComponent(Energy.class).getEnergy());s.event(id,0);});
         s.at(900,()->{s.level.setBlockAndUpdate(tile.getBlockPos().south(),Blocks.REDSTONE_BLOCK.defaultBlockState());});
         s.at(950,()->{s.behave(id,"automatic redstone mode selects step-up",tile.getMode()==TileEntityTransformer.Mode.redstone&&tile.getActive()&&energy.getSourceTier()==energy.getSinkTier()+1);s.level.setBlockAndUpdate(tile.getBlockPos().south(),Blocks.AIR.defaultBlockState());});
         s.at(1000,()->{s.behave(id,"automatic mode returns to step-down without signal",!tile.getActive()&&energy.getSinkTier()==energy.getSourceTier()+1);s.event(id,1);});
      });
      int index=0;
      for(String id:List.of("batbox_chargepad","cesu_chargepad","mfe_chargepad","mfsu_chargepad")){
         int start=8100+index++*150;
         s.at(start,()->{s.player.getAbilities().flying=false;s.player.onUpdateAbilities();s.player.setItemInHand(InteractionHand.MAIN_HAND,stack("re_battery",1));var pos=s.tile(id).getBlockPos();s.player.teleportTo(pos.getX()+0.5,pos.getY()+0.8,pos.getZ()+0.5);});
         s.at(start+100,()->{double charge=ElectricItem.manager.getCharge(s.player.getMainHandItem());s.behave(id,"standing player receives wireless battery charge through actual block collision",charge>0);s.observe(id,"standingPlayerBatteryEU",charge);});
      }
      s.guard("magnetizer","powered iron pole setup",()->{
         var pole=s.tile("magnetizer").getBlockPos().north();for(int y=0;y<7;y++)s.level.setBlockAndUpdate(pole.above(y),block("iron_fence").defaultBlockState());
         s.at(8950,()->{ACTOR_ENERGY_BLOCK_POS=s.tile("magnetizer").getBlockPos();s.player.teleportTo(pole.getX()-0.1,pole.getY(),pole.getZ()+0.5);s.player.setDeltaMovement(0,0,0);s.player.sendSystemMessage(net.minecraft.network.chat.Component.literal("IC2 verification: jump once beside the powered iron pole (Space)."));s.observe("magnetizer","jumpWindow",true);});
         for(int tick=8951;tick<9160;tick++)s.at(tick,()->{double peak=((Number)s.rows.get("magnetizer").getOrDefault("peakPolePlayerY",0)).doubleValue();s.observe("magnetizer","peakPolePlayerY",Math.max(peak,s.player.getY()));});
         s.at(9160,()->{s.behave("magnetizer","powered iron pole lifts actual player beyond ordinary jump height",((Number)s.rows.get("magnetizer").get("peakPolePlayerY")).doubleValue()>pole.getY()+2);s.observe("magnetizer","jumpWindow",false);s.player.teleportTo(168.5,182,170.5);});
      });
   }
}
