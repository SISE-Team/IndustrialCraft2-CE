package ic2ce.neoforge.thermal;

import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.FuelInput;
import ic2ce.neoforge.inventory.OutputInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Combustion has a retained heat reservoir and a persisted pending-ash output transaction. */
@NotClassic
public class CombustionHeater extends BufferedHeatSource implements IHasGui, ScreenValues {
  private int storedHeat, pendingAsh;
  public int activityMeter, ticksSinceLastActiveUpdate = ProjectRuntime.random.nextInt(256);
  @GuiSynced public int fuel, itemFuelTime;
  public final FuelInput fuelSlot = new FuelInput(this, "fuel", 1, false);
  public final OutputInventory outputslot = new OutputInventory(this, "output", 1);
  public static final int emittedHU =
      (int)
          Math.clamp(
              Math.round(
                  20
                      * NativeConfigValues.getDouble(
                          ProjectConfiguration.get(), "balance/energy/heatgenerator/solid")),
              0,
              Integer.MAX_VALUE);

  public CombustionHeater(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.SOLID_HEAT_GENERATOR, position, block);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean changed = false;
    if (pendingAsh > 0 && outputslot.canAdd(new ItemStack(ItemDefinitions.ASHES, pendingAsh))) {
      outputslot.add(new ItemStack(ItemDefinitions.ASHES, pendingAsh));
      pendingAsh = 0;
      changed = true;
    }
    if (needsFuel()) changed |= gainFuel();
    boolean burning = gainheat();
    if (changed) setChanged();
    if (!delayActiveUpdate()) setActive(burning);
    else {
      activityMeter += burning ? 1 : -1;
      if ((++ticksSinceLastActiveUpdate & 255) == 0) {
        setActive(activityMeter > 0);
        activityMeter = 0;
      }
    }
  }

  public boolean gainheat() {
    if (!isConverting() || emittedHU <= 0) return false;
    storedHeat = (int) Math.min(Integer.MAX_VALUE, (long) storedHeat + emittedHU);
    if (--fuel == 0 && getLevel().getRandom().nextBoolean()) {
      var ash = new ItemStack(ItemDefinitions.ASHES);
      if (outputslot.canAdd(ash)) outputslot.add(ash);
      else pendingAsh++;
    }
    return true;
  }

  public boolean needsFuel() {
    return fuel == 0 && storedHeat == 0 && getHeatBuffer() == 0 && pendingAsh == 0;
  }

  public boolean gainFuel() {
    if (emittedHU <= 0
        || pendingAsh > 0
        || !outputslot.canAdd(new ItemStack(ItemDefinitions.ASHES))) return false;
    int ticks = fuelSlot.consumeFuel() / 4;
    if (ticks <= 0) return false;
    fuel = itemFuelTime = ticks;
    return true;
  }

  public boolean isConverting() {
    return fuel > 0;
  }

  public boolean delayActiveUpdate() {
    return false;
  }

  @Override
  protected int fillHeatBuffer(int maximum) {
    int amount = Math.min(Math.max(0, maximum), storedHeat);
    storedHeat -= amount;
    return amount;
  }

  @Override
  public int getMaxHeatEmittedPerTick() {
    return emittedHU;
  }

  @Override
  public double getGuiValue(String name) {
    if ("fuel".equals(name))
      return fuel == 0 ? 0 : Math.clamp((double) fuel / Math.max(1, itemFuelTime), 0, 1);
    throw new IllegalArgumentException("Unknown heat combustion value: " + name);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    fuel = Math.max(0, data.getIntOr("fuel", 0));
    itemFuelTime = Math.max(0, data.getIntOr("itemFuelTime", fuel));
    storedHeat = Math.max(0, data.getIntOr("storedHeat", 0));
    pendingAsh = Math.clamp(data.getIntOr("pendingAsh", 0), 0, 1);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("fuel", fuel);
    data.putInt("itemFuelTime", itemFuelTime);
    data.putInt("storedHeat", storedHeat);
    data.putInt("pendingAsh", pendingAsh);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return DocumentMenu.create(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(id, inventory, this);
  }
}
