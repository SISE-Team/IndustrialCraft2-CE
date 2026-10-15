package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.ProcessingMenus;
import ic2ce.neoforge.processing.fluid.PressureBoiler;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/**
 * Slot-free boiler control menu exposes pressure, calcification and actual water/steam accounting.
 */
public class BoilerMenu extends OwnedMenu<PressureBoiler> {
  private static final List<String> FIELDS =
      List.of(
          "waterTank",
          "heatInput",
          "inputMB",
          "outputMB",
          "pressure",
          "systemHeat",
          "outputFluid",
          "calcification");

  public BoilerMenu(int id, Inventory inventory, PressureBoiler boiler) {
    super(ProcessingMenus.STEAM_GENERATOR, id, inventory, boiler);
  }

  @Override
  public List<String> getNetworkedFields() {
    return FIELDS;
  }
}
