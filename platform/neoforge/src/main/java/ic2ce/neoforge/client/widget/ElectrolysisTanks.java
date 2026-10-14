package ic2ce.neoforge.client.widget;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.IElectrolyzerRecipeManager.ElectrolyzerOutput;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.processing.client.ElectrolysisScreen;
import ic2ce.neoforge.processing.fluid.FluidElectrolyzer;
import java.util.List;
import net.minecraft.network.chat.Component;

/** Centered product slots cover every immutable one-to-five-outlet recipe. */
public class ElectrolysisTanks extends FluidWidget {
  private final FluidElectrolyzer electrolyzer;
  private final int id;
  private ElectrolyzerOutput output;
  private Ic2FluidStack contents;

  public ElectrolysisTanks(ElectrolysisScreen screen, int x, int y, int id) {
    super(screen, x, y, 18, 18);
    if (id < 0 || id > 4)
      throw new IllegalArgumentException("Electrolysis slot outside five outlets");
    electrolyzer = screen.getContainer().base;
    this.id = id;
  }

  public static int productIndex(int count, int slot) {
    return switch (count) {
      case 1 -> slot == 2 ? 0 : -1;
      case 2 -> slot == 1 ? 0 : slot == 3 ? 1 : -1;
      case 3 -> slot >= 1 && slot <= 3 ? slot - 1 : -1;
      case 4 -> slot == 0 ? 0 : slot == 1 ? 1 : slot == 3 ? 2 : slot == 4 ? 3 : -1;
      case 5 -> slot >= 0 && slot < 5 ? slot : -1;
      default -> -1;
    };
  }

  public boolean isActive() {
    return output != null;
  }

  @Override
  public void tick() {
    var recipe = electrolyzer.getCurrentRecipe();
    int index = recipe == null ? -1 : productIndex(recipe.outputs().size(), id);
    var selected = index < 0 ? null : recipe.outputs().get(index);
    if (selected != output) {
      output = selected;
      contents = output == null ? null : output.getOutput();
    }
  }

  @Override
  protected Ic2FluidStack getFluidStack() {
    return contents;
  }

  @Override
  protected List<Component> getToolTip() {
    var lines = super.getToolTip();
    if (output != null) {
      String side =
          output.tankDirection().getSerializedName().replace("up", "top").replace("down", "bottom");
      lines.add(
          Component.translatable(
              "ic2.fluid.output_side", LocalizedText.translate("ic2.dir." + side)));
    }
    return lines;
  }
}
