package ic2ce.neoforge.processing.menu;

import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.processing.solid.MetalFormer;
import ic2ce.neoforge.registry.MenuDefinitions;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

/** Native rolling/cutting/extrusion mode synchronization. */
public class MetalFormingMenu extends ProcessingMenu<MetalFormer> {
  public MetalFormingMenu(int id, Inventory inventory, MetalFormer machine) {
    super(
        MenuDefinitions.METAL_FORMER, id, inventory, machine, 166, 17, 53, 17, 17, 116, 35, 152, 8);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(super.getNetworkedFields(), "mode");
  }
}
