package ic2ce.neoforge.kinetic;

import ic2ce.api.energy.tile.IHeatSource;
import ic2ce.api.energy.tile.IKineticSource;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.kinetic.menu.StirlingEngineMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cogeneration reserves cooling capacity before splitting four heat units into twelve KU and one
 * coolant heat unit.
 */
@NotClassic
public class HeatDrivenCogenerator extends InventoryBlockEntity
    implements IKineticSource, IUpgradableBlock, IHasGui {
  public final Ic2FluidTank inputTank, outputTank;
  public final OutputInventory hotoutputSlot, cooloutputSlot;
  public final TankContainerInput hotfluidinputSlot;
  public final FluidRecipeInput coolfluidinputSlot;
  public final UpgradeInventory upgradeSlot;
  private int heatbuffer, kUBuffer;
  private long liquidHeatStored;
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));
  private static final int HEAT_CAPACITY = 1000, KINETIC_CAPACITY = 2000;
  private static final Direction[] SIDES = Direction.values();

  public HeatDrivenCogenerator(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.STIRLING_KINETIC_GENERATOR, position, block);
    var liquids = Recipes.liquidHeatupManager.getSingleDirectionLiquidManager();
    inputTank = fluids.addTankInsert("inputTank", 2000, FluidPorts.fluidPredicate(liquids));
    outputTank = fluids.addTankExtract("outputTank", 2000);
    hotoutputSlot = new OutputInventory(this, "hotOutputSlot", 1);
    cooloutputSlot = new OutputInventory(this, "outputSlot", 1);
    coolfluidinputSlot =
        new FluidRecipeInput(
            this,
            "coolfluidinputSlot",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.TOP,
            FluidContainerInventory.OpType.Drain,
            liquids);
    hotfluidinputSlot =
        new TankContainerInput(
            this,
            "hotfluidoutputSlot",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.BOTTOM,
            FluidContainerInventory.OpType.Fill,
            outputTank);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 3);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean changed = coolfluidinputSlot.processIntoTank(inputTank, cooloutputSlot);
    changed |= hotfluidinputSlot.processFromTank(outputTank, hotoutputSlot);
    heatbuffer += drawHeat(HEAT_CAPACITY - heatbuffer);
    boolean active = false;
    var input = inputTank.getFluidStack();
    if (input != null
        && !input.isEmpty()
        && Recipes.liquidHeatupManager
            .getSingleDirectionLiquidManager()
            .acceptsFluid(input.getFluid())) {
      var exchange = Recipes.liquidHeatupManager.getHeatExchangeProperty(input.getFluid());
      if (exchange != null && exchange.huPerMB > 0 && exchange.outputFluid != null) {
        int available =
            Math.min(
                inputTank.getFluidAmount(),
                Math.max(0, outputTank.getCapacity() - outputTank.getFluidAmount()));
        available =
            outputTank.fillMbUnchecked(Ic2FluidStack.create(exchange.outputFluid, available), true);
        long budget = Math.max(0, (long) available * exchange.huPerMB - liquidHeatStored);
        int groups =
            (int) Math.min(budget, Math.min(heatbuffer / 4, (KINETIC_CAPACITY - kUBuffer) / 12));
        if (groups > 0) {
          heatbuffer -= groups * 4;
          kUBuffer += groups * 12;
          liquidHeatStored += groups;
          active = true;
          changed = true;
        }
        int converted = (int) Math.min(available, liquidHeatStored / exchange.huPerMB);
        if (converted > 0) {
          inputTank.drainMbUnchecked(converted, false);
          outputTank.fillMbUnchecked(Ic2FluidStack.create(exchange.outputFluid, converted), false);
          liquidHeatStored -= (long) converted * exchange.huPerMB;
          changed = true;
        }
      }
    }
    setActive(active);
    upgradeSlot.tick();
    if (changed) setChanged();
  }

  private int drawHeat(int maximum) {
    if (maximum <= 0 || getLevel() == null) return 0;
    int remaining = maximum;
    for (var side : SIDES)
      if (side != getFacing()
          && getLevel().getBlockEntity(worldPosition.relative(side))
              instanceof IHeatSource source) {
        int offered =
            Math.clamp(source.drawHeat(side.getOpposite(), remaining, true), 0, remaining);
        if (offered > 0)
          remaining -= Math.clamp(source.drawHeat(side.getOpposite(), offered, false), 0, offered);
        if (remaining == 0) break;
      }
    return maximum - remaining;
  }

  @Override
  public int getConnectionBandwidth(Direction side) {
    return side == getFacing() ? KINETIC_CAPACITY : 0;
  }

  @Override
  public int maxrequestkineticenergyTick(Direction side) {
    return drawKineticEnergy(side, Integer.MAX_VALUE, true);
  }

  @Override
  public int requestkineticenergy(Direction side, int amount) {
    return drawKineticEnergy(side, amount, false);
  }

  @Override
  public int drawKineticEnergy(Direction side, int request, boolean simulate) {
    if (side != getFacing() || request <= 0) return 0;
    int amount = Math.min(request, kUBuffer);
    if (!simulate && amount > 0) {
      kUBuffer -= amount;
      setChanged();
    }
    return amount;
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    if (data.contains("inputTank")) inputTank.fromNbt(data.getCompoundOrEmpty("inputTank"));
    if (data.contains("outputTank")) outputTank.fromNbt(data.getCompoundOrEmpty("outputTank"));
    heatbuffer = Math.clamp(data.getIntOr("heatbuffer", 0), 0, HEAT_CAPACITY);
    kUBuffer =
        Math.clamp(data.getIntOr("kUBuffer", data.getIntOr("kubuffer", 0)), 0, KINETIC_CAPACITY);
    liquidHeatStored =
        Math.clamp(
            data.getLongOr("liquidHeatStored", data.getIntOr("liquidHeatStored", 0)),
            0,
            Integer.MAX_VALUE);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.put("inputTank", inputTank.toNbt(new CompoundTag()));
    data.put("outputTank", outputTank.toNbt(new CompoundTag()));
    data.putInt("heatbuffer", heatbuffer);
    data.putInt("kUBuffer", kUBuffer);
    data.putLong("liquidHeatStored", liquidHeatStored);
  }

  public Ic2FluidTank getInputTank() {
    return inputTank;
  }

  public Ic2FluidTank getOutputTank() {
    return outputTank;
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing,
        UpgradableProperty.FluidConsuming,
        UpgradableProperty.FluidProducing);
  }

  @Override
  public double getEnergy() {
    return 40;
  }

  @Override
  public boolean useEnergy(double amount) {
    return Double.isFinite(amount) && amount >= 0;
  }

  @Override
  public OwnedMenu<HeatDrivenCogenerator> createServerScreenHandler(int id, Player player) {
    return new StirlingEngineMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new StirlingEngineMenu(id, inventory, this);
  }
}
