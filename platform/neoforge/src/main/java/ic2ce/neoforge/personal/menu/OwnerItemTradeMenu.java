package ic2ce.neoforge.personal.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.personal.ItemTradeTerminal;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Owner access follows actual profile authorization on every native validity check. */
public class OwnerItemTradeMenu extends PhysicalInventoryMenu<ItemTradeTerminal> {
  public final boolean canToggleInfinite;
  private static final java.util.List<String> FIELDS =
      java.util.List.of("stock", "totalTradeCount");

  public OwnerItemTradeMenu(
      int id, Inventory inventory, ItemTradeTerminal machine, boolean allowInfinite) {
    super(MenuDefinitions.TRADE_O_MAT_OPEN, id, inventory, machine, 166);
    cell(machine.demandSlot, 0, 50, 19);
    cell(machine.offerSlot, 0, 50, 53);
    cell(machine.inputSlot, 0, 80, 19);
    cell(machine.outputSlot, 0, 80, 53);
    this.canToggleInfinite = allowInfinite;
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }

  @Override
  public boolean stillValid(net.minecraft.world.entity.player.Player actor) {
    return super.stillValid(actor) && base.permitsAccess(actor.getGameProfile());
  }
}
