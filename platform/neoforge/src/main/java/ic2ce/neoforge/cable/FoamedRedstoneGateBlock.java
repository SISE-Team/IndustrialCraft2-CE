package ic2ce.neoforge.cable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Explicit physical conductor construction owns the variant pairing and native state-definition
 * context.
 */
public final class FoamedRedstoneGateBlock extends RedstoneGateBlock {
  public static FoamedRedstoneGateBlock create(Properties properties) {
    return construct(ConductorMaterial.splitter, 0, () -> new FoamedRedstoneGateBlock(properties));
  }

  private FoamedRedstoneGateBlock(Properties properties) {
    super(properties, ConductorMaterial.splitter, 0);
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
  protected com.mojang.serialization.MapCodec<FoamedRedstoneGateBlock> codec() {
    return com.mojang.serialization.MapCodec.unit(this);
  }
}
