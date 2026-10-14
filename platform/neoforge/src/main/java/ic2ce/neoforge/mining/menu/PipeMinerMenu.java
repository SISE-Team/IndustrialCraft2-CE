package ic2ce.neoforge.mining.menu;

import ic2ce.neoforge.mining.PipeMiner;
import ic2ce.neoforge.processing.menu.PoweredMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Separate physical drill, pipe, scanner and mined output buffer. */
public class PipeMinerMenu extends PoweredMenu<PipeMiner> {
  private static final java.util.List<String> FIELDS = java.util.List.of("progress");

  public PipeMinerMenu(int id, Inventory inventory, PipeMiner machine) {
    super(MenuDefinitions.MINER, id, inventory, machine, 166, 152, 58);
    cell(machine.scannerSlot, 0, 8, 58);
    cell(machine.pipeSlot, 0, 8, 40);
    cell(machine.drillSlot, 0, 8, 22);
    cell(machine.upgradeSlot, 0, 152, 22);
    for (int slot = 0; slot < machine.buffer.size(); slot++)
      cell(machine.buffer, slot, 44 + (slot % 5) * 18, 22 + (slot / 5) * 18);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
