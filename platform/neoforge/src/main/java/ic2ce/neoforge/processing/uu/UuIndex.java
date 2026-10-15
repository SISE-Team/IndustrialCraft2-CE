package ic2ce.neoforge.processing.uu;

import ic2ce.api.recipe.Recipes;
import ic2ce.engine.config.Config;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * Owns resolver registration and seeds; each refresh produces a complete immutable cost generation.
 */
public final class UuIndex {
  public static final UuIndex instance = new UuIndex();
  final List<IRecipeResolver> resolvers = new ArrayList<>();
  final List<ILateRecipeResolver> lateResolvers = new ArrayList<>();

  private UuIndex() {}

  public void addResolver(IRecipeResolver resolver) {
    resolvers.add(Objects.requireNonNull(resolver));
  }

  public void addResolver(ILateRecipeResolver resolver) {
    lateResolvers.add(Objects.requireNonNull(resolver));
  }

  public void add(ItemStack stack, double value) {
    UuGraph.set(stack, value);
  }

  public double get(ItemStack stack) {
    return UuGraph.get(stack);
  }

  public double getInBuckets(ItemStack stack) {
    return UuGraph.get(stack) * 1e-5;
  }

  public void init() {}

  public void init(ServerLevel world) {
    Objects.requireNonNull(world);
    resolvers.clear();
    lateResolvers.clear();
    addResolver(new RecipeResolver());
    addResolver(new VanillaSmeltingResolver());
    for (var provider :
        List.of(
            Recipes.macerator,
            Recipes.extractor,
            Recipes.compressor,
            Recipes.centrifuge,
            Recipes.blockcutter,
            Recipes.blastfurnace,
            Recipes.metalformerExtruding,
            Recipes.metalformerCutting,
            Recipes.metalformerRolling,
            Recipes.oreWashing)) addResolver(new MachineRecipeResolver(provider.get(world)));
    addResolver(new ScrapBoxResolver());
    addResolver(new RecyclerResolver());
    addResolver(new ManualRecipeResolver());
    refresh(true);
  }

  public void refresh(boolean reset) {
    if (reset) UuGraph.clearInitialValues();
    var scanned = ProjectConfiguration.get().getSub("balance/uu-values/world scan");
    if (scanned == null) {
      scanned = new Config("uu scan values");
      try (var input =
          ProjectRuntime.class.getResourceAsStream("/assets/ic2/config/uu_scan_values.ini")) {
        if (input == null) throw new IllegalStateException("Missing packaged material seed values");
        scanned.load(input);
      } catch (java.io.IOException failed) {
        throw new IllegalStateException("Cannot read material seed values", failed);
      }
      ProjectRuntime.log.info(
          DiagnosticArea.Uu,
          "Loading packaged UU scan values; /ic2 uu-world-scan <small|medium|large> calibrates this"
              + " world.");
    }
    addSeeds(scanned);
    addSeeds(ProjectConfiguration.get().getSub("balance/uu-values/predefined"));
    UuGraph.build(reset);
  }

  private void addSeeds(Config config) {
    if (config == null) return;
    var values = config.valueIterator();
    while (values.hasNext()) {
      var entry = values.next();
      ItemStack stack;
      try {
        stack =
            NativeConfigValues.asStack(
                entry
                    .name
                    .replace("IC2:misc_resource#", "ic2:")
                    .replace("ic2:misc_resource#", "ic2:"));
      } catch (ParseException invalid) {
        throw new Config.ParseException("Invalid material seed key", entry, invalid);
      }
      double cost = entry.getDouble();
      if (!Double.isFinite(cost) || cost < 0)
        throw new Config.ParseException(
            "Material seed value must be finite and nonnegative", entry);
      if (stack == null || stack.isEmpty())
        ProjectRuntime.log.warn(
            DiagnosticArea.Uu, "Unknown material seed %s on line %d", entry.name, entry.getLine());
      else add(stack, cost);
    }
  }
}
