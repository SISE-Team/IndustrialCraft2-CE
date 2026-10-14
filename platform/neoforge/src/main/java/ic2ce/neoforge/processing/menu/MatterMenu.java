package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.processing.fabrication.LiquidMatterFabricator;
import ic2ce.neoforge.registry.MenuDefinitions;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Amplifier billing, UU filling and matter energy state. */
public class MatterMenu extends PhysicalInventoryMenu<LiquidMatterFabricator> {
  public MatterMenu(int id, Inventory inventory, LiquidMatterFabricator machine) {
    super(MenuDefinitions.MATTER_GENERATOR, id, inventory, machine, 166);
    cell(machine.amplifierSlot, 0, 72, 40);
    cell(machine.outputSlot, 0, 125, 59);
    cell(machine.containerslot, 0, 125, 23);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 152, 8, 0, 18);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(super.getNetworkedFields(), "energy", "scrap", "fluidTank");
  }
}
