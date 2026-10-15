package ic2ce.neoforge.processing.fabrication;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.recipe.IPatternStorage;
import ic2ce.core.IHasGui;
import ic2ce.core.item.ItemCrystalMemory;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.core.util.ItemComparableItemStack;
import ic2ce.core.util.StackTag;
import ic2ce.core.uu.UuIndex;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.ItemWhitelistInput;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.menu.PatternLibraryMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ordered unit-item patterns with indexed membership, owned data and tolerant crystal/save imports.
 */
@NotClassic
public class PatternLibrary extends InventoryBlockEntity
    implements IHasGui, INetworkClientTileEntityEventListener, IPatternStorage {
  public final ItemWhitelistInput diskSlot;
  private final List<ItemStack> patterns = new ArrayList<>();
  private final Set<ItemComparableItemStack> membership = new HashSet<>();
  private final List<ItemStack> view =
      new AbstractList<>() {
        @Override
        public ItemStack get(int position) {
          return patterns.get(position).copy();
        }

        @Override
        public int size() {
          return patterns.size();
        }
      };
  public int index, maxIndex;
  public ItemStack pattern = ItemStack.EMPTY;
  public double patternUu, patternEu;

  public PatternLibrary(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.PATTERN_STORAGE, position, state);
    diskSlot =
        new ItemWhitelistInput(
            this,
            "SaveSlot",
            InventorySlot.Access.IO,
            1,
            InventorySlot.InvSide.ANY,
            ItemDefinitions.CRYSTAL_MEMORY);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    readContents(data);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    writeContents(data);
  }

  public void readContents(CompoundTag data) {
    patterns.clear();
    membership.clear();
    var entries = data.getListOrEmpty("patterns");
    for (int position = 0; position < entries.size(); position++) {
      var entry = entries.getCompound(position).orElse(null);
      if (entry != null) store(StackTag.parse(getSerializationLookup(), entry));
    }
    index = 0;
    refreshInfo();
  }

  private void writeContents(CompoundTag data) {
    var entries = new ListTag();
    var lookup = getSerializationLookup();
    for (var entry : patterns) entries.add(StackTag.save(lookup, entry, new CompoundTag()));
    data.put("patterns", entries);
  }

  @Override
  public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing) {
    super.onPlaced(stack, placer, facing);
    if (!getLevel().isClientSide()) {
      var data = StackTag.get(stack);
      if (data != null) readContents(data);
    }
  }

  @Override
  public ItemStack adjustDrop(ItemStack drop, boolean wrench) {
    drop = super.adjustDrop(drop, wrench);
    if (wrench || teBlock.getDefaultDrop() == MachineBlockType.DefaultDrop.Self) {
      var data = StackTag.getOrCreate(drop);
      writeContents(data);
      StackTag.set(drop, data);
    }
    return drop;
  }

  private boolean store(ItemStack stack) {
    if (stack == null
        || stack.isEmpty()
        || !membership.add(new ItemComparableItemStack(stack, true))) return false;
    patterns.add(stack.copyWithCount(1));
    return true;
  }

  @Override
  public boolean addPattern(ItemStack stack) {
    if (!store(stack)) return false;
    refreshInfo();
    setChanged();
    return true;
  }

  public boolean containsPattern(ItemStack stack) {
    return stack != null
        && !stack.isEmpty()
        && membership.contains(new ItemComparableItemStack(stack, false));
  }

  @Override
  public List<ItemStack> getPatterns() {
    return view;
  }

  public void refreshInfo() {
    maxIndex = patterns.size();
    index = maxIndex == 0 ? 0 : Math.clamp(index, 0, maxIndex - 1);
    pattern = maxIndex == 0 ? ItemStack.EMPTY : patterns.get(index).copy();
    patternUu = pattern.isEmpty() ? 0 : UuIndex.instance.getInBuckets(pattern);
    patternEu = 0;
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    switch (event) {
      case 0, 1 -> {
        if (!patterns.isEmpty()) {
          index = Math.floorMod(index + (event == 0 ? -1 : 1), patterns.size());
          refreshInfo();
        }
      }
      case 2 -> {
        var crystal = diskSlot.get();
        if (!pattern.isEmpty() && crystal.getItem() instanceof ItemCrystalMemory memory)
          memory.writecontentsTag(getLevel(), crystal, patterns.get(index).copy());
      }
      case 3 -> {
        var crystal = diskSlot.get();
        if (crystal.getItem() instanceof ItemCrystalMemory memory)
          addPattern(memory.readItemStack(crystal));
      }
      default -> {
        return;
      }
    }
    setChanged();
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return new PatternLibraryMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new PatternLibraryMenu(id, inventory, this);
  }
}
