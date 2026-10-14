package ic2ce.integration.jei.recipe.machine;

import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/** Each distinct metal-forming recipe category owns its mode-specific native icon. */
public final class MetalFormerCategory extends DynamicCategory {
  private final ItemStack icon;

  public MetalFormerCategory(RecipeType<IORecipeWrapper> type, int mode, IGuiHelper graphics) {
    super(BlockDefinitions.METAL_FORMER, type, graphics);
    icon =
        new ItemStack(
            switch (mode) {
              case 0 -> ItemDefinitions.COPPER_CABLE;
              case 1 -> ItemDefinitions.FORGE_HAMMER;
              case 2 -> ItemDefinitions.CUTTER;
              default -> throw new IllegalArgumentException("Unknown metal forming mode");
            });
  }

  @Override
  public void draw(
      IORecipeWrapper recipe,
      IRecipeSlotsView slots,
      GuiGraphicsExtractor graphics,
      double x,
      double y) {
    super.draw(recipe, slots, graphics, x, y);
    graphics.item(icon, 70 + xOffset, 35 + yOffset);
  }
}
