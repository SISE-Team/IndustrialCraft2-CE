package ic2ce.neoforge.utility.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.utility.ChunkAnchor;
import net.minecraft.world.entity.player.Inventory;

/** Chunk lease menu owns actual charge, upgrade and loaded chunk count. */
public class ChunkAnchorMenu extends PhysicalInventoryMenu<ChunkAnchor> {
  private static final java.util.List<String> FIELDS = java.util.List.of("loadedChunks");

  public ChunkAnchorMenu(int id, Inventory inventory, ChunkAnchor machine) {
    super(MenuDefinitions.CHUNK_LOADER, id, inventory, machine, 250);
    cell(machine.dischargeSlot, 0, 8, 143);
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 8, 44, 0, 18);
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }
}
