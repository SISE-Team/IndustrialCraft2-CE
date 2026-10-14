package ic2ce.neoforge.menu.document;

import ic2ce.neoforge.handheld.HandheldStorage;
import ic2ce.neoforge.menu.slot.TemplateSlot;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Native handheld menu owns its close/save lifecycle and the actual thrown stack. */
public class HandheldDocumentMenu<T extends HandheldStorage> extends DocumentMenu<T> {
  private boolean closed;

  public static HandheldDocumentMenu<HandheldStorage> create(
      int id, Inventory inventory, HandheldStorage storage, ScreenDocument.GuiNode document) {
    return new HandheldDocumentMenu<>(
        MenuDefinitions.DYNAMIC_ITEM, id, inventory, storage, document);
  }

  public static <T extends HandheldStorage> HandheldDocumentMenu<T> create(
      MenuType<DocumentMenu<T>> type,
      int id,
      Inventory inventory,
      T storage,
      ScreenDocument.GuiNode document) {
    return new HandheldDocumentMenu<>(type, id, inventory, storage, document);
  }

  protected HandheldDocumentMenu(
      MenuType<DocumentMenu<T>> type,
      int id,
      Inventory inventory,
      T storage,
      ScreenDocument.GuiNode document) {
    super(type, id, inventory, storage, document);
    if (storage.player != inventory.player)
      throw new IllegalArgumentException("Handheld menu owner differs from physical storage owner");
  }

  @Override
  protected TemplateSlot.ChangeCallback getCallback() {
    return base.makeSaveCallback();
  }

  @Override
  public void onContainerEvent(String event) {
    if (!closed && event != null && !player.level().isClientSide()) base.onEvent(event);
  }

  @Override
  public void clicked(int index, int button, ContainerInput action, Player actor) {
    if (closed || actor != player || action == null) return;
    Slot source =
        !actor.level().isClientSide() && index >= 0 && index < slots.size()
            ? slots.get(index)
            : null;
    ItemStack physical = source == null ? ItemStack.EMPTY : source.getItem();
    boolean owns = base.isThisContainer(physical);
    super.clicked(index, button, action, actor);
    if (owns
        && source != null
        && !source.hasItem()
        && !base.stillValid(actor)
        && actor instanceof ServerPlayer nativePlayer) {
      base.saveAsThrown(physical);
      nativePlayer.closeContainer();
    }
  }

  @Override
  public void removed(Player actor) {
    if (actor != player || closed) return;
    closed = true;
    base.onScreenClosed(actor);
    super.removed(actor);
  }
}
