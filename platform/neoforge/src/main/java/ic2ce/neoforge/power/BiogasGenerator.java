package ic2ce.neoforge.power;

import ic2ce.api.recipe.Recipes;
import ic2ce.core.SemiFluidFuelManager;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.core.network.GuiSynced;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.FluidContainerInventory;
import ic2ce.neoforge.inventory.FluidRecipeInput;
import ic2ce.neoforge.inventory.OutputInventory;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/** Semi-fluid fuel is consumed in recipe batches whose tick duration and power remain distinct. */
@NotClassic
public class BiogasGenerator extends BufferedGenerator {
  public final FluidContainerInventory fluidSlot;
  public final OutputInventory outputSlot;
  @GuiSynced protected final Ic2FluidTank fluidTank;
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));

  public BiogasGenerator(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.SEMIFLUID_GENERATOR, position, block, 32, 1, 32000);
    fluidTank =
        fluids.addTankInsert("fluid", 10000, FluidPorts.fluidPredicate(Recipes.semiFluidGenerator));
    fluidSlot = new FluidRecipeInput(this, "fluidSlot", 1, Recipes.semiFluidGenerator);
    outputSlot = new OutputInventory(this, "output", 1);
  }

  public static void init() {
    Recipes.semiFluidGenerator = new SemiFluidFuelManager();
    double factor =
        NativeConfigValues.getDouble(
            ProjectConfiguration.get(), "balance/energy/generator/semiFluidBiogas");
    if (Double.isFinite(factor) && factor > 0)
      addFuel(
          FluidDefinitions.BIOGAS.still,
          10,
          (int) Math.clamp(Math.round(16 * factor), 1, Integer.MAX_VALUE));
  }

  public static void addFuel(Fluid fluid, int amount, int eu) {
    Recipes.semiFluidGenerator.addFluid(fluid, amount, eu);
  }

  @Override
  public void updateEntityServer() {
    super.updateEntityServer();
    if (fluidSlot.processIntoTank(fluidTank, outputSlot)) setChanged();
  }

  @Override
  public boolean gainFuel() {
    var contents = fluidTank.getFluidStack();
    if (contents == null || contents.isEmpty()) return false;
    var burn = Recipes.semiFluidGenerator.getBurnProperty(contents.getFluid());
    if (burn == null || burn.amount <= 0 || burn.power <= 0 || contents.getAmountMb() < burn.amount)
      return false;
    fluidTank.drainMbUnchecked(burn.amount, false);
    production = burn.power;
    fuel = burn.amount;
    return true;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.GENERATOR_GEOTHERMAL_LOOP;
  }
}
