package ic2ce.neoforge.menu.document;

import ic2ce.neoforge.inventory.IInventorySlotHolder;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.core.util.Util;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.slot.MachineSlot;
import ic2ce.neoforge.menu.slot.TemplateSlot;
import ic2ce.neoforge.registry.MenuDefinitions;
import java.lang.reflect.Modifier;
import java.util.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Server menu projection of retained XML assets, with bounded traversal and immutable class
 * metadata.
 */
public class DocumentMenu<T extends Container> extends OwnedMenu<T> {
  private static final ClassValue<List<String>> NETWORK_FIELDS =
      new ClassValue<>() {
        @Override
        protected List<String> computeValue(Class<?> type) {
          var names = new LinkedHashSet<String>();
          for (var current = type;
              current != null && current != Object.class && current != BlockEntity.class;
              current = current.getSuperclass()) {
            for (var field : current.getDeclaredFields())
              if (!Modifier.isStatic(field.getModifiers())
                  && field.isAnnotationPresent(GuiSynced.class)) names.add(field.getName());
          }
          return List.copyOf(names);
        }
      };
  final ScreenDocument.GuiNode guiNode;

  public static DocumentMenu<InventoryBlockEntity> create(
      int id, Inventory inventory, InventoryBlockEntity tile) {
    return new DocumentMenu<>(
        MenuDefinitions.DYNAMIC_BE,
        id,
        inventory,
        tile,
        ScreenDocument.parse(Util.getName(tile.getBlockType()), tile.getClass()));
  }

  public static <T extends Container> DocumentMenu<T> create(
      MenuType<DocumentMenu<T>> type,
      int id,
      Inventory inventory,
      T base,
      ScreenDocument.GuiNode document) {
    return new DocumentMenu<>(type, id, inventory, base, document);
  }

  protected DocumentMenu(
      MenuType<DocumentMenu<T>> type,
      int id,
      Inventory inventory,
      T base,
      ScreenDocument.GuiNode document) {
    super(type, id, inventory, base);
    guiNode = Objects.requireNonNull(document);
    project(inventory, document, 0, new IdentityHashMap<>());
  }

  private InventorySlot inventorySlot(String name) {
    if (!(base instanceof IInventorySlotHolder holder))
      throw new IllegalArgumentException(
          "XML slot requires an inventory holder: " + base.getClass().getName());
    var slot = holder.getInventorySlot(name);
    if (slot == null) throw new IllegalArgumentException("Unknown XML inventory slot: " + name);
    return slot;
  }

  private void project(
      Inventory inventory,
      ScreenDocument.ParentNode parent,
      int depth,
      IdentityHashMap<ScreenDocument.ParentNode, Boolean> path) {
    if (depth > 64 || path.put(parent, Boolean.TRUE) != null)
      throw new IllegalArgumentException("Cyclic or excessive XML menu depth");
    try {
      for (var node : parent.getNodes()) {
        if (slots.size() > 4096)
          throw new IllegalArgumentException("XML menu exceeds native slot bound");
        if (node instanceof ScreenDocument.EnvironmentNode environment
            && environment.environment != ScreenProjection.GAME) continue;
        switch (node) {
          case ScreenDocument.PlayerInventoryNode playerSlots -> {
            int left = playerSlots.x + (playerSlots.style.width - 16) / 2,
                top = playerSlots.y + (playerSlots.style.height - 16) / 2;
            int dx = playerSlots.style.width + playerSlots.spacing,
                dy = playerSlots.style.height + playerSlots.spacing;
            for (int ordinal = 0; ordinal < 27; ordinal++)
              addSlot(
                  new Slot(
                      inventory, ordinal + 9, left + (ordinal % 9) * dx, top + (ordinal / 9) * dy));
            for (int column = 0; column < 9; column++)
              addSlot(
                  new Slot(inventory, column, left + column * dx, top + playerSlots.hotbarOffset));
          }
          case ScreenDocument.SlotHologramNode template -> {
            if (!(base instanceof TemplateStorage provider))
              throw new IllegalArgumentException(
                  "XML template requires a holographic slot provider");
            addSlot(
                new TemplateSlot(
                    provider.getStacksForName(template.name),
                    template.index,
                    template.x + (template.style.width - 16) / 2,
                    template.y + (template.style.height - 16) / 2,
                    template.stackSizeLimit,
                    getCallback()));
          }
          case ScreenDocument.SlotNode cell ->
              addSlot(
                  new MachineSlot(
                      inventorySlot(cell.name),
                      cell.index,
                      cell.x + (cell.style.width - 16) / 2,
                      cell.y + (cell.style.height - 16) / 2));
          case ScreenDocument.SlotGridNode grid -> {
            var storage = inventorySlot(grid.name);
            int available = storage.size() - grid.offset;
            if (grid.offset < 0)
              throw new IllegalArgumentException("Negative XML inventory grid offset");
            if (available <= 0) break;
            var shape = grid.getDimension(storage.size());
            if (shape.rows <= 0 || shape.cols <= 0)
              throw new IllegalArgumentException("Invalid XML inventory grid dimensions");
            long capacity = (long) shape.rows * shape.cols;
            if (capacity > 4096)
              throw new IllegalArgumentException("XML inventory grid exceeds native slot bound");
            int left = grid.x + (grid.style.width - 16) / 2,
                top = grid.y + (grid.style.height - 16) / 2;
            for (int ordinal = 0; ordinal < Math.min(available, capacity); ordinal++) {
              int row = grid.vertical ? ordinal % shape.rows : ordinal / shape.cols,
                  column = grid.vertical ? ordinal / shape.rows : ordinal % shape.cols;
              addSlot(
                  new MachineSlot(
                      storage,
                      grid.offset + ordinal,
                      left + column * (grid.style.width + grid.spacing),
                      top + row * (grid.style.height + grid.spacing)));
            }
          }
          default -> {}
        }
        if (node instanceof ScreenDocument.ParentNode branch)
          project(inventory, branch, depth + 1, path);
      }
    } finally {
      path.remove(parent);
    }
  }

  protected TemplateSlot.ChangeCallback getCallback() {
    return null;
  }

  @Override
  public List<String> getNetworkedFields() {
    return NETWORK_FIELDS.get(base.getClass());
  }
}
