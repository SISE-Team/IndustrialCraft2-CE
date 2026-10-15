package ic2ce.neoforge.processing.fluid;

import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.*;
import ic2ce.neoforge.agriculture.AgricultureItems;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.processing.ProcessingBlockEntities;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.processing.menu.FermentationMenu;
import ic2ce.neoforge.recipe.processing.FermentationRecipes;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.thermal.HeatSupply;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Heat-paid fermentation batches retain fertilizer credit until the item output can accept it. */
@NotClassic
public class BiomassFermenter extends InventoryBlockEntity
    implements IHasGui, ScreenValues, IUpgradableBlock {
  private final Ic2FluidTank inputTank, outputTank;
  public final FluidRecipeInput fluidInputCellInSlot;
  public final TankContainerInput fluidOutputCellInSlot;
  public final OutputInventory fluidInputCellOutSlot, fluidOutputCellOutSlot, fertiliserSlot;
  public final UpgradeInventory upgradeSlot;
  protected final FluidPorts fluids;
  private int heatBuffer;
  public int progress;
  private final int maxProgress =
      Math.max(
          1,
          NativeConfigValues.getInt(
              ProjectConfiguration.get(), "balance/fermenter/biomass_per_fertilizier"));
  private static final Set<UpgradableProperty> PROPERTIES =
      Set.of(
          UpgradableProperty.ItemConsuming,
          UpgradableProperty.ItemProducing,
          UpgradableProperty.FluidConsuming,
          UpgradableProperty.FluidProducing);

  public BiomassFermenter(BlockPos position, BlockState state) {
    super(ProcessingBlockEntities.FERMENTER, position, state);
    fluids = addComponent(new FluidPorts(this));
    outputTank = fluids.addTankExtract("output", 2000);
    inputTank = fluids.addTankInsert("input", 10000, FluidPorts.fluidPredicate(Recipes.fermenter));
    fluidInputCellOutSlot = new OutputInventory(this, "biomassOutput", 1);
    fluidOutputCellOutSlot = new OutputInventory(this, "biogassOutput", 1);
    fertiliserSlot = new OutputInventory(this, "output", 1);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 2);
    fluidOutputCellInSlot =
        new TankContainerInput(
            this,
            "biogasInput",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.BOTTOM,
            FluidContainerInventory.OpType.Fill,
            outputTank);
    fluidInputCellInSlot =
        new FluidRecipeInput(
            this,
            "biomassInput",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.TOP,
            FluidContainerInventory.OpType.Drain,
            Recipes.fermenter);
  }

  public static void init() {
    Recipes.fermenter = new FermentationRecipes();
    Recipes.fermenter.addRecipe(
        FluidDefinitions.BIOMASS.still,
        NativeConfigValues.getInt(
            ProjectConfiguration.get(), "balance/fermenter/need_amount_biomass_per_run"),
        NativeConfigValues.getInt(ProjectConfiguration.get(), "balance/fermenter/hU_per_run"),
        FluidDefinitions.BIOGAS.still,
        NativeConfigValues.getInt(
            ProjectConfiguration.get(), "balance/fermenter/output_amount_biogas_per_run"));
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    if (data.contains("inputTank")) inputTank.fromNbt(data.getCompoundOrEmpty("inputTank"));
    if (data.contains("outputTank")) outputTank.fromNbt(data.getCompoundOrEmpty("outputTank"));
    progress = Math.max(0, data.getIntOr("progress", 0));
    heatBuffer = Math.max(0, data.getIntOr("heatBuffer", 0));
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.remove("inputTank");
    data.remove("outputTank");
    data.putInt("progress", progress);
    data.putInt("heatBuffer", heatBuffer);
  }

  private boolean exportFertilizer() {
    var fertilizer = new ItemStack(AgricultureItems.FERTILIZER);
    while (progress >= maxProgress) {
      if (!fertiliserSlot.canAdd(fertilizer)) return false;
      fertiliserSlot.add(fertilizer);
      progress -= maxProgress;
    }
    return true;
  }

  private boolean ferment() {
    if (!exportFertilizer() || inputTank.isEmpty()) return false;
    var recipe = Recipes.fermenter.getFermentationInformation(inputTank.getFluidStack().getFluid());
    if (recipe == null
        || recipe.inputAmount <= 0
        || recipe.outputAmount <= 0
        || recipe.output == null
        || recipe.heat <= 0
        || inputTank.getFluidAmount() < recipe.inputAmount) return false;
    var output = recipe.getOutput();
    if (outputTank.fillMbUnchecked(output, true) != recipe.outputAmount) return false;
    int drawn =
        HeatSupply.fromSide(
            getLevel(),
            worldPosition,
            getFacing(),
            Math.min(100, Math.max(0, recipe.heat - heatBuffer)));
    heatBuffer += drawn;
    if (heatBuffer < recipe.heat) return drawn > 0;
    inputTank.drainMbUnchecked(recipe.inputAmount, false);
    outputTank.fillMbUnchecked(output, false);
    heatBuffer -= recipe.heat;
    progress = (int) Math.min(Integer.MAX_VALUE, (long) progress + recipe.inputAmount);
    exportFertilizer();
    return true;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    fluidInputCellInSlot.processIntoTank(inputTank, fluidInputCellOutSlot);
    fluidOutputCellInSlot.processFromTank(outputTank, fluidOutputCellOutSlot);
    int previousProgress = progress, previousHeat = heatBuffer;
    boolean worked = ferment();
    setActive(worked);
    boolean upgraded = upgradeSlot.tickNoMark();
    if (worked || upgraded || progress != previousProgress || heatBuffer != previousHeat)
      setChanged();
  }

  public Ic2FluidTank getInputTank() {
    return inputTank;
  }

  public Ic2FluidTank getOutputTank() {
    return outputTank;
  }

  public int gaugeLiquidScaled(int width, int index) {
    var tank = index == 0 ? inputTank : index == 1 ? outputTank : null;
    return tank == null
        ? 0
        : (int) ((long) Math.max(0, width) * tank.getFluidAmount() / tank.getCapacity());
  }

  @Override
  public double getGuiValue(String key) {
    if (key.equals("progress")) return (double) progress / maxProgress;
    if (!key.equals("heat")) throw new IllegalArgumentException("Unknown fermenter gauge: " + key);
    var recipe =
        inputTank.isEmpty()
            ? null
            : Recipes.fermenter.getFermentationInformation(inputTank.getFluidStack().getFluid());
    int capacity =
        recipe == null
            ? NativeConfigValues.getInt(ProjectConfiguration.get(), "balance/fermenter/hU_per_run")
            : recipe.heat;
    return capacity <= 0 ? 0 : (double) heatBuffer / capacity;
  }

  @Override
  public double getEnergy() {
    return 40;
  }

  @Override
  public boolean useEnergy(double amount) {
    return Double.isFinite(amount) && amount >= 0 && amount <= 40;
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return PROPERTIES;
  }

  @Override
  public OwnedMenu<BiomassFermenter> createServerScreenHandler(int syncId, Player player) {
    return new FermentationMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new FermentationMenu(syncId, inventory, this);
  }
}
