package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.processing.fluid.SteamCondenser;
import ic2ce.neoforge.registry.MenuDefinitions;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Four physical vents, output water containers and condensation progress. */
public class CondensationMenu extends PoweredMenu<SteamCondenser> {
  public CondensationMenu(int id, Inventory inventory, SteamCondenser machine) {
    super(MenuDefinitions.CONDENSER, id, inventory, machine, 184, 8, 44);
    cell(machine.waterInputSlot, 0, 26, 73);
    cell(machine.waterOutputSlot, 0, 134, 73);
    cell(machine.upgradeSlot, 0, 152, 73);
    for (int vent = 0; vent < 4; vent++)
      cell(machine.ventSlots, vent, 26 + (vent % 2) * 108, 26 + (vent / 2) * 18);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(super.getNetworkedFields(), "inputTank", "outputTank", "progress");
  }
}
