package ic2ce.api.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Drill actions are device-owned; the public default pays only a finite physical bill. */
public interface IMiningDrill {
  int energyUse(ItemStack drill, Level world, BlockPos position, BlockState state);

  int breakTime(ItemStack drill, Level world, BlockPos position, BlockState state);

  boolean breakBlock(ItemStack drill, Level world, BlockPos position, BlockState state);

  default boolean tryUsePower(ItemStack drill, double amount) {
    if (drill == null || drill.isEmpty() || !Double.isFinite(amount) || amount < 0) return false;
    return amount == 0
        || ElectricItem.manager != null && ElectricItem.manager.use(drill, amount, null);
  }
}
