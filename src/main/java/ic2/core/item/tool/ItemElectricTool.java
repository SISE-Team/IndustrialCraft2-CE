package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.api.network.INetworkItemEventListener;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.ElectricItemTooltipHandler;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.sound.Sound;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class ItemElectricTool extends Item implements IElectricItem, INetworkItemEventListener, IItemHudInfo {
   public double operationEnergyCost;
   private final Collection<TagKey<Block>> effectiveBlocks;
   private final ToolMaterial material;
   protected final float speed;
   public int maxCharge;
   public int transferLimit;
   public int tier;
   protected Sound idleSound;
   protected Sound startSound;
   protected Sound stopSound;
   protected boolean wasEquipped;

   protected ItemElectricTool(Properties settings, int operationEnergyCost) {
      this(settings, operationEnergyCost, ToolMaterial.IRON, Collections.emptyList());
   }

   protected ItemElectricTool(Properties settings, int operationEnergyCost, ToolMaterial material, Collection<TagKey<Block>> effectiveBlocks) {
      this(settings, 2.0F, -3.0F, operationEnergyCost, material, effectiveBlocks, true);
   }

   protected ItemElectricTool(Properties settings, int operationEnergyCost, ToolMaterial material, Collection<TagKey<Block>> effectiveBlocks, boolean canMineCreative) {
      this(settings, 2.0F, -3.0F, operationEnergyCost, material, effectiveBlocks, canMineCreative);
   }

   private ItemElectricTool(
      Properties settings, float attackDamage, float attackSpeed, int operationEnergyCost, ToolMaterial material, Collection<TagKey<Block>> effectiveBlocks, boolean canMineCreative
   ) {
      // 26.1.2 removed DiggerItem; tool behavior comes from item components.
      super(electricToolProperties(settings, material, effectiveBlocks, attackDamage, attackSpeed, canMineCreative));
      this.operationEnergyCost = operationEnergyCost;
      this.effectiveBlocks = effectiveBlocks;
      this.material = material;
      this.speed = material.speed();
   }

   private static Properties electricToolProperties(Properties settings, ToolMaterial material, Collection<TagKey<Block>> effectiveBlocks, float attackDamage, float attackSpeed, boolean canMineCreative) {
      var lookup = net.minecraft.core.registries.BuiltInRegistries.acquireBootstrapRegistrationLookup(net.minecraft.core.registries.BuiltInRegistries.BLOCK);
      var rules = new java.util.ArrayList<net.minecraft.world.item.component.Tool.Rule>();
      rules.add(net.minecraft.world.item.component.Tool.Rule.deniesDrops(lookup.getOrThrow(material.incorrectBlocksForDrops())));
      for (var tag : effectiveBlocks) rules.add(net.minecraft.world.item.component.Tool.Rule.minesAndDrops(lookup.getOrThrow(tag), material.speed()));
      return settings.stacksTo(1)
         .component(net.minecraft.core.component.DataComponents.WEAPON, new net.minecraft.world.item.component.Weapon(0))
         .component(net.minecraft.core.component.DataComponents.TOOL, new net.minecraft.world.item.component.Tool(rules, 1.0F, 0, canMineCreative))
         .attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder()
            .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage + material.attackDamageBonus(), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
            .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED, new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).build());
   }

   public static boolean consumeEnergy(ItemStack stack, double amount, int tier, LivingEntity entity) {
      if (!(stack.getItem() instanceof IElectricItem)) {
         return false;
      } else if (!ElectricItem.manager.canUse(stack, amount)) {
         return false;
      } else {
         // The server owns charge changes and shutdown packets.
         if (entity != null && entity.level().isClientSide()) return true;
         boolean isConsumed;
         if (entity == null) {
            isConsumed = Util.isSimilar(ElectricItem.manager.discharge(stack, amount, tier, true, false, false), amount);
         } else {
            isConsumed = ElectricItem.manager.use(stack, amount, entity);
         }

         if (ElectricItem.manager.getCharge(stack) <= 0.0 && entity instanceof Player player) {
            IC2.network.get(true).initiateItemEvent(player, stack, 0, true);
         }

         return isConsumed;
      }
   }

   @Override
   public void onNetworkEvent(ItemStack stack, Player player, int event) {
      player.playSound(this.getShutdownSound(), 1.0F, 1.0F);
   }

   public boolean consumeEnergy(ItemStack stack, double amount, LivingEntity entity) {
      return consumeEnergy(stack, amount, this.tier, entity);
   }

   @Override
   public List<String> getHudInfo(ItemStack stack, boolean advanced) {
      List<String> info = new LinkedList<>();
      info.add(ElectricItem.manager.getToolTip(stack));
      info.add(Localization.translate("ic2.item.tooltip.PowerTier", this.tier));
      return info;
   }

   public InteractionResult useOn(UseOnContext context) {
      ElectricItem.manager.use(context.getItemInHand(), 0.0, context.getPlayer());
      return super.useOn(context);
   }

   public InteractionResult use(Level world, Player player, InteractionHand hand) {
      ElectricItem.manager.use(StackUtil.get(player, hand), 0.0, player);
      return super.use(world, player, hand);
   }

   public float getDestroySpeed(ItemStack stack, BlockState state) {
      return this.isEffective(state) && ElectricItem.manager.canUse(stack, this.operationEnergyCost) ? this.speed : 1.0F;
   }

   @Override
   public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
      return !state.is(this.material.incorrectBlocksForDrops()) ? this.isEffective(state)
         : false;
   }

   private boolean isEffective(BlockState state) {
      for (TagKey<Block> tag : this.effectiveBlocks) {
         if (state.is(tag)) {
            return true;
         }
      }

      return false;
   }

   public void hurtEnemy(ItemStack itemstack, LivingEntity entityliving, LivingEntity entityliving1) {
   }

   public int getEnchantmentValue() {
      return 0;
   }

   @Override
   public boolean canProvideEnergy(ItemStack stack) {
      return false;
   }

   @Override
   public double getMaxCharge(ItemStack stack) {
      return this.maxCharge;
   }

   @Override
   public int getTier(ItemStack stack) {
      return this.tier;
   }

   @Override
   public double getTransferLimit(ItemStack stack) {
      return this.transferLimit;
   }

   public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity user) {
      if (state.getDestroySpeed(world, pos) != 0.0F) {
         this.consumeEnergy(stack, this.operationEnergyCost, user);
      }

      return true;
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   public void addCreativeVariants(java.util.function.Consumer<ItemStack> output) {
      java.util.List<ItemStack> subItems = new java.util.ArrayList<>();
      ElectricItemManager.addChargeVariants(this, subItems);
      subItems.forEach(output);
   }

   public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag context) {
      ElectricItemTooltipHandler.addTooltip(stack, tooltip);
   }

   protected ItemStack getItemStack(double charge) {
      ItemStack ret = new ItemStack(this);
      ElectricItem.manager.charge(ret, charge, Integer.MAX_VALUE, true, false);
      return ret;
   }

   public void inventoryTick(ItemStack itemstack, net.minecraft.server.level.ServerLevel world, Entity entity, net.minecraft.world.entity.EquipmentSlot i) {
      boolean isEquipped = i == EquipmentSlot.MAINHAND && entity instanceof LivingEntity;
      if (IC2.sideProxy.isRendering()) {
         if (isEquipped && !this.wasEquipped) {
            this.initSound((LivingEntity)entity, itemstack);
            if (this.idleSound != null) {
               this.idleSound.play();
            }

            if (this.startSound != null) {
               this.startSound.playOnce();
            }
         } else if (!isEquipped && this.idleSound != null && entity instanceof LivingEntity theEntity) {
            ItemStack stack = theEntity.getItemBySlot(EquipmentSlot.MAINHAND);
            if (stack == null || stack.getItem() != this || stack == itemstack) {
               if (this.stopSound != null) {
                  this.stopSound.playOnce();
               }

               this.clearSound(theEntity);
            }
         }

         this.wasEquipped = isEquipped;
      }
   }

   protected void initSound(LivingEntity entity, ItemStack stack) {
      SoundEvent idleEvent;
      if (this.idleSound == null && (idleEvent = this.getIdleSound(entity, stack)) != null) {
         this.idleSound = IC2.soundManager.createSound(entity, idleEvent, SoundSource.PLAYERS, entity, 1.0F, 1.0F);
      }

      SoundEvent startEvent;
      if (this.startSound == null && (startEvent = this.getStartSound(entity, stack)) != null) {
         this.stopSound = IC2.soundManager.createSound(entity, startEvent, SoundSource.PLAYERS, entity, 1.0F, 1.0F);
      }

      SoundEvent stopEvent;
      if (this.stopSound == null && (stopEvent = this.getStopSound(entity, stack)) != null) {
         this.stopSound = IC2.soundManager.createSound(entity, stopEvent, SoundSource.PLAYERS, entity, 1.0F, 1.0F);
      }
   }

   protected void clearSound(LivingEntity entity) {
      if (this.idleSound != null) {
         IC2.soundManager.removeSound(entity, this.idleSound);
         this.idleSound = null;
      }

      if (this.startSound != null) {
         IC2.soundManager.removeSound(entity, this.startSound);
         this.startSound = null;
      }

      if (this.stopSound != null) {
         IC2.soundManager.removeSound(entity, this.stopSound);
         this.stopSound = null;
      }
   }

   public boolean onDroppedByPlayer(ItemStack stack, Player player) {
      this.clearSound(player);
      return true;
   }

   protected SoundEvent getIdleSound(LivingEntity player, ItemStack stack) {
      return null;
   }

   protected SoundEvent getStopSound(LivingEntity player, ItemStack stack) {
      return null;
   }

   protected SoundEvent getStartSound(LivingEntity player, ItemStack stack) {
      return null;
   }

   public SoundEvent getShutdownSound() {
      return Ic2SoundEvents.ITEM_ELECTRIC_SHUTDOWN;
   }

   public boolean isBarVisible(ItemStack stack) {
      return true;
   }

   public int getBarWidth(ItemStack stack) {
      return (int)Math.round(ElectricItem.manager.getChargeLevel(stack) * 13.0);
   }

   public int getBarColor(ItemStack stack) {
      return Mth.hsvToRgb((float)(ElectricItem.manager.getChargeLevel(stack) / 3.0), 1.0F, 1.0F);
   }

   public boolean canUse(ItemStack stack) {
      return ElectricItem.manager.canUse(stack, this.operationEnergyCost);
   }
}
