package ic2ce.neoforge.utility;

import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.item.upgrade.UpgradeItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

/** Real damage, exact shared boost budgets and upgrade reach on a native metal pole. */
public final class UtilityContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native utility: " + label);
  }

  public static Result run(ServerLevel world) {
    return new UtilityContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8560, 100, 8560);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, UtilityBlocks.TESLA_COIL.defaultBlockState());
    var coil = (ArcCoil) world.getBlockEntity(position);
    var energy = coil.getComponent(ElectricalBuffer.class);
    energy.forceAddEnergy(400);
    var cows = new ArrayList<net.minecraft.world.entity.animal.cow.Cow>();
    float before = 0;
    for (int index = 0; index < 3; index++) {
      var cow = EntityType.COW.create(world, EntitySpawnReason.COMMAND);
      cow.setPos(position.getX() + 2.5, position.getY(), position.getZ() + 0.2 + index * 0.2);
      cow.setNoAi(true);
      check(world.addFreshEntity(cow), "native arc target joins loaded world");
      cows.add(cow);
      before += cow.getHealth();
    }
    boolean hit = coil.shock(1);
    float after = 0;
    for (var cow : cows) after += cow.getHealth();
    check(
        hit && before - after == 1 && energy.getEnergy() == 0,
        "one paid damage point reaches a real target even among three targets without losing an"
            + " integer remainder");
    for (var cow : cows) cow.discard();
    energy.forceAddEnergy(400);
    var immune = EntityType.COW.create(world, EntitySpawnReason.COMMAND);
    immune.setPos(position.getX() + 2.5, position.getY(), position.getZ() + 0.5);
    immune.setNoAi(true);
    immune.setInvulnerable(true);
    world.addFreshEntity(immune);
    check(
        !coil.shock(1) && energy.getEnergy() == 400,
        "rejected actual damage refunds the full reserved arc bill");
    immune.discard();
    world.removeBlock(position, false);
    var pole = position.south(8);
    var magnetPosition = pole.north();
    world.setBlockAndUpdate(magnetPosition, UtilityBlocks.MAGNETIZER.defaultBlockState());
    var machine = (PoleDrive) world.getBlockEntity(magnetPosition);
    world.setBlockAndUpdate(
        magnetPosition,
        machine.getBlockState().setValue(machine.getBlockType().facingProperty, Direction.SOUTH));
    energy = machine.getComponent(ElectricalBuffer.class);
    energy.forceAddEnergy(2);
    check(
        !machine.tryBoost(Double.NaN) && !machine.tryBoost(-1) && energy.getEnergy() == 2,
        "invalid boost requests cannot crash or debit storage");
    check(
        machine.tryBoost(0.5) && machine.tryBoost(0.5) && energy.getEnergy() == 0,
        "two half boosts share exactly two paid EU");
    for (int y = 0; y <= 21; y++)
      world.setBlockAndUpdate(pole.above(y), UtilityBlocks.IRON_FENCE.defaultBlockState());
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                java.util.UUID.fromString("ff43a8fd-481a-4a13-8fd4-602bfa692d57"), "IC2PoleProbe"));
    player.setPos(pole.getX() + 0.5, pole.getY() + 21, pole.getZ() + 0.5);
    player.setShiftKeyDown(false);
    player.setDeltaMovement(0, -4, 0);
    player.fallDistance = 10;
    var fence = (IndustrialFence) UtilityBlocks.IRON_FENCE;
    fence.entityInside(
        world.getBlockState(pole.above(21)), world, pole.above(21), player, null, true);
    check(
        player.fallDistance == 10 && player.getDeltaMovement().y == -4,
        "fast falling outside unpowered range never uses a tautological slow-speed reset");
    machine.upgradeSlot.put(new ItemStack(UpgradeItems.OVERCLOCKER_UPGRADE));
    machine.setChanged();
    energy.forceAddEnergy(2);
    player.setDeltaMovement(0, 0, 0);
    fence.entityInside(
        world.getBlockState(pole.above(21)), world, pole.above(21), player, null, true);
    check(
        machine.getPoleRange() == 21 && player.getDeltaMovement().y > 0 && energy.getEnergy() == 0,
        "actual overclocker augmentation reaches the twenty-first pole block and pays the real"
            + " player boost; range="
            + machine.getPoleRange()
            + ", velocity="
            + player.getDeltaMovement().y
            + ", energy="
            + energy.getEnergy()
            + ", facing="
            + machine.getFacing()
            + ", removed="
            + machine.isRemoved()
            + ", sameNativeInstance="
            + (world.getBlockEntity(magnetPosition) == machine));
    for (int y = 0; y <= 21; y++) world.removeBlock(pole.above(y), false);
    world.removeBlock(magnetPosition, false);
    return new Result(checks, List.copyOf(failures));
  }
}
