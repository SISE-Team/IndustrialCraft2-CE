package ic2ce.neoforge.handheld.menu;

import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.handheld.HandheldStorage;
import ic2ce.neoforge.menu.OwnedMenu;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * Native container actions keep the physical handheld identified and reject malformed indices
 * before mutation.
 */
public class HandheldMenu<T extends HandheldStorage> extends OwnedMenu<T> {
  public HandheldMenu(MenuType<?> type, int id, T inventory) {
    super(type, id, inventory.player.getInventory(), inventory);
  }

  @Override
  public void clicked(int slot, int button, ContainerInput input, Player actor) {
    if (actor != player || slot >= slots.size() || slot < -1 && slot != -999) return;
    boolean physicalSlot = slot >= 0 && base.isThisContainer(slots.get(slot).getItem());
    if (input == ContainerInput.QUICK_MOVE && physicalSlot) return;
    if (input == ContainerInput.SWAP) {
      if (slot < 0 || button < 0 || button >= actor.getInventory().getContainerSize()) return;
      int selected = -1;
      if (base.isThisContainer(actor.getInventory().getItem(button))) {
        var target = slots.get(slot);
        if (target.container == actor.getInventory() && target.getContainerSlot() < 9)
          selected = target.getContainerSlot();
      } else if (physicalSlot && button < 9) selected = button;
      if (selected >= 0 && actor instanceof ServerPlayer server) {
        actor.getInventory().setSelectedSlot(selected);
        server.connection.send(new ClientboundSetHeldSlotPacket(selected));
      }
    }
    var touched = physicalSlot ? slots.get(slot).getItem() : ItemStack.EMPTY;
    super.clicked(slot, button, input, actor);
    if (physicalSlot
        && (input == ContainerInput.PICKUP || input == ContainerInput.THROW)
        && actor instanceof ServerPlayer server) {
      base.saveAsThrown(touched);
      server.closeContainer();
    } else if (input == ContainerInput.CLONE && base.isThisContainer(getCarried()))
      StackTag.update(
          getCarried(),
          data -> {
            data.remove("uid");
            return data;
          });
  }

  @Override
  public void removed(Player actor) {
    base.onScreenClosed(actor);
    super.removed(actor);
  }
}
