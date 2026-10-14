package ic2ce.neoforge.world.block;

import com.mojang.authlib.GameProfile;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Actual native material spans, mixed pillars, physical reinforcement, collapse and climbing
 * boundaries.
 */
public final class ScaffoldContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String text) {
    checks++;
    if (!ok) failures.add("native scaffold: " + text);
  }

  public static Result run(ServerLevel world) {
    return new ScaffoldContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var base = new BlockPos(12168, 100, 12168);
    for (int x = -1; x <= 2; x++)
      for (int z = -1; z <= 1; z++) {
        world.setChunkForced((base.getX() >> 4) + x, (base.getZ() >> 4) + z, true);
        world.getChunk((base.getX() >> 4) + x, (base.getZ() >> 4) + z);
      }
    int ordinal = 0;
    for (var block :
        List.of(
            BlockDefinitions.WOODEN_SCAFFOLD,
            BlockDefinitions.REINFORCED_WOODEN_SCAFFOLD,
            BlockDefinitions.IRON_SCAFFOLD,
            BlockDefinitions.REINFORCED_IRON_SCAFFOLD)) {
      var origin = base.offset(0, ordinal * 8, 0);
      for (int x = -1; x <= 14; x++) {
        world.setBlock(origin.offset(x, -1, 0), Blocks.AIR.defaultBlockState(), 2);
        world.setBlock(origin.offset(x, 0, 0), Blocks.AIR.defaultBlockState(), 2);
      }
      world.setBlock(origin.below(), Blocks.STONE.defaultBlockState(), 2);
      world.setBlock(origin, block.defaultBlockState(), 2);
      int reach = ordinal == 0 ? 2 : ordinal == 3 ? 12 : 5;
      for (int distance = 1; distance <= reach; distance++) {
        var position = origin.east(distance);
        check(
            block.defaultBlockState().canSurvive(world, position),
            "native material permits exact supported reach " + distance + "/" + reach);
        world.setBlock(position, block.defaultBlockState(), 2);
      }
      check(
          !block.defaultBlockState().canSurvive(world, origin.east(reach + 1)),
          "native span cannot exceed its material reach " + reach);
      world.setBlock(origin.below(), Blocks.AIR.defaultBlockState(), 2);
      ((StructuralScaffold) block)
          .tick(block.defaultBlockState(), world, origin, RandomSource.create(1));
      boolean collapsed = true;
      for (int x = 0; x <= reach; x++) collapsed &= world.isEmptyBlock(origin.east(x));
      check(collapsed, "actual native unsupported span collapses every physical cell once");
      ordinal++;
    }
    var actor =
        FakePlayerFactory.get(
            world,
            new GameProfile(
                UUID.fromString("5947ea48-c2d9-46cc-a252-a1f5a2bfec50"), "ScaffoldProbe"));
    actor.getAbilities().mayBuild = true;
    actor.getAbilities().instabuild = false;
    actor.setPos(base.getX(), base.getY(), base.getZ() - 2);
    var hit = new BlockHitResult(Vec3.atCenterOf(base), Direction.NORTH, base, false);
    for (var block : List.of(BlockDefinitions.WOODEN_SCAFFOLD, BlockDefinitions.IRON_SCAFFOLD))
      for (var hand : InteractionHand.values()) {
        world.setBlock(base.below(), Blocks.STONE.defaultBlockState(), 2);
        world.setBlock(base, block.defaultBlockState(), 2);
        boolean wooden = block == BlockDefinitions.WOODEN_SCAFFOLD;
        var stack = new ItemStack(wooden ? Items.STICK : BlockDefinitions.IRON_FENCE.asItem(), 4);
        actor.setItemInHand(hand, stack);
        actor.setItemInHand(
            hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND,
            new ItemStack(Items.DIAMOND));
        block.defaultBlockState().useItemOn(stack, world, actor, hand, hit);
        check(
            world
                    .getBlockState(base)
                    .is(
                        wooden
                            ? BlockDefinitions.REINFORCED_WOODEN_SCAFFOLD
                            : BlockDefinitions.REINFORCED_IRON_SCAFFOLD)
                && stack.getCount() == (wooden ? 2 : 3),
            "actual native " + hand + " reinforcement pays the distinct material bill");
        check(
            actor
                .getItemInHand(
                    hand == InteractionHand.MAIN_HAND
                        ? InteractionHand.OFF_HAND
                        : InteractionHand.MAIN_HAND)
                .is(Items.DIAMOND),
            "native reinforcement preserves the unused hand");
      }
    world.setBlock(base, BlockDefinitions.WOODEN_SCAFFOLD.defaultBlockState(), 2);
    actor.getAbilities().mayBuild = false;
    var sticks = new ItemStack(Items.STICK, 2);
    actor.setItemInHand(InteractionHand.MAIN_HAND, sticks);
    world.getBlockState(base).useItemOn(sticks, world, actor, InteractionHand.MAIN_HAND, hit);
    check(
        world.getBlockState(base).is(BlockDefinitions.WOODEN_SCAFFOLD) && sticks.getCount() == 2,
        "denied structural reinforcement changes neither world nor physical bill");
    actor.getAbilities().mayBuild = true;
    world.setBlock(
        base.below(),
        Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP),
        2);
    check(
        BlockDefinitions.WOODEN_SCAFFOLD.defaultBlockState().canSurvive(world, base),
        "native full upper support face is a valid slab foundation");
    world.setBlock(base.below(), Blocks.STONE.defaultBlockState(), 2);
    world.setBlock(base, BlockDefinitions.REINFORCED_IRON_SCAFFOLD.defaultBlockState(), 2);
    for (int y = 1; y <= 8; y++)
      world.setBlock(base.above(y), BlockDefinitions.WOODEN_SCAFFOLD.defaultBlockState(), 2);
    world.setBlock(base.above(8).east(), BlockDefinitions.WOODEN_SCAFFOLD.defaultBlockState(), 2);
    check(
        BlockDefinitions.WOODEN_SCAFFOLD
            .defaultBlockState()
            .canSurvive(world, base.above(8).east(2)),
        "mixed native vertical pillar retains grounded support for an elevated wooden span");
    actor.setShiftKeyDown(true);
    actor.setDeltaMovement(1, -2, -1);
    actor.fallDistance = 20;
    ((StructuralScaffold) BlockDefinitions.WOODEN_SCAFFOLD)
        .entityInside(
            BlockDefinitions.WOODEN_SCAFFOLD.defaultBlockState(), world, base, actor, null, true);
    check(
        actor.getDeltaMovement().x == .15
            && actor.getDeltaMovement().z == -.15
            && actor.getDeltaMovement().y == .08
            && actor.fallDistance == 0,
        "actual player scaffold ascent clamps lateral motion and fall distance");
    actor.setShiftKeyDown(false);
    actor.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }
}
