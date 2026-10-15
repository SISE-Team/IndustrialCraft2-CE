package ic2ce.neoforge.power;

import ic2ce.api.util.FluidContainerOutputMode;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Bucket fuel and surrounding-flow microenergy have separate accounting and never rescale raw water
 * count.
 */
public class WaterMill extends RotorGenerator implements ScreenValues {
  private static final double MULTIPLIER =
      NativeConfigValues.getDouble(ProjectConfiguration.get(), "balance/energy/generator/water");
  private static final boolean AUTOMATION =
      NativeConfigValues.getBool(ProjectConfiguration.get(), "balance/watermillAutomation");
  public final FluidContainerInventory fuelSlot;
  private int visibilityTicks = ProjectRuntime.random.nextInt(128);
  @GuiSynced public int water;
  public int microStorage, maxWater = 2000;

  public WaterMill(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.WATER_GENERATOR, position, block, 2, 1, 4, 2);
    fuelSlot =
        new FluidWhitelistInput(
            this,
            "fuel",
            AUTOMATION ? InventorySlot.Access.IO : InventorySlot.Access.NONE,
            1,
            InventorySlot.InvSide.TOP,
            FluidContainerInventory.OpType.Drain,
            Fluids.WATER);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    updateWaterCount();
  }

  @Override
  public boolean gainFuel() {
    if (fuel <= Math.max(0, maxWater - 500) && !fuelSlot.isEmpty()) {
      var original = fuelSlot.get();
      var plan =
          LiquidUtil.drainContainer(
              original, Fluids.WATER, 1000, FluidContainerOutputMode.InPlacePreferred);
      if (plan == null
          || plan.fluidChange.getAmountMb() != 1000
          || plan.extraOutput != null && !plan.extraOutput.isEmpty()) return false;
      production = ProjectRuntime.envProxy.hasRecipeRemainder(original) ? 1 : 2;
      fuelSlot.put(plan.inPlaceOutput);
      fuel += 500;
      return true;
    }
    if (fuel > 0) return false;
    flowPower();
    int wholeEnergy = microStorage / 100;
    if (wholeEnergy <= 0) return false;
    production = wholeEnergy;
    microStorage %= 100;
    fuel = 1;
    return true;
  }

  public void flowPower() {
    if ((++visibilityTicks & 127) == 0) updateWaterCount();
    if (Double.isFinite(MULTIPLIER) && MULTIPLIER > 0)
      microStorage =
          (int)
              Math.clamp(
                  (long) microStorage + Math.round(water * MULTIPLIER), 0, Integer.MAX_VALUE);
  }

  public void updateWaterCount() {
    var world = getLevel();
    if (world == null) {
      water = 0;
      return;
    }
    int count = 0;
    var cursor = new BlockPos.MutableBlockPos();
    for (int x = -1; x <= 1; x++)
      for (int y = -1; y <= 1; y++)
        for (int z = -1; z <= 1; z++) {
          cursor.set(worldPosition.getX() + x, worldPosition.getY() + y, worldPosition.getZ() + z);
          if (world.getBlockState(cursor).getFluidState().is(FluidTags.WATER)) count++;
        }
    water = count;
  }

  @Override
  public boolean needsFuel() {
    return fuel == 0 || fuel <= Math.max(0, maxWater - 500);
  }

  @Override
  public boolean isConverting() {
    return fuel > 0;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.GENERATOR_WATER_LOOP;
  }

  @Override
  protected boolean delayActiveUpdate() {
    return true;
  }

  @Override
  protected boolean shouldRotorRotate() {
    return water > 0 || fuel > 0;
  }

  @Override
  protected float rotorSpeedFactor() {
    return fuel > 0 ? 1 : Math.clamp(water / 25.0F, 0, 1);
  }

  @Override
  public double getGuiValue(String name) {
    if ("water".equals(name)) return Math.clamp((double) fuel / Math.max(1, maxWater), 0, 1);
    throw new IllegalArgumentException("Unknown watermill value: " + name);
  }
}
