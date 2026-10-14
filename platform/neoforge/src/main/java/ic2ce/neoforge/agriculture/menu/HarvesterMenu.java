package ic2ce.neoforge.agriculture.menu;

import ic2ce.neoforge.agriculture.AutoHarvester;
import ic2ce.neoforge.processing.menu.PoweredMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Harvest output cell projection includes incomplete final rows. */
public class HarvesterMenu extends PoweredMenu<AutoHarvester> {
  private static final java.util.List<String> FIELDS = java.util.List.of("energy");

  public HarvesterMenu(int id, Inventory inventory, AutoHarvester machine) {
    super(MenuDefinitions.CROP_HARVESTER, id, inventory, machine, 166, 16, 53);
    for (int slot = 0; slot < machine.contentSlot.size(); slot++)
      cell(machine.contentSlot, slot, 48 + (slot % 5) * 18, 17 + (slot / 5) * 18);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 152, 8, 0, 18);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
