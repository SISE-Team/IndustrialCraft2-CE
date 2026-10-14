package ic2ce.neoforge.world.block;

import ic2ce.api.tile.*;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Immutable color-family publication and transactional conversion into an actual imprinted wall.
 */
public final class ConstructionWall extends Block implements StainableBlock, RetexturableBlock {
  public static final DyeColor DEFAULT_COLOR = DyeColor.LIGHT_GRAY;
  private static volatile Map<DyeColor, ConstructionWall> palette = Map.of();
  private final DyeColor color;

  public ConstructionWall(Properties properties, DyeColor color) {
    super(properties);
    this.color = Objects.requireNonNull(color);
    synchronized (ConstructionWall.class) {
      if (palette.containsKey(color))
        throw new IllegalArgumentException("Duplicate construction-wall color " + color);
      var next = new EnumMap<DyeColor, ConstructionWall>(DyeColor.class);
      next.putAll(palette);
      next.put(color, this);
      palette = Collections.unmodifiableMap(next);
    }
  }

  @Override
  public DyeColor getColor(Level world, BlockPos position, Direction side) {
    return color;
  }

  @Override
  public boolean setColor(Level world, BlockPos position, Direction side, DyeColor requested) {
    var target = get(requested);
    return !world.isClientSide()
        && target != null
        && target != this
        && world.getBlockState(position).is(this)
        && world.setBlockAndUpdate(position, target.defaultBlockState());
  }

  @Override
  public boolean retexture(
      BlockState state,
      Level world,
      BlockPos position,
      Direction side,
      Player owner,
      BlockState reference,
      String variant,
      Direction referenceSide,
      int[] tints) {
    if (world.isClientSide()
        || side == null
        || reference == null
        || referenceSide == null
        || tints == null
        || tints.length == 0
        || tints.length > 64
        || world.getBlockState(position) != state
        || owner != null && (!owner.getAbilities().mayBuild || !world.mayInteract(owner, position)))
      return false;
    if (!world.setBlockAndUpdate(position, BlockDefinitions.OBSCURED_WALL.defaultBlockState()))
      return false;
    boolean committed = false;
    try {
      if (world.getBlockEntity(position) instanceof ImprintedWall wall) {
        wall.setColor(color);
        committed =
            wall.getBlockType()
                .retexture(
                    wall.getBlockState(),
                    world,
                    position,
                    side,
                    owner,
                    reference,
                    variant,
                    referenceSide,
                    tints.clone());
      }
      return committed;
    } finally {
      if (!committed) world.setBlockAndUpdate(position, state);
    }
  }

  public static ConstructionWall get(DyeColor color) {
    return color == null ? null : palette.get(color);
  }
}
