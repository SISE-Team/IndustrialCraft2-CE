package ic2ce.api.energy.prefab;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Native addon block-entity lifecycle and value persistence adapter for a finite energy buffer. */
public class BasicEnergyTe<T extends BasicEnergyTile> extends BlockEntity {
  protected T energyBuffer;

  protected BasicEnergyTe(BlockEntityType<?> type, BlockPos position, BlockState state) {
    super(type, position, state);
  }

  public T getEnergyBuffer() {
    return energyBuffer;
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    load(input.read("ic2", CompoundTag.CODEC).orElseGet(CompoundTag::new));
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    var data = new CompoundTag();
    saveAdditional(data);
    output.store("ic2", CompoundTag.CODEC, data);
  }

  @Override
  public void setRemoved() {
    if (energyBuffer != null) energyBuffer.invalidate();
    super.setRemoved();
  }

  public void load(CompoundTag data) {
    if (energyBuffer != null) energyBuffer.readFromNBT(data);
  }

  protected void saveAdditional(CompoundTag data) {
    if (energyBuffer != null) energyBuffer.writeToNBT(data);
  }

  public static class Sink extends BasicEnergyTe<BasicSink> {
    public Sink(
        BlockEntityType<?> type, BlockPos position, BlockState state, int capacity, int tier) {
      super(type, position, state);
      energyBuffer = new BasicSink(this, capacity, tier);
    }
  }

  public static class Source extends BasicEnergyTe<BasicSource> {
    public Source(
        BlockEntityType<?> type, BlockPos position, BlockState state, int capacity, int tier) {
      super(type, position, state);
      energyBuffer = new BasicSource(this, capacity, tier);
    }
  }
}
