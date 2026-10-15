package ic2ce.neoforge.agriculture.menu;

import ic2ce.neoforge.agriculture.AgricultureMenus;
import ic2ce.neoforge.agriculture.FluidCropCaretaker;
import ic2ce.neoforge.processing.menu.PoweredMenu;
import net.minecraft.world.entity.player.Inventory;

/** Independent fluid water/herbicide circuits with physical container remainders. */
public class FluidCareMenu extends PoweredMenu<FluidCropCaretaker> {
  private static final java.util.List<String> FIELDS = java.util.List.of("waterTank", "exTank");

  public FluidCareMenu(int id, Inventory inventory, FluidCropCaretaker machine) {
    super(AgricultureMenus.CROPMATRON, id, inventory, machine, 192, 134, 80);
    strip(machine.fertilizerSlot, 0, machine.fertilizerSlot.size(), 8, 80, 18, 0);
    cell(machine.exInputSlot, 0, 49, 27);
    cell(machine.exOutputSlot, 0, 67, 27);
    cell(machine.wasserinputSlot, 0, 57, 56);
    cell(machine.wasseroutputSlot, 0, 75, 56);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 152, 26, 0, 18);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
