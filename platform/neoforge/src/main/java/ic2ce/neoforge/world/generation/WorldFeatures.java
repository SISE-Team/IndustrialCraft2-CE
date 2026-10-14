package ic2ce.neoforge.world.generation;

import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Native datapack feature definitions and biome modifiers own generation; bootstrap registers the
 * independent canopy type.
 */
public final class WorldFeatures {
  public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE_KEY =
      ResourceKey.create(
          Registries.CONFIGURED_FEATURE, ProjectRuntime.getIdentifier("rubber_tree"));
  public static final List<ResourceKey<PlacedFeature>> ORE_FEATURES =
      List.of(
              "ore_lead",
              "ore_lead_lower",
              "ore_tin_small",
              "ore_tin_upper",
              "ore_uranium",
              "ore_uranium_buried",
              "ore_uranium_large")
          .stream()
          .map(
              name ->
                  ResourceKey.create(Registries.PLACED_FEATURE, ProjectRuntime.getIdentifier(name)))
          .toList();
  public static final List<ResourceKey<PlacedFeature>> TREE_FEATURES =
      List.of("trees_rubber_jungle", "trees_rubber_forest", "trees_rubber_swamp").stream()
          .map(
              name ->
                  ResourceKey.create(Registries.PLACED_FEATURE, ProjectRuntime.getIdentifier(name)))
          .toList();

  private WorldFeatures() {}

  public static void init() {
    ResinCanopy.init();
  }

  public static Holder.Reference<ConfiguredFeature<?, ?>> rubberTree(Level world) {
    return world
        .registryAccess()
        .lookupOrThrow(Registries.CONFIGURED_FEATURE)
        .getOrThrow(RUBBER_TREE_KEY);
  }
}
