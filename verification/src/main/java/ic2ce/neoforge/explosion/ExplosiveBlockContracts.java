package ic2ce.neoforge.explosion;

import ic2ce.api.entity.block.ExplosiveEntity;
import ic2ce.neoforge.world.WorldItems;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Actual server priming observes addon cancellation, actor ownership, exact paid items and one-shot
 * transitions.
 */
public final class ExplosiveBlockContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native explosive block: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ExplosiveBlockContracts().exercise(world);
  }

  private static final class Probe extends DemolitionBlockEntity {
    ExplosiveEntity created;

    Probe(BlockPos pos, BlockState state) {
      super(pos, state);
    }

    @Override
    protected ExplosiveEntity getEntity(LivingEntity cause) {
      return created = super.getEntity(cause);
    }
  }

  private static final class SavedProbe extends PrimedDemolitionCharge {
    SavedProbe(ServerLevel world) {
      super(world, 0, 110, 0);
    }

    void restore(ServerLevel world, CompoundTag tag) {
      readAdditionalSaveData(
          TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), tag));
    }
  }

  private static Probe place(ServerLevel world, BlockPos pos) {
    world.setBlockAndUpdate(pos, ExplosionBlocks.ITNT.defaultBlockState());
    var probe = new Probe(pos, world.getBlockState(pos));
    world.setBlockEntity(probe);
    return probe;
  }

  private Result exercise(ServerLevel world) {
    var pos = new BlockPos(1670, 110, 1300);
    world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
    var actor =
        FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("7e7e4e74-1111-4111-8111-444400001414"), "ChargeProbe"));
    actor.setGameMode(GameType.SURVIVAL);
    actor.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
    actor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FIRE_CHARGE, 2));
    var pending = place(world, pos);
    Consumer<EntityJoinLevelEvent> deny =
        event -> {
          if (event.getEntity() == pending.created) event.setCanceled(true);
        };
    NeoForge.EVENT_BUS.addListener(deny);
    try {
      var result = pending.onActivated(actor, InteractionHand.MAIN_HAND, Direction.UP, Vec3.ZERO);
      check(
          result == InteractionResult.PASS
              && world.getBlockEntity(pos) == pending
              && actor.getMainHandItem().getCount() == 2,
          "actual cancelled native entity admission preserves block and both ignition items");
      check(
          pending.getAuxDrops(0).isEmpty()
              && pending.adjustDrop(new ItemStack(ExplosionItems.ITNT), true) != null,
          "cancelled admission clears one-shot state and preserves normal block recovery");
    } finally {
      NeoForge.EVENT_BUS.unregister(deny);
    }
    var accepted = pending.onActivated(actor, InteractionHand.MAIN_HAND, Direction.UP, Vec3.ZERO);
    check(
        accepted == InteractionResult.CONSUME
            && world.getBlockEntity(pos) == null
            && actor.getMainHandItem().getCount() == 1
            && pending.created.getFuse() == 60,
        "accepted demolition priming consumes exactly one fire charge and keeps its distinct fuse");
    var first = pending.created;
    check(
        !pending.explode(actor, false)
            && pending.created == first
            && pending.adjustDrop(new ItemStack(ExplosionItems.ITNT), true) == null,
        "removed/primed native charge cannot spawn a duplicate entity or another block drop");
    first.discard();
    var spectator = place(world, pos);
    actor.setGameMode(GameType.SPECTATOR);
    int remaining = actor.getMainHandItem().getCount();
    check(
        spectator.onActivated(actor, InteractionHand.MAIN_HAND, Direction.UP, Vec3.ZERO)
                == InteractionResult.PASS
            && spectator.created == null
            && actor.getMainHandItem().getCount() == remaining,
        "spectator cannot pay for or ignite a native explosive");
    actor.setGameMode(GameType.SURVIVAL);
    actor.setPos(0, 110, 0);
    check(
        spectator.onActivated(actor, InteractionHand.MAIN_HAND, Direction.UP, Vec3.ZERO)
                == InteractionResult.PASS
            && spectator.created == null,
        "distant actor cannot ignite another loaded native block");
    actor.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
    actor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
    check(
        spectator.onActivated(actor, InteractionHand.MAIN_HAND, Direction.UP, Vec3.ZERO)
                == InteractionResult.CONSUME
            && actor.getMainHandItem().getDamageValue() == 1,
        "accepted native flint ignition pays exactly one real durability unit");
    spectator.created.discard();
    world.setBlockAndUpdate(pos, ExplosionBlocks.NUKE.defaultBlockState());
    var nuke = (ConfiguredNuclearCharge) world.getBlockEntity(pos);
    nuke.outsideSlot.put(new ItemStack(ExplosionItems.ITNT, 64));
    nuke.insideSlot.put(new ItemStack(WorldItems.URANIUM_BLOCK, 64));
    float power = nuke.getNukeExplosivePower();
    check(
        Float.isFinite(power) && power >= 0 && nuke.getRadiationRange() == 384,
        "actual registered nuclear inventories project distinct uranium-block radiation range");
    nuke.outsideSlot.clear();
    check(
        nuke.getNukeExplosivePower() < 0 && nuke.getRadiationRange() == 0,
        "removing the native detonator invalidates payload and stale radiation observation");
    world.removeBlock(pos, false);
    var offhand = place(world, pos);
    actor.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    actor.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.FIRE_CHARGE, 2));
    var nativeUse =
        actor.gameMode.useItemOn(
            actor,
            world,
            actor.getOffhandItem(),
            InteractionHand.OFF_HAND,
            new net.minecraft.world.phys.BlockHitResult(
                Vec3.atCenterOf(pos), Direction.UP, pos, false));
    check(
        world.getBlockEntity(pos) == null
            && actor.getOffhandItem().getCount() == 1
            && offhand.created != null,
        "actual native server item-on-block path primes from the off hand and pays that hand only");
    if (offhand.created != null) offhand.created.discard();
    var chained = place(world, pos);
    var origin =
        new ic2ce.neoforge.explosion.NativeBlast(
            world,
            null,
            Vec3.atCenterOf(pos).x(),
            Vec3.atCenterOf(pos).y(),
            Vec3.atCenterOf(pos).z(),
            1,
            0);
    world.getBlockState(pos).onExplosionHit(world, pos, origin, (stack, position) -> {});
    check(
        world.getBlockEntity(pos) == null
            && chained.created != null
            && chained.created.getFuse() >= 7
            && chained.created.getFuse() < 22,
        "actual native explosion callback reaches the demolition entity before block removal and"
            + " shortens the chain fuse");
    if (chained.created != null) chained.created.discard();
    var saved = new SavedProbe(world);
    var tag = new CompoundTag();
    tag.putFloat("Power", Float.NaN);
    tag.putFloat("DropRate", Float.POSITIVE_INFINITY);
    tag.putFloat("EntityDamage", -5);
    tag.putInt("RadiationRange", -20);
    tag.putInt("FuseTicks", -5);
    saved.restore(world, tag);
    check(
        saved.explosivePower == 0
            && saved.dropRate == 0
            && saved.damageVsEntities == 0
            && saved.radiationRange == 0
            && saved.getFuse() == 0,
        "native malformed saved explosive scalars restore to finite nonnegative owned values");
    saved.discard();
    world.removeBlock(pos, false);
    world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, false);
    return new Result(checks, List.copyOf(failures));
  }
}
