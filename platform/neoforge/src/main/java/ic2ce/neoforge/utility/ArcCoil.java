package ic2ce.neoforge.utility;

import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.damage.DamageProfiles;
import ic2ce.neoforge.item.armor.SealedGarment;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Redstone-gated arcs reserve four hundred EU per planned damage point and refund rejected hits.
 */
public class ArcCoil extends NativeBlockEntity {
  private static final DustParticleOptions EFFECT = new DustParticleOptions(0x1A1AFF, 1);
  protected final SignalInput redstone;
  protected final ElectricalBuffer energy;
  private int ticker;

  public ArcCoil(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.TESLA_COIL, position, state);
    redstone = addComponent(new SignalInput(this));
    energy = addComponent(ElectricalBuffer.asBasicSink(this, 10000, 2));
    ticker = Math.floorMod(position.hashCode(), 32);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (!redstone.hasRedstoneInput() || !energy.useEnergy(1)) return;
    if (++ticker < 32) return;
    ticker = 0;
    int points = (int) (Math.min(energy.getEnergy(), energy.getCapacity()) / 400);
    if (points > 0 && shock(points)) setChanged();
  }

  protected boolean shock(int totalDamage) {
    if (!(getLevel() instanceof ServerLevel world) || totalDamage <= 0) return false;
    int budget =
        Math.min(totalDamage, (int) (Math.min(energy.getEnergy(), energy.getCapacity()) / 400));
    if (budget == 0) return false;
    var targets =
        world.getEntitiesOfClass(
            LivingEntity.class,
            new AABB(worldPosition).inflate(4),
            entity ->
                entity.isAlive()
                    && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)
                    && !SealedGarment.hasCompleteHazmat(entity));
    if (targets.isEmpty()) return false;
    boolean hit = false;
    int quotient = budget / targets.size(), remainder = budget % targets.size();
    for (int index = 0; index < targets.size(); index++) {
      int damage = quotient + (index < remainder ? 1 : 0);
      if (damage == 0) continue;
      double bill = damage * 400.0;
      if (!energy.useEnergy(bill)) break;
      var target = targets.get(index);
      boolean accepted;
      try {
        accepted = target.hurtServer(world, DamageProfiles.electricity(), damage);
      } catch (RuntimeException | Error failure) {
        energy.forceAddEnergy(bill);
        throw failure;
      }
      if (!accepted) {
        energy.forceAddEnergy(bill);
        continue;
      }
      hit = true;
      var random = world.getRandom();
      for (int particle = 0; particle < Math.min(32, damage); particle++)
        world.addParticle(
            EFFECT,
            target.getX() + random.nextFloat() - 0.5,
            target.getY() + random.nextFloat() * target.getBbHeight(),
            target.getZ() + random.nextFloat() - 0.5,
            0,
            0,
            0);
    }
    return hit;
  }
}
