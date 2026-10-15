package ic2ce.neoforge.thermal;

import ic2ce.api.energy.tile.IHeatSource;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Bounded heat endpoint; simulation and invalid requests never change its stored heat. */
public abstract class BufferedHeatSource extends InventoryBlockEntity implements IHeatSource {
  @GuiSynced protected int transmitHeat, maxHeatEmitpeerTick;
  protected int HeatBuffer;

  protected BufferedHeatSource(
      BlockEntityType<? extends BufferedHeatSource> type, BlockPos position, BlockState block) {
    super(type, position, block);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    int maximum = Math.max(0, getMaxHeatEmittedPerTick());
    maxHeatEmitpeerTick = maximum;
    int missing = Math.max(0, maximum - HeatBuffer);
    if (missing > 0) addtoHeatBuffer(Math.clamp(fillHeatBuffer(missing), 0, missing));
  }

  @Override
  public int maxrequestHeatTick(Direction side) {
    return getConnectionBandwidth(side);
  }

  @Override
  public int getConnectionBandwidth(Direction side) {
    return facingMatchesDirection(side) ? Math.max(0, getMaxHeatEmittedPerTick()) : 0;
  }

  @Override
  public int requestHeat(Direction side, int request) {
    return drawHeat(side, request, false);
  }

  @Override
  public int drawHeat(Direction side, int request, boolean simulate) {
    if (request <= 0 || !facingMatchesDirection(side)) return 0;
    int amount = Math.min(request, Math.min(HeatBuffer, Math.max(0, getMaxHeatEmittedPerTick())));
    if (!simulate) {
      HeatBuffer -= amount;
      transmitHeat = amount;
    }
    return amount;
  }

  public boolean facingMatchesDirection(Direction side) {
    return side == getFacing();
  }

  public int getHeatBuffer() {
    return HeatBuffer;
  }

  public void setHeatBuffer(int heat) {
    HeatBuffer = Math.max(0, heat);
  }

  public void addtoHeatBuffer(int heat) {
    if (heat > 0) HeatBuffer = (int) Math.min(Integer.MAX_VALUE, (long) HeatBuffer + heat);
  }

  public int gettransmitHeat() {
    return transmitHeat;
  }

  public String getOutput() {
    return transmitHeat + " / " + Math.max(0, getMaxHeatEmittedPerTick());
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    HeatBuffer = Math.max(0, data.getIntOr("HeatBuffer", 0));
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("HeatBuffer", HeatBuffer);
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (ProjectRuntime.sideProxy.isSimulating())
      maxHeatEmitpeerTick = Math.max(0, getMaxHeatEmittedPerTick());
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (ProjectRuntime.sideProxy.isSimulating())
      maxHeatEmitpeerTick = Math.max(0, getMaxHeatEmittedPerTick());
  }

  protected abstract int fillHeatBuffer(int maximum);

  public abstract int getMaxHeatEmittedPerTick();
}
