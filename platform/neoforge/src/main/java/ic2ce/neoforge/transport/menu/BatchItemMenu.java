package ic2ce.neoforge.transport.menu;

import ic2ce.neoforge.menu.slot.TemplateSlot;
import ic2ce.neoforge.processing.menu.PoweredMenu;
import ic2ce.neoforge.transport.BatchItemSorter;
import ic2ce.neoforge.transport.TransportMenus;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;

/**
 * Actual batch buffers are separate from six detached filter rows with committed native persistence
 * callbacks.
 */
public class BatchItemMenu extends PoweredMenu<BatchItemSorter> {
  private static final java.util.List<String> FIELDS = java.util.List.of("defaultRoute");

  public BatchItemMenu(int id, Inventory inventory, BatchItemSorter machine) {
    super(TransportMenus.SORTING_MACHINE, id, inventory, machine, 243, 188, 219);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 188, 161, 0, 18);
    strip(machine.buffer, 0, machine.buffer.size(), 8, 141, 18, 0);
    for (var direction : Direction.values()) {
      var filters = machine.getFilterSlots(direction);
      for (int filter = 0; filter < filters.length; filter++)
        addSlot(
            new TemplateSlot(
                filters,
                filter,
                80 + filter * 18,
                19 + direction.ordinal() * 20,
                64,
                ignored -> {
                  if (machine.hasLevel() && !machine.getLevel().isClientSide())
                    machine.setChanged();
                }));
    }
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
