package ic2ce.neoforge.world.block;

import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * Wet normal/reinforced foam cures by distinct finite clocks or an actual paid sand interaction.
 */
public final class CuringFoam extends Block {
  public static final EnumProperty<FoamType> typeProperty =
      EnumProperty.create("type", FoamType.class);
  private static final Direction[] FACES = Direction.values();

  public CuringFoam(Properties properties) {
    super(properties);
    registerDefaultState(defaultBlockState().setValue(typeProperty, FoamType.normal));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(typeProperty);
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter world, BlockPos position, CollisionContext context) {
    return Shapes.empty();
  }

  @Override
  public void randomTick(
      BlockState state, ServerLevel world, BlockPos position, RandomSource random) {
    int speed = world.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
    if (speed <= 0 || world.getBlockState(position) != state) return;
    float probability =
        Math.clamp(
            getHardenChance(world, position, state, state.getValue(typeProperty)) * 4096f / speed,
            0,
            1);
    if (random.nextFloat() < probability)
      world.setBlockAndUpdate(position, state.getValue(typeProperty).getResult());
  }

  @Override
  public InteractionResult useItemOn(
      ItemStack stack,
      BlockState state,
      Level world,
      BlockPos position,
      Player owner,
      InteractionHand hand,
      BlockHitResult hit) {
    if (stack.isEmpty() || !stack.is(Items.SAND)) return InteractionResult.TRY_WITH_EMPTY_HAND;
    if (!owner.getAbilities().mayBuild
        || !world.mayInteract(owner, position)
        || !owner.mayUseItemAt(position, hit.getDirection(), stack)) return InteractionResult.FAIL;
    if (world.isClientSide()) return InteractionResult.SUCCESS;
    if (world.getBlockState(position) != state
        || !world.setBlockAndUpdate(position, state.getValue(typeProperty).getResult()))
      return InteractionResult.FAIL;
    if (!owner.getAbilities().instabuild) stack.shrink(1);
    return InteractionResult.SUCCESS;
  }

  public static float getHardenChance(
      Level world, BlockPos position, BlockState state, FoamType type) {
    int light = world.getMaxLocalRawBrightness(position);
    if (!state.canOcclude())
      for (var face : FACES)
        light = Math.max(light, world.getMaxLocalRawBrightness(position.relative(face)));
    light = Math.clamp(light, 0, 15);
    return 1f / (20 * type.hardenTime * (16 - light));
  }

  public enum FoamType implements StringRepresentable {
    normal(300),
    reinforced(600);
    public final int hardenTime;

    FoamType(int seconds) {
      hardenTime = seconds;
    }

    @Override
    public String getSerializedName() {
      return name();
    }

    public List<ItemStack> getDrops() {
      return List.of();
    }

    public BlockState getResult() {
      return switch (this) {
        case normal -> ConstructionWall.get(ConstructionWall.DEFAULT_COLOR).defaultBlockState();
        case reinforced -> BlockDefinitions.REINFORCED_STONE.defaultBlockState();
      };
    }
  }
}
