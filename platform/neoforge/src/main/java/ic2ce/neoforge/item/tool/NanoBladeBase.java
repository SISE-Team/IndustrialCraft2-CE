package ic2ce.neoforge.item.tool;


import ic2ce.api.item.ElectricItem;
import ic2ce.api.sound.item.ISwingSoundItem;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.item.armor.NanoArmor;
import ic2ce.neoforge.item.armor.QuantumArmor;
import ic2ce.neoforge.menu.slot.EquipmentOrder;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.SharedRandom;
import ic2ce.neoforge.runtime.SideServices;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Owned activation and owner-clock idle bills; armor discharge is partial-aware and remains a
 * separate paid attack phase.
 */
public abstract class NanoBladeBase extends PoweredTool implements ISwingSoundItem {
  public static int ticker;
  public static final int ANIMATION_FRAME = 4;

  public NanoBladeBase(Properties properties) {
    super(properties.rarity(Rarity.UNCOMMON), 10, ToolMaterial.DIAMOND, List.of(), false);
    maxCharge = 160000;
    transferLimit = 500;
    tier = 3;
  }

  @Override
  public boolean consumeEnergy(ItemStack stack, double amount, LivingEntity owner) {
    boolean consumed = super.consumeEnergy(stack, amount, owner);
    if (!consumed && (owner == null || !owner.level().isClientSide())) setActive(stack, false);
    return consumed;
  }

  @Override
  public float getDestroySpeed(ItemStack stack, BlockState state) {
    return isActive(stack) ? state.is(Blocks.COBWEB) ? 50 : 4 : 1;
  }

  @Override
  public void hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity owner) {
    if (owner.level().isClientSide() || !isActive(stack) || !consumeEnergy(stack, 400, owner))
      return;
    if (owner instanceof ServerPlayer player
        && victim instanceof Player other
        && !player.canHarmPlayer(other)) return;
    for (var slot : EquipmentOrder.getAll()) {
      if (!ElectricItem.manager.canUse(stack, 2000)) break;
      var armor = victim.getItemBySlot(slot);
      double drain =
          armor.getItem() instanceof QuantumArmor
              ? 300000
              : armor.getItem() instanceof NanoArmor ? 48000 : 0;
      if (drain <= 0 || !consumeEnergy(stack, 2000, owner)) continue;
      ElectricItem.manager.discharge(armor, drain, Integer.MAX_VALUE, true, false, false);
      if (ElectricItem.manager.getCharge(armor) < 1) victim.setItemSlot(slot, ItemStack.EMPTY);
    }
  }

  public SoundEvent getRandomSwingSound() {
    return switch (SharedRandom.INSTANCE.nextInt(3)) {
      case 1 -> SoundDefinitions.ITEM_NANOSABER_SWING2;
      case 2 -> SoundDefinitions.ITEM_NANOSABER_SWING3;
      default -> SoundDefinitions.ITEM_NANOSABER_SWING1;
    };
  }

  public boolean canAttackBlock(BlockState state, Level world, BlockPos position, Player player) {
    return !player.getAbilities().instabuild;
  }

  @Override
  public InteractionResult use(Level world, Player player, InteractionHand hand) {
    var held = player.getItemInHand(hand);
    if (world.isClientSide() || !held.is(this)) return InteractionResult.PASS;
    if (isActive(held)) setActive(held, false);
    else {
      if (!ElectricItem.manager.canUse(held, 16)) return super.use(world, player, hand);
      setActive(held, true);
    }
    return InteractionResult.SUCCESS.heldItemTransformedTo(held);
  }

  @Override
  public void inventoryTick(
      ItemStack stack,
      net.minecraft.server.level.ServerLevel world,
      Entity entity,
      EquipmentSlot slot) {
    if (!isActive(stack) || !(entity instanceof ServerPlayer owner)) return;
    boolean quickAccess = slot == EquipmentSlot.MAINHAND;
    if (!quickAccess)
      for (int index = 0; index < 9; index++)
        if (owner.getInventory().getItem(index) == stack) {
          quickAccess = true;
          break;
        }
    int interval = quickAccess ? 16 : 64;
    ticker = entity.tickCount;
    if (Math.floorMod(entity.tickCount, interval) == 0)
      consumeEnergy(stack, quickAccess ? 64 : 16, owner);
  }

  public float getActiveData() {
    var world = SideServices.proxy().getPlayerWorld();
    long time = world == null ? ticker : world.getGameTime();
    return (Math.floorMod(time / 5, 10) + 1) / 10F;
  }

  public Rarity getRarity(ItemStack stack) {
    return Rarity.UNCOMMON;
  }

  public static boolean isActive(ItemStack stack) {
    var data = stack == null || stack.isEmpty() ? null : StackTag.get(stack);
    return data != null && isActive(data);
  }

  public static boolean isActive(CompoundTag data) {
    return data != null && data.getBooleanOr("active", false);
  }

  private static void setActive(ItemStack stack, boolean active) {
    if (stack != null && !stack.isEmpty())
      StackTag.update(
          stack,
          data -> {
            data.putBoolean("active", active);
            return data;
          });
  }

  @Override
  protected SoundEvent getIdleSound(LivingEntity owner, ItemStack stack) {
    return isActive(stack) ? SoundDefinitions.ITEM_NANOSABER_IDLE : null;
  }

  @Override
  protected SoundEvent getStartSound(LivingEntity owner, ItemStack stack) {
    return isActive(stack) ? SoundDefinitions.ITEM_NANOSABER_POWER_UP : null;
  }

  @Override
  public SoundEvent getSwingSound(LivingEntity owner, InteractionHand hand) {
    return getRandomSwingSound();
  }
}
