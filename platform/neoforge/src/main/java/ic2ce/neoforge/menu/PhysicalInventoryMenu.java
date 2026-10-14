package ic2ce.neoforge.menu;

import ic2ce.core.*;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.menu.slot.MachineSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/** Physical inventory menu with explicit native machine cells and bounded strip placement. */
public abstract class PhysicalInventoryMenu<T extends Container> extends OwnedMenu<T> {
  public PhysicalInventoryMenu(MenuType<?> type, int id, Inventory inventory, T base, int height) {
    this(type, id, inventory, base, 178, height);
  }

  public PhysicalInventoryMenu(
      MenuType<?> type, int id, Inventory inventory, T base, int width, int height) {
    super(type, id, inventory, base);
    if (width < 162 || height < 90 || width > 4096 || height > 4096)
      throw new IllegalArgumentException("Invalid physical inventory menu dimensions");
    addPlayerInventorySlots(inventory, width, height);
  }

  protected final void cell(InventorySlot storage, int index, int x, int y) {
    addSlot(new MachineSlot(storage, index, x, y));
  }

  protected final void strip(
      InventorySlot storage, int first, int count, int x, int y, int dx, int dy) {
    if (first < 0 || count < 0 || count > storage.size() - first)
      throw new IllegalArgumentException("Machine menu strip exceeds physical inventory");
    for (int offset = 0; offset < count; offset++)
      cell(storage, first + offset, x + offset * dx, y + offset * dy);
  }
}
