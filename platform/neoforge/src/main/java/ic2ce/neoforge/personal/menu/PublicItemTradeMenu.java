package ic2ce.neoforge.personal.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.personal.ItemTradeTerminal;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Public offer observations are read-only while actual customer bills remain physical. */
public class PublicItemTradeMenu extends PhysicalInventoryMenu<ItemTradeTerminal> {
  private static final java.util.List<String> FIELDS = java.util.List.of("stock");

  public PublicItemTradeMenu(int id, Inventory inventory, ItemTradeTerminal machine) {
    super(MenuDefinitions.TRADE_O_MAT_CLOSED, id, inventory, machine, 166);
    addSlot(new ic2ce.neoforge.menu.slot.DisplaySlot(machine.demandSlot, 0, 50, 19));
    addSlot(new ic2ce.neoforge.menu.slot.DisplaySlot(machine.offerSlot, 0, 50, 38));
    cell(machine.inputSlot, 0, 143, 17);
    cell(machine.outputSlot, 0, 143, 53);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
