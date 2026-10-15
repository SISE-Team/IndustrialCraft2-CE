package ic2ce.neoforge.processing.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.*;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.api.util.FluidContainerOutputMode;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.processing.menu.FluidPackagingMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Distinct draining/filling plans bill their real container and exact fluid components only after
 * physical capacity admission.
 */
@NotClassic
public class LiquidPackager extends PoweredProcessor<Void, Object, Object> {
  public final FluidContainerInventory drainInputSlot, fillInputSlot;
  @GuiSynced public final Ic2FluidTank fluidTank;
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));
  private Plan selected;
  private Work work;

  private record Work(boolean draining, ItemStack prototype, Ic2FluidStack moved) {}

  private record Plan(
      MachineRecipeResult<Void, Object, Object> result,
      boolean draining,
      ItemStack before,
      ItemStack after,
      Ic2FluidStack fluidBefore,
      Ic2FluidStack moved) {}

  private static final Set<UpgradableProperty> UPGRADES =
      Collections.unmodifiableSet(
          EnumSet.of(
              UpgradableProperty.Processing,
              UpgradableProperty.Transformer,
              UpgradableProperty.EnergyStorage,
              UpgradableProperty.ItemConsuming,
              UpgradableProperty.ItemProducing,
              UpgradableProperty.FluidConsuming,
              UpgradableProperty.FluidProducing));

  public LiquidPackager(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.FLUID_BOTTLER, position, state, 2, 100, 1);
    fluidTank = fluids.addTank("fluidTank", 8000);
    drainInputSlot =
        new TankContainerInput(
            this,
            "drainInput",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.TOP,
            FluidContainerInventory.OpType.Drain,
            fluidTank);
    fillInputSlot =
        new TankContainerInput(
            this,
            "fillInput",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.BOTTOM,
            FluidContainerInventory.OpType.Fill,
            fluidTank);
  }

  private static Ic2FluidStack snapshot(Ic2FluidStack fluid) {
    return fluid == null || fluid.isEmpty() ? null : fluid.copy();
  }

  private static boolean sameFluid(Ic2FluidStack first, Ic2FluidStack second) {
    return first == null
        ? second == null || second.isEmpty()
        : second != null
            && first.getAmountMb() == second.getAmountMb()
            && first.hasExactFluid(second);
  }

  private static boolean sameStack(ItemStack first, ItemStack second) {
    return first.isEmpty()
        ? second.isEmpty()
        : first.getCount() == second.getCount()
            && ItemStack.isSameItemSameComponents(first, second);
  }

  private MachineRecipeResult<Void, Object, Object> choose(Plan plan) {
    if (work != null
        && (work.draining != plan.draining
            || !ItemStack.isSameItemSameComponents(work.prototype, plan.before)
            || !sameFluid(work.moved, plan.moved))) {
      progress = 0;
      guiProgress = 0;
    }
    work = new Work(plan.draining, plan.before.copyWithCount(1), plan.moved.copy());
    selected = plan;
    return plan.result;
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    selected = null;
    work = null;
    var saved = data.getCompoundOrEmpty("PackagingWork");
    var prototype = StackTag.parseHolder(saved.getCompoundOrEmpty("container"));
    var moved = Ic2FluidStack.read(saved.getCompoundOrEmpty("fluid"));
    if (!prototype.isEmpty() && moved != null && !moved.isEmpty())
      work =
          new Work(saved.getBooleanOr("draining", false), prototype.copyWithCount(1), moved.copy());
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    if (work == null) {
      data.remove("PackagingWork");
      return;
    }
    var saved = new CompoundTag();
    saved.putBoolean("draining", work.draining);
    saved.put(
        "container",
        StackTag.save(
            ic2ce.neoforge.runtime.ProjectRuntime.envProxy.getRegistryLookup(),
            work.prototype,
            new CompoundTag()));
    var fluid = new CompoundTag();
    work.moved.toNbt(fluid);
    saved.put("fluid", fluid);
    data.put("PackagingWork", saved);
  }

  @SuppressWarnings("unchecked")
  private static MachineRecipeResult<Void, Object, Object> erase(
      MachineRecipeResult<?, ?, ?> result) {
    return (MachineRecipeResult<Void, Object, Object>) result;
  }

  @Override
  protected Collection<ItemStack> getOutput(Object output) {
    return output instanceof IEmptyFluidContainerRecipeManager.Output drained
        ? StackUtil.copy(drained.container)
        : super.getOutput(output);
  }

  @Override
  public MachineRecipeResult<Void, Object, Object> getRecipeResult() {
    selected = null;
    processResult = null;
    var previous = snapshot(fluidTank.getFluidStack());
    if (!drainInputSlot.isEmpty()) {
      var before = drainInputSlot.get().copy();
      var result =
          Recipes.emptyFluidContainer.apply(
              before.copy(),
              previous == null ? null : previous.getFluid(),
              FluidContainerOutputMode.EmptyFullToOutput,
              false);
      if (result != null) {
        var moved = result.getOutput().fluid;
        var output = result.getOutput().container;
        if (moved != null
            && !moved.isEmpty()
            && fluidTank.fillMbUnchecked(moved, true) == moved.getAmountMb()
            && outputSlot.canAdd(output)) {
          var erased = erase(result);
          return choose(
              new Plan(
                  erased, true, before, result.getAdjustedInput().copy(), previous, moved.copy()));
        }
      }
    }
    if (previous == null || fillInputSlot.isEmpty()) return null;
    var before = fillInputSlot.get().copy();
    var result =
        Recipes.fillFluidContainer.apply(
            new IFillFluidContainerRecipeManager.Input(before.copy(), previous),
            FluidContainerOutputMode.EmptyFullToOutput,
            false);
    if (result == null || !outputSlot.canAdd(result.getOutput())) return null;
    var remainder = result.getAdjustedInput();
    int remaining = remainder.fluid == null ? 0 : remainder.fluid.getAmountMb();
    if (remaining < 0
        || remaining >= previous.getAmountMb()
        || remaining > 0 && !previous.hasExactFluid(remainder.fluid)) return null;
    var moved = previous.copyWithAmountMb(previous.getAmountMb() - remaining);
    if (fluidTank.drainMbUnchecked(moved, true) != moved.getAmountMb()) return null;
    var erased = erase(result);
    return choose(new Plan(erased, false, before, remainder.container.copy(), previous, moved));
  }

  @Override
  public void operateOnce(
      MachineRecipeResult<Void, Object, Object> result, Collection<ItemStack> output) {
    var plan = selected;
    var input = plan != null && plan.draining ? drainInputSlot : fillInputSlot;
    if (plan == null
        || plan.result != result
        || !sameStack(plan.before, input.get())
        || !sameFluid(plan.fluidBefore, fluidTank.getFluidStack())
        || !outputSlot.canAdd(output))
      throw new IllegalStateException("Fluid packaging reservation changed before commit");
    int bill = plan.moved.getAmountMb();
    int accepted =
        plan.draining
            ? fluidTank.fillMbUnchecked(plan.moved, true)
            : fluidTank.drainMbUnchecked(plan.moved, true);
    if (accepted != bill)
      throw new IllegalStateException("Fluid packaging bill is no longer admitted");
    input.put(plan.after.copy());
    int paid =
        plan.draining
            ? fluidTank.fillMbUnchecked(plan.moved, false)
            : fluidTank.drainMbUnchecked(plan.moved, false);
    if (paid != bill || outputSlot.add(output) != 0)
      throw new IllegalStateException(
          "Fluid packaging commit diverged from its actual reservation");
    selected = null;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player actor) {
    return new FluidPackagingMenu(id, actor.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new FluidPackagingMenu(id, inventory, this);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return UPGRADES;
  }
}
