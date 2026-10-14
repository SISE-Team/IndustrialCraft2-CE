package ic2ce.neoforge.item.tool;

import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.world.block.ResinLog;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

/**
 * Native automated collection and loose output are mutually exclusive, including the original
 * dry-scar duplication edge.
 */
public final class ResinContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run(ServerLevel world) {
    var failures = new ArrayList<String>();
    int checks = 0;
    var position = new BlockPos(9360, 100, 9360);
    world.getChunkAt(position);
    var wet =
        Arrays.stream(ResinLog.RubberWoodState.values())
            .filter(state -> state.wet && state.facing == Direction.NORTH)
            .findFirst()
            .orElseThrow();
    var state =
        BlockDefinitions.RUBBER_LOG.defaultBlockState().setValue(ResinLog.stateProperty, wet);
    var output = new ArrayList<ItemStack>();
    world.setBlock(position, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    boolean extracted =
        ResinExtraction.extract(null, world, position, Direction.NORTH, state, output, false);
    checks++;
    if (!extracted
        || output.size() != 1
        || output.getFirst().getCount() < 1
        || output.getFirst().getCount() > 3
        || world.getBlockState(position).getValue(ResinLog.stateProperty).wet)
      failures.add(
          "native resin: automated null-player wet extraction owns one bounded quantity and dries"
              + " the actual scar");
    checks++;
    if (!world.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(2)).isEmpty())
      failures.add("native resin: collected wet output must not also spawn loose items");
    int dryResin = 0;
    var dry = state.setValue(ResinLog.stateProperty, wet.getDry());
    world.getRandom().setSeed(726482);
    for (int trial = 0; trial < 1000; trial++) {
      world.setBlock(position, dry, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
      output.clear();
      ResinExtraction.extract(null, world, position, Direction.NORTH, dry, output, false);
      for (var stack : output) {
        dryResin += stack.getCount();
        checks++;
        if (!stack.is(ItemDefinitions.RESIN) || stack.getCount() != 1)
          failures.add("native resin: dry collection owns exactly one resin per successful draw");
      }
    }
    checks++;
    if (dryResin < 100
        || dryResin > 300
        || !world.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(2)).isEmpty())
      failures.add(
          "native resin: dry extraction is bounded and cannot duplicate the collected resin into"
              + " the world; quantity="
              + dryResin);
    world.setBlock(position, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    output.clear();
    checks++;
    if (ResinExtraction.extract(null, world, position, Direction.SOUTH, state, output, false)
        || !output.isEmpty()
        || world.getBlockState(position) != state)
      failures.add("native resin: wrong face cannot change a scar or create output");
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
