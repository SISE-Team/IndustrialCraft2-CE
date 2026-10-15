package ic2ce.neoforge.item.energy;

import ic2ce.api.item.ElectricItem;
import ic2ce.api.item.IElectricItem;
import ic2ce.api.network.INetworkItemEventListener;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Rechargeable handheld supply uses distinct per-target two-rate bills and actual observed source
 * debits.
 */
public class RechargeableSupply extends ElectricItemBase implements INetworkItemEventListener {
  public RechargeableSupply(Properties properties, double capacity, double transfer, int tier) {
    super(properties, capacity, transfer, tier);
  }

  @Override
  public boolean canProvideEnergy(ItemStack stack) {
    return true;
  }

  @Override
  public InteractionResult use(Level world, Player player, InteractionHand hand) {
    var source = player.getItemInHand(hand);
    if (world.isClientSide() || !source.is(this) || source.getCount() != 1)
      return InteractionResult.PASS;
    boolean transferred = false;
    for (int slot = 0; slot < 9 && ElectricItem.manager.getCharge(source) > 0; slot++) {
      var target = player.getInventory().getItem(slot);
      if (target == source
          || target.isEmpty()
          || target.getItem() instanceof IElectricItem electric && electric.canProvideEnergy(target)
          || ElectricItem.manager.discharge(
                  target, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true, true)
              > 0) continue;
      transferred |= ItemChargeTransfers.move(source, target, 2 * transferLimit, tier) > 0;
    }
    if (transferred) {
      player.containerMenu.broadcastChanges();
      ProjectRuntime.network.get(true).initiateItemEvent(player, source, 0, true);
    }
    return InteractionResult.SUCCESS.heldItemTransformedTo(source);
  }

  @Override
  public void onNetworkEvent(ItemStack stack, Player player, int event) {
    if (event == 0 && player.level().isClientSide())
      player.playSound(SoundDefinitions.ITEM_BATTERY_USE, 1, 1);
  }
}
