package ic2ce.neoforge.transport.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.transport.FairFluidRouter;
import net.minecraft.world.entity.player.Inventory;

/** Fair fluid distribution uses real empty and filled container ports. */
public class FairFluidMenu extends PhysicalInventoryMenu<FairFluidRouter> {
  private static final java.util.List<String> FIELDS = java.util.List.of("fluidTank");

  public FairFluidMenu(int id, Inventory inventory, FairFluidRouter machine) {
    super(MenuDefinitions.FLUID_DISTRIBUTOR, id, inventory, machine, 184);
    cell(machine.inputSlot, 0, 9, 54);
    cell(machine.OutputSlot, 0, 9, 72);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
