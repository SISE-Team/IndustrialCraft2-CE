package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.processing.fluid.BiomassFermenter;
import ic2ce.neoforge.registry.MenuDefinitions;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Independent biomass input, biogas output, fertilizer and heat admission state. */
public class FermentationMenu extends PhysicalInventoryMenu<BiomassFermenter> {
  public FermentationMenu(int id, Inventory inventory, BiomassFermenter machine) {
    super(MenuDefinitions.FERMENTER, id, inventory, machine, 184);
    cell(machine.fluidInputCellInSlot, 0, 14, 46);
    cell(machine.fluidInputCellOutSlot, 0, 14, 64);
    cell(machine.fluidOutputCellInSlot, 0, 148, 43);
    cell(machine.fluidOutputCellOutSlot, 0, 148, 61);
    cell(machine.fertiliserSlot, 0, 86, 83);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 125, 83, 18, 0);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(
        super.getNetworkedFields(), "inputTank", "outputTank", "progress", "heatBuffer");
  }
}
