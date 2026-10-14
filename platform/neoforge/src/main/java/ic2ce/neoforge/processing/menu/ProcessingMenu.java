package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.processing.PoweredProcessor;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/** Processing menu independently binds optional input/output and the actual upgrade inventory. */
public class ProcessingMenu<T extends PoweredProcessor<?, ?, ?>> extends PoweredMenu<T> {
  public ProcessingMenu(
      MenuType<? extends ProcessingMenu<T>> type, int id, Inventory inventory, T machine) {
    this(type, id, inventory, machine, 166, 56, 53, 56, 17, 116, 35, 152, 8);
  }

  public ProcessingMenu(
      MenuType<? extends ProcessingMenu<T>> type,
      int id,
      Inventory inventory,
      T machine,
      int height,
      int powerX,
      int powerY,
      int inputX,
      int inputY,
      int outputX,
      int outputY,
      int upgradeX,
      int upgradeY) {
    super(type, id, inventory, machine, height, powerX, powerY);
    if (machine.inputSlot != null) cell(machine.inputSlot, 0, inputX, inputY);
    if (machine.outputSlot != null) cell(machine.outputSlot, 0, outputX, outputY);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), upgradeX, upgradeY, 0, 18);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(super.getNetworkedFields(), "guiProgress");
  }
}
