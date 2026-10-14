package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.processing.fluid.LiquidPackager;
import ic2ce.neoforge.registry.MenuDefinitions;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Separate paid fill/drain container paths and shared native tank. */
public class FluidPackagingMenu extends ProcessingMenu<LiquidPackager> {
  public FluidPackagingMenu(int id, Inventory inventory, LiquidPackager machine) {
    super(
        MenuDefinitions.FLUID_BOTTLER, id, inventory, machine, 184, 8, 53, 0, 0, 117, 53, 152, 26);
    cell(machine.drainInputSlot, 0, 44, 35);
    cell(machine.fillInputSlot, 0, 44, 72);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(super.getNetworkedFields(), "fluidTank");
  }
}
