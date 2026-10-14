package ic2ce.neoforge.item;

import ic2ce.neoforge.item.container.FixedCell;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

/** Real source/waterlogged world cells and native machine wear quantity boundaries. */
public final class WorldCellContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native world cell: " + label);
  }

  public static Result run(ServerLevel world) {
    return new WorldCellContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var pos = new BlockPos(11264, 100, 11264);
    world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
    world.getChunkAt(pos);
    var cell = (FixedCell) ItemDefinitions.EMPTY_CELL;
    world.setBlock(pos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 1), 3);
    var output = cell.tryDrainFluid(world, pos, world.getBlockState(pos));
    check(
        output.isEmpty() && world.getBlockState(pos).getValue(LiquidBlock.LEVEL) == 1,
        "flowing water cannot manufacture a source-filled cell");
    world.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
    output = cell.tryDrainFluid(world, pos, world.getBlockState(pos));
    check(
        output.is(ItemDefinitions.WATER_CELL) && output.getCount() == 1 && world.isEmptyBlock(pos),
        "one actual native water source becomes exactly one filled physical cell");
    world.setBlock(
        pos,
        Blocks.OAK_STAIRS
            .defaultBlockState()
            .setValue(BlockStateProperties.WATERLOGGED, true)
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
        3);
    output = cell.tryDrainFluid(world, pos, world.getBlockState(pos));
    check(
        output.is(ItemDefinitions.WATER_CELL)
            && world.getBlockState(pos).is(Blocks.OAK_STAIRS)
            && !world.getBlockState(pos).getValue(BlockStateProperties.WATERLOGGED)
            && world.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING)
                == Direction.WEST,
        "native waterlogged pickup preserves the block and its physical orientation");
    world.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
    var water = (FixedCell) ItemDefinitions.WATER_CELL;
    check(
        !water.emptyContents(null, world, pos, null) && world.getBlockState(pos).is(Blocks.STONE),
        "solid native obstruction cannot be overwritten by a paid fluid cell");
    world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    check(
        water.emptyContents(null, world, pos, null)
            && world.getFluidState(pos).isSource()
            && world.getFluidState(pos).getType() == Fluids.WATER,
        "actual world placement creates one native source fluid");
    var cutter = new ItemStack(ItemDefinitions.CUTTER);
    var before = cutter.copy();
    check(
        !PhysicalWear.damage(cutter, -1, (net.minecraft.world.entity.LivingEntity) null, null)
            && ItemStack.matches(cutter, before),
        "negative machine wear cannot repair or increase a physical tool budget");
    cutter.setDamageValue(cutter.getMaxDamage() - 1);
    boolean broken =
        PhysicalWear.damage(
            cutter, Integer.MAX_VALUE, (net.minecraft.server.level.ServerLevel) null, null, null);
    check(
        broken && cutter.getCount() == 1 && cutter.getDamageValue() == cutter.getMaxDamage(),
        "machine overflow wear produces a bounded break signal and preserves the slot-owned item"
            + " count");
    return new Result(checks, List.copyOf(failures));
  }
}
