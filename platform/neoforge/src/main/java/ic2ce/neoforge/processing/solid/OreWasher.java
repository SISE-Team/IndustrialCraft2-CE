package ic2ce.neoforge.processing.solid;

import ic2ce.api.recipe.IRecipeInput;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.network.GuiSynced;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.FluidContainerInventory;
import ic2ce.neoforge.inventory.FluidWhitelistInput;
import ic2ce.neoforge.inventory.ItemRecipeInput;
import ic2ce.neoforge.inventory.OutputInventory;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Ore recipes reserve their exact water budget before consuming item inputs. */
@NotClassic
public class OreWasher extends PoweredProcessor<IRecipeInput, Collection<ItemStack>, ItemStack> {
  public final FluidContainerInventory fluidSlot;
  public final OutputInventory cellSlot;
  @GuiSynced protected final Ic2FluidTank fluidTank;
  protected final FluidPorts fluids;

  public OreWasher(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.ORE_WASHING_PLANT, position, state, 16, 500, 3);
    inputSlot = new ItemRecipeInput(this, "input", 1, Recipes.oreWashing);
    fluidSlot =
        new FluidWhitelistInput(this, "fluid", 1, net.minecraft.world.level.material.Fluids.WATER);
    cellSlot = new OutputInventory(this, "cell", 1);
    fluids = addComponent(new FluidPorts(this));
    fluidTank =
        fluids.addTankInsert(
            "fluid",
            8000,
            FluidPorts.fluidPredicate(net.minecraft.world.level.material.Fluids.WATER));
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (fluidTank.getFluidAmount() < fluidTank.getCapacity()) gainFluid();
  }

  private static int waterCost(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe) {
    var metadata = recipe.getRecipe().getMetaData();
    return metadata == null ? -1 : metadata.getIntOr("amount", 0);
  }

  @Override
  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getRecipeResult() {
    var recipe = super.getRecipeResult();
    if (recipe == null) return null;
    int required = waterCost(recipe);
    return required >= 0 && required <= fluidTank.getFluidAmount() ? recipe : null;
  }

  @Override
  public void operateOnce(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe,
      Collection<ItemStack> output) {
    int required = waterCost(recipe);
    if (required < 0 || required > fluidTank.getFluidAmount())
      throw new IllegalStateException("Ore washing water budget changed before commit");
    if (required > 0) fluidTank.drainMbUnchecked(required, false);
    super.operateOnce(recipe, output);
  }

  public boolean gainFluid() {
    return fluidSlot.processIntoTank(fluidTank, cellSlot);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.Processing,
        UpgradableProperty.Transformer,
        UpgradableProperty.EnergyStorage,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing,
        UpgradableProperty.FluidConsuming);
  }

  @Override
  protected boolean directRecipeOutputPreview() {
    return getClass() == OreWasher.class;
  }
}
