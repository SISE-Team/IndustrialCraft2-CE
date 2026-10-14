package ic2ce.neoforge.cable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Explicit physical conductor construction owns the variant pairing and native state-definition
 * context.
 */
public final class FoamConductorBlock extends ConductorBlock {
  public static final FoamFinish DEFAULT_FOAM = FoamFinish.SOFT;

  public static FoamConductorBlock create(
      Properties properties, ConductorMaterial material, int depth) {
    return construct(material, depth, () -> new FoamConductorBlock(properties, material, depth));
  }

  private FoamConductorBlock(Properties properties, ConductorMaterial material, int depth) {
    super(properties, material, depth);
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
  protected com.mojang.serialization.MapCodec<FoamConductorBlock> codec() {
    return com.mojang.serialization.MapCodec.unit(this);
  }
}
