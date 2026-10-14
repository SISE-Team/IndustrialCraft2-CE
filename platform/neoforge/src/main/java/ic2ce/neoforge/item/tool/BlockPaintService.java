package ic2ce.neoforge.item.tool;

import ic2ce.core.util.VanillaColorBlockId;
import ic2ce.neoforge.cable.ConductorBlock;
import java.util.EnumMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Native recoloring preserves physical properties, paired furniture and owned block-entity data.
 */
public final class BlockPaintService {
  private static final EnumMap<VanillaColorBlockId, Block[]> PALETTE =
      new EnumMap<>(VanillaColorBlockId.class);

  private BlockPaintService() {}

  public static synchronized Block colorBlock(DyeColor color, VanillaColorBlockId family) {
    var colors = PALETTE.get(family);
    if (colors == null) {
      colors = new Block[DyeColor.values().length];
      for (var pigment : DyeColor.values())
        colors[pigment.ordinal()] =
            BuiltInRegistries.BLOCK.getValue(
                Identifier.withDefaultNamespace(pigment.getName() + "_" + family.id));
      PALETTE.put(family, colors);
    }
    return colors[color.ordinal()];
  }

  public static VanillaColorBlockId family(BlockState state) {
    var block = state.getBlock();
    if (block instanceof BedBlock) return VanillaColorBlockId.BED;
    if (block instanceof ShulkerBoxBlock) return VanillaColorBlockId.SHULKER_BOX;
    if (block instanceof WallBannerBlock) return VanillaColorBlockId.WALL_BANNER;
    if (block instanceof BannerBlock) return VanillaColorBlockId.BANNER;
    if (block instanceof GlazedTerracottaBlock) return VanillaColorBlockId.GLAZED_TERRACOTTA;
    if (block instanceof ConcretePowderBlock) return VanillaColorBlockId.CONCRETE_POWDER;
    if (block instanceof StainedGlassPaneBlock || block == Blocks.GLASS_PANE)
      return VanillaColorBlockId.STAINED_GLASS_PANE;
    if (block instanceof StainedGlassBlock || block == Blocks.GLASS)
      return VanillaColorBlockId.STAINED_GLASS;
    if (state.is(BlockTags.WOOL)) return VanillaColorBlockId.WOOL;
    if (state.is(BlockTags.WOOL_CARPETS)) return VanillaColorBlockId.CARPET;
    if (state.is(BlockTags.CANDLES)) return VanillaColorBlockId.CANDLE;
    if (state.is(BlockTags.TERRACOTTA)) return VanillaColorBlockId.TERRACOTTA;
    var id = BuiltInRegistries.BLOCK.getKey(block);
    return id.getNamespace().equals("minecraft") && id.getPath().endsWith("_concrete")
        ? VanillaColorBlockId.CONCRETE
        : null;
  }

  @SuppressWarnings("unchecked")
  private static BlockState replacement(BlockState state, DyeColor color) {
    for (var property : state.getProperties())
      if (property.getValueClass() == DyeColor.class) {
        var pigment = (Property<DyeColor>) property;
        return pigment.getPossibleValues().contains(color) ? state.setValue(pigment, color) : state;
      }
    var family = family(state);
    if (family == null) return state;
    var block = colorBlock(color, family);
    return block == null || block == Blocks.AIR ? state : block.withPropertiesOf(state);
  }

  public static boolean canPaint(Level world, BlockPos position, DyeColor color) {
    if (color == null || !world.isLoaded(position)) return false;
    var state = world.getBlockState(position);
    if (state.getBlock() instanceof ConductorBlock cable)
      return cable.hasColor() && state.getValue(ConductorBlock.colorProperty) != color;
    var next = replacement(state, color);
    if (next == state) return false;
    if (!(state.getBlock() instanceof BedBlock)) return true;
    var other = position.relative(BedBlock.getConnectedDirection(state));
    return validBed(world, state, other);
  }

  private static boolean validBed(Level world, BlockState first, BlockPos other) {
    if (!world.isLoaded(other)) return false;
    var second = world.getBlockState(other);
    return second.is(first.getBlock())
        && second.getValue(BedBlock.PART) != first.getValue(BedBlock.PART)
        && second.getValue(BedBlock.FACING) == first.getValue(BedBlock.FACING);
  }

  private record Cell(BlockPos position, BlockState before, BlockState after, CompoundTag data) {}

  private static Cell cell(Level world, BlockPos position, BlockState before, BlockState after) {
    var entity = world.getBlockEntity(position);
    return new Cell(
        position.immutable(),
        before,
        after,
        entity == null ? null : entity.saveWithFullMetadata(world.registryAccess()).copy());
  }

  private static boolean install(Level world, Cell cell, boolean forward) {
    var state = forward ? cell.after : cell.before;
    if (!world.setBlock(
            cell.position,
            state,
            Block.UPDATE_CLIENTS
                | Block.UPDATE_KNOWN_SHAPE
                | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS)
        && world.getBlockState(cell.position) != state) return false;
    if (cell.data != null) {
      var replacement =
          BlockEntity.loadStatic(cell.position, state, cell.data.copy(), world.registryAccess());
      if (replacement == null) return false;
      world.setBlockEntity(replacement);
      replacement.setChanged();
    }
    return true;
  }

  public static boolean paint(
      Level world, BlockPos position, DyeColor color, Predicate<BlockPos> permission) {
    if (world.isClientSide() || !canPaint(world, position, color) || !permission.test(position))
      return false;
    var state = world.getBlockState(position);
    if (state.getBlock() instanceof ConductorBlock cable)
      return cable.recolor(state, world, position, color);
    var first = cell(world, position, state, replacement(state, color));
    Cell second = null;
    if (state.getBlock() instanceof BedBlock) {
      var other = position.relative(BedBlock.getConnectedDirection(state));
      if (!validBed(world, state, other) || !permission.test(other)) return false;
      var before = world.getBlockState(other);
      second = cell(world, other, before, replacement(before, color));
    }
    if (!install(world, first, true)) {
      install(world, first, false);
      return false;
    }
    if (second != null && !install(world, second, true)) {
      install(world, first, false);
      install(world, second, false);
      return false;
    }
    world.updateNeighborsAt(first.position, first.after.getBlock());
    world.sendBlockUpdated(first.position, first.before, first.after, Block.UPDATE_ALL);
    if (second != null) {
      world.updateNeighborsAt(second.position, second.after.getBlock());
      world.sendBlockUpdated(second.position, second.before, second.after, Block.UPDATE_ALL);
    }
    return true;
  }
}
