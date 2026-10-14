package ic2ce.core.uu;

import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.List;
import net.minecraft.world.item.Item;

/**
 * Spent reactor material derives from its actual full rod at zero additional manufacturing cost.
 */
public final class ManualRecipeResolver implements IRecipeResolver {
  private record Rod(Item fresh, Item spent) {}

  private static final class Definitions {
    static final List<Rod> RODS =
        List.of(
            new Rod(ItemDefinitions.URANIUM_FUEL_ROD, ItemDefinitions.DEPLETED_URANIUM_FUEL_ROD),
            new Rod(
                ItemDefinitions.DUAL_URANIUM_FUEL_ROD,
                ItemDefinitions.DEPLETED_DUAL_URANIUM_FUEL_ROD),
            new Rod(
                ItemDefinitions.QUAD_URANIUM_FUEL_ROD,
                ItemDefinitions.DEPLETED_QUAD_URANIUM_FUEL_ROD),
            new Rod(ItemDefinitions.MOX_FUEL_ROD, ItemDefinitions.DEPLETED_MOX_FUEL_ROD),
            new Rod(ItemDefinitions.DUAL_MOX_FUEL_ROD, ItemDefinitions.DEPLETED_DUAL_MOX_FUEL_ROD),
            new Rod(ItemDefinitions.QUAD_MOX_FUEL_ROD, ItemDefinitions.DEPLETED_QUAD_MOX_FUEL_ROD));
  }

  @Override
  public List<RecipeTransformation> getTransformations() {
    return Definitions.RODS.stream()
        .map(
            rod ->
                new RecipeTransformation(
                    0,
                    List.of(List.of(new LeanItemStack(rod.fresh()))),
                    new LeanItemStack(rod.spent())))
        .toList();
  }
}
