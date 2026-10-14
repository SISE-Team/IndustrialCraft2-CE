package ic2ce.neoforge.reactor;

import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** A live inventory view through an attached chamber or vessel hatch. */
public interface ReactorInventoryView extends Container {
  NuclearCore getReactorInstance();

  @Override
  default int getContainerSize() {
    var core = getReactorInstance();
    return core == null ? 0 : core.getContainerSize();
  }

  @Override
  default boolean isEmpty() {
    var core = getReactorInstance();
    return core == null || core.isEmpty();
  }

  @Override
  default ItemStack getItem(int slot) {
    var core = getReactorInstance();
    return core == null ? ItemStack.EMPTY : core.getItem(slot);
  }

  @Override
  default ItemStack removeItem(int slot, int count) {
    var core = getReactorInstance();
    return core == null ? ItemStack.EMPTY : core.removeItem(slot, count);
  }

  @Override
  default ItemStack removeItemNoUpdate(int slot) {
    var core = getReactorInstance();
    return core == null ? ItemStack.EMPTY : core.removeItemNoUpdate(slot);
  }

  @Override
  default void setItem(int slot, ItemStack stack) {
    var core = getReactorInstance();
    if (core != null) core.setItem(slot, stack);
  }

  @Override
  default int getMaxStackSize() {
    var core = getReactorInstance();
    return core == null ? 1 : core.getMaxStackSize();
  }

  @Override
  default boolean stillValid(Player player) {
    var core = getReactorInstance();
    return core != null && core.stillValid(player);
  }

  @Override
  default void startOpen(ContainerUser user) {
    var core = getReactorInstance();
    if (core != null) core.startOpen(user);
  }

  @Override
  default void stopOpen(ContainerUser user) {
    var core = getReactorInstance();
    if (core != null) core.stopOpen(user);
  }

  @Override
  default boolean canPlaceItem(int slot, ItemStack stack) {
    var core = getReactorInstance();
    return core != null && core.canPlaceItem(slot, stack);
  }

  @Override
  default void clearContent() {
    var core = getReactorInstance();
    if (core != null) core.clearContent();
  }
}
