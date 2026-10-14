package ic2ce.neoforge.world.block;

import ic2ce.core.util.Keyboard;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.neoforge.world.surface.StateVariants;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Actual distinct sheet contact, registered native signs and canonical state decoding. */
public final class SurfaceContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native surface: " + label);
  }

  public static Result run(ServerLevel world) {
    return new SurfaceContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var pos = new BlockPos(1680, 130, 1300);
    var actor =
        FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("7e7e4e74-1111-4111-8111-555500001414"), "SurfaceProbe"));
    actor.setPos(pos.getX() + .5, pos.getY() + .2, pos.getZ() + .5);
    var resin = (ReactiveSheet) BlockDefinitions.RESIN_SHEET;
    var rubber = (ReactiveSheet) BlockDefinitions.RUBBER_SHEET;
    var wool = (ReactiveSheet) BlockDefinitions.WOOL_SHEET;
    world.removeBlock(pos.below(), false);
    check(
        !resin.canSurvive(resin.defaultBlockState(), world, pos)
            && wool.canSurvive(wool.defaultBlockState(), world, pos),
        "resin needs ground support while wool keeps its distinct free-position policy");
    world.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
    check(
        resin.canSurvive(resin.defaultBlockState(), world, pos)
            && resin
                .getCollisionShape(resin.defaultBlockState(), world, pos, CollisionContext.empty())
                .isEmpty(),
        "actual resin is attachable and still does not physically collide");
    actor.fallDistance = 12;
    actor.setDeltaMovement(1, -1, 1);
    resin.entityInside(resin.defaultBlockState(), world, pos, actor, null, true);
    check(
        Math.abs(actor.fallDistance - 9) < .0001
            && Math.abs(actor.getDeltaMovement().x - .6) < 1e-9
            && Math.abs(actor.getDeltaMovement().y + .85) < 1e-9,
        "native resin drag changes all three physical motion components and fall distance");
    actor.setShiftKeyDown(true);
    check(
        wool.getCollisionShape(wool.defaultBlockState(), world, pos, CollisionContext.of(actor))
            .isEmpty(),
        "actual sneaking player passes through the one-way wool surface");
    actor.setShiftKeyDown(false);
    actor.fallDistance = 10;
    wool.entityInside(wool.defaultBlockState(), world, pos, actor, null, true);
    check(
        Math.abs(actor.fallDistance - 9.5) < .0001,
        "wool changes fall distance independently of resin velocity drag");
    world.removeBlock(pos.below(), false);
    world.setBlockAndUpdate(pos.east(2), Blocks.STONE.defaultBlockState());
    world.setBlockAndUpdate(pos.west(2), Blocks.STONE.defaultBlockState());
    world.setBlockAndUpdate(pos.east(), rubber.defaultBlockState());
    world.setBlockAndUpdate(pos.west(), rubber.defaultBlockState());
    world.setBlockAndUpdate(pos, rubber.defaultBlockState());
    actor.fallDistance = 20;
    actor.setDeltaMovement(1, -1, 1);
    rubber.entityInside(rubber.defaultBlockState(), world, pos, actor, null, true);
    check(
        world.getBlockState(pos).is(rubber)
            && actor.fallDistance == 0
            && Math.abs(actor.getDeltaMovement().y - .8) < 1e-9,
        "native rubber span with opposite anchors rebounds and cancels fall damage");
    actor.setShiftKeyDown(true);
    actor.setDeltaMovement(0, -1, 0);
    rubber.entityInside(rubber.defaultBlockState(), world, pos, actor, null, true);
    check(
        Math.abs(actor.getDeltaMovement().y - .1) < 1e-9,
        "native sneak control selects the distinct weak rubber rebound");
    actor.setShiftKeyDown(false);
    ((Keyboard) ProjectRuntime.keyboard).processKeyUpdate(actor, 16);
    check(
        ProjectRuntime.keyboard.isJumpKeyDown(actor)
            && !ProjectRuntime.keyboard.isModeSwitchKeyDown(actor),
        "synchronized jump bit is distinct from mode-switch input");
    actor.setDeltaMovement(0, -1, 0);
    rubber.entityInside(rubber.defaultBlockState(), world, pos, actor, null, true);
    check(
        Math.abs(actor.getDeltaMovement().y - 1.3) < 1e-9,
        "actual synchronized jump input selects the distinct stronger rebound");
    ((Keyboard) ProjectRuntime.keyboard).processKeyUpdate(actor, 0);
    world.removeBlock(pos.west(2), false);
    world.removeBlock(pos.west(), false);
    actor.setDeltaMovement(0, -1, 0);
    rubber.entityInside(rubber.defaultBlockState(), world, pos, actor, null, true);
    check(
        world.getBlockState(pos).isAir(),
        "rubber without opposite support breaks in the authoritative native server level");
    var sign =
        new ic2ce.neoforge.world.block.RubberSignEntity(
            pos, BlockDefinitions.RUBBER_SIGN.defaultBlockState());
    check(
        sign.getType() == BlockEntityDefinitions.SIGN
            && sign.getType().isValid(sign.getBlockState()),
        "native rubber sign stores the actual registered type in its superclass"
            + " validity/serialization protocol");
    var state =
        Blocks.OAK_LOG
            .defaultBlockState()
            .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X);
    var variant = StateVariants.getVariantString(state);
    check(
        StateVariants.getState(Blocks.OAK_LOG, variant) == state
            && variant.equals(StateVariants.getVariantString(state)),
        "canonical registered native variant roundtrip is stable");
    check(
        StateVariants.getState(Blocks.OAK_LOG, "axis=invalid") == null
            && StateVariants.getState(Blocks.OAK_LOG, "axis=x,axis=y") == null
            && StateVariants.getState(Blocks.OAK_LOG, "missing=true") == null,
        "malformed, duplicate and unknown native state properties cannot silently select another"
            + " block variant");
    for (var offset : List.of(pos, pos.east(), pos.west(), pos.east(2), pos.west(2), pos.below()))
      world.removeBlock(offset, false);
    return new Result(checks, List.copyOf(failures));
  }
}
