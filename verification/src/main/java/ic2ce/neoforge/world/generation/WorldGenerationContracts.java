package ic2ce.neoforge.world.generation;

import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * Actual loaded native tree and each distinct configured ore feature run in isolated prepared
 * terrain.
 */
public final class WorldGenerationContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String text) {
    checks++;
    if (!ok) failures.add("native world generation: " + text);
  }

  public static Result run(ServerLevel world) {
    return new WorldGenerationContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var placed = world.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
    for (var key : WorldFeatures.ORE_FEATURES)
      check(
          placed.get(key).isPresent(), "distinct native ore placement loaded " + key.identifier());
    for (var key : WorldFeatures.TREE_FEATURES)
      check(
          placed.get(key).isPresent(),
          "native biome-specific tree placement loaded " + key.identifier());
    var configured = world.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE);
    var tree = WorldFeatures.rubberTree(world).value();
    var cursor = new BlockPos.MutableBlockPos();
    int grown = 0, resin = 0;
    for (int example = 0; example < 12; example++) {
      var center = new BlockPos(11520 + example * 16, 100, 11520);
      prepare(world, center, 4, 0, 16);
      world.setBlock(center.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
      boolean success =
          tree.place(
              world,
              world.getChunkSource().getGenerator(),
              RandomSource.create(2026101404 + example),
              center);
      int logs = 0, leaves = 0;
      for (int y = 0; y < 16; y++)
        for (int x = -4; x <= 4; x++)
          for (int z = -4; z <= 4; z++) {
            cursor.setWithOffset(center, x, y, z);
            var state = world.getBlockState(cursor);
            if (state.is(BlockDefinitions.RUBBER_LOG)) {
              logs++;
              if (state.getValue(ic2ce.neoforge.world.block.ResinLog.stateProperty).wet) resin++;
            } else if (state.is(BlockDefinitions.RUBBER_LEAVES)) leaves++;
          }
      check(
          success && logs >= 4 && logs <= 8 && leaves > 8,
          "actual native resin tree " + example + " logs=" + logs + " leaves=" + leaves);
      if (success) grown++;
    }
    check(
        grown == 12 && resin > 0, "native tree distribution produces usable resin-bearing trunks");
    int ordinal = 0;
    for (String name :
        List.of(
            "ore_lead",
            "ore_tin",
            "ore_tin_small",
            "ore_uranium",
            "ore_uranium_buried",
            "ore_uranium_large")) {
      var center = new BlockPos(11840 + ordinal * 32, 100, 11520);
      prepare(world, center, 10, -8, 8);
      for (int y = -8; y <= 8; y++)
        for (int x = -10; x <= 10; x++)
          for (int z = -10; z <= 10; z++) {
            cursor.setWithOffset(center, x, y, z);
            world.setBlock(cursor, Blocks.STONE.defaultBlockState(), 2);
          }
      var key =
          ResourceKey.create(Registries.CONFIGURED_FEATURE, ProjectRuntime.getIdentifier(name));
      var feature = configured.getOrThrow(key).value();
      boolean success =
          feature.place(
              world,
              world.getChunkSource().getGenerator(),
              RandomSource.create(2026101450 + ordinal),
              center);
      int ores = 0;
      for (int y = -8; y <= 8; y++)
        for (int x = -10; x <= 10; x++)
          for (int z = -10; z <= 10; z++) {
            cursor.setWithOffset(center, x, y, z);
            var state = world.getBlockState(cursor);
            if (state.is(BlockDefinitions.LEAD_ORE)
                || state.is(BlockDefinitions.TIN_ORE)
                || state.is(BlockDefinitions.URANIUM_ORE)) ores++;
          }
      check(
          success && ores > 0,
          "actual distinct native " + name + " creates registered ore cells " + ores);
      ordinal++;
    }
    return new Result(checks, List.copyOf(failures));
  }

  private static void prepare(ServerLevel world, BlockPos center, int radius, int minY, int maxY) {
    for (int x = (center.getX() - radius) >> 4; x <= (center.getX() + radius) >> 4; x++)
      for (int z = (center.getZ() - radius) >> 4; z <= (center.getZ() + radius) >> 4; z++) {
        world.setChunkForced(x, z, true);
        world.getChunk(x, z);
      }
    var cursor = new BlockPos.MutableBlockPos();
    for (int x = -radius; x <= radius; x++)
      for (int z = -radius; z <= radius; z++) {
        cursor.setWithOffset(center, x, minY - 1, z);
        world.setBlock(cursor, Blocks.DIRT.defaultBlockState(), 2);
        for (int y = minY; y <= maxY; y++) {
          cursor.setWithOffset(center, x, y, z);
          world.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
        }
      }
  }
}
