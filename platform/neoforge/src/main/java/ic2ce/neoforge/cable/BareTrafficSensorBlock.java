package ic2ce.neoforge.cable;

import java.util.Objects;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Explicit physical conductor construction owns the variant pairing and native state-definition
 * context.
 */
public final class BareTrafficSensorBlock extends TrafficSensorBlock {
  private final FoamedTrafficSensorBlock foamedVariant;

  public static BareTrafficSensorBlock create(
      Properties properties, FoamedTrafficSensorBlock foamed) {
    Objects.requireNonNull(foamed);
    if (foamed.type != ConductorMaterial.detector || foamed.insulation != 0)
      throw new IllegalArgumentException("Foamed conductor does not match its bare material/depth");
    return construct(
        ConductorMaterial.detector, 0, () -> new BareTrafficSensorBlock(properties, foamed));
  }

  private BareTrafficSensorBlock(Properties properties, FoamedTrafficSensorBlock foamed) {
    super(properties);
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

  public FoamedTrafficSensorBlock getFoamCableBlock() {
    return foamedVariant;
  }

  @Override
  protected com.mojang.serialization.MapCodec<BareTrafficSensorBlock> codec() {
    return com.mojang.serialization.MapCodec.unit(this);
  }
}
