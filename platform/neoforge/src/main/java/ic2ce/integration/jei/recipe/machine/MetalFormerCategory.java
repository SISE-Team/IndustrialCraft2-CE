package ic2ce.integration.jei.recipe.machine;

import ic2ce.neoforge.cable.CableItems;
import ic2ce.neoforge.item.tool.ToolItems;
import ic2ce.neoforge.processing.ProcessingBlocks;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/** Each distinct metal-forming recipe category owns its mode-specific native icon. */
public final class MetalFormerCategory extends DynamicCategory {
  private final ItemStack icon;

  public MetalFormerCategory(RecipeType<IORecipeWrapper> type, int mode, IGuiHelper graphics) {
    super(ProcessingBlocks.METAL_FORMER, type, graphics);
    icon =
        new ItemStack(
            switch (mode) {
              case 0 -> CableItems.COPPER_CABLE;
              case 1 -> ToolItems.FORGE_HAMMER;
              case 2 -> ToolItems.CUTTER;
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
