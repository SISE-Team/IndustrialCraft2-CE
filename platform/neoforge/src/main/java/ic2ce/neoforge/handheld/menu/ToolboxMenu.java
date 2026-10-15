package ic2ce.neoforge.handheld.menu;

import ic2ce.neoforge.handheld.HandheldMenus;
import ic2ce.neoforge.handheld.ToolboxStorage;
import ic2ce.neoforge.menu.slot.StorageItemSlot;

/** Nine physical toolbox slots precede the native three-row inventory and hotbar. */
public class ToolboxMenu extends HandheldMenu<ToolboxStorage> {
  public ToolboxMenu(int id, ToolboxStorage storage) {
    super(HandheldMenus.TOOL_BOX, id, storage);
    for (int slot = 0; slot < 9; slot++)
      addSlot(new StorageItemSlot(storage, slot, 8 + 18 * slot, 41));
    addPlayerInventorySlots(player.getInventory(), 178, 166);
  }
}
