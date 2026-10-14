package ic2ce.neoforge.power;

import ic2ce.core.network.GuiSynced;
import ic2ce.core.util.ParticleUtil;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.FuelInput;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Combustion burns an already lit fuel item continuously, including while its EU buffer is full.
 */
public class CombustionGenerator extends BufferedGenerator implements ScreenValues {
  public final FuelInput fuelSlot;
  @GuiSynced public int totalFuel;

  public CombustionGenerator(BlockPos position, BlockState block) {
    super(
        BlockEntityDefinitions.GENERATOR,
        position,
        block,
        Math.round(
            10
                * NativeConfigValues.getDouble(
                    ProjectConfiguration.get(), "balance/energy/generator/generator")),
        1,
        4000);
    fuelSlot = new FuelInput(this, "fuel", 1, false);
  }

  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    if (getActive()) ParticleUtil.showFurnaceFlames(getLevel(), worldPosition, getFacing());
  }

  @Override
  public boolean gainFuel() {
    int ticks = fuelSlot.consumeFuel() / 4;
    if (ticks <= 0) return false;
    fuel += ticks;
    totalFuel = ticks;
    return true;
  }

  @Override
  public boolean isConverting() {
    return fuel > 0;
  }

  public double getFuelRatio() {
    return fuel <= 0 ? 0 : Math.clamp((double) fuel / Math.max(1, totalFuel), 0, 1);
  }

  @Override
  public double getGuiValue(String name) {
    if ("fuel".equals(name)) return getFuelRatio();
    throw new IllegalArgumentException("Unknown combustion value: " + name);
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.GENERATOR_GENERATOR_LOOP;
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    totalFuel = Math.max(0, data.getIntOr("totalFuel", 0));
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("totalFuel", totalFuel);
  }
}
