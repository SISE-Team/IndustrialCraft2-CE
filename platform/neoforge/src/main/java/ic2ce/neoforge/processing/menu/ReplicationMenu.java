package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.processing.ProcessingMenus;
import ic2ce.neoforge.processing.fabrication.PatternReplicator;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** UU material debit, replicated output and pattern cycle state. */
public class ReplicationMenu extends PoweredMenu<PatternReplicator> {
  public ReplicationMenu(int id, Inventory inventory, PatternReplicator machine) {
    super(ProcessingMenus.REPLICATOR, id, inventory, machine, 184, 152, 83);
    cell(machine.outputSlot, 0, 90, 59);
    cell(machine.fluidSlot, 0, 8, 27);
    cell(machine.cellSlot, 0, 8, 72);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 152, 8, 0, 18);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(
        super.getNetworkedFields(),
        "fluidTank",
        "uuProcessed",
        "pattern",
        "mode",
        "index",
        "maxIndex",
        "patternUu",
        "patternEu");
  }
}
