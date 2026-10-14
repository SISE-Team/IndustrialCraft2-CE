package ic2ce.neoforge.thermal.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.thermal.FluidBurner;
import net.minecraft.world.entity.player.Inventory;

/** Fluid burner owns its distinct fuel admission and heat output fields. */
public class FluidHeaterMenu extends PhysicalInventoryMenu<FluidBurner> {
  private static final java.util.List<String> FIELDS =
      java.util.List.of("fluidTank", "transmitHeat", "maxHeatEmitpeerTick");

  public FluidHeaterMenu(int id, Inventory inventory, FluidBurner machine) {
    super(MenuDefinitions.FLUID_HEAT_GENERATOR, id, inventory, machine, 166);
    cell(machine.fluidSlot, 0, 27, 21);
    cell(machine.outputSlot, 0, 27, 54);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
