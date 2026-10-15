package ic2ce.neoforge.terrain;

import ic2ce.api.item.ITerraformingBP;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.inventory.TypeFilteredInput;
import ic2ce.neoforge.registry.SoundDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Blueprint-specific terrain work, with a prepaid attempt and a ninety-percent failed-work refund.
 */
public class TerrainController extends PoweredBlockEntity {
  public int failedAttempts, inactiveTicks;
  private BlockPos lastPos;
  public final TypeFilteredInput tfbpSlot =
      new TypeFilteredInput(this, "tfbp", 1, ITerraformingBP.class);

  public TerrainController(BlockPos position, BlockState state) {
    super(TerrainBlockEntities.TERRAFORMER, position, state, 100000, 4);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    var world = getLevel();
    boolean working = false;
    var stack = tfbpSlot.get();
    if (world != null && !stack.isEmpty() && stack.getItem() instanceof ITerraformingBP blueprint) {
      double cost = blueprint.getConsume(stack);
      int range = blueprint.getRange(stack);
      if (Double.isFinite(cost)
          && cost >= 0
          && cost <= energy.getCapacity()
          && range >= 0
          && range <= 1024
          && energy.getEnergy() >= cost) {
        int failures = Math.clamp(failedAttempts, 0, 4);
        int radius = lastPos == null ? range * (failures + 1) / 5 : range / 10;
        var center = lastPos == null ? worldPosition : lastPos;
        var random = world.getRandom();
        var chosen =
            new BlockPos(
                center.getX() + random.nextInt(radius + 1) - random.nextInt(radius + 1),
                worldPosition.getY(),
                center.getZ() + random.nextInt(radius + 1) - random.nextInt(radius + 1));
        if (world.isLoaded(chosen)
            && !world.isOutsideBuildHeight(chosen)
            && energy.useEnergy(cost)) {
          boolean changed;
          try {
            changed = blueprint.terraform(stack, world, chosen);
          } catch (RuntimeException | Error error) {
            energy.forceAddEnergy(cost);
            throw error;
          }
          working = true;
          if (changed) {
            failedAttempts = 0;
            lastPos = chosen;
          } else {
            energy.forceAddEnergy(cost * 0.9);
            failedAttempts = Math.min(5, failures + 1);
            lastPos = null;
          }
          setChanged();
        }
      }
    }
    if (working) {
      inactiveTicks = 0;
      activate(false);
    } else if (getActive() && ++inactiveTicks > 30) shutdown(false);
  }

  @Override
  protected InteractionResult onActivated(
      Player player, InteractionHand hand, Direction side, Vec3 hit) {
    var world = getLevel();
    if (world == null || player.isShiftKeyDown()) return InteractionResult.PASS;
    if (world.isClientSide()) return InteractionResult.SUCCESS;
    if (!tfbpSlot.isEmpty()) {
      var owned = tfbpSlot.get().copy();
      tfbpSlot.clear();
      StackUtil.dropAsEntity(world, worldPosition, owned);
      lastPos = null;
      failedAttempts = 0;
      setChanged();
      return InteractionResult.SUCCESS;
    }
    var held = player.getItemInHand(hand);
    if (held.isEmpty()
        || !(held.getItem() instanceof ITerraformingBP blueprint)
        || !blueprint.canInsert(held, player, world, worldPosition)) return InteractionResult.PASS;
    tfbpSlot.put(held.copyWithCount(1));
    if (!player.getAbilities().instabuild) held.shrink(1);
    lastPos = null;
    failedAttempts = 0;
    setChanged();
    return InteractionResult.SUCCESS;
  }

  public static BlockPos getFirstSolidBlockFrom(Level world, BlockPos position, int offset) {
    return surface(world, position, offset, true);
  }

  public static BlockPos getFirstBlockFrom(Level world, BlockPos position, int offset) {
    return surface(world, position, offset, false);
  }

  private static BlockPos surface(Level world, BlockPos position, int offset, boolean solid) {
    if (world == null || position == null || !world.isLoaded(position)) return null;
    int minimum = world.getMinY(), maximum = world.getMaxY();
    int start = (int) Math.min((long) maximum, (long) position.getY() + offset);
    if (start < minimum) return null;
    var cursor = new BlockPos.MutableBlockPos(position.getX(), start, position.getZ());
    for (int y = start; y >= minimum; y--) {
      cursor.setY(y);
      var state = world.getBlockState(cursor);
      if (solid ? state.isSolidRender() : !state.isAir()) return cursor.immutable();
    }
    return null;
  }

  public static boolean switchGround(
      Level world, BlockPos position, Block from, BlockState to, boolean upwards) {
    if (world == null
        || position == null
        || world.isOutsideBuildHeight(position)
        || !world.isLoaded(position)) return false;
    var cursor = position.mutable();
    int minimum = world.getMinY();
    while (cursor.getY() >= minimum) {
      boolean matches = world.getBlockState(cursor).is(from);
      if (upwards ? !matches : matches) break;
      cursor.move(Direction.DOWN);
    }
    if (upwards) {
      if (cursor.getY() == position.getY()) return false;
      cursor.move(Direction.UP);
    } else if (cursor.getY() < minimum) return false;
    return !world.isOutsideBuildHeight(cursor) && world.setBlockAndUpdate(cursor, to);
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_TERRAFORMER_LOOP;
  }
}
