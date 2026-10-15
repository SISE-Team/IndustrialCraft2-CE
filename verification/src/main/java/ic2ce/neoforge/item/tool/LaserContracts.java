package ic2ce.neoforge.item.tool;

import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.projectile.MiningBeam;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.SideServices;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Actual emitted native projectiles, per-mode bills and real loaded-world beam collisions. */
public final class LaserContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native laser: " + label);
  }

  public static Result run(ServerLevel world) {
    return new LaserContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var added = new ArrayList<MiningBeam>();
    Consumer<EntityJoinLevelEvent> observer =
        event -> {
          if (event.getLevel() == world && event.getEntity() instanceof MiningBeam beam)
            added.add(beam);
        };
    NeoForge.EVENT_BUS.addListener(observer);
    try {
      return exercise(world, added);
    } finally {
      NeoForge.EVENT_BUS.unregister(observer);
      for (var beam : added) beam.discard();
    }
  }

  private Result exercise(ServerLevel world, List<MiningBeam> added) {
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("397e30b2-a07a-48c0-9f45-fa3600e51915"), "IC2LaserProbe"));
    player.getAbilities().instabuild = false;
    player.getInventory().clearContent();
    SideServices.keyboard().processKeyUpdate(player, 0);
    var pos = new BlockPos(10560, 100, 10560);
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++) {
        world.setChunkForced((pos.getX() >> 4) + x, (pos.getZ() >> 4) + z, true);
        world.getChunkAt(pos.offset(x * 16, 0, z * 16));
      }
    player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
    player.setXRot(0);
    player.setYRot(0);
    var area = new AABB(pos).inflate(30);
    var tool = (MiningLaser) ItemDefinitions.MINING_LASER;
    int[] bills = {1250, 100, 5000, 3000, 2500, 10000, 5000, 7500},
        counts = {1, 1, 1, 1, 1, 25, 1, 9};
    for (int mode = 0; mode < 8; mode++) {
      for (var entity : added) entity.discard();
      added.clear();
      var stack = new ItemStack(tool);
      ElectricItem.manager.charge(stack, 100000, 3, true, false);
      int value = mode;
      StackTag.update(
          stack,
          data -> {
            data.putInt("laserSetting", value);
            return data;
          });
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      InteractionResult result =
          mode == 3 || mode == 7
              ? tool.onItemUseFirst(
                  stack,
                  new UseOnContext(
                      player,
                      InteractionHand.MAIN_HAND,
                      new BlockHitResult(
                          Vec3.atCenterOf(pos.south(3)), Direction.NORTH, pos.south(3), false)))
              : tool.use(world, player, InteractionHand.MAIN_HAND);
      var beams = List.copyOf(added);
      check(
          result.consumesAction()
              && beams.size() == counts[mode]
              && ElectricItem.manager.getCharge(stack) == 100000 - bills[mode],
          "mode "
              + mode
              + " emits its exact native geometry and pays one complete independent bill; result="
              + result
              + ", beams="
              + beams.size()
              + ", EU="
              + ElectricItem.manager.getCharge(stack));
      if (mode == 4)
        check(
            !beams.isEmpty() && beams.getFirst().isSmeltMode,
            "superheat mode marks its actual native projectile");
      if (mode == 6)
        check(
            !beams.isEmpty() && beams.getFirst().isExplosiveMode,
            "explosive mode marks its actual native projectile");
    }
    for (var entity : added) entity.discard();
    added.clear();
    var poor = new ItemStack(tool);
    ElectricItem.manager.charge(poor, 99, 3, true, false);
    StackTag.update(
        poor,
        data -> {
          data.putInt("laserSetting", 1);
          return data;
        });
    player.setItemInHand(InteractionHand.MAIN_HAND, poor);
    tool.use(world, player, InteractionHand.MAIN_HAND);
    check(
        ElectricItem.manager.getCharge(poor) == 99
            && world.getEntitiesOfClass(MiningBeam.class, area).isEmpty(),
        "incomplete focus bill cannot emit a native projectile or spend the remaining energy");
    var negative = new ItemStack(tool);
    StackTag.update(
        negative,
        data -> {
          data.putInt("laserSetting", Integer.MIN_VALUE);
          return data;
        });
    player.setItemInHand(InteractionHand.MAIN_HAND, negative);
    SideServices.keyboard().processKeyUpdate(player, 1 << 3);
    tool.use(world, player, InteractionHand.MAIN_HAND);
    check(
        StackTag.get(negative).getIntOr("laserSetting", -1) >= 0,
        "corrupt negative saved mode switches into the bounded native policy range");
    SideServices.keyboard().processKeyUpdate(player, 0);
    var target = pos.south(6);
    for (int y = 0; y < 4; y++)
      for (int z = 0; z < 12; z++)
        world.setBlock(pos.offset(0, y, z), Blocks.AIR.defaultBlockState(), 3);
    world.setBlock(target, Blocks.STONE.defaultBlockState(), 3);
    world.setBlock(target.south(), Blocks.STONE.defaultBlockState(), 3);
    var beam = beam(world, player, pos.south(2), 30, 20, 20, false);
    for (int tick = 0; tick < 10 && !beam.isRemoved(); tick++) beam.tick();
    check(
        world.isEmptyBlock(target) && world.isEmptyBlock(target.south()),
        "real moving beam penetrates and removes two successive paid native stone blocks");
    var blocked = target.east(3);
    world.setBlock(blocked, Blocks.STONE.defaultBlockState(), 3);
    Consumer<BreakBlockEvent> deny =
        event -> {
          if (event.getPos().equals(blocked)) event.setCanceled(true);
        };
    NeoForge.EVENT_BUS.addListener(deny);
    try {
      beam = beam(world, player, blocked.north(3), 20, 20, 20, false);
      for (int tick = 0; tick < 10 && !beam.isRemoved(); tick++) beam.tick();
      check(
          world.getBlockState(blocked).is(Blocks.STONE) && beam.isRemoved(),
          "a canceled actual native block-break event preserves the target and terminates its"
              + " beam");
    } finally {
      NeoForge.EVENT_BUS.unregister(deny);
    }
    world.setBlock(blocked, Blocks.BEDROCK.defaultBlockState(), 3);
    beam = beam(world, player, blocked.north(3), 20, 20, 20, false);
    for (int tick = 0; tick < 10 && !beam.isRemoved(); tick++) beam.tick();
    check(
        world.getBlockState(blocked).is(Blocks.BEDROCK) && beam.isRemoved(),
        "unbreakable native hardness cannot be converted into free laser output");
    beam = beam(world, player, pos.east(8), 4, 20, 1, false);
    for (int tick = 0; tick < 10 && !beam.isRemoved(); tick++) beam.tick();
    check(
        beam.isRemoved() && beam.range <= 0,
        "real focus range counts actual motion and expires without an endless beam");
    var smelted = pos.east(10).south(6);
    world.setBlock(smelted, Blocks.IRON_ORE.defaultBlockState(), 3);
    beam = beam(world, player, smelted.north(3), 20, 20, 1, true);
    for (int tick = 0; tick < 10 && !beam.isRemoved(); tick++) beam.tick();
    var section =
        world.getEntitiesOfClass(
            net.minecraft.world.entity.item.ItemEntity.class, new AABB(smelted).inflate(4));
    int ingots =
        section.stream()
            .filter(item -> item.getItem().is(Items.IRON_INGOT))
            .mapToInt(item -> item.getItem().getCount())
            .sum();
    check(
        world.isEmptyBlock(smelted) && ingots == 1,
        "actual superheat beam uses the native smelting recipe and produces exactly one paid iron"
            + " ingot");
    var victim =
        net.minecraft.world.entity.EntityType.COW.create(
            world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
    victim.setPos(pos.getX() + 14.5, pos.getY() + .1, pos.getZ() + 6.5);
    victim.setNoAi(true);
    world.addFreshEntity(victim);
    float health = victim.getHealth();
    beam = beam(world, player, pos.east(14).south(3), 20, 20, 1, false);
    for (int tick = 0; tick < 10 && !beam.isRemoved(); tick++) beam.tick();
    check(
        victim.getHealth() < health && beam.isRemoved(),
        "actual native laser collision damages the real living target and consumes its projectile");
    victim.discard();
    for (var entity : world.getEntitiesOfClass(MiningBeam.class, area)) entity.discard();
    player.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }

  private static MiningBeam beam(
      ServerLevel world,
      net.minecraft.world.entity.player.Player owner,
      BlockPos start,
      float range,
      float power,
      int blocks,
      boolean smelt) {
    var beam = new MiningBeam(world, owner);
    beam.setPos(start.getX() + .5, start.getY() + .5, start.getZ() + .5);
    beam.init(owner, range, power, blocks, false, smelt, true);
    beam.setDeltaMovement(0, 0, 3);
    world.addFreshEntity(beam);
    return beam;
  }
}
