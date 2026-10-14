package ic2ce.neoforge.transport.menu;

import ic2ce.neoforge.processing.menu.PoweredMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.transport.MeteredFluidOutlet;
import net.minecraft.world.entity.player.Inventory;

/** Metered outlet keeps quantity and redstone control state separate. */
public class MeteredFluidMenu extends PoweredMenu<MeteredFluidOutlet> {
  private static final java.util.List<String> FIELDS =
      java.util.List.of("fluidTank", "outputmb", "mode");

  public MeteredFluidMenu(int id, Inventory inventory, MeteredFluidOutlet machine) {
    super(MenuDefinitions.FLUID_REGULATOR, id, inventory, machine, 184, 8, 57);
    cell(machine.wasserinputSlot, 0, 58, 53);
    cell(machine.wasseroutputSlot, 0, 58, 71);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
