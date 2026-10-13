package ic2.api.energy.prefab;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BasicEnergyTe<T extends BasicEnergyTile> extends BlockEntity {
   protected T energyBuffer;

   protected BasicEnergyTe(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   @Override
   protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
      super.loadAdditional(input);
      this.load(input.read("ic2", CompoundTag.CODEC).orElseGet(CompoundTag::new));
   }

   @Override
   protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
      super.saveAdditional(output);
      CompoundTag data = new CompoundTag();
      this.saveAdditional(data);
      output.store("ic2", CompoundTag.CODEC, data);
   }

   public T getEnergyBuffer() {
      return this.energyBuffer;
   }

   public void setRemoved() {
      super.setRemoved();
      this.energyBuffer.invalidate();
   }

   public void load(CompoundTag nbt) {
      this.energyBuffer.readFromNBT(nbt);
   }

   protected void saveAdditional(CompoundTag nbt) {
      this.energyBuffer.writeToNBT(nbt);
   }

   public static class Sink extends BasicEnergyTe<BasicSink> {
      public Sink(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity, int tier) {
         super(type, pos, state);
         this.energyBuffer = new BasicSink(this, (double)capacity, tier);
      }
   }

   public static class Source extends BasicEnergyTe<BasicSource> {
      public Source(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity, int tier) {
         super(type, pos, state);
         this.energyBuffer = new BasicSource(this, (double)capacity, tier);
      }
   }
}
