package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.sound.item.ISwingSoundItem;
import ic2.core.IC2;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.slot.ArmorSlot;
import ic2.core.util.StackUtil;
import java.util.Collections;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractItemNanoSaber extends ItemElectricTool implements ISwingSoundItem {
   public static int ticker = 0;
   private int soundTicker = 0;
   public static final int ANIMATION_FRAME = 4;

   public AbstractItemNanoSaber(Properties settings) {
      super(settings.rarity(Rarity.UNCOMMON), 10, ToolMaterial.DIAMOND, Collections.emptyList(), false);
      this.maxCharge = 160000;
      this.transferLimit = 500;
      this.tier = 3;
   }

   @Override
   public boolean consumeEnergy(ItemStack stack, double amount, LivingEntity entity) {
      if (!super.consumeEnergy(stack, amount, entity)) {
         if (entity == null || !entity.level().isClientSide()) setActive(stack, false);
         return false;
      } else {
         return true;
      }
   }

   @Override
   public float getDestroySpeed(ItemStack stack, BlockState state) {
      if (isActive(stack)) {
         this.soundTicker++;
         if (IC2.sideProxy.isRendering() && this.soundTicker % 4 == 0) {
            // swing sound: entity representation removed from ItemStack in 26.1.2
         }

         return state.getBlock() == Blocks.COBWEB ? 50.0F : 4.0F;
      } else {
         return 1.0F;
      }
   }

   @Override
   public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity source) {
      if (!isActive(stack)) {
         return;
      } else {
         if (IC2.sideProxy.isSimulating()) {
            this.consumeEnergy(stack, 400.0, source);
            if (!(source instanceof ServerPlayer) || !(target instanceof Player) || ((ServerPlayer)source).canHarmPlayer((Player)target)) {
               for (EquipmentSlot slot : ArmorSlot.getAll()) {
                  if (!ElectricItem.manager.canUse(stack, 2000.0)) {
                     break;
                  }

                  ItemStack armor = target.getItemBySlot(slot);
                  if (armor != null) {
                     double amount = 0.0;
                     if (armor.getItem() instanceof ItemArmorNanoSuit) {
                        amount = 48000.0;
                     } else if (armor.getItem() instanceof ItemArmorQuantumSuit) {
                        amount = 300000.0;
                     }

                     if (amount > 0.0) {
                        ItemElectricTool.consumeEnergy(armor, amount, Integer.MAX_VALUE, null);
                        if (!ElectricItem.manager.canUse(armor, 1.0)) {
                           target.setItemSlot(slot, ItemStack.EMPTY);
                        }

                        this.consumeEnergy(stack, 2000.0, source);
                     }
                  }
               }
            }
         }

      }
   }

   public SoundEvent getRandomSwingSound() {
      return switch (IC2.random.nextInt(3)) {
         case 1 -> Ic2SoundEvents.ITEM_NANOSABER_SWING2;
         case 2 -> Ic2SoundEvents.ITEM_NANOSABER_SWING3;
         default -> Ic2SoundEvents.ITEM_NANOSABER_SWING1;
      };
   }

   public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
      return miner.isCreative() ? false : true;
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      return super.useOn(context);
   }

   @Override
   public InteractionResult use(@NotNull Level world, Player player, InteractionHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      if (world.isClientSide()) {
         return InteractionResult.PASS;
      } else {
         CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
         if (isActive(nbt)) {
            setActive(stack, false);
            return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
         } else if (ElectricItem.manager.canUse(stack, 16.0)) {
            setActive(stack, true);
            return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
         } else {
            return super.use(world, player, hand);
         }
      }
   }

   @Override
   public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel world, Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
      super.inventoryTick(stack, world, entity, slot);
      CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
      if (isActive(nbt)) {
         // Each owner's clock is independent of other sabers in any inventory.
         ticker = entity.tickCount;
         boolean hotbar = slot == EquipmentSlot.MAINHAND || entity instanceof Player owner
            && java.util.stream.IntStream.range(0, 9).anyMatch(index -> owner.getInventory().getItem(index) == stack);
         int interval = hotbar ? 16 : 64;
         double cost = hotbar ? 64.0 : 16.0;
         if (entity.tickCount % interval == 0 && entity instanceof ServerPlayer player
            && !this.consumeEnergy(stack, cost, player)) {
            setActive(stack, false);
         }
      }
   }

   public float getActiveData() {
      return ticker > 0 ? (float)(Math.floor(ticker / 20.0 * 4.0) % 10.0 + 1.0) / 10.0F : 0.0F;
   }

   public Rarity getRarity(ItemStack stack) {
      return Rarity.UNCOMMON;
   }

   public static boolean isActive(ItemStack stack) {
      CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
      return isActive(nbt);
   }

   public static boolean isActive(CompoundTag nbt) {
      return nbt.getBooleanOr("active", false);
   }

   private static void setActive(ItemStack stack, boolean active) {
      ic2.core.util.StackTag.update(stack, nbt -> { nbt.putBoolean("active", active); return nbt; });
   }

   @Override
   protected SoundEvent getIdleSound(LivingEntity player, ItemStack stack) {
      return Ic2SoundEvents.ITEM_NANOSABER_IDLE;
   }

   @Override
   protected SoundEvent getStartSound(LivingEntity player, ItemStack stack) {
      return Ic2SoundEvents.ITEM_NANOSABER_POWER_UP;
   }

   @Override
   public SoundEvent getSwingSound(LivingEntity entity, InteractionHand hand) {
      return this.getRandomSwingSound();
   }
}
