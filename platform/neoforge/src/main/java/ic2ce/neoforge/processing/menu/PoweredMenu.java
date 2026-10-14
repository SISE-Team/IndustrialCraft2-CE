package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/** Electrical machine menu exposes exactly its actual discharge ledger slot. */
public abstract class PoweredMenu<T extends PoweredBlockEntity> extends PhysicalInventoryMenu<T> {
  public PoweredMenu(
      MenuType<? extends PoweredMenu<T>> type,
      int id,
      Inventory inventory,
      T machine,
      int height,
      int x,
      int y) {
    super(type, id, inventory, machine, height);
    cell(machine.dischargeSlot, 0, x, y);
  }
}
