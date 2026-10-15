package ic2ce.neoforge.storage.box;

import ic2ce.core.*;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portable item storage writes owned physical slots into the sole block drop, including empty
 * snapshots.
 */
public abstract class TileEntityStorageBox extends InventoryBlockEntity implements IHasGui {
  protected final InventorySlot inventory;

  public TileEntityStorageBox(
      BlockEntityType<? extends TileEntityStorageBox> type,
      BlockPos position,
      BlockState state,
      int slots) {
    super(type, position, state);
    if (slots <= 0) throw new IllegalArgumentException("Storage box must expose positive capacity");
    inventory =
        new InventorySlot(
            this, "inventory", InventorySlot.Access.IO, slots, InventorySlot.InvSide.ANY);
  }

  @Override
  protected List<ItemStack> getAuxDrops(int fortune) {
    return List.of();
  }

  @Override
  public void onPlaced(ItemStack blockItem, LivingEntity placer, Direction facing) {
    super.onPlaced(blockItem, placer, facing);
    if (getLevel() == null || getLevel().isClientSide()) return;
    var data = StackTag.get(blockItem);
    inventory.clear();
    if (data != null) inventory.readFromNbt(data);
    setChanged();
  }

  @Override
  public ItemStack adjustDrop(ItemStack original, boolean wrench) {
    if (original == null || original.isEmpty()) return ItemStack.EMPTY;
    var owned = original.copy();
    var data = StackTag.getOrCreate(owned);
    inventory.writeToNbt(data);
    StackTag.set(owned, data);
    return owned;
  }

  @Override
  public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag flags) {
    tooltip.add(LocalizedText.translate("ic2.tooltip.storage_box.retains"));
    tooltip.add(LocalizedText.translate("ic2.tooltip.storage_box.slots", inventory.size()));
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return DocumentMenu.create(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory playerInventory, GrowingBuffer data) {
    return DocumentMenu.create(syncId, playerInventory, this);
  }
}
