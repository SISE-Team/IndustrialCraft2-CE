package ic2ce.neoforge.inventory;

import ic2ce.engine.inventory.DurabilityBudget;
import ic2ce.neoforge.item.PhysicalWear;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Same-component consumption with explicit remainder and wear simulation policies. */
public abstract class ConsumableInventory extends InventorySlot {
  public ConsumableInventory(IInventorySlotHolder<?> holder, String name, int count) {
    super(holder, name, Access.I, count, InvSide.TOP);
  }

  public ConsumableInventory(
      IInventorySlotHolder<?> holder, String name, Access access, int count, InvSide side) {
    super(holder, name, access, count, side);
  }

  @Override
  public abstract boolean accepts(ItemStack stack);

  @Override
  public boolean canOutput() {
    return super.canOutput() || access != Access.NONE && !isEmpty() && !accepts(get());
  }

  public ItemStack consume(int amount) {
    return consume(amount, false, false);
  }

  public ItemStack consume(int amount, boolean simulate, boolean consumeContainers) {
    if (amount < 0) throw new IllegalArgumentException("Negative consumption");
    if (amount == 0) return null;
    ItemStack template = null;
    int consumed = 0;
    boolean changed = false;
    for (int i = 0; i < size() && consumed < amount; i++) {
      ItemStack held = get(i);
      if (held.isEmpty()
          || !accepts(held)
          || template != null && !ItemStack.isSameItemSameComponents(held, template)) continue;
      boolean remainder = !consumeContainers && ProjectRuntime.envProxy.hasRecipeRemainder(held);
      if (remainder && held.getCount() != 1) continue;
      if (template == null) template = held;
      int taken = Math.min(amount - consumed, held.getCount());
      consumed += taken;
      if (simulate) continue;
      ItemStack next;
      if (taken < held.getCount()) next = held.copyWithCount(held.getCount() - taken);
      else if (remainder) {
        next = ProjectRuntime.envProxy.getRecipeRemainder(held);
        if (next != null
            && next.isDamageableItem()
            && PhysicalWear.getDamage(next) > PhysicalWear.getMaxDamage(next))
          next = ItemStack.EMPTY;
      } else next = ItemStack.EMPTY;
      putFromNBT(i, next);
      changed = true;
    }
    if (changed) onChanged();
    return consumed == 0 ? null : template.copyWithCount(consumed);
  }

  public int damage(int amount, boolean simulate) {
    return damage(amount, simulate, null);
  }

  public int damage(int amount, boolean simulate, LivingEntity actor) {
    if (amount < 0) throw new IllegalArgumentException("Negative wear");
    int spent = 0;
    ItemStack template = null;
    boolean changed = false;
    for (int i = 0; i < size() && spent < amount; i++) {
      ItemStack held = get(i);
      if (held.isEmpty()
          || !accepts(held)
          || !held.isDamageableItem()
          || template != null && !ItemStack.isSameItemSameComponents(held, template)) continue;
      int maximum = PhysicalWear.getMaxDamage(held);
      if (maximum <= 0) continue;
      if (template == null) template = held.copy();
      if (simulate) {
        spent +=
            Math.min(
                amount - spent,
                DurabilityBudget.available(held.getCount(), PhysicalWear.getDamage(held), maximum));
        continue;
      }
      ItemStack next = held.copy();
      while (!next.isEmpty() && spent < amount) {
        int available = Math.max(0, maximum - PhysicalWear.getDamage(next));
        if (available == 0) {
          next.shrink(1);
          if (!next.isEmpty()) PhysicalWear.setDamage(next, 0, false);
          continue;
        }
        int wear = Math.min(amount - spent, available), count = next.getCount();
        PhysicalWear.damage(next, wear, (ServerLevel) base.getParent().getLevel(), actor, null);
        spent += wear;
        // Entity-owned vanilla damage already consumes a broken item;
        // machine-owned wear leaves that responsibility with this slot.
        if (!next.isEmpty()
            && next.getCount() == count
            && PhysicalWear.getDamage(next) >= maximum) {
          next.shrink(1);
          if (!next.isEmpty()) PhysicalWear.setDamage(next, 0, false);
        }
      }
      putFromNBT(i, next);
      changed = true;
    }
    if (changed) onChanged();
    return spent;
  }
}
