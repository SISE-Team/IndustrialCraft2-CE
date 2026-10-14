package ic2ce.neoforge.cable;

import java.util.Objects;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Explicit physical conductor construction owns the variant pairing and native state-definition
 * context.
 */
public final class BareCableBlock extends ConductorBlock {
  private final FoamConductorBlock foamedVariant;

  public static BareCableBlock create(
      Properties properties, ConductorMaterial material, int depth, FoamConductorBlock foamed) {
    Objects.requireNonNull(foamed);
    if (foamed.type != material || foamed.insulation != depth)
      throw new IllegalArgumentException("Foamed conductor does not match its bare material/depth");
    return construct(
        material, depth, () -> new BareCableBlock(properties, material, depth, foamed));
  }

  private BareCableBlock(
      Properties properties, ConductorMaterial material, int depth, FoamConductorBlock foamed) {
    super(properties, material, depth);
    foamedVariant = foamed;
  }

  @Override
  public boolean isFoam() {
    return false;
  }

  @Override
  public boolean isHardFoam(BlockState state) {
    return false;
  }

  public FoamConductorBlock getFoamCableBlock() {
    return foamedVariant;
  }

  @Override
  protected com.mojang.serialization.MapCodec<BareCableBlock> codec() {
    return com.mojang.serialization.MapCodec.unit(this);
  }
}
