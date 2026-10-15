package ic2ce.neoforge.explosion;

import ic2ce.api.event.ExplosionEvent;
import ic2ce.api.tile.ExplosionWhitelist;
import ic2ce.neoforge.damage.DamageProfiles;
import ic2ce.neoforge.damage.RadiationEffect;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Actual native blocks, loot callbacks, cancelled blasts and center entities exercise the
 * independent phased pipeline.
 */
public final class BlastContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native blast: " + label);
  }

  public static Result run(ServerLevel world) {
    return new BlastContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var spawn = world.getRespawnData().globalPos().pos();
    var center = new BlockPos(spawn.getX(), 120, spawn.getZ());
    var origin = Vec3.atCenterOf(center);
    world.getChunk(center);
    world.setChunkForced(center.getX() >> 4, center.getZ() >> 4, true);
    world.setBlockAndUpdate(center, Blocks.AIR.defaultBlockState());
    int[] cobble = {0};
    var primed = new ArrayList<PrimedDemolitionCharge>();
    Consumer<EntityJoinLevelEvent> observer =
        event -> {
          if (event.getLevel() != world) return;
          if (event.getEntity() instanceof ItemEntity item
              && item.getItem().is(Items.COBBLESTONE)
              && item.position().distanceToSqr(origin) < 100)
            cobble[0] += item.getItem().getCount();
          if (event.getEntity() instanceof PrimedDemolitionCharge charge
              && charge.position().distanceToSqr(origin) < 100) primed.add(charge);
        };
    NeoForge.EVENT_BUS.addListener(observer);
    try {
      var target = center.east();
      world.setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
      var manual = new NativeBlast(world, null, origin.x, origin.y, origin.z, 1, 1);
      manual.destroy(target.getX(), target.getY(), target.getZ(), false);
      manual.doExplosion();
      check(
          world.getBlockState(target).isAir() && cobble[0] == 1,
          "actual native block destruction produces exactly one callback-owned cobblestone");
      manual.doExplosion();
      check(cobble[0] == 1, "completed blast is idempotent and cannot duplicate native loot");
      world.setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
      var vapor = new NativeBlast(world, null, origin.x, origin.y, origin.z, 1, 1);
      vapor.destroy(target.getX(), target.getY(), target.getZ(), true);
      vapor.doExplosion();
      check(
          world.getBlockState(target).isAir() && cobble[0] == 1,
          "vaporized cell cannot recover a block drop from a later native loot callback");
      world.setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
      Consumer<ExplosionEvent> deny =
          event -> {
            if (event.getLevel() == world && event.pos.equals(origin)) event.setCanceled(true);
          };
      NeoForge.EVENT_BUS.addListener(deny);
      try {
        var stopped = new NativeBlast(world, null, origin.x, origin.y, origin.z, 1, 1);
        stopped.destroy(target.getX(), target.getY(), target.getZ(), false);
        stopped.doExplosion();
        check(
            world.getBlockState(target).is(Blocks.STONE) && cobble[0] == 1,
            "actual cancelled blast event leaves both native block and output unchanged");
      } finally {
        NeoForge.EVENT_BUS.unregister(deny);
      }
      world.setBlockAndUpdate(target, Blocks.BEDROCK.defaultBlockState());
      world.setBlockAndUpdate(center.east(2), Blocks.STONE.defaultBlockState());
      ExplosionWhitelist.addWhitelistedBlock(Blocks.BEDROCK);
      try {
        new NativeBlast(world, null, origin.x, origin.y, origin.z, 5, 0).doExplosion();
        check(
            world.getBlockState(target).is(Blocks.BEDROCK)
                && world.getBlockState(center.east(2)).is(Blocks.STONE),
            "whitelisted infinite-resistance native barrier keeps its blocked ray route intact");
      } finally {
        ExplosionWhitelist.removeWhitelistedBlock(Blocks.BEDROCK);
      }
      world.removeBlock(target, false);
      world.removeBlock(center.east(2), false);
      world.setBlockAndUpdate(center, ExplosionBlocks.ITNT.defaultBlockState());
      new NativeBlast(world, null, origin.x, origin.y, origin.z, 2, 0).doExplosion();
      check(
          world.getBlockState(center).isAir()
              && primed.size() == 1
              && primed.getFirst().getFuse() >= 7
              && primed.getFirst().getFuse() < 22,
          "actual phased blast invokes industrial TNT before removal and creates one shortened-fuse"
              + " charge");
      primed.forEach(Entity::discard);
      world.removeBlock(center, false);
      var pig = new Pig(EntityType.PIG, world);
      pig.setPos(origin);
      world.addFreshEntity(pig);
      boolean visible = false;
      for (var entity : world.getAllEntities()) if (entity == pig) visible = true;
      check(visible, "actual explosion-center mob is admitted to the current native entity getter");
      float health = pig.getHealth();
      new NativeBlast(world, null, origin.x, origin.y, origin.z, 3, 0).doExplosion();
      var motion = pig.getDeltaMovement();
      check(
          pig.getHealth() < health && Double.isFinite(motion.x + motion.y + motion.z),
          "actual center entity receives native damage without a zero-distance NaN impulse");
      pig.discard();
      world.removeBlock(target, false);
      var radiated = new Pig(EntityType.PIG, world);
      radiated.setPos(origin.add(10, 0, 0));
      world.addFreshEntity(radiated);
      new NativeBlast(
              world, null, origin.x, origin.y, origin.z, .1F, 0, NativeBlast.Type.Nuclear, null, 60)
          .doExplosion();
      check(
          radiated.hasEffect(net.minecraft.world.effect.MobEffects.HUNGER)
              && radiated.hasEffect(RadiationEffect.radiationHolder()),
          "actual loaded living entity receives distinct external nuclear radiation beyond block"
              + " blast reach");
      radiated.discard();
      check(
          DamageProfiles.electricity() == DamageProfiles.electricity()
              && DamageProfiles.radiation().is(DamageProfiles.RADIATION_KEY),
          "live native registry resolves stable current-generation damage identities");
      check(
          RadiationEffect.radiation.shouldApplyEffectTickThisTick(25, 0)
              && !RadiationEffect.radiation.shouldApplyEffectTickThisTick(24, 0)
              && RadiationEffect.radiation.shouldApplyEffectTickThisTick(1, 5)
              && !RadiationEffect.radiation.shouldApplyEffectTickThisTick(0, 0),
          "radiation pulse rate follows bounded amplifier scheduling and excludes expired"
              + " durations");
    } finally {
      NeoForge.EVENT_BUS.unregister(observer);
      world.removeBlock(center, false);
      world.removeBlock(center.east(), false);
      world.removeBlock(center.east(2), false);
      world.setChunkForced(center.getX() >> 4, center.getZ() >> 4, false);
    }
    return new Result(checks, List.copyOf(failures));
  }
}
