package ic2ce.neoforge.terrain.program;

import ic2ce.neoforge.world.BiomeUtil;
import ic2ce.neoforge.terrain.TerrainController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MushroomBlock;

/** Bounded three-stage soil conversion, mushroom seeding and native giant growth. */
public final class FungalProgram extends TerrainProgram {
  @Override
  public boolean terraform(Level world, BlockPos chosen) {
    if (!(world instanceof ServerLevel server)) return false;
    var surface = TerrainController.getFirstSolidBlockFrom(world, chosen, 20);
    if (surface == null || !editable(world, surface)) return false;
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++) {
        var column = surface.offset(x, 0, z);
        if (!world.isLoaded(column)) continue;
        for (int y = Math.min(world.getMaxY(), surface.getY() + 5);
            y >= Math.max(world.getMinY(), surface.getY() - 1);
            y--) {
          var cell = new BlockPos(column.getX(), y, column.getZ());
          var ground = world.getBlockState(cell);
          if (ground.is(Blocks.DIRT) || ground.is(Blocks.GRASS_BLOCK)) {
            if (replace(world, cell, Blocks.MYCELIUM.defaultBlockState())) {
              BiomeUtil.setBiome(
                  world,
                  cell,
                  world
                      .registryAccess()
                      .lookupOrThrow(Registries.BIOME)
                      .getOrThrow(Biomes.MUSHROOM_FIELDS));
              return true;
            }
          }
        }
      }
    var ground = world.getBlockState(surface);
    if (ground.is(Blocks.BROWN_MUSHROOM_BLOCK) || ground.is(Blocks.RED_MUSHROOM_BLOCK)) {
      if (replace(world, surface, Blocks.MYCELIUM.defaultBlockState())) return true;
    }
    if (!ground.is(Blocks.MYCELIUM)) return false;
    var above = surface.above();
    var state = world.getBlockState(above);
    if (state.isAir() || state.is(Blocks.SHORT_GRASS))
      return replace(
          world,
          above,
          (world.getRandom().nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM)
              .defaultBlockState());
    if (!(state.getBlock() instanceof MushroomBlock mushroom)) return false;
    return mushroom.growMushroom(server, above, state, world.getRandom());
  }
}
