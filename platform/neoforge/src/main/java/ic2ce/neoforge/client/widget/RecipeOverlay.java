package ic2ce.neoforge.client.widget;

import com.google.common.base.Function;
import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Recipe overlays bind a stable category snapshot and remain transparent. */
public class RecipeOverlay extends ActionButton<RecipeOverlay> {
  public static Function<String[], ClickAction> jeiRecipeListOpener;

  public static boolean canUse() {
    return jeiRecipeListOpener != null;
  }

  public RecipeOverlay(ScreenWidget<?> wrapped, String[] categories) {
    this(wrapped.gui, wrapped.x, wrapped.y, wrapped.width, wrapped.height, categories);
  }

  public RecipeOverlay(
      MachineScreen<?> gui, int x, int y, int width, int height, String[] categories) {
    super(gui, x, y, width, height, opener(categories));
  }

  private static ClickAction opener(String[] categories) {
    var open = Objects.requireNonNull(jeiRecipeListOpener, "Recipe integration unavailable");
    return Objects.requireNonNull(open.apply(categories.clone()));
  }

  @Override
  protected List<Component> getToolTip() {
    return List.of(Component.translatable("ic2.jei.recipes"));
  }

  @Override
  @SkippedMethod
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {}
}
