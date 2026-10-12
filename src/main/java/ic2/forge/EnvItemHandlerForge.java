package ic2.forge;

import com.mojang.authlib.GameProfile;
import ic2.core.block.personal.IPersonalBlock;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.item.EnvItemHandler;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jetbrains.annotations.Nullable;
final class EnvItemHandlerForge implements EnvItemHandler {
   @Nullable
   private static Container getStorage(BlockEntity target, Direction side, GameProfile profile, @Nullable Set<Container> personalInventories) {
      if (target instanceof IPersonalBlock && profile != null) {
         Container privilegedInventory = ((IPersonalBlock)target).getPrivilegedInventory(profile);
         if (personalInventories != null) {
            personalInventories.add(privilegedInventory);
         }

         return privilegedInventory;
      } else if (target instanceof RandomizableContainerBlockEntity containerBlockEntity) {
         return containerBlockEntity;
      } else {
         return target instanceof TileEntityInventory tileEntityInventory ? tileEntityInventory : null;
      }
   }

   @Nullable
   private static EnvItemHandlerForge.HandlerForge getStorage(BlockEntity source, Direction dir, @Nullable Set<Container> personalInventories) {
      BlockEntity target = source.getLevel().getBlockEntity(source.getBlockPos().relative(dir));
      GameProfile profile = source instanceof IPersonalBlock ? ((IPersonalBlock)source).getOwner() : null;
      return EnvItemHandlerForge.HandlerForge.ofNullable(getStorage(target, dir.getOpposite(), profile, personalInventories), dir);
   }

   private ItemStack extractItemFrom(EnvItemHandlerForge.HandlerForge storage, int amount, boolean simulate) {
      for (int i = 0; i < storage.getSlots(); i++) {
         ItemStack itemStack = storage.getStackInSlot(i);
         if (!itemStack.isEmpty() && storage.canExtractItem(i, itemStack)) {
            return storage.extractItem(i, amount, simulate);
         }
      }

      return ItemStack.EMPTY;
   }

   private ItemStack extractItemFrom(EnvItemHandlerForge.HandlerForge storage, int amount, Predicate<ItemStack> filter, boolean simulate) {
      for (int i = 0; i < storage.getSlots(); i++) {
         ItemStack itemStack = storage.getStackInSlot(i);
         if (!itemStack.isEmpty() && storage.canExtractItem(i, itemStack) && filter.test(itemStack)) {
            return storage.extractItem(i, amount, simulate);
         }
      }

      return ItemStack.EMPTY;
   }

   private int insertItemTo(EnvItemHandlerForge.HandlerForge storage, ItemStack stack, boolean simulate) {
      for (int i = 0; i < storage.getSlots(); i++) {
         ItemStack itemStack = storage.getStackInSlot(i);
         if (storage.canInsertItem(i, stack) && (itemStack.isEmpty() || ItemStack.isSameItemSameComponents(itemStack, stack) && itemStack.getCount() < itemStack.getMaxStackSize())) {
            ItemStack remainStack = storage.insertItem(i, stack, simulate);
            return stack.getCount() - remainStack.getCount();
         }
      }

      return 0;
   }

   @Override
   public int fetch(BlockEntity source, ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) return 0;
      int fetched = 0;
      for (var adjacent : this.getAdjacentInventories(source)) {
         HandlerForge storage = (HandlerForge)adjacent;
         for (int slot = 0; slot < storage.getSlots() && fetched < stack.getCount(); slot++) {
            ItemStack current = storage.getStackInSlot(slot);
            if (ItemStack.isSameItemSameComponents(current, stack)) {
               fetched += storage.extractItem(slot, stack.getCount() - fetched, simulate).getCount();
            }
         }
         if (fetched == stack.getCount()) break;
      }
      return fetched;
   }

   @Override
   public int deposit(BlockEntity te, Direction side, ItemStack stack, GameProfile accessor, boolean simulate) {
      return this.deposit(EnvItemHandlerForge.HandlerForge.ofNullable(getStorage(te, side, accessor, null), side), stack, simulate);
   }

   @Override
   public int deposit(EnvItemHandler.AdjacentInventory inv, ItemStack stack, boolean simulate) {
      return this.deposit((EnvItemHandlerForge.HandlerForge)inv, stack, simulate);
   }

   private int deposit(EnvItemHandlerForge.HandlerForge storage, ItemStack stack, boolean simulate) {
      return storage == null ? 0 : this.insertItemTo(storage, stack, simulate);
   }

   @Override
   public int distribute(BlockEntity source, ItemStack stack, boolean simulate) {
      if (stack == null || stack.isEmpty()) return 0;
      ItemStack remaining = stack.copy();
      for (var adjacent : this.getAdjacentInventories(source)) {
         HandlerForge storage = (HandlerForge)adjacent;
         for (int slot = 0; slot < storage.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = storage.insertItem(slot, remaining, simulate);
         }
         if (remaining.isEmpty()) break;
      }
      return stack.getCount() - remaining.getCount();
   }

   @Nullable
   @Override
   public EnvItemHandler.AdjacentInventory getAdjacentInventory(BlockEntity source, Direction side) {
      return getStorage(source, side, null);
   }

   @Override
   public List<? extends EnvItemHandler.AdjacentInventory> getAdjacentInventories(BlockEntity source) {
      List<EnvItemHandlerForge.HandlerForge> inventories = new ArrayList<>();
      Set<Container> personalInventories = new HashSet<>();

      for (Direction dir : Util.ALL_DIRS) {
         EnvItemHandlerForge.HandlerForge maybeHandler = getStorage(source, dir, personalInventories);
         if (maybeHandler != null) {
            inventories.add(maybeHandler);
         }
      }

      inventories.sort(
         Comparator.<EnvItemHandlerForge.HandlerForge, Boolean>comparing(storage -> personalInventories.contains(storage.getDelegate()))
            .thenComparingInt(storage -> {
               int slotsEstimate = 0;
               slotsEstimate += storage.getSlots();
               return -slotsEstimate;
            })
      );
      return inventories;
   }

   @Override
   public EnvItemHandler.AdjacentInventory wrapInventory(BlockEntity inventory, Direction side) {
      return new EnvItemHandlerForge.HandlerForge((Container)inventory, side);
   }

   @Override
   public int transfer(EnvItemHandler.AdjacentInventory from, EnvItemHandler.AdjacentInventory to, int maxAmount) {
      EnvItemHandlerForge.HandlerForge fromStorage = (EnvItemHandlerForge.HandlerForge)from;
      EnvItemHandlerForge.HandlerForge toStorage = (EnvItemHandlerForge.HandlerForge)to;
      ItemStack extractedItemStack = this.extractItemFrom(fromStorage, maxAmount, true);
      if (extractedItemStack.getCount() > 0 && this.insertItemTo(toStorage, extractedItemStack, true) > 0) {
         this.extractItemFrom(fromStorage, maxAmount, false);
         this.insertItemTo(toStorage, extractedItemStack, false);
         return extractedItemStack.getCount();
      } else {
         return 0;
      }
   }

   @Override
   public int transfer(EnvItemHandler.AdjacentInventory from, EnvItemHandler.AdjacentInventory to, int maxAmount, Predicate<ItemStack> filter) {
      EnvItemHandlerForge.HandlerForge fromStorage = (EnvItemHandlerForge.HandlerForge)from;
      EnvItemHandlerForge.HandlerForge toStorage = (EnvItemHandlerForge.HandlerForge)to;
      ItemStack extractedItemStack = this.extractItemFrom(fromStorage, maxAmount, filter, true);
      if (extractedItemStack.getCount() > 0 && this.insertItemTo(toStorage, extractedItemStack, true) > 0) {
         this.extractItemFrom(fromStorage, maxAmount, filter, false);
         this.insertItemTo(toStorage, extractedItemStack, false);
         return extractedItemStack.getCount();
      } else {
         return 0;
      }
   }

   private static class HandlerForge implements EnvItemHandler.AdjacentInventory {
      final Direction side;
      Container inventory;

      private HandlerForge(Container inventory, Direction side) {
         this.side = side;
         this.inventory = inventory;
      }

      public int getSlots() {
         return this.inventory.getContainerSize();
      }

      public ItemStack getStackInSlot(int slot) {
         return this.inventory.getItem(slot);
      }

      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
         if (!this.canInsertItem(slot, stack)) {
            return stack;
         }
         ItemStack current = this.inventory.getItem(slot);
         int max = Math.min(this.inventory.getMaxStackSize(), stack.getMaxStackSize());
         int space;
         if (current.isEmpty()) {
            space = Math.min(max, stack.getCount());
            if (space <= 0) {
               return stack;
            }
            if (!simulate) {
               ItemStack inserted = stack.copy();
               inserted.setCount(space);
               this.inventory.setItem(slot, inserted);
            }
         } else if (ItemStack.isSameItemSameComponents(current, stack)) {
            space = Math.min(max - current.getCount(), stack.getCount());
            if (space <= 0) {
               return stack;
            }
            if (!simulate) {
               current.setCount(current.getCount() + space);
               this.inventory.setChanged();
            }
         } else {
            return stack;
         }

         ItemStack remain = stack.copy();
         remain.setCount(stack.getCount() - space);
         return remain;
      }

      public ItemStack extractItem(int slot, int amount, boolean simulate) {
         ItemStack current = this.inventory.getItem(slot);
         if (current.isEmpty() || amount <= 0 || !this.canExtractItem(slot, current)) {
            return ItemStack.EMPTY;
         }
         int extracted = Math.min(amount, current.getCount());
         ItemStack result = current.copy();
         result.setCount(extracted);
         if (!simulate) {
            current.setCount(current.getCount() - extracted);
            if (current.getCount() <= 0) {
               this.inventory.setItem(slot, ItemStack.EMPTY);
            } else {
               this.inventory.setChanged();
            }
         }
         return result;
      }

      public boolean canInsertItem(int slot, ItemStack stack) {
         return this.inventory.canPlaceItem(slot, stack)
            && !(this.inventory instanceof WorldlyContainer sidedInventory && !sidedInventory.canPlaceItemThroughFace(slot, stack, this.side));
      }

      public boolean canExtractItem(int slot, ItemStack stack) {
         return !(this.inventory instanceof WorldlyContainer sidedInventory && !sidedInventory.canTakeItemThroughFace(slot, stack, this.side));
      }

      @Override
      public Direction getSide() {
         return this.side;
      }

      private Container getDelegate() {
         return this.inventory;
      }

      static EnvItemHandlerForge.HandlerForge ofNullable(Container inventory, Direction side) {
         return inventory == null ? null : new EnvItemHandlerForge.HandlerForge(inventory, side);
      }
   }
}
