package ic2ce.neoforge.explosion;

import ic2ce.api.tile.ExplosionWhitelist;
import ic2ce.engine.explosion.DamageMask;
import ic2ce.neoforge.damage.DamageProfiles;
import ic2ce.neoforge.damage.RadiationEffect;
import ic2ce.neoforge.item.armor.SealedGarment;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.runtime.ProgressionService;
import ic2ce.neoforge.runtime.SideServices;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.*;

/**
 * Independent phased blast tracing: immutable block observations, compact damage flags and native
 * destruction callbacks.
 */
public final class NativeBlast implements Explosion {
  public enum Type {
    Normal,
    Heat,
    Electrical,
    Nuclear
  }

  private final ServerLevel world;
  private final Entity source;
  private final LivingEntity igniter;
  private final Vec3 origin;
  private final float power, dropChance;
  private final Type type;
  private final int radiation;
  private final double reach;
  private final DamageMask damaged;
  private final DamageSource damageSource;
  private final ArrayList<Victim> victims = new ArrayList<>();
  private final Long2DoubleOpenHashMap absorption = new Long2DoubleOpenHashMap();
  private PathNavigationRegion region;
  private boolean completed;

  private static final class Victim {
    final Entity entity;
    final double squaredDistance;
    double damage, motionX, motionY, motionZ;

    Victim(Entity entity, Vec3 origin) {
      this.entity = entity;
      squaredDistance = entity.position().distanceToSqr(origin);
    }
  }

  private static final class StackIdentity {
    final ItemStack prototype;
    final int hash;

    StackIdentity(ItemStack stack) {
      prototype = stack.copyWithCount(1);
      hash = ItemStack.hashItemAndComponents(prototype);
    }

    @Override
    public int hashCode() {
      return hash;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof StackIdentity key
          && ItemStack.isSameItemSameComponents(prototype, key.prototype);
    }
  }

  private record DropKey(int x, int z, StackIdentity item) {}

  private static final class Drop {
    final ItemStack prototype;
    long count;
    int top;

    Drop(ItemStack stack, int y) {
      prototype = stack.copyWithCount(1);
      count = stack.getCount();
      top = y;
    }

    void add(ItemStack stack, int y) {
      count = Math.addExact(count, stack.getCount());
      top = Math.max(top, y);
    }
  }

  public NativeBlast(
      Level world, Entity source, double x, double y, double z, float power, float drops) {
    this(world, source, x, y, z, power, drops, Type.Normal);
  }

  public NativeBlast(
      Level world,
      Entity source,
      double x,
      double y,
      double z,
      float power,
      float drops,
      Type type) {
    this(world, source, x, y, z, power, drops, type, null, 0);
  }

  public NativeBlast(
      Level world, Entity source, BlockPos position, float power, float drops, Type type) {
    this(
        world,
        source,
        position.getX() + .5,
        position.getY() + .5,
        position.getZ() + .5,
        power,
        drops,
        type);
  }

  public NativeBlast(
      Level world, Entity source, BlockPos position, int power, float drops, Type type) {
    this(world, source, position, (float) power, drops, type);
  }

  public NativeBlast(
      Level level,
      Entity source,
      double x,
      double y,
      double z,
      float power,
      float drops,
      Type type,
      LivingEntity igniter,
      int radiation) {
    if (!(level instanceof ServerLevel server))
      throw new IllegalArgumentException("Blast tracing requires an actual server level");
    if (!Double.isFinite(x + y + z)
        || !Float.isFinite(power)
        || power < 0
        || !Float.isFinite(drops)
        || radiation < 0) throw new IllegalArgumentException("Invalid native blast profile");
    world = server;
    this.source = source;
    this.igniter = igniter;
    origin = new Vec3(x, y, z);
    this.power = power;
    dropChance = Math.clamp(drops, 0, 1);
    this.type = Objects.requireNonNull(type);
    this.radiation = radiation;
    reach = power / .4;
    int radius = (int) Math.ceil(reach);
    if (radius > 16383)
      throw new IllegalArgumentException("Blast region exceeds native packed-coordinate bounds");
    int side = Math.max(2, Math.multiplyExact(radius, 2));
    var center = BlockPos.containing(origin);
    damaged =
        new DamageMask(
            center.getX() - radius,
            world.getMinY(),
            center.getZ() - radius,
            side,
            world.getHeight(),
            side);
    damageSource =
        type == Type.Nuclear
            ? new DamageSource(
                world
                    .registryAccess()
                    .lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                    .getOrThrow(DamageProfiles.NUKE_KEY),
                source,
                igniter)
            : world.damageSources().explosion(source, igniter);
    absorption.defaultReturnValue(Double.NaN);
  }

  @Override
  public ServerLevel level() {
    return world;
  }

  @Override
  public BlockInteraction getBlockInteraction() {
    return BlockInteraction.DESTROY;
  }

  @Override
  public float radius() {
    return power;
  }

  @Override
  public Vec3 center() {
    return origin;
  }

  @Override
  public boolean canTriggerBlocks() {
    return true;
  }

  @Override
  public Entity getDirectSourceEntity() {
    return source;
  }

  @Override
  public LivingEntity getIndirectSourceEntity() {
    return igniter;
  }

  @Override
  public boolean shouldAffectBlocklikeEntities() {
    return true;
  }

  public LivingEntity getSourceMob() {
    return igniter;
  }

  public void destroy(int x, int y, int z, boolean vaporized) {
    if (!completed) damaged.mark(x, y, z, vaporized);
  }

  public void doExplosion() {
    if (completed) return;
    completed = true;
    try {
      if (power <= 0
          || !EnvProxy.get().announceExplosion(
              world, source, origin, power, igniter, radiation, reach)) return;
      int radius = (int) Math.ceil(reach);
      var center = BlockPos.containing(origin);
      region =
          new PathNavigationRegion(
              world,
              center.offset(-radius, -radius, -radius),
              center.offset(radius, radius, radius));
      for (var entity :
          world.getEntities(
              source,
              new AABB(
                  origin.x - reach,
                  origin.y - reach,
                  origin.z - reach,
                  origin.x + reach,
                  origin.y + reach,
                  origin.z + reach)))
        if ((entity instanceof LivingEntity || entity instanceof ItemEntity)
            && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))
          victims.add(new Victim(entity, origin));
      victims.sort(Comparator.comparingDouble(victim -> victim.squaredDistance));
      var cursor = new BlockPos.MutableBlockPos();
      int divisions = Math.max(1, (int) Math.ceil(Math.PI / Math.atan(1 / reach)));
      for (int azimuth = 0; azimuth < 2 * divisions; azimuth++) {
        double phi = Math.TAU / divisions * azimuth, cos = Math.cos(phi), sin = Math.sin(phi);
        for (int elevation = 0; elevation < divisions; elevation++) {
          double theta = Math.PI / divisions * elevation, polar = Math.sin(theta);
          trace(
              polar * cos,
              Math.cos(theta),
              polar * sin,
              !victims.isEmpty() && azimuth % 8 == 0 && elevation % 8 == 0,
              cursor);
        }
      }
      applyDamage();
      applyRadiation();
      SideServices.network(true).initiateExplosionEffect(world, origin, type);
      destroyBlocks();
    } finally {
      victims.clear();
      absorption.clear();
      absorption.trim();
      damaged.clear();
      region = null;
    }
  }

  private double resistance(BlockState state, BlockPos position) {
    if (state.isAir()) return .5;
    long key = position.asLong();
    double cached = absorption.get(key);
    if (!Double.isNaN(cached)) return cached;
    double value;
    if (state.is(Blocks.WATER) && type != Type.Normal) value = 1.5;
    else {
      float resistance = EnvProxy.get().getBlastResistance(state, world, position, this);
      value =
          !Float.isFinite(resistance) || resistance < 0
              ? -1
              : .5 + (resistance + 4) * .3 * (type == Type.Heat ? 6 : 1);
    }
    absorption.put(key, value);
    return value;
  }

  private void trace(
      double dx, double dy, double dz, boolean entities, BlockPos.MutableBlockPos cursor) {
    double x = origin.x, y = origin.y, z = origin.z, remaining = power;
    int step = 0;
    while (remaining > 0) {
      cursor.set(
          net.minecraft.util.Mth.floor(x),
          net.minecraft.util.Mth.floor(y),
          net.minecraft.util.Mth.floor(z));
      if (!damaged.contains(cursor.getX(), cursor.getY(), cursor.getZ())) break;
      var state = region.getBlockState(cursor);
      double bill = resistance(state, cursor);
      if (bill < 0) break;
      boolean transparent = bill > 1000 && !ExplosionWhitelist.isBlockWhitelisted(state.getBlock());
      if (transparent) bill = .5;
      else {
        if (bill > remaining) break;
        if (!state.isAir())
          damaged.mark(cursor.getX(), cursor.getY(), cursor.getZ(), remaining > 8);
      }
      if (entities && (step + 4) % 8 == 0 && remaining >= .25)
        sampleVictims(x, y, z, step, remaining);
      remaining -= bill;
      x += dx;
      y += dy;
      z += dz;
      step++;
    }
  }

  private void sampleVictims(double x, double y, double z, int step, double strength) {
    double minimum = step == 4 ? 0 : (double) (step - 5) * (step - 5),
        maximum = (double) (step + 5) * (step + 5);
    int low = 0, high = victims.size();
    while (low < high) {
      int middle = (low + high) >>> 1;
      if (victims.get(middle).squaredDistance < minimum) low = middle + 1;
      else high = middle;
    }
    for (int index = low; index < victims.size(); index++) {
      var victim = victims.get(index);
      if (victim.squaredDistance >= maximum) break;
      var entity = victim.entity;
      double px = entity.getX() - x, py = entity.getY() - y, pz = entity.getZ() - z;
      if (px * px + py * py + pz * pz > 25) continue;
      victim.damage += 4 * strength;
      double distance = Math.sqrt(victim.squaredDistance);
      if (distance > 0) {
        double impulse = .0875 * strength / distance;
        victim.motionX += (entity.getX() - origin.x) * impulse;
        victim.motionY += (entity.getY() - origin.y) * impulse;
        victim.motionZ += (entity.getZ() - origin.z) * impulse;
      }
    }
  }

  private void applyDamage() {
    for (var victim : victims) {
      var entity = victim.entity;
      if (victim.damage <= 0 || !entity.isAlive()) continue;
      boolean accepted =
          entity.hurtServer(world, damageSource, (float) Math.min(Float.MAX_VALUE, victim.damage));
      if (!accepted) continue;
      double length = Math.hypot(Math.hypot(victim.motionX, victim.motionY), victim.motionZ),
          factor = length > 60 ? 60 / length : 1;
      entity.setDeltaMovement(
          entity
              .getDeltaMovement()
              .add(victim.motionX * factor, victim.motionY * factor, victim.motionZ * factor));
      if (type == Type.Nuclear
          && entity == igniter
          && entity instanceof Player actor
          && actor.getHealth() <= 0)
        ProgressionService.issueAchievement(actor, "dieFromOwnNuke");
    }
  }

  private void applyRadiation() {
    if (type != Type.Nuclear || radiation < 1) return;
    var exposed = new ArrayList<LivingEntity>();
    for (var candidate : world.getAllEntities())
      if (candidate instanceof LivingEntity living
          && living.isAlive()
          && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(living)) exposed.add(living);
    for (var entity : exposed)
      if (!SealedGarment.hasCompleteHazmat(entity)) {
        double distance = entity.position().distanceTo(origin);
        int hunger = (int) Math.clamp(120 * (radiation - distance), 0, Integer.MAX_VALUE),
            dose = (int) Math.clamp(80 * (radiation / 3 - distance), 0, Integer.MAX_VALUE);
        if (hunger > 0) entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, hunger, 0));
        if (dose > 0) RadiationEffect.radiation.applyTo(entity, dose, 0);
      }
  }

  private void destroyBlocks() {
    boolean drops = world.getGameRules().get(GameRules.BLOCK_DROPS);
    var collected = new LinkedHashMap<DropKey, Drop>();
    damaged.forEach(
        (x, y, z, vaporized) -> {
          var position = new BlockPos(x, y, z);
          var state = world.getBlockState(position);
          if (state.isAir()) return;
          state.onExplosionHit(
              world,
              position,
              this,
              (stack, where) -> {
                if (!drops
                    || vaporized
                    || stack.isEmpty()
                    || world.getRandom().nextFloat() > dropChance) return;
                var key =
                    new DropKey(
                        Math.floorDiv(where.getX(), 2),
                        Math.floorDiv(where.getZ(), 2),
                        new StackIdentity(stack));
                var previous = collected.get(key);
                if (previous == null) collected.put(key, new Drop(stack, where.getY()));
                else previous.add(stack, where.getY());
              });
        });
    for (var entry : collected.entrySet()) {
      var key = entry.getKey();
      var drop = entry.getValue();
      int limit = Math.max(1, drop.prototype.getMaxStackSize());
      for (long remaining = drop.count; remaining > 0; ) {
        int count = (int) Math.min(remaining, limit);
        var entity =
            new ItemEntity(
                world,
                (key.x + world.getRandom().nextFloat()) * 2,
                drop.top + .5,
                (key.z + world.getRandom().nextFloat()) * 2,
                drop.prototype.copyWithCount(count));
        entity.setDefaultPickUpDelay();
        world.addFreshEntity(entity);
        remaining -= count;
      }
    }
  }
}
