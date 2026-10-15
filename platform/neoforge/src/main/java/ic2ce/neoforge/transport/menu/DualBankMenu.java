package ic2ce.neoforge.transport.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.transport.DualItemBuffer;
import ic2ce.neoforge.transport.TransportMenus;
import net.minecraft.world.entity.player.Inventory;

/** Distinct left/right physical buffer banks preserve every cell. */
public class DualBankMenu extends PhysicalInventoryMenu<DualItemBuffer> {
  public DualBankMenu(int id, Inventory inventory, DualItemBuffer machine) {
    super(TransportMenus.ITEM_BUFFER, id, inventory, machine, 232);
    for (int slot = 0; slot < machine.leftcontentSlot.size(); slot++)
      cell(machine.leftcontentSlot, slot, 8 + (slot % 4) * 18, 18 + (slot / 4) * 18);
    for (int slot = 0; slot < machine.rightcontentSlot.size(); slot++)
      cell(machine.rightcontentSlot, slot, 98 + (slot % 4) * 18, 18 + (slot / 4) * 18);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 35, 128, 90, 0);
  }
}
