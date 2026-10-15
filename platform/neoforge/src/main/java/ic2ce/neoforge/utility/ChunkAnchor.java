package ic2ce.neoforge.utility;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.utility.menu.ChunkAnchorMenu;
import ic2ce.neoforge.world.ChunkLeases;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

/** Owned bounded chunk selection, paid tick leases and symmetric native attachment/release. */
@NotClassic
public class ChunkAnchor extends InventoryBlockEntity
    implements INetworkClientTileEntityEventListener, IHasGui, IUpgradableBlock {
  private static final Set<UpgradableProperty> UPGRADES =
      Set.of(
          UpgradableProperty.EnergyStorage,
          UpgradableProperty.ItemConsuming,
          UpgradableProperty.ItemProducing,
          UpgradableProperty.Transformer);
  private final LongSet loadedChunks = new LongOpenHashSet(9);
  private LongSet selectionView;
  private final double euPerChunk;
  private boolean attached;
  public final UpgradeInventory upgradeSlot;
  public final DischargeInventory dischargeSlot;
  public final ElectricalBuffer energy;

  public ChunkAnchor(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.CHUNK_LOADER, position, state);
    double configured =
        NativeConfigValues.getDouble(ProjectConfiguration.get(), "balance/euPerChunk");
    if (!Double.isFinite(configured) || configured < 0)
      throw new IllegalArgumentException("Chunk-loader cost must be finite and nonnegative");
    euPerChunk = configured;
    upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
    dischargeSlot =
        new DischargeInventory(this, InventorySlot.Access.IO, 1, true, InventorySlot.InvSide.ANY);
    energy =
        addComponent(ElectricalBuffer.asBasicSink(this, 2500, 1).addManagedSlot(dischargeSlot));
    loadedChunks.add(ChunkPos.pack(position));
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    loadedChunks.clear();
    loadedChunks.add(ChunkPos.pack(worldPosition));
    var list = data.getListOrEmpty("loadedChunks");
    for (int index = 0; index < list.size() && loadedChunks.size() < getMaxChunks(); index++)
      if (list.get(index) instanceof LongTag value
          && isChunkInRange(ChunkPos.unpack(value.value()))) loadedChunks.add(value.value());
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    var list = new ListTag();
    long[] selected = loadedChunks.toLongArray();
    java.util.Arrays.sort(selected);
    for (long chunk : selected) list.add(LongTag.valueOf(chunk));
    data.put("loadedChunks", list);
  }

  private void attach() {
    if (attached || !(getLevel() instanceof ServerLevel world)) return;
    ChunkLeases.addChunkLoader(world, worldPosition, loadedChunks);
    attached = true;
  }

  private void removeLease(boolean forgetSaved) {
    if (!(getLevel() instanceof ServerLevel world)) {
      attached = false;
      return;
    }
    if (forgetSaved) ChunkLeases.removeChunkLoader(world, worldPosition);
    else ChunkLeases.releaseRuntime(world, worldPosition);
    attached = false;
  }

  @Override
  public void onLoaded() {
    super.onLoaded();
    if (getLevel() instanceof ServerLevel) {
      setOverclockRates();
      if (getActive()) attach();
    }
  }

  @Override
  protected void onUnloaded() {
    removeLease(false);
    super.onUnloaded();
  }

  @Override
  protected void onBlockBreak() {
    removeLease(true);
    super.onBlockBreak();
  }

  @Override
  public void setActive(boolean active) {
    super.setActive(active);
    if (getLevel() instanceof ServerLevel) {
      if (active) attach();
      else removeLease(true);
    }
  }

  @Override
  public void updateEntityServer() {
    super.updateEntityServer();
    boolean paid = energy.useEnergy(loadedChunks.size() * euPerChunk);
    if (getActive() != paid) {
      setActive(paid);
      super.setChanged();
    } else if (paid && !attached) attach();
    upgradeSlot.tick();
  }

  @Override
  public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing) {
    super.onPlaced(stack, placer, facing);
    loadedChunks.add(ChunkPos.pack(worldPosition));
  }

  public LongSet getLoadedChunks() {
    if (selectionView == null) selectionView = LongSets.unmodifiable(loadedChunks);
    return selectionView;
  }

  public boolean isChunkInRange(ChunkPos chunk) {
    return chunk != null
        && Math.abs((long) chunk.x() - (worldPosition.getX() >> 4)) <= 4
        && Math.abs((long) chunk.z() - (worldPosition.getZ() >> 4)) <= 4;
  }

  public int getMaxChunks() {
    return 9;
  }

  private void selectionChanged() {
    if (attached && getLevel() instanceof ServerLevel world)
      ChunkLeases.updateChunkLoader(world, worldPosition, loadedChunks);
    super.setChanged();
  }

  public void addChunkToLoaded(ChunkPos chunk) {
    if (!(getLevel() instanceof ServerLevel)
        || !isChunkInRange(chunk)
        || loadedChunks.size() >= getMaxChunks()) return;
    if (loadedChunks.add(chunk.pack())) selectionChanged();
  }

  public void removeChunkFromLoaded(ChunkPos chunk) {
    if (!(getLevel() instanceof ServerLevel)
        || chunk == null
        || chunk.pack() == ChunkPos.pack(worldPosition)) return;
    if (loadedChunks.remove(chunk.pack())) selectionChanged();
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event < 0 || event > 255 || !(getLevel() instanceof ServerLevel)) return;
    int x = (event & 15) - 8, z = (event >>> 4 & 15) - 8;
    var chunk = new ChunkPos((worldPosition.getX() >> 4) + x, (worldPosition.getZ() >> 4) + z);
    if (loadedChunks.contains(chunk.pack())) removeChunkFromLoaded(chunk);
    else addChunkToLoaded(chunk);
  }

  public void setOverclockRates() {
    upgradeSlot.onChanged();
    int tier = upgradeSlot.getTier(1);
    energy.setSinkTier(tier);
    dischargeSlot.setTier(tier);
    energy.setCapacity(upgradeSlot.getEnergyStorage(2500, 0, 0));
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (getLevel() instanceof ServerLevel) setOverclockRates();
  }

  @Override
  public double getEnergy() {
    return energy.getEnergy();
  }

  @Override
  public boolean useEnergy(double amount) {
    return energy.useEnergy(amount);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return UPGRADES;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new ChunkAnchorMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new ChunkAnchorMenu(syncId, inventory, this);
  }
}
