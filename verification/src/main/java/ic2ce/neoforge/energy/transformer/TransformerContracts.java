package ic2ce.neoforge.energy.transformer;

import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/**
 * Packet multiplicity, native orientation and actual GUI input/output meanings across voltage
 * modes.
 */
public final class TransformerContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native transformer: " + label);
  }

  public static Result run(ServerLevel world) {
    return new TransformerContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8390, 100, 8390);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, BlockDefinitions.LV_TRANSFORMER.defaultBlockState());
    var transformer = (VoltageTransformer) world.getBlockEntity(position);
    transformer.setFacing(world, Direction.EAST);
    transformer.onNetworkEvent(null, 1);
    var energy = transformer.energy;
    check(
        energy.getSinkTier() == 2 && energy.getSourceTier() == 1 && energy.getPacketOutput() == 4,
        "step-down converts one high packet to four low packets");
    check(
        transformer.getinputflow() == 128 && transformer.getoutputflow() == 32,
        "step-down screen shows high input and low output");
    transformer.onNetworkEvent(null, 2);
    check(
        energy.getSinkTier() == 1
            && energy.getSourceTier() == 2
            && energy.getPacketOutput() == 1
            && energy.getSourceDirs().equals(java.util.Set.of(Direction.EAST)),
        "step-up uses the real oriented front with one high packet");
    check(
        transformer.getinputflow() == 32 && transformer.getoutputflow() == 128,
        "step-up screen shows low input and high output");
    transformer.onNetworkEvent(null, Integer.MAX_VALUE);
    check(
        transformer.getMode() == VoltageTransformer.Mode.stepup,
        "invalid remote mode cannot change an existing configuration");
    var saved = new CompoundTag();
    transformer.saveAdditional(saved);
    check(saved.getIntOr("mode", -1) == 2, "button choice persists through native save");
    saved.putInt("mode", -1);
    transformer.load(saved);
    check(
        transformer.getMode() == VoltageTransformer.Mode.redstone,
        "corrupt saved mode restores automatic control");
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
