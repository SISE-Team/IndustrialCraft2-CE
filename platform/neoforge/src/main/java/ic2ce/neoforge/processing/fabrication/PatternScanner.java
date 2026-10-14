package ic2ce.neoforge.processing.fabrication;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.recipe.IPatternStorage;
import ic2ce.core.IHasGui;
import ic2ce.core.item.ItemCrystalMemory;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.profile.NotClassic;
import ic2ce.core.util.StackTag;
import ic2ce.core.util.StackUtil;
import ic2ce.core.uu.UuGraph;
import ic2ce.core.uu.UuIndex;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.inventory.ConsumableInventory;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.ItemWhitelistInput;
import ic2ce.neoforge.inventory.ScannerInput;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.menu.ScanningMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Server-authoritative scanning caches one canonical candidate per operation and bills only
 * advancing ticks.
 */
@NotClassic
public class PatternScanner extends PoweredBlockEntity
    implements IHasGui, INetworkClientTileEntityEventListener {
  private ItemStack currentStack = ItemStack.EMPTY, pattern = ItemStack.EMPTY;
  public int progress;
  public final int duration = 3300;
  public final ConsumableInventory inputSlot;
  public final InventorySlot diskSlot;
  private State state = State.IDLE;
  public double patternUu, patternEu;
  private static final Direction[] DIRECTIONS = Direction.values();

  public PatternScanner(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.UU_SCANNER, position, block, 512000, 4);
    inputSlot = new ScannerInput(this, "input", 1);
    diskSlot =
        new ItemWhitelistInput(
            this,
            "disk",
            InventorySlot.Access.IO,
            1,
            InventorySlot.InvSide.ANY,
            ItemDefinitions.CRYSTAL_MEMORY);
  }

  private void clearWork() {
    progress = 0;
    currentStack = pattern = ItemStack.EMPTY;
    patternUu = patternEu = 0;
  }

  public void reset() {
    clearWork();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean running = false;
    if (progress >= duration) {
      if (pattern.isEmpty()) {
        clearWork();
        state = State.IDLE;
      }
    } else {
      var input = inputSlot.get();
      if (input.isEmpty()
          || !currentStack.isEmpty() && !StackUtil.checkItemEquality(currentStack, input)) {
        clearWork();
        state = State.IDLE;
      } else {
        var storage = getPatternStorage();
        if (storage == null && diskSlot.isEmpty()) {
          clearWork();
          state = State.NO_STORAGE;
        } else {
          if (currentStack.isEmpty()) currentStack = input.copyWithCount(1);
          if (pattern.isEmpty()) {
            var candidate = UuGraph.find(currentStack);
            pattern =
                candidate == null || candidate.isEmpty()
                    ? ItemStack.EMPTY
                    : candidate.copyWithCount(1);
          }
          if (pattern.isEmpty()) state = State.FAILED;
          else if (recorded(pattern, storage)) {
            clearWork();
            state = State.ALREADY_RECORDED;
          } else if (!energy.useEnergy(256)) state = State.NO_ENERGY;
          else {
            running = true;
            state = State.SCANNING;
            if (++progress >= duration) {
              refreshInfo();
              if (Double.isFinite(patternUu) && patternUu >= 0) {
                inputSlot.put(input.copyWithCount(input.getCount() - 1));
                state = State.COMPLETED;
                setChanged();
              } else state = State.FAILED;
            }
          }
        }
      }
    }
    setActive(running);
  }

  private boolean recorded(ItemStack candidate, IPatternStorage storage) {
    var disk = diskSlot.get();
    if (disk.getItem() instanceof ItemCrystalMemory memory
        && StackUtil.checkItemEquality(memory.readItemStack(disk), candidate)) return true;
    if (storage instanceof PatternLibrary indexed) return indexed.containsPattern(candidate);
    if (storage != null)
      for (var entry : storage.getPatterns())
        if (StackUtil.checkItemEquality(entry, candidate)) return true;
    return false;
  }

  public IPatternStorage getPatternStorage() {
    var world = getLevel();
    if (world == null) return null;
    for (var side : DIRECTIONS)
      if (world.getBlockEntity(worldPosition.relative(side)) instanceof IPatternStorage storage)
        return storage;
    return null;
  }

  public boolean savetoDisk(ItemStack recorded) {
    var crystal = diskSlot.get();
    if (recorded == null
        || recorded.isEmpty()
        || !(crystal.getItem() instanceof ItemCrystalMemory memory)) return false;
    memory.writecontentsTag(getLevel(), crystal, recorded.copyWithCount(1));
    return true;
  }

  private void record() {
    if (pattern.isEmpty() || !Double.isFinite(patternUu) || patternUu < 0) {
      clearWork();
      state = State.IDLE;
      return;
    }
    if (!savetoDisk(pattern)) {
      var storage = getPatternStorage();
      if (storage == null || !storage.addPattern(pattern.copyWithCount(1))) {
        state = State.TRANSFER_ERROR;
        return;
      }
    }
    clearWork();
    state = State.IDLE;
    setChanged();
  }

  private void refreshInfo() {
    patternUu = pattern.isEmpty() ? 0 : UuIndex.instance.getInBuckets(pattern);
    patternEu = 0;
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    progress = Math.clamp(data.getIntOr("progress", 0), 0, duration);
    currentStack =
        StackTag.parse(getSerializationLookup(), data.getCompoundOrEmpty("currentStack"));
    pattern = StackTag.parse(getSerializationLookup(), data.getCompoundOrEmpty("pattern"));
    int saved = data.getIntOr("state", 0);
    state = saved >= 0 && saved < State.values().length ? State.values()[saved] : State.IDLE;
    refreshInfo();
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("progress", progress);
    data.putInt("state", state.ordinal());
    if (currentStack.isEmpty()) data.remove("currentStack");
    else
      data.put(
          "currentStack", StackTag.save(getSerializationLookup(), currentStack, new CompoundTag()));
    if (pattern.isEmpty()) data.remove("pattern");
    else data.put("pattern", StackTag.save(getSerializationLookup(), pattern, new CompoundTag()));
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event == 0) {
      clearWork();
      state = State.IDLE;
      setChanged();
    } else if (event == 1 && isDone()) record();
  }

  public int getPercentageDone() {
    return (int) ((long) 100 * progress / duration);
  }

  public int getSubPercentageDoneScaled(int width) {
    return (int) ((long) width * ((long) 100 * progress % duration) / duration);
  }

  public boolean isDone() {
    return progress >= duration;
  }

  public State getState() {
    return state;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return new ScanningMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new ScanningMenu(id, inventory, this);
  }

  public enum State {
    IDLE,
    SCANNING,
    COMPLETED,
    FAILED,
    NO_STORAGE,
    NO_ENERGY,
    TRANSFER_ERROR,
    ALREADY_RECORDED
  }
}
