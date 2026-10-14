package ic2ce.neoforge.mining;

import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

/** Actual pipe removal at its exact bill and retained partial inventory overflow. */
public final class PipeMiningContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native pipe miner: " + label);
  }

  public static Result run(ServerLevel world) {
    try {
      return new PipeMiningContracts().exercise(world);
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException(failure);
    }
  }

  private Result exercise(ServerLevel world) throws ReflectiveOperationException {
    var position = new BlockPos(8520, 100, 8520);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, BlockDefinitions.MINER.defaultBlockState());
    var miner = (PipeMiner) world.getBlockEntity(position);
    var energy = miner.getComponent(ElectricalBuffer.class);
    world.setBlockAndUpdate(position.below(), BlockDefinitions.MINING_PIPE_TIP.defaultBlockState());
    energy.forceAddEnergy(60);
    for (int tick = 0; tick < 20; tick++) miner.updateEntityServer();
    int pipes = 0;
    for (var stack : miner.buffer)
      if (stack.is(ItemDefinitions.MINING_PIPE)) pipes += stack.getCount();
    check(
        world.getBlockState(position.below()).isAir() && energy.getEnergy() == 0 && pipes == 1,
        "twenty paid native steps retract a real pipe with exactly sixty EU");
    for (int index = 0; index < miner.buffer.size(); index++)
      miner.buffer.put(index, new ItemStack(Items.BEDROCK, 64));
    miner.buffer.put(0, new ItemStack(Items.GOLD_INGOT, 63));
    var deliver = PipeMiner.class.getDeclaredMethod("storeDrop", ItemStack.class);
    deliver.setAccessible(true);
    var input = new ItemStack(Items.GOLD_INGOT, 2);
    deliver.invoke(miner, input);
    int loose =
        world.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(3)).stream()
            .filter(entity -> entity.getItem().is(Items.GOLD_INGOT))
            .mapToInt(entity -> entity.getItem().getCount())
            .sum();
    check(
        miner.buffer.get(0).getCount() == 64 && loose == 1 && input.getCount() == 2,
        "one stored and one loose gold ingot conserve a partial-capacity drop without mutating the"
            + " input snapshot; stored="
            + miner.buffer.get(0).getCount()
            + ", loose="
            + loose
            + ", input="
            + input.getCount());
    var corrupt = new CompoundTag();
    corrupt.putInt("lastMode", Integer.MAX_VALUE);
    corrupt.putInt("progress", -1);
    miner.load(corrupt);
    check(miner.progress == 0, "corrupt saved mode and negative progress load safely");
    for (var item : world.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(3)))
      item.discard();
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
