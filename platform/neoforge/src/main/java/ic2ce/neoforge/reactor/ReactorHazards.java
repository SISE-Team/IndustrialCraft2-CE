package ic2ce.neoforge.reactor;

import ic2ce.api.reactor.IReactorComponent;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.damage.DamageProfiles;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.diagnostics.Positions;
import ic2ce.neoforge.diagnostics.ProjectLog;
import ic2ce.neoforge.explosion.NativeBlast;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.world.block.WorldMaterials;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Reactor-specific radiation, environmental damage, meltdown and bounded client particles. */
public final class ReactorHazards {
  private ReactorHazards() {}

  public static boolean apply(NuclearCore core) {
    var world = core.getLevel();
    if (world == null
        || world.isClientSide()
        || core.getHeat() < 4000
        || !(NativeConfigValues.getFloat(
                ProjectConfiguration.get(), "protection/reactorExplosionPowerLimit")
            > 0)) return false;
    double ratio = (double) core.getHeat() / Math.max(1, core.getMaxHeat());
    if (ratio >= 1) {
      explode(core);
      return true;
    }
    float chance = Math.max(0, core.getHeatEffectModifier());
    var random = world.getRandom();
    if (ratio >= 0.85 && random.nextFloat() <= 0.2 * chance) {
      var position = randomPosition(core, 2);
      var state = world.getBlockState(position);
      if (state.isAir()) world.setBlockAndUpdate(position, Blocks.FIRE.defaultBlockState());
      else if (state.getDestroySpeed(world, position) >= 0
          && world.getBlockEntity(position) == null) {
        var replacement =
            WorldMaterials.isReactorCasingLike(state)
                ? net.minecraft.world.level.material.Fluids.FLOWING_LAVA
                    .defaultFluidState()
                    .createLegacyBlock()
                : Blocks.FIRE.defaultBlockState();
        world.setBlockAndUpdate(position, replacement);
      }
    }
    if (ratio >= 0.7) {
      var area = new AABB(core.getBlockPos()).inflate(3);
      for (var entity :
          world.getEntitiesOfClass(
              LivingEntity.class, area, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
        float damage = (float) Math.min(Integer.MAX_VALUE, random.nextInt(4) * (double) chance);
        if (damage > 0) entity.hurt(DamageProfiles.radiation(), damage);
      }
    }
    if (ratio >= 0.5 && random.nextFloat() <= chance) {
      var position = randomPosition(core, 2);
      if (world.getFluidState(position).is(FluidTags.WATER)) world.removeBlock(position, false);
    }
    if (ratio >= 0.4 && random.nextFloat() <= chance) {
      var position = randomPosition(core, 2);
      if (world.getBlockEntity(position) == null
          && WorldMaterials.isFlammableLike(world.getBlockState(position), world, position))
        world.setBlockAndUpdate(position, Blocks.FIRE.defaultBlockState());
    }
    return false;
  }

  public static BlockPos randomPosition(NuclearCore core, int requestedRadius) {
    if (requestedRadius <= 0 || core.getLevel() == null) return null;
    int radius = Math.min(requestedRadius, 1024), span = 2 * radius + 1;
    var random = core.getLevel().getRandom();
    int x, y, z;
    do {
      x = random.nextInt(span) - radius;
      y = random.nextInt(span) - radius;
      z = random.nextInt(span) - radius;
    } while (x == 0 && y == 0 && z == 0);
    return core.getBlockPos().offset(x, y, z);
  }

  public static void explode(NuclearCore core) {
    var world = core.getLevel();
    if (world == null || world.isClientSide() || core.isRemoved()) return;
    double power = 10, mitigation = 1;
    for (int slot = 0; slot < core.reactorSlot.rawSize(); slot++) {
      var stack = core.reactorSlot.get(slot);
      if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IReactorComponent component) {
        float influence = component.influenceExplosion(stack, core);
        if (Float.isFinite(influence)) {
          if (influence > 0 && influence < 1) mitigation *= influence;
          else power += influence;
        }
      }
      core.reactorSlot.put(slot, ItemStack.EMPTY);
    }
    power = Math.max(0, power * Math.max(0, core.getHeatEffectModifier()) * mitigation);
    ProjectLog.LOG.log(
        DiagnosticArea.PlayerActivity,
        org.apache.logging.log4j.Level.INFO,
        "Nuclear reactor at %s melted (raw explosion power %f)",
        Positions.format(core),
        power);
    float limit =
        NativeConfigValues.getFloat(
            ProjectConfiguration.get(), "protection/reactorExplosionPowerLimit");
    float bounded = (float) Math.min(power, Math.max(0, limit));
    for (var face : Direction.values()) {
      var position = core.getBlockPos().relative(face);
      if (world.getBlockEntity(position) instanceof ReactorChamber chamber
          && chamber.getReactorInstance() == core) world.removeBlock(position, false);
    }
    world.removeBlock(core.getBlockPos(), false);
    if (bounded > 0)
      new NativeBlast(world, null, core.getBlockPos(), bounded, 0.01F, NativeBlast.Type.Nuclear)
          .doExplosion();
  }

  public static void particles(Level world, BlockPos position, int heat) {
    if (world == null || !world.isClientSide() || heat < 1000 || world.getRandom().nextInt(8) != 0)
      return;
    var random = world.getRandom();
    int count = random.nextInt(Math.min(64, heat / 1000));
    for (int n = 0; n < count; n++)
      world.addParticle(
          ParticleTypes.SMOKE,
          position.getX() + random.nextFloat(),
          position.getY() + 0.95,
          position.getZ() + random.nextFloat(),
          0,
          0,
          0);
    count -= random.nextInt(4) + 3;
    for (int n = 0; n < count; n++)
      world.addParticle(
          ParticleTypes.FLAME,
          position.getX() + random.nextFloat(),
          position.getY() + 1,
          position.getZ() + random.nextFloat(),
          0,
          0,
          0);
  }
}
