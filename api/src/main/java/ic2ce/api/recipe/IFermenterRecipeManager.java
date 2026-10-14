package ic2ce.api.recipe;

import ic2ce.api.fluid.Ic2FluidStack;
import java.util.Map;
import net.minecraft.world.level.material.Fluid;

public interface IFermenterRecipeManager extends ILiquidAcceptManager {
  void addRecipe(Fluid var1, int var2, int var3, Fluid var4, int var5);

  IFermenterRecipeManager.FermentationProperty getFermentationInformation(Fluid var1);

  Ic2FluidStack getOutput(Fluid var1);

  Map<Fluid, IFermenterRecipeManager.FermentationProperty> getRecipeMap();

  public static final class FermentationProperty {
    public final int inputAmount;
    public final int heat;
    public final Fluid output;
    public final int outputAmount;

    public FermentationProperty(int inputAmount, int heat, Fluid output, int outputAmount) {
      if (inputAmount <= 0
          || heat <= 0
          || outputAmount <= 0
          || output == null
          || output == net.minecraft.world.level.material.Fluids.EMPTY)
        throw new IllegalArgumentException(
            "Fermentation requires positive heat and physical fluid quantities");
      this.inputAmount = inputAmount;
      this.heat = heat;
      this.output = output;
      this.outputAmount = outputAmount;
    }

    public Ic2FluidStack getOutput() {
      return this.output == null ? null : Ic2FluidStack.create(this.output, this.outputAmount);
    }
  }
}
