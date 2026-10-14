package ic2ce.neoforge.cable;

import java.util.Objects;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Explicit physical conductor construction owns the variant pairing and native state-definition
 * context.
 */
public final class BareRedstoneGateBlock extends RedstoneGateBlock {
  private final FoamedRedstoneGateBlock foamedVariant;

  public static BareRedstoneGateBlock create(
      Properties properties, FoamedRedstoneGateBlock foamed) {
    Objects.requireNonNull(foamed);
    if (foamed.type != ConductorMaterial.splitter || foamed.insulation != 0)
      throw new IllegalArgumentException("Foamed conductor does not match its bare material/depth");
    return construct(
        ConductorMaterial.splitter, 0, () -> new BareRedstoneGateBlock(properties, foamed));
  }

  private BareRedstoneGateBlock(Properties properties, FoamedRedstoneGateBlock foamed) {
    super(properties, ConductorMaterial.splitter, 0);
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

  public FoamedRedstoneGateBlock getFoamCableBlock() {
    return foamedVariant;
  }

  @Override
  protected com.mojang.serialization.MapCodec<BareRedstoneGateBlock> codec() {
    return com.mojang.serialization.MapCodec.unit(this);
  }
}
