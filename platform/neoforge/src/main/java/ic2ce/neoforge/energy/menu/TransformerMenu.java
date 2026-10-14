package ic2ce.neoforge.energy.menu;

import ic2ce.neoforge.energy.transformer.VoltageTransformer;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Transformer signal mode and distinct measured input/output flows. */
public class TransformerMenu extends PhysicalInventoryMenu<VoltageTransformer> {
  private static final java.util.List<String> FIELDS =
      java.util.List.of("configuredMode", "inputFlow", "outputFlow");

  public TransformerMenu(int id, Inventory inventory, VoltageTransformer machine) {
    super(MenuDefinitions.TRANSFORMER, id, inventory, machine, 219);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
