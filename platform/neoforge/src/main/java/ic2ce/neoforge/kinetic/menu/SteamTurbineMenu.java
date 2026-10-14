package ic2ce.neoforge.kinetic.menu;

import ic2ce.neoforge.kinetic.SteamTurbine;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Steam turbine reports independent condensation and venting constraints. */
public class SteamTurbineMenu extends PhysicalInventoryMenu<SteamTurbine> {
  private static final java.util.List<String> FIELDS =
      java.util.List.of(
          "distilledWaterTank", "kUoutput", "ventingSteam", "throttled", "blockedByWater");

  public SteamTurbineMenu(int id, Inventory inventory, SteamTurbine machine) {
    super(MenuDefinitions.STEAM_KINETIC_GENERATOR, id, inventory, machine, 166);
    cell(machine.upgradeSlot, 0, 152, 26);
    cell(machine.turbineSlot, 0, 80, 26);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
