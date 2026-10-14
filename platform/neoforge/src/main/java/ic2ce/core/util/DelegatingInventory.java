package ic2ce.core.util;

import java.util.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

/**
 * Native container delegation preserves stack-specific capacity and transactional writes, including
 * modern container callbacks.
 */
public class DelegatingInventory implements Container {
  private final Container parent;

  public DelegatingInventory(Container parent) {
    this.parent = Objects.requireNonNull(parent);
  }

  private boolean valid(int slot) {
    return slot >= 0 && slot < parent.getContainerSize();
  }

  @Override
  public int getContainerSize() {
    return parent.getContainerSize();
  }

  @Override
  public boolean isEmpty() {
    return parent.isEmpty();
  }

  @Override
  public ItemStack getItem(int slot) {
    return valid(slot) ? parent.getItem(slot) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack removeItem(int slot, int count) {
    return valid(slot) && count > 0 ? parent.removeItem(slot, count) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    return valid(slot) ? parent.removeItemNoUpdate(slot) : ItemStack.EMPTY;
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    if (valid(slot)) parent.setItem(slot, Objects.requireNonNull(stack));
  }

  @Override
  public void setItem(int slot, ItemStack stack, boolean inTransaction) {
    if (valid(slot)) parent.setItem(slot, Objects.requireNonNull(stack), inTransaction);
  }

  @Override
  public int getMaxStackSize() {
    return parent.getMaxStackSize();
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return parent.getMaxStackSize(stack);
  }

  @Override
  public void setChanged() {
    parent.setChanged();
  }

  @Override
  public boolean stillValid(Player actor) {
    return actor != null && parent.stillValid(actor);
  }

  @Override
  public void startOpen(ContainerUser actor) {
    parent.startOpen(actor);
  }

  @Override
  public void stopOpen(ContainerUser actor) {
    parent.stopOpen(actor);
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    return valid(slot) && stack != null && parent.canPlaceItem(slot, stack);
  }

  @Override
  public int countItem(Item item) {
    return parent.countItem(item);
  }

  @Override
  public boolean hasAnyOf(Set<Item> items) {
    return parent.hasAnyOf(items);
  }

  @Override
  public void clearContent() {
    parent.clearContent();
  }
}
