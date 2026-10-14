package ic2ce.neoforge.inventory;

import ic2ce.api.energy.tile.IDischargingSlot;
import ic2ce.api.info.Info;
import ic2ce.api.item.ElectricItem;
import ic2ce.core.block.IInventorySlotHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DischargeInventory extends InventorySlot implements IDischargingSlot {
  public int tier;
  public boolean allowRedstoneDust = true;

  public DischargeInventory(IInventorySlotHolder<?> holder, Access access, int tier) {
    this(holder, access, tier, InvSide.ANY);
  }

  public DischargeInventory(IInventorySlotHolder<?> holder, Access access, int tier, InvSide side) {
    this(holder, access, tier, true, side);
  }

  public DischargeInventory(
      IInventorySlotHolder<?> holder, Access access, int tier, boolean redstone, InvSide side) {
    super(holder, "discharge", access, 1, side);
    setTier(tier);
    allowRedstoneDust = redstone;
  }

  public void setTier(int tier) {
    if (tier < 0) throw new IllegalArgumentException("Negative item discharge tier");
    this.tier = tier;
  }

  private boolean permitted(ItemStack stack) {
    return stack != null
        && !stack.isEmpty()
        && (allowRedstoneDust || stack.getItem() != Items.REDSTONE);
  }

  @Override
  public boolean accepts(ItemStack stack) {
    return permitted(stack)
        && (Info.getItemInfo().getEnergyValue(stack) > 0
            || ElectricItem.manager.discharge(
                    stack, Double.POSITIVE_INFINITY, tier, true, true, true)
                > 0);
  }

  @Override
  public double discharge(double amount, boolean ignoreLimit) {
    if (!(amount > 0)) throw new IllegalArgumentException("Invalid discharge amount");
    ItemStack stack = get();
    if (!permitted(stack)) return 0;
    double transferred =
        ElectricItem.manager.discharge(stack, amount, tier, ignoreLimit, true, false);
    if (transferred > 0) return transferred;
    double discrete = Info.getItemInfo().getEnergyValue(stack);
    if (discrete <= 0) return 0;
    put(stack.getCount() == 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1));
    return discrete;
  }
}
