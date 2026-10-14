package ic2ce.neoforge.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Native leaf decay with the project's physical flammability contract. */
public final class ResinLeaves extends LeavesBlock {
  public ResinLeaves(float particleChance, Properties properties) {
    super(particleChance, properties);
  }

  @Override
  public com.mojang.serialization.MapCodec<ResinLeaves> codec() {
    return com.mojang.serialization.MapCodec.unit(this);
  }

  @Override
  protected void spawnFallingLeavesParticle(
      net.minecraft.world.level.Level world,
      BlockPos position,
      net.minecraft.util.RandomSource random) {
    var particle =
        net.minecraft.core.particles.ColorParticleOption.create(
            net.minecraft.core.particles.ParticleTypes.TINTED_LEAVES, 6723908);
    net.minecraft.util.ParticleUtils.spawnParticleBelow(world, position, random, particle);
  }

  @Override
  public int getFlammability(
      BlockState state, BlockGetter world, BlockPos position, Direction face) {
    return ic2ce.neoforge.EnvProxyNeoForge.flammability(state);
  }

  @Override
  public int getFireSpreadSpeed(
      BlockState state, BlockGetter world, BlockPos position, Direction face) {
    return ic2ce.neoforge.EnvProxyNeoForge.fireSpread(state);
  }
}
