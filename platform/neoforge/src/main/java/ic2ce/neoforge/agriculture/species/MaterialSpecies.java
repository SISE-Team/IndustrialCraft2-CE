package ic2ce.neoforge.agriculture.species;

import ic2ce.api.crops.CropCard;
import ic2ce.api.crops.ICropType;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import net.minecraft.resources.Identifier;

/**
 * Shared material descriptors are immutable and cached independently of species growth policies.
 */
public abstract class MaterialSpecies extends CropCard {
  private record TextureKey(String owner, String id, int stages, boolean vanilla) {}

  private static final ConcurrentHashMap<TextureKey, List<Identifier>> TEXTURES =
      new ConcurrentHashMap<>();

  public MaterialSpecies(ICropType type) {
    super(type);
  }

  @Override
  public String getUnlocalizedName() {
    return "ic2.crop." + getId();
  }

  @Override
  public String getDiscoveredBy() {
    return "IC2 Team";
  }

  protected final List<Identifier> textures(boolean vanilla) {
    var key =
        new TextureKey(
            vanilla ? "minecraft" : "ic2", getId(), Math.clamp(getMaxAge(), 0, 256), vanilla);
    return TEXTURES.computeIfAbsent(
        key,
        descriptor ->
            IntStream.rangeClosed(1, descriptor.stages)
                .mapToObj(
                    stage ->
                        Identifier.fromNamespaceAndPath(
                            descriptor.owner,
                            descriptor.vanilla
                                ? "block/" + descriptor.id + "_stage_" + stage
                                : "block/crop/" + descriptor.id + "_" + stage))
                .toList());
  }

  @Override
  public List<Identifier> getTexturesLocation() {
    return textures(false);
  }
}
