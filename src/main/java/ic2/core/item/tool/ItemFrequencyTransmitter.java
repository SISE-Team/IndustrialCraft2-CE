package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemFrequencyTransmitter extends Item {
   private static final String targetSetNbt = "targetSet";
   private static final String targetJustSetNbt = "targetJustSet";
   private static final String targetXNbt = "targetX";
   private static final String targetYNbt = "targetY";
   private static final String targetZNbt = "targetZ";

   public ItemFrequencyTransmitter(Properties settings) {
      super(settings);
   }

   public InteractionResult use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      if (IC2.sideProxy.isSimulating()) {
         CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
         boolean hadJustSet = nbtData.getBooleanOr("targetJustSet", false);
         if (nbtData.getBooleanOr("targetSet", false) && !hadJustSet) {
            nbtData.putBoolean("targetSet", false);
            ic2.core.util.StackTag.set(stack, nbtData);
            IC2.sideProxy.messagePlayer(player, "ic2.frequency_transmitter.unlinked");
         }

         if (hadJustSet) {
            nbtData.putBoolean("targetJustSet", false);
            ic2.core.util.StackTag.set(stack, nbtData);
         }
      }

      return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
   }

   public InteractionResult useOn(UseOnContext context) {
      Level world = context.getLevel();
      Player player = context.getPlayer();
      BlockPos pos = context.getClickedPos();
      InteractionHand hand = context.getHand();
      if (player == null) {
         return InteractionResult.PASS;
      } else if (world.isClientSide()) {
         return InteractionResult.PASS;
      } else if (!(world.getBlockEntity(pos) instanceof TileEntityTeleporter tp)) {
         return InteractionResult.PASS;
      } else {
         CompoundTag nbtData = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
         boolean targetSet = nbtData.getBooleanOr("targetSet", false);
         boolean justSetTarget = true;
         BlockPos target = new BlockPos(nbtData.getIntOr("targetX", 0), nbtData.getIntOr("targetY", 0), nbtData.getIntOr("targetZ", 0));
         if (!targetSet) {
            targetSet = true;
            target = tp.getBlockPos();
            IC2.sideProxy.messagePlayer(player, "ic2.frequency_transmitter.linked");
         } else if (tp.getBlockPos().equals(target)) {
            IC2.sideProxy.messagePlayer(player, "ic2.frequency_transmitter.self");
         } else if (tp.hasTarget() && tp.getTarget().equals(target)) {
            IC2.sideProxy.messagePlayer(player, "ic2.frequency_transmitter.unchanged");
         } else {
            BlockEntity targetTe = world.getBlockEntity(target);
            if (targetTe instanceof TileEntityTeleporter) {
               tp.setTarget(target);
               ((TileEntityTeleporter)targetTe).setTarget(pos);
               IC2.sideProxy.messagePlayer(player, "ic2.frequency_transmitter.established");
            } else {
               justSetTarget = false;
               targetSet = false;
            }
         }

         nbtData.putBoolean("targetSet", targetSet);
         ic2.core.util.StackTag.set(StackUtil.get(player, hand), nbtData);
         nbtData.putBoolean("targetJustSet", justSetTarget);
         ic2.core.util.StackTag.set(StackUtil.get(player, hand), nbtData);
         nbtData.putInt("targetX", target.getX());
         ic2.core.util.StackTag.set(StackUtil.get(player, hand), nbtData);
         nbtData.putInt("targetY", target.getY());
         ic2.core.util.StackTag.set(StackUtil.get(player, hand), nbtData);
         nbtData.putInt("targetZ", target.getZ());
         ic2.core.util.StackTag.set(StackUtil.get(player, hand), nbtData);
         return InteractionResult.SUCCESS;
      }
   }

   public void appendHoverText(ItemStack stack, Item.TooltipContext world, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag advanced) {
      CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
      if (nbtData.getBooleanOr("targetSet", false)) {
         tooltip.accept(
            Component.translatable(
               "ic2.frequency_transmitter.tooltip.target",
               new Object[]{nbtData.getIntOr("targetX", 0), nbtData.getIntOr("targetY", 0), nbtData.getIntOr("targetZ", 0)}
            )
         );
      } else {
         tooltip.accept(Component.translatable("ic2.frequency_transmitter.tooltip.blank"));
      }
   }
}
