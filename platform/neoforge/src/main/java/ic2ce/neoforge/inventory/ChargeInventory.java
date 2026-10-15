package ic2ce.neoforge.inventory;

import ic2ce.api.energy.tile.IChargingSlot;
import ic2ce.api.item.ElectricItem;
import net.minecraft.world.item.ItemStack;

public class ChargeInventory extends InventorySlot implements IChargingSlot {
  public int tier;

  public ChargeInventory(IInventorySlotHolder<?> holder, int tier) {
    super(holder, "charge", Access.IO, 1, InvSide.TOP);
    setTier(tier);
  }

  public void setTier(int tier) {
    if (tier < 0) throw new IllegalArgumentException("Negative item charge tier");
    this.tier = tier;
  }

  @Override
  public boolean accepts(ItemStack stack) {
    return stack != null
        && !stack.isEmpty()
        && ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, tier, true, true) > 0;
  }

  @Override
  public double charge(double amount) {
    if (!(amount > 0)) throw new IllegalArgumentException("Invalid charge amount");
    ItemStack stack = get();
    return stack.isEmpty() ? 0 : ElectricItem.manager.charge(stack, amount, tier, false, false);
  }
}
