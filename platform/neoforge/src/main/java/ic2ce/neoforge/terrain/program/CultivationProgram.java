package ic2ce.neoforge.terrain.program;

import ic2ce.neoforge.terrain.TerrainController;
import ic2ce.neoforge.world.DirectionSets;
import ic2ce.neoforge.world.WorldBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Weighted vegetation choices rebuilt immutably, with valid crop and paired-plant placement. */
public final class CultivationProgram extends TerrainProgram {
  private static List<BlockState> plants = List.of();

  @Override
  public void init() {
    var choices = new ArrayList<BlockState>();
    for (var block :
        new Block[] {
          Blocks.SHORT_GRASS,
          Blocks.SHORT_GRASS,
          Blocks.FERN,
          Blocks.POPPY,
          Blocks.DANDELION,
          Blocks.SHORT_GRASS,
          Blocks.ROSE_BUSH,
          Blocks.SUNFLOWER
        }) choices.add(block.defaultBlockState());
    for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTags.SAPLINGS))
      if (BuiltInRegistries.BLOCK.getKey(holder.value()).getNamespace().equals("minecraft"))
        choices.add(holder.value().defaultBlockState());
    for (var block :
        new Block[] {
          Blocks.WHEAT,
          Blocks.RED_MUSHROOM,
          Blocks.BROWN_MUSHROOM,
          Blocks.PUMPKIN,
          Blocks.MELON,
          WorldBlocks.RUBBER_SAPLING
        }) choices.add(block.defaultBlockState());
    plants = List.copyOf(choices);
  }

  static boolean containsPlant(Block block) {
    for (var state : plants) if (state.getBlock() == block) return true;
    return false;
  }

  @Override
  public boolean terraform(Level world, BlockPos chosen) {
    var surface = TerrainController.getFirstSolidBlockFrom(world, chosen, 10);
    if (surface == null || !editable(world, surface)) return false;
    if (TerrainController.switchGround(
        world, surface, Blocks.SAND, Blocks.DIRT.defaultBlockState(), true)) return true;
    boolean converted = false;
    for (int depth = 0; depth < 4; depth++) {
      if (!TerrainController.switchGround(
          world, surface, Blocks.END_STONE, Blocks.DIRT.defaultBlockState(), true)) break;
      converted = true;
    }
    var ground = world.getBlockState(surface);
    if (ground.is(Blocks.DIRT))
      return replace(world, surface, Blocks.GRASS_BLOCK.defaultBlockState()) || converted;
    return ground.is(Blocks.GRASS_BLOCK) && grow(world, surface) || converted;
  }

  private static boolean grow(Level world, BlockPos ground) {
    var above = ground.above();
    var existing = world.getBlockState(above);
    var random = world.getRandom();
    if (plants.isEmpty()
        || !(existing.isAir() || existing.is(Blocks.SHORT_GRASS) && random.nextInt(4) == 0))
      return false;
    var plant = plants.get(random.nextInt(plants.size()));
    if (plant.hasProperty(DirectionalBlock.FACING))
      plant =
          plant.setValue(
              DirectionalBlock.FACING,
              DirectionSets.HORIZONTAL_DIRS[random.nextInt(DirectionSets.HORIZONTAL_DIRS.length)]);
    if (plant.getBlock() instanceof CropBlock) {
      if (!replace(world, ground, Blocks.FARMLAND.defaultBlockState())) return false;
    } else if (!plant.canSurvive(world, above)) return false;
    if (plant.getBlock() instanceof DoublePlantBlock) {
      var upper = above.above();
      if (!editable(world, upper) || !world.isEmptyBlock(upper)) return false;
      if (!replace(world, above, plant.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER)))
        return false;
      return replace(world, upper, plant.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
    }
    return replace(world, above, plant);
  }
}
