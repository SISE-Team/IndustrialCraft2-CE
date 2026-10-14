package ic2ce.neoforge.cable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Explicit physical conductor construction owns the variant pairing and native state-definition
 * context.
 */
public final class FoamedTrafficSensorBlock extends TrafficSensorBlock {
  public static FoamedTrafficSensorBlock create(Properties properties) {
    return construct(ConductorMaterial.detector, 0, () -> new FoamedTrafficSensorBlock(properties));
  }

  private FoamedTrafficSensorBlock(Properties properties) {
    super(properties);
  }

  @Override
  public boolean isFoam() {
    return true;
  }

  @Override
  public boolean isHardFoam(BlockState state) {
    return state.is(this) && state.getValue(foamProperty).isHard();
  }

  @Override
  protected com.mojang.serialization.MapCodec<FoamedTrafficSensorBlock> codec() {
    return com.mojang.serialization.MapCodec.unit(this);
  }
}
