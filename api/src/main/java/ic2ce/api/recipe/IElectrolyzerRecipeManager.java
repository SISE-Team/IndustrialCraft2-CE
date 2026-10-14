package ic2ce.api.recipe;

import ic2ce.api.fluid.Ic2FluidStack;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;

/** Immutable electrolysis specifications; one distinct native outlet is required per product. */
public interface IElectrolyzerRecipeManager extends ILiquidAcceptManager {
  void addRecipe(Fluid input, int inputAmount, int euPerTick, ElectrolyzerOutput... outputs);

  void addRecipe(
      Fluid input, int inputAmount, int euPerTick, int duration, ElectrolyzerOutput... outputs);

  ElectrolyzerRecipe getElectrolysisInformation(Fluid input);

  ElectrolyzerOutput[] getOutput(Fluid input);

  Map<Fluid, ElectrolyzerRecipe> getRecipeMap();

  record ElectrolyzerOutput(Fluid fluid, int fluidAmount, Direction tankDirection) {
    public ElectrolyzerOutput {
      Objects.requireNonNull(fluid);
      Objects.requireNonNull(tankDirection);
      if (fluidAmount <= 0) throw new IllegalArgumentException("Nonpositive electrolysis output");
    }

    public Ic2FluidStack getOutput() {
      return Ic2FluidStack.create(fluid, fluidAmount);
    }
  }

  record ElectrolyzerRecipe(
      int inputAmount, int EUaTick, int ticksNeeded, List<ElectrolyzerOutput> outputs) {
    public ElectrolyzerRecipe(int input, int eu, int duration, ElectrolyzerOutput... targets) {
      this(input, eu, duration, Arrays.asList(targets));
    }

    public ElectrolyzerRecipe {
      if (inputAmount <= 0 || EUaTick < 0 || ticksNeeded <= 0)
        throw new IllegalArgumentException("Invalid electrolysis quantity, power or duration");
      outputs = List.copyOf(outputs);
      if (outputs.isEmpty() || outputs.size() > 5)
        throw new IllegalArgumentException("Electrolysis requires one to five outlets");
      int directions = 0;
      for (var output : outputs) {
        int bit = 1 << output.tankDirection().ordinal();
        if ((directions & bit) != 0)
          throw new IllegalArgumentException("Duplicate electrolysis outlet");
        directions |= bit;
      }
    }
  }
}
