package ic2ce.neoforge.processing.uu;

import ic2ce.neoforge.item.consumable.ConsumableItems;
import ic2ce.neoforge.reactor.ReactorItems;
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
            new Rod(ReactorItems.URANIUM_FUEL_ROD, ConsumableItems.DEPLETED_URANIUM_FUEL_ROD),
            new Rod(
                ReactorItems.DUAL_URANIUM_FUEL_ROD,
                ConsumableItems.DEPLETED_DUAL_URANIUM_FUEL_ROD),
            new Rod(
                ReactorItems.QUAD_URANIUM_FUEL_ROD,
                ConsumableItems.DEPLETED_QUAD_URANIUM_FUEL_ROD),
            new Rod(ReactorItems.MOX_FUEL_ROD, ConsumableItems.DEPLETED_MOX_FUEL_ROD),
            new Rod(ReactorItems.DUAL_MOX_FUEL_ROD, ConsumableItems.DEPLETED_DUAL_MOX_FUEL_ROD),
            new Rod(ReactorItems.QUAD_MOX_FUEL_ROD, ConsumableItems.DEPLETED_QUAD_MOX_FUEL_ROD));
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
