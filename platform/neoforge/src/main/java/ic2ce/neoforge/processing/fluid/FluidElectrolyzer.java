package ic2ce.neoforge.processing.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.IElectrolyzerRecipeManager;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.*;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.client.widget.SupplierGauge;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.ProcessingBlockEntities;
import ic2ce.neoforge.processing.menu.ElectrolysisMenu;
import ic2ce.neoforge.recipe.processing.ElectrolysisRecipes;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.storage.StationaryFluidStore;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Electrolysis owns metered electrical progress and commits all directional products together. */
public class FluidElectrolyzer extends PoweredBlockEntity
    implements IUpgradableBlock, IHasGui, SupplierGauge.IGaugeRatioProvider {
  protected int progress;
  protected IElectrolyzerRecipeManager.ElectrolyzerRecipe recipe;
  protected final Ic2FluidTank input;
  public final UpgradeInventory upgradeSlot;
  protected final FluidPorts fluids;
  private final BlockEntity[] destinations = new BlockEntity[5];
  private final Ic2FluidStack[] products = new Ic2FluidStack[5];
  private IElectrolyzerRecipeManager.ElectrolyzerRecipe preparedRecipe;
  private static final Set<UpgradableProperty> PROPERTIES =
      Set.of(UpgradableProperty.FluidConsuming);

  public FluidElectrolyzer(BlockPos position, BlockState state) {
    super(ProcessingBlockEntities.ELECTROLYZER, position, state, 32000, 2);
    fluids = addComponent(new FluidPorts(this));
    input = fluids.addTankInsert("input", 8000, FluidPorts.fluidPredicate(Recipes.electrolyzer));
    upgradeSlot = new UpgradeInventory(this, "upgradeSlot", 4);
  }

  public static void init() {
    Recipes.electrolyzer = new ElectrolysisRecipes();
    Recipes.electrolyzer.addRecipe(
        net.minecraft.world.level.material.Fluids.WATER,
        40,
        32,
        new IElectrolyzerRecipeManager.ElectrolyzerOutput(
            FluidDefinitions.HYDROGEN.still, 26, Direction.DOWN),
        new IElectrolyzerRecipeManager.ElectrolyzerOutput(
            FluidDefinitions.OXYGEN.still, 13, Direction.UP));
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    progress = Math.max(0, data.getIntOr("progress", 0));
    recipe = preparedRecipe = null;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("progress", progress);
  }

  protected boolean canOperate() {
    var selected =
        input.isEmpty()
            ? null
            : Recipes.electrolyzer.getElectrolysisInformation(input.getFluidStack().getFluid());
    if (selected != recipe) {
      if (recipe != null || selected == null) progress = 0;
      recipe = selected;
    }
    return recipe != null
        && input.getFluidAmount() >= recipe.inputAmount()
        && energy.getEnergy() >= recipe.EUaTick()
        && reserveProducts();
  }

  private boolean reserveProducts() {
    if (preparedRecipe != recipe) {
      for (int index = 0; index < recipe.outputs().size(); index++)
        products[index] = recipe.outputs().get(index).getOutput();
      preparedRecipe = recipe;
    }
    for (int index = 0; index < recipe.outputs().size(); index++) {
      var face = recipe.outputs().get(index).tankDirection();
      var position = worldPosition.relative(face);
      if (!getLevel().hasChunkAt(position)) return false;
      var destination = getLevel().getBlockEntity(position);
      if (!(destination instanceof StationaryFluidStore)) return false;
      for (int earlier = 0; earlier < index; earlier++)
        if (destinations[earlier] == destination) return false;
      if (LiquidUtil.fillTile(destination, face.getOpposite(), products[index], true)
          != products[index].getAmountMb()) return false;
      destinations[index] = destination;
    }
    return true;
  }

  protected void operate() {
    if (recipe == null || input.getFluidAmount() < recipe.inputAmount() || !reserveProducts())
      throw new IllegalStateException("Electrolysis inputs changed during commit");
    for (int index = 0; index < recipe.outputs().size(); index++) {
      var face = recipe.outputs().get(index).tankDirection();
      if (LiquidUtil.fillTile(destinations[index], face.getOpposite(), products[index], false)
          != products[index].getAmountMb())
        throw new IllegalStateException("Electrolysis outlet reservation changed during commit");
      destinations[index].setChanged();
    }
    input.drainMbUnchecked(recipe.inputAmount(), false);
  }

  @Override
  public void updateEntityServer() {
    super.updateEntityServer();
    int before = progress;
    boolean advanced = canOperate();
    if (advanced && energy.useEnergy(recipe.EUaTick())) {
      progress = Math.min(progress, recipe.ticksNeeded() - 1) + 1;
      if (progress == recipe.ticksNeeded()) {
        operate();
        progress = 0;
      }
    } else advanced = false;
    setActive(advanced);
    boolean changed = upgradeSlot.tickNoMark();
    if (advanced || changed || progress != before) super.setChanged();
  }

  protected boolean canFillTank(Direction face, Ic2FluidStack fluid) {
    var destination = getLevel().getBlockEntity(worldPosition.relative(face));
    return destination instanceof StationaryFluidStore
        && LiquidUtil.fillTile(destination, face.getOpposite(), fluid, true) == fluid.getAmountMb();
  }

  protected void fillTank(Direction face, Ic2FluidStack fluid) {
    var destination = getLevel().getBlockEntity(worldPosition.relative(face));
    if (destination instanceof StationaryFluidStore)
      LiquidUtil.fillTile(destination, face.getOpposite(), fluid, false);
  }

  public Ic2FluidTank getInput() {
    return input;
  }

  public boolean hasRecipe() {
    return recipe != null;
  }

  public IElectrolyzerRecipeManager.ElectrolyzerRecipe getCurrentRecipe() {
    return recipe;
  }

  @Override
  public double getRatio() {
    return recipe == null ? 0 : (double) progress / recipe.ticksNeeded();
  }

  @Override
  public double getEnergy() {
    return energy.getEnergy();
  }

  @Override
  public boolean useEnergy(double amount) {
    return energy.useEnergy(amount);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return PROPERTIES;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new ElectrolysisMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new ElectrolysisMenu(syncId, inventory, this);
  }
}
