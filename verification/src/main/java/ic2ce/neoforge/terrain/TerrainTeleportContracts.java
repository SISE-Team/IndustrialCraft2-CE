package ic2ce.neoforge.terrain;

import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.energy.EnergyBlocks;
import ic2ce.neoforge.energy.storage.EnergyBank;
import ic2ce.neoforge.terrain.program.*;
import ic2ce.neoforge.terrain.program.CultivationProgram;
import ic2ce.neoforge.terrain.program.FrostProgram;
import ic2ce.neoforge.terrain.program.FungalProgram;
import ic2ce.neoforge.terrain.program.IrrigationProgram;
import ic2ce.neoforge.terrain.program.LevelingProgram;
import ic2ce.neoforge.utility.LinkedTeleporter;
import ic2ce.neoforge.utility.UtilityBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** Real bank debits, native endpoint movement and below-zero blueprint edits. */
public final class TerrainTeleportContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native terrain/teleport: " + label);
  }

  public static Result run(ServerLevel world) {
    return new TerrainTeleportContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var start = new BlockPos(8640, 100, 8640);
    world.getChunkAt(start);
    world.setBlockAndUpdate(start, UtilityBlocks.TELEPORTER.defaultBlockState());
    var teleporter = (LinkedTeleporter) world.getBlockEntity(start);
    check(
        !teleporter.tryConsumeEnergy(1) && teleporter.getAvailableEnergy() == 0,
        "no adjacent source rejects work without division by zero");
    try {
      teleporter.consumeEnergy(-1);
      check(false, "negative native energy rejected");
    } catch (IllegalArgumentException expected) {
      check(true, "negative native energy rejected");
    }
    world.setBlockAndUpdate(start.west(), EnergyBlocks.MFSU.defaultBlockState());
    world.setBlockAndUpdate(start.east(), EnergyBlocks.MFSU.defaultBlockState());
    var left = (EnergyBank) world.getBlockEntity(start.west());
    var right = (EnergyBank) world.getBlockEntity(start.east());
    left.setStored(100);
    right.setStored(200);
    check(
        !teleporter.tryConsumeEnergy(301) && left.getStored() == 100 && right.getStored() == 200,
        "short complete reservation leaves every source untouched");
    check(
        teleporter.tryConsumeEnergy(250) && left.getStored() == 0 && right.getStored() == 50,
        "capacity-aware native allocation pays exactly 250 EU across unequal sources");
    var target = start.south(5);
    world.setBlockAndUpdate(target, UtilityBlocks.TELEPORTER.defaultBlockState());
    var mutable = target.mutable();
    teleporter.setTarget(mutable);
    mutable.move(4, 0, 0);
    check(
        teleporter.getTarget().equals(target),
        "link owns its position rather than borrowing a mutable caller");
    var data = new CompoundTag();
    teleporter.saveAdditional(data);
    check(
        data.getIntOr("targetZ", 0) == target.getZ(),
        "native saved link records actual destination");
    teleporter.setTarget(null);
    teleporter.saveAdditional(data);
    check(!data.contains("targetX"), "cleared link removes stale fields from a reused save target");
    teleporter.setTarget(target);
    left.setStored(100000);
    right.setStored(100000);
    var cow = EntityType.COW.create(world, EntitySpawnReason.COMMAND);
    cow.setNoAi(true);
    cow.setPos(start.getX() + .5, start.getY() + 1, start.getZ() + .5);
    world.addFreshEntity(cow);
    int before = teleporter.getAvailableEnergy(), expected = (int) (100 * Math.pow(15, .7) * 5);
    teleporter.teleport(cow, Double.NaN);
    check(
        teleporter.getAvailableEnergy() == before && cow.blockPosition().getZ() == start.getZ(),
        "invalid distance cannot move or charge an entity");
    teleporter.teleport(cow, 0);
    check(
        cow.distanceToSqr(target.getX() + .5, target.getY() + 1.5, target.getZ() + .5) < .01
            && teleporter.getAvailableEnergy() == before - expected,
        "actual native teleport charges distance derived from linked endpoints rather than a caller"
            + " discount");
    cow.discard();
    world.removeBlock(start, false);
    world.removeBlock(start.west(), false);
    world.removeBlock(start.east(), false);
    world.removeBlock(target, false);

    var column = new BlockPos(8648, -20, 8648);
    world.setBlockAndUpdate(column, Blocks.DIRT.defaultBlockState());
    check(
        column.equals(TerrainController.getFirstBlockFrom(world, column.above(2), 2)),
        "exposed column search finds a block below Y zero");
    check(
        column.equals(TerrainController.getFirstSolidBlockFrom(world, column.above(2), 2)),
        "solid column search finds terrain below Y zero");
    check(
        TerrainController.switchGround(
                world, column, Blocks.DIRT, Blocks.SAND.defaultBlockState(), false)
            && world.getBlockState(column).is(Blocks.SAND),
        "ground substitution works below zero");
    var irrigation = new IrrigationProgram();
    check(
        irrigation.terraform(world, column) && world.getBlockState(column).is(Blocks.DIRT),
        "native irrigation restores below-zero sand to soil");
    var cultivation = new CultivationProgram();
    cultivation.init();
    cultivation.init();
    check(
        cultivation.terraform(world, column) && world.getBlockState(column).is(Blocks.GRASS_BLOCK),
        "cultivation has repeatable initialization and turns real soil into grass");
    var frost = new FrostProgram();
    world.setBlockAndUpdate(column, Blocks.WATER.defaultBlockState());
    check(
        frost.terraform(world, column) && world.getBlockState(column).is(Blocks.ICE),
        "frost freezes exposed actual water below zero");
    var flatten = new LevelingProgram();
    flatten.init();
    world.setBlockAndUpdate(column, Blocks.STONE.defaultBlockState());
    check(
        flatten.terraform(world, column.below()) && world.getBlockState(column).isAir(),
        "flattening removes one permitted block above its target plane");
    world.setBlockAndUpdate(column, Blocks.DIRT.defaultBlockState());
    var mushroom = new FungalProgram();
    check(
        mushroom.terraform(world, column) && world.getBlockState(column).is(Blocks.MYCELIUM),
        "fungal first phase converts the real soil and biome");
    check(
        mushroom.terraform(world, column)
            && world.getBlockState(column.above()).getBlock()
                instanceof net.minecraft.world.level.block.MushroomBlock,
        "fungal second phase seeds above its substrate");

    var machinePos = new BlockPos(8640, 110, 8640);
    world.setBlockAndUpdate(machinePos, TerrainBlocks.TERRAFORMER.defaultBlockState());
    var terra = (TerrainController) world.getBlockEntity(machinePos);
    var energy = terra.getComponent(ElectricalBuffer.class);
    for (int x = -8; x <= 8; x++)
      for (int z = -8; z <= 8; z++)
        world.setBlockAndUpdate(machinePos.offset(x, -1, z), Blocks.DIRT.defaultBlockState());
    terra.tfbpSlot.put(new ItemStack(TerrainItems.DESERTIFICATION_TFBP));
    terra.updateEntityServer();
    check(
        !terra.getActive() && energy.getEnergy() == 0 && sand(world, machinePos) == 0,
        "unpaid blueprint cannot mutate a single candidate terrain cell");
    energy.forceAddEnergy(2500);
    terra.updateEntityServer();
    check(
        terra.getActive() && energy.getEnergy() == 0 && sand(world, machinePos) == 1,
        "exact full bill permits one real blueprint operation and exhausts storage");
    terra.tfbpSlot.clear();
    for (int tick = 0; tick < 31; tick++) terra.updateEntityServer();
    check(
        !terra.getActive(),
        "removing the blueprint ends active state after its bounded idle delay");
    world.removeBlock(machinePos, false);
    return new Result(checks, List.copyOf(failures));
  }

  private static int sand(ServerLevel world, BlockPos origin) {
    int count = 0;
    for (int x = -8; x <= 8; x++)
      for (int z = -8; z <= 8; z++)
        if (world.getBlockState(origin.offset(x, -1, z)).is(Blocks.SAND)) count++;
    return count;
  }
}
