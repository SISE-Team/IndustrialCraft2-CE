package ic2ce.neoforge.transport.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.transport.PriorityFluidRouter;
import net.minecraft.world.entity.player.Inventory;

/** Priority fluid circuit owns its independent container endpoints. */
public class PriorityFluidMenu extends PhysicalInventoryMenu<PriorityFluidRouter> {
  public static final short HEIGHT = 211;
  private static final java.util.List<String> FIELDS = java.util.List.of("fluidTank");

  public PriorityFluidMenu(int id, Inventory inventory, PriorityFluidRouter machine) {
    super(MenuDefinitions.WEIGHTED_FLUID_DISTRIBUTOR, id, inventory, machine, 211);
    cell(machine.inputSlot, 0, 8, 108);
    cell(machine.OutputSlot, 0, 152, 108);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
