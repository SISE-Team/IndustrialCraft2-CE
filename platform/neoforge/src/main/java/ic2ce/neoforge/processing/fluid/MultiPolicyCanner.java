package ic2ce.neoforge.processing.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.recipe.IEmptyFluidContainerRecipeManager;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.api.util.FluidContainerOutputMode;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.util.LiquidUtil;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.CanningContainerInput;
import ic2ce.neoforge.inventory.CanningInput;
import ic2ce.neoforge.inventory.FluidContainerInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.processing.menu.CanningMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Four canning protocols with complete fluid/container plans before consuming recipe inputs. */
public class MultiPolicyCanner extends PoweredProcessor<Object, Object, Object>
    implements INetworkClientTileEntityEventListener {
  private Mode mode = Mode.BottleSolid;
  public static final int eventSetModeBase = 0, eventSwapTanks = Mode.values.length + 1;
  public final Ic2FluidTank inputTank, outputTank;
  public final CanningContainerInput canInputSlot;
  protected final FluidPorts fluids;

  private record Enrichment(
      ItemStack remainingContainers, List<ItemStack> filled, Ic2FluidStack remainder) {}

  public MultiPolicyCanner(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.CANNER, position, state, 4, 200, 1);
    inputSlot = new CanningInput(this, "input", 1);
    canInputSlot = new CanningContainerInput(this, "canInput", 1);
    fluids = addComponent(new FluidPorts(this));
    inputTank = fluids.addTankInsert("inputTank", 8000);
    outputTank = fluids.addTankExtract("outputTank", 8000);
    canInputSlot.setOpType(FluidContainerInventory.OpType.None);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    int saved = data.getIntOr("mode", 0);
    mode = saved >= 0 && saved < Mode.values.length ? Mode.values[saved] : Mode.BottleSolid;
    configureContainerOperation();
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("mode", mode.ordinal());
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode selected) {
    Objects.requireNonNull(selected);
    if (selected != mode) {
      mode = selected;
      invalidateOperation();
    }
    configureContainerOperation();
    if (ProjectRuntime.sideProxy.isRendering()) stopLoopingSound();
  }

  private void configureContainerOperation() {
    canInputSlot.setOpType(
        switch (mode) {
          case BottleSolid -> FluidContainerInventory.OpType.None;
          case BottleLiquid -> FluidContainerInventory.OpType.Fill;
          case EmptyLiquid -> FluidContainerInventory.OpType.Drain;
          case EnrichLiquid -> FluidContainerInventory.OpType.Both;
        });
  }

  private void invalidateOperation() {
    progress = 0;
    guiProgress = 0;
    recipeResult = null;
    processResult = null;
    setChanged();
  }

  public Ic2FluidTank getInputTank() {
    return inputTank;
  }

  public Ic2FluidTank getOutputTank() {
    return outputTank;
  }

  @Override
  protected Collection<ItemStack> getOutput(Object result) {
    return switch (result) {
      case ItemStack item -> List.of(item.copy());
      case Ic2FluidStack _ -> List.of();
      case IEmptyFluidContainerRecipeManager.Output empty -> empty.container;
      default -> super.getOutput(result);
    };
  }

  private Enrichment planEnrichment(Ic2FluidStack produced) {
    var fluid = produced.copy();
    var containers = canInputSlot.get().copy();
    var filled = new ArrayList<ItemStack>();
    while (!containers.isEmpty() && !fluid.isEmpty()) {
      var operation =
          LiquidUtil.fillContainer(containers, fluid, FluidContainerOutputMode.EmptyFullToOutput);
      if (operation == null || operation.fluidChange.isEmpty()) break;
      int used = operation.fluidChange.getAmountMb();
      if (used > fluid.getAmountMb() || !fluid.hasExactFluid(operation.fluidChange))
        throw new IllegalStateException("Container fill violated enrichment fluid conservation");
      if (operation.extraOutput != null && !operation.extraOutput.isEmpty()) {
        filled.add(operation.extraOutput);
        if (!outputSlot.canAdd(filled)) {
          filled.removeLast();
          break;
        }
      }
      containers = operation.inPlaceOutput == null ? ItemStack.EMPTY : operation.inPlaceOutput;
      fluid.decreaseMb(used);
    }
    return outputTank.fillMbUnchecked(fluid, true) == fluid.getAmountMb()
        ? new Enrichment(containers, filled, fluid)
        : null;
  }

  @Override
  public MachineRecipeResult<Object, Object, Object> getRecipeResult() {
    boolean additiveMode = mode == Mode.BottleSolid || mode == Mode.EnrichLiquid;
    if (additiveMode ? inputSlot.isEmpty() : canInputSlot.isEmpty()) return null;
    var result = inputSlot.process();
    if (result == null || !outputSlot.canAdd(getOutput(result.getOutput()))) return null;
    switch (mode) {
      case EmptyLiquid -> {
        var output = (IEmptyFluidContainerRecipeManager.Output) result.getOutput();
        if (outputTank.fillMbUnchecked(output.fluid, true) != output.fluid.getAmountMb())
          return null;
      }
      case EnrichLiquid -> {
        if (planEnrichment((Ic2FluidStack) result.getOutput()) == null) return null;
      }
      default -> {}
    }
    return result;
  }

  @Override
  public void operateOnce(
      MachineRecipeResult<Object, Object, Object> recipe, Collection<ItemStack> output) {
    Enrichment plan =
        mode == Mode.EnrichLiquid ? planEnrichment((Ic2FluidStack) recipe.getOutput()) : null;
    if (mode == Mode.EnrichLiquid && plan == null)
      throw new IllegalStateException("Enrichment capacity changed before commit");
    super.operateOnce(recipe, output);
    switch (mode) {
      case EmptyLiquid -> {
        var fluid = ((IEmptyFluidContainerRecipeManager.Output) recipe.getOutput()).fluid;
        if (outputTank.fillMbUnchecked(fluid, false) != fluid.getAmountMb())
          throw new IllegalStateException("Canner output overflow");
      }
      case EnrichLiquid -> {
        canInputSlot.put(plan.remainingContainers());
        if (outputSlot.add(plan.filled()) != 0)
          throw new IllegalStateException("Filled container output overflow");
        if (outputTank.fillMbUnchecked(plan.remainder(), false) != plan.remainder().getAmountMb())
          throw new IllegalStateException("Enriched fluid output overflow");
      }
      default -> {}
    }
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = new ArrayList<>(super.getNetworkedFields());
    fields.add("canInputSlot");
    return fields;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return switch (mode) {
      case BottleSolid, BottleLiquid -> SoundDefinitions.MACHINE_CANNER_OPERATE;
      case EmptyLiquid -> SoundDefinitions.MACHINE_CANNER_REVERSE;
      case EnrichLiquid -> null;
    };
  }

  @Override
  public SoundEvent getInterruptSoundEvent() {
    return mode == Mode.EnrichLiquid ? null : SoundDefinitions.MACHINE_INTERRUPT1;
  }

  @Override
  public OwnedMenu<MultiPolicyCanner> createServerScreenHandler(int id, Player player) {
    return new CanningMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new CanningMenu(id, inventory, this);
  }

  @Override
  public void onNetworkUpdate(String field) {
    super.onNetworkUpdate(field);
    if (field.equals("mode")) setMode(mode);
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event >= eventSetModeBase && event < eventSetModeBase + Mode.values.length)
      setMode(Mode.values[event - eventSetModeBase]);
    else if (event == eventSwapTanks) switchTanks();
  }

  private boolean switchTanks() {
    if (progress != 0) return false;
    var input = inputTank.getFluidStack();
    inputTank.setFluidStack(outputTank.getFluidStack());
    outputTank.setFluidStack(input);
    invalidateOperation();
    return true;
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.Processing,
        UpgradableProperty.Transformer,
        UpgradableProperty.EnergyStorage,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing,
        UpgradableProperty.FluidConsuming,
        UpgradableProperty.FluidProducing);
  }

  public enum Mode {
    BottleSolid,
    EmptyLiquid,
    BottleLiquid,
    EnrichLiquid;
    public static final Mode[] values = values();
  }
}
