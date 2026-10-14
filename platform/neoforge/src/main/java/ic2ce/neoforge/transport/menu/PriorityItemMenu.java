package ic2ce.neoforge.transport.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.transport.PriorityItemRouter;
import net.minecraft.world.entity.player.Inventory;

/** Priority item router exposes only physical buffered bill slots. */
public class PriorityItemMenu extends PhysicalInventoryMenu<PriorityItemRouter> {
  public static final short HEIGHT = 211;

  public PriorityItemMenu(int id, Inventory inventory, PriorityItemRouter machine) {
    super(MenuDefinitions.WEIGHTED_ITEM_DISTRIBUTOR, id, inventory, machine, 211);
    strip(machine.buffer, 0, machine.buffer.size(), 8, 108, 18, 0);
  }
}
