package ic2ce.neoforge.reactor;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.*;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.reactor.*;
import ic2ce.api.recipe.Recipes;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.core.util.*;
import ic2ce.engine.reactor.ReactorCooling;
import ic2ce.neoforge.audio.PlaybackHandle;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.reactor.menu.ReactorMenu;
import ic2ce.neoforge.reactor.part.HeatCell;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Thermal-first reactor cycles, conserved per-tick electrical output and transactional coolant. */
public class NuclearCore extends InventoryBlockEntity
    implements IHasGui, IReactor, IEnergySource, IMetaDelegate, ScreenValues {
  public PlaybackHandle soundMain, soundGeiger;
  private int soundBand;
  public final FluidPorts.InternalFluidTank inputTank, outputTank;
  public final ReactorInventory reactorSlot;
  public final OutputInventory coolantoutputSlot, hotcoolantoutputSlot;
  public final FluidRecipeInput coolantinputSlot;
  public final TankContainerInput hotcoolinputSlot;
  public final SignalInput redstone;
  protected final FluidPorts fluids;
  public float output, hem = 1;
  public int updateTicker, heat, maxHeat = 10000, EmitHeat;
  public boolean addedToEnergyNet;
  private boolean fluidCooled;
  private int pendingHeat;
  private final ReactorCooling cooling = new ReactorCooling();
  private List<IEnergyTile> subTiles = List.of();
  private long layoutTick = Long.MIN_VALUE, energyTick = Long.MIN_VALUE;
  private int columns = 9;
  private double drawnThisTick;
  private static final double HU_MULTIPLIER =
      40.0
          * NativeConfigValues.getFloat(
              ProjectConfiguration.get(), "balance/energy/FluidReactor/outputModifier");

  public NuclearCore(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.NUCLEAR_REACTOR, position, state);
    updateTicker = ProjectRuntime.random.nextInt(20);
    fluids = addComponent(new FluidPorts(this));
    inputTank =
        fluids.addTank(
            "inputTank",
            10000,
            InventorySlot.Access.NONE,
            InventorySlot.InvSide.ANY,
            FluidPorts.fluidPredicate(Recipes.liquidHeatupManager));
    outputTank = fluids.addTank("outputTank", 10000, InventorySlot.Access.NONE);
    reactorSlot = new ReactorInventory(this, "reactor", 54);
    coolantinputSlot =
        new FluidRecipeInput(
            this,
            "coolantinputSlot",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.ANY,
            FluidContainerInventory.OpType.Drain,
            Recipes.liquidHeatupManager);
    hotcoolinputSlot =
        new TankContainerInput(
            this,
            "hotcoolinputSlot",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.ANY,
            FluidContainerInventory.OpType.Fill,
            outputTank);
    coolantoutputSlot = new OutputInventory(this, "coolantoutputSlot", 1);
    hotcoolantoutputSlot = new OutputInventory(this, "hotcoolantoutputSlot", 1);
    redstone = addComponent(new SignalInput(this));
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (getLevel().isClientSide()) return;
    refreshChambers();
    fluidCooled = ReactorStructure.isFluidReactor(this);
    configureConnections();
  }

  @Override
  protected void onUnloaded() {
    if (getLevel() != null && getLevel().isClientSide()) {
      ProjectRuntime.soundManager.removeAllSound(this);
      soundMain = null;
      soundGeiger = null;
      soundBand = 0;
    }
    if (addedToEnergyNet) {
      EnergyNet.instance.removeTile(this);
      addedToEnergyNet = false;
    }
    subTiles = List.of();
    layoutTick = Long.MIN_VALUE;
    super.onUnloaded();
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    heat = Math.max(0, data.getIntOr("heat", 0));
    output = bounded(data.getFloatOr("output", 0));
    maxHeat = Math.max(1, data.getIntOr("maxHeat", 10000));
    hem = bounded(data.getFloatOr("hem", 1));
    pendingHeat = Math.max(0, data.getIntOr("pendingHeat", 0));
    fluidCooled = data.getBooleanOr("fluidCooled", false);
    cooling.restore(data.getDoubleOr("coolingHu", 0), data.getDoubleOr("hullHeatFraction", 0));
    energyTick = layoutTick = Long.MIN_VALUE;
    drawnThisTick = 0;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("heat", heat);
    data.putFloat("output", output);
    data.putInt("maxHeat", maxHeat);
    data.putFloat("hem", hem);
    data.putInt("pendingHeat", pendingHeat);
    data.putBoolean("fluidCooled", fluidCooled);
    data.putDouble("coolingHu", cooling.pendingHu());
    data.putDouble("hullHeatFraction", cooling.hullFraction());
  }

  private static float bounded(float value) {
    return Float.isFinite(value) ? Math.max(0, value) : 0;
  }

  private void beginEnergyTick() {
    long tick = getLevel() == null ? 0 : getLevel().getGameTime();
    if (energyTick != tick) {
      energyTick = tick;
      drawnThisTick = 0;
    }
  }

  @Override
  public double getOfferedEnergy() {
    beginEnergyTick();
    return fluidCooled ? 0 : Math.max(0, getReactorEUEnergyOutput() - drawnThisTick);
  }

  @Override
  public void drawEnergy(double amount) {
    if (!Double.isFinite(amount) || amount <= 0) return;
    beginEnergyTick();
    drawnThisTick += Math.min(amount, Math.max(0, getReactorEUEnergyOutput() - drawnThisTick));
  }

  @Override
  public double getReactorEUEnergyOutput() {
    double factor =
        NativeConfigValues.getFloat(ProjectConfiguration.get(), "balance/energy/generator/nuclear");
    return Double.isFinite(factor) && factor > 0 ? bounded(output) * 5.0 * factor : 0;
  }

  @Override
  public int getSourceTier() {
    return 5;
  }

  @Override
  public boolean emitsEnergyTo(IEnergyAcceptor target, Direction face) {
    return !fluidCooled;
  }

  @Override
  public List<IEnergyTile> getSubTiles() {
    return subTiles;
  }

  @Override
  protected void onNeighborChange(Block changed) {
    super.onNeighborChange(changed);
    layoutTick = Long.MIN_VALUE;
    if (getLevel() != null && !getLevel().isClientSide()) refreshChambers();
  }

  public void refreshChambers() {
    var world = getLevel();
    if (world == null) return;
    var members = new ArrayList<IEnergyTile>(7);
    members.add(this);
    for (var direction : Direction.values())
      if (world.getBlockEntity(worldPosition.relative(direction)) instanceof ReactorChamber chamber
          && !chamber.isRemoved()
          && chamber.getReactorInstance() == this) {
        members.add(chamber);
        if (!world.isClientSide() && chamber.redstone.getLinkReceiver() != redstone) {
          chamber.redstone.unlinkOutbound();
          chamber.redstone.linkTo(redstone);
        }
      }
    if (members.equals(subTiles)) return;
    boolean registered = addedToEnergyNet;
    if (registered) EnergyNet.instance.removeTile(this);
    subTiles = List.copyOf(members);
    layoutTick = Long.MIN_VALUE;
    if (registered) EnergyNet.instance.addBlockEntityTile(this);
  }

  private void configureConnections() {
    if (fluidCooled && addedToEnergyNet) {
      EnergyNet.instance.removeTile(this);
      addedToEnergyNet = false;
    }
    if (!fluidCooled && !addedToEnergyNet) {
      EnergyNet.instance.addBlockEntityTile(this);
      addedToEnergyNet = true;
    }
    fluids.changeConnectivity(
        inputTank,
        fluidCooled ? InventorySlot.Access.I : InventorySlot.Access.NONE,
        InventorySlot.InvSide.ANY);
    fluids.changeConnectivity(
        outputTank,
        fluidCooled ? InventorySlot.Access.O : InventorySlot.Access.NONE,
        InventorySlot.InvSide.ANY);
    for (var origin : redstone.getLinkedOrigins())
      if (origin.getParent() instanceof SignalPort) origin.unlinkOutbound();
    if (!fluidCooled) return;
    var cursor = new BlockPos.MutableBlockPos();
    for (int y = -2; y <= 2; y++)
      for (int z = -2; z <= 2; z++)
        for (int x = -2; x <= 2; x++) {
          if (Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z))) != 2) continue;
          cursor.set(worldPosition.getX() + x, worldPosition.getY() + y, worldPosition.getZ() + z);
          if (getLevel().getBlockEntity(cursor) instanceof SignalPort port) {
            port.redstone.unlinkOutbound();
            port.redstone.linkTo(redstone);
          }
        }
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (updateTicker++ % getTickRate() != 0) return;
    if (!Util.isAreaLoaded(getLevel(), worldPosition, 8)) {
      output = 0;
      EmitHeat = 0;
      setActive(false);
    } else {
      refreshChambers();
      boolean enclosed = ReactorStructure.isFluidReactor(this);
      if (enclosed != fluidCooled) {
        if (!enclosed) {
          addHeat(cooling.exchange(pendingHeat, HU_MULTIPLIER, 0, 0).returnedHullHeat());
          pendingHeat = 0;
          EmitHeat = 0;
        }
        fluidCooled = enclosed;
        configureConnections();
        ProjectRuntime.network.get(true).updateTileEntityField(this, "fluidCooled");
      }
      dropAllUnfittingStuff();
      output = 0;
      maxHeat = 10000;
      hem = 1;
      processChambers();
      if (fluidCooled) exchangeCoolant();
      else {
        pendingHeat = 0;
        EmitHeat = 0;
      }
      if (calculateHeatEffects()) return;
      setActive(heat >= 1000 || output > 0);
      setChanged();
    }
    ProjectRuntime.network.get(true).updateTileEntityField(this, "output");
  }

  private void exchangeCoolant() {
    coolantinputSlot.processIntoTank(inputTank, coolantoutputSlot);
    hotcoolinputSlot.processFromTank(outputTank, hotcoolantoutputSlot);
    var input = inputTank.getFluidStack();
    var property =
        input == null || input.isEmpty()
            ? null
            : Recipes.liquidHeatupManager.getHeatExchangeProperty(input.getFluid());
    int capacity = 0;
    if (property != null && property.huPerMB > 0) {
      var possible = Ic2FluidStack.create(property.outputFluid, inputTank.getFluidAmount());
      capacity = outputTank.fillMbUnchecked(possible, true);
    }
    var exchange =
        cooling.exchange(
            pendingHeat, HU_MULTIPLIER, property == null ? 0 : property.huPerMB, capacity);
    pendingHeat = 0;
    EmitHeat = exchange.emittedHu();
    addHeat(exchange.returnedHullHeat());
    if (exchange.millibuckets() == 0) return;
    // Both tanks are private world-thread buffers; the reservation cannot change during commit.
    var produced = Ic2FluidStack.create(property.outputFluid, exchange.millibuckets());
    if (outputTank.fillMbUnchecked(produced, false) != exchange.millibuckets()
        || inputTank.drainMbUnchecked(exchange.millibuckets(), false).getAmountMb()
            != exchange.millibuckets())
      throw new IllegalStateException("Reactor coolant reservation changed during commit");
  }

  public void processChambers() {
    int width = getReactorSize();
    for (int phase = 0; phase < 2; phase++)
      for (int y = 0; y < 6; y++)
        for (int x = 0; x < width; x++) {
          var stack = reactorSlot.get(x, y);
          if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IReactorComponent component)
            component.processChamber(stack, this, x, y, phase == 0);
        }
  }

  public int getReactorSize() {
    var world = getLevel();
    if (world == null) return 9;
    columns = 3;
    for (var direction : Direction.values())
      if (world.getBlockEntity(worldPosition.relative(direction)) instanceof ReactorChamber chamber
          && !chamber.isRemoved()
          && chamber.getReactorInstance() == this) columns++;
    return columns;
  }

  public void dropAllUnfittingStuff() {
    int visible = reactorSlot.size();
    for (int slot = 0; slot < reactorSlot.rawSize(); slot++) {
      var stack = reactorSlot.get(slot);
      if (!StackUtil.isEmpty(stack) && (slot >= visible || !isUsefulItem(stack, false))) {
        reactorSlot.put(slot, ItemStack.EMPTY);
        eject(stack);
      }
    }
  }

  public boolean isUsefulItem(ItemStack stack, boolean insertion) {
    if (StackUtil.isEmpty(stack) || !(stack.getItem() instanceof IBaseReactorComponent component))
      return false;
    if (insertion
        && fluidCooled
        && stack.getItem() instanceof HeatCell cell
        && cell.getCurrentHeat(stack, this, 0, 0) > 0) return false;
    return !insertion || component.canBePlacedIn(stack, this);
  }

  public void eject(ItemStack stack) {
    if (getLevel() != null && !getLevel().isClientSide() && !StackUtil.isEmpty(stack))
      StackUtil.dropAsEntity(getLevel(), worldPosition, stack);
  }

  @Override
  public boolean produceEnergy() {
    return redstone.hasRedstoneInput()
        && NativeConfigValues.getFloat(
                ProjectConfiguration.get(), "balance/energy/generator/nuclear")
            > 0;
  }

  @Override
  public int getTickRate() {
    return 20;
  }

  @Override
  public BlockEntity getCoreTe() {
    return this;
  }

  @Override
  public BlockPos getPosition() {
    return worldPosition;
  }

  @Override
  public Level getWorldObj() {
    return getLevel();
  }

  @Override
  public int getHeat() {
    return Math.max(0, heat);
  }

  @Override
  public void setHeat(int value) {
    heat = Math.max(0, value);
  }

  @Override
  public int addHeat(int amount) {
    return heat = (int) Math.clamp((long) getHeat() + amount, 0, Integer.MAX_VALUE);
  }

  @Override
  public int getMaxHeat() {
    return maxHeat;
  }

  @Override
  public void setMaxHeat(int value) {
    maxHeat = Math.max(1, value);
  }

  @Override
  public float getHeatEffectModifier() {
    return hem;
  }

  @Override
  public void setHeatEffectModifier(float value) {
    hem = bounded(value);
  }

  @Override
  public float getReactorEnergyOutput() {
    return bounded(output);
  }

  @Override
  public float addOutput(float amount) {
    if (Float.isFinite(amount))
      output = (float) Math.clamp((double) bounded(output) + amount, 0, Float.MAX_VALUE);
    return output;
  }

  @Override
  public void addEmitHeat(int amount) {
    if (fluidCooled && amount > 0)
      pendingHeat = (int) Math.min(Integer.MAX_VALUE, (long) pendingHeat + amount);
  }

  @Override
  public boolean isFluidCooled() {
    return fluidCooled;
  }

  @Override
  public ItemStack getItemAt(int x, int y) {
    return x >= 0 && x < getReactorSize() && y >= 0 && y < 6
        ? reactorSlot.get(x, y)
        : ItemStack.EMPTY;
  }

  @Override
  public void setItemAt(int x, int y, ItemStack stack) {
    if (x >= 0 && x < getReactorSize() && y >= 0 && y < 6) reactorSlot.put(x, y, stack);
  }

  public int gaugeHeatScaled(int width) {
    return maxHeat <= 0
        ? 0
        : (int)
            Math.clamp(
                (long) Math.max(0, width) * getHeat() * 100 / ((long) maxHeat * 85),
                0,
                Integer.MAX_VALUE);
  }

  public int gaugeLiquidScaled(int width, int index) {
    var tank = index == 0 ? inputTank : index == 1 ? outputTank : null;
    return tank == null || tank.getCapacity() == 0
        ? 0
        : (int) ((long) Math.max(0, width) * tank.getFluidAmount() / tank.getCapacity());
  }

  @Override
  public double getGuiValue(String key) {
    if (!key.equals("heat")) throw new IllegalArgumentException("Unknown reactor gauge: " + key);
    return maxHeat <= 0 ? 0 : (double) getHeat() / maxHeat;
  }

  public Ic2FluidTank getinputtank() {
    return inputTank;
  }

  public Ic2FluidTank getoutputtank() {
    return outputTank;
  }

  @Override
  public int getMaxStackSize() {
    return 1;
  }

  @Override
  protected InteractionResult onActivated(
      Player player, InteractionHand hand, Direction face, Vec3 hit) {
    return player.getItemInHand(hand).is(ItemDefinitions.REACTOR_CHAMBER)
        ? InteractionResult.PASS
        : super.onActivated(player, hand, face, hit);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new ReactorMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new ReactorMenu(syncId, inventory, this);
  }

  public boolean calculateHeatEffects() {
    return ReactorHazards.apply(this);
  }

  public BlockPos getRandCoord(int radius) {
    return ReactorHazards.randomPosition(this, radius);
  }

  @Override
  public void explode() {
    ReactorHazards.explode(this);
  }

  public static void showHeatEffects(Level world, BlockPos position, int heat) {
    ReactorHazards.particles(world, position, heat);
  }

  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    showHeatEffects(getLevel(), worldPosition, heat);
  }

  @Override
  public void onNetworkUpdate(String field) {
    if (field.equals("output")) updateSounds();
    super.onNetworkUpdate(field);
  }

  private void updateSounds() {
    int band = output <= 0 ? 0 : output < 40 ? 1 : output < 80 ? 2 : 3;
    if (band == soundBand) return;
    if (band == 0) {
      if (soundMain != null) soundMain.stop();
      if (soundGeiger != null) soundGeiger.stop();
    } else {
      if (soundBand == 0) {
        if (soundMain == null)
          soundMain =
              ProjectRuntime.soundManager.createSound(
                  this,
                  SoundDefinitions.GENERATOR_NUCLEAR_LOOP,
                  SoundSource.BLOCKS,
                  worldPosition,
                  1,
                  1);
        if (soundMain != null) soundMain.play();
      }
      if (soundGeiger != null) ProjectRuntime.soundManager.removeSound(this, soundGeiger);
      var event =
          switch (band) {
            case 1 -> SoundDefinitions.GENERATOR_NUCLEAR_LOW_POWER;
            case 2 -> SoundDefinitions.GENERATOR_NUCLEAR_MEDIUM_POWER;
            default -> SoundDefinitions.GENERATOR_NUCLEAR_HIGH_POWER;
          };
      soundGeiger =
          ProjectRuntime.soundManager.createSound(
              this, event, SoundSource.BLOCKS, worldPosition, 1, 1);
      if (soundGeiger != null) soundGeiger.play();
    }
    soundBand = band;
  }
}
