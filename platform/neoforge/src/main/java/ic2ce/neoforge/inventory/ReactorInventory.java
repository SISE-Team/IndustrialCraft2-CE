package ic2ce.neoforge.inventory;

import ic2ce.neoforge.reactor.NuclearCore;
import net.minecraft.world.item.ItemStack;

/** A fixed physical 9x6 core with a chamber-dependent logical inventory view. */
public class ReactorInventory extends InventorySlot {
  public ReactorInventory(NuclearCore reactor, String name, int count) {
    super(reactor, name, Access.IO, count);
    if (count != 54)
      throw new IllegalArgumentException("Reactor storage must contain 54 physical positions");
    setStackSizeLimit(1);
  }

  private int columns() {
    return Math.clamp(((NuclearCore) base).getReactorSize(), 3, 9);
  }

  @Override
  public int size() {
    return columns() * 6;
  }

  public int rawSize() {
    return super.size();
  }

  @Override
  public boolean accepts(ItemStack stack) {
    return stack != null && !stack.isEmpty() && ((NuclearCore) base).isUsefulItem(stack, true);
  }

  @Override
  public ItemStack get(int logical) {
    return super.get(physical(logical));
  }

  @Override
  public void put(int logical, ItemStack stack) {
    super.put(physical(logical), stack);
  }

  public ItemStack get(int x, int y) {
    return super.get(coordinates(x, y));
  }

  public void put(int x, int y, ItemStack stack) {
    super.put(coordinates(x, y), stack);
  }

  private static int coordinates(int x, int y) {
    if (x < 0 || x >= 9 || y < 0 || y >= 6)
      throw new IndexOutOfBoundsException("Reactor position outside 9x6 core");
    return y * 9 + x;
  }

  private int physical(int logical) {
    if (logical < 0 || logical >= rawSize()) throw new IndexOutOfBoundsException(logical);
    int columns = columns(), active = columns * 6;
    if (logical < active) return logical / columns * 9 + logical % columns;
    int hidden = logical - active, width = 9 - columns;
    return hidden / width * 9 + columns + hidden % width;
  }
  // NBT indices are physical. Inherited persistence deliberately bypasses
  // the logical view so rebuilding chambers cannot move saved components.
}
