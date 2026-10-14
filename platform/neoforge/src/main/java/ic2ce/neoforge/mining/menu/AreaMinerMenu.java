package ic2ce.neoforge.mining.menu;

import ic2ce.neoforge.mining.AreaMiner;
import ic2ce.neoforge.processing.menu.PoweredMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Area scanner filter grid owns actual slot quantities and complete final rows. */
public class AreaMinerMenu extends PoweredMenu<AreaMiner> {
  private static final java.util.List<String> FIELDS =
      java.util.List.of("mineTarget", "blacklist", "silkTouch");

  public AreaMinerMenu(int id, Inventory inventory, AreaMiner machine) {
    super(MenuDefinitions.ADVANCED_MINER, id, inventory, machine, 203, 8, 80);
    cell(machine.scannerSlot, 0, 8, 26);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 152, 26, 0, 18);
    for (int slot = 0; slot < machine.filterSlot.size(); slot++)
      cell(machine.filterSlot, slot, 36 + (slot % 5) * 18, 44 + (slot / 5) * 18);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
