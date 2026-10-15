package ic2ce.neoforge.power;

import ic2ce.api.crops.BiomeType;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.world.BiomeUtil;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.power.menu.SolarCollectorMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

/** Solar generation samples weather and sky visibility on a staggered schedule. */
public class SolarCollector extends BufferedGenerator {
  public float skyLight;
  @GuiSynced public boolean sunlight;
  private int visibilityTicks = ProjectRuntime.random.nextInt(128);
  private static final double MULTIPLIER =
      NativeConfigValues.getDouble(ProjectConfiguration.get(), "balance/energy/generator/solar");

  public SolarCollector(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.SOLAR_GENERATOR, position, block, 1, 1, 2);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    updateSunVisibility();
  }

  @Override
  public boolean gainEnergy() {
    if ((++visibilityTicks & 127) == 0) updateSunVisibility();
    if (skyLight <= 0 || !Double.isFinite(MULTIPLIER) || MULTIPLIER <= 0) return false;
    energy.addEnergy(MULTIPLIER * skyLight);
    return true;
  }

  public void updateSunVisibility() {
    skyLight = getLevel() == null ? 0 : getSkyLight(getLevel(), worldPosition.above());
    sunlight = skyLight > 0;
  }

  public static float getSkyLight(Level world, BlockPos position) {
    if (!world.dimensionType().hasSkyLight()) return 0;
    double phase = (Math.floorMod(world.getOverworldClockTime(), 24000L) + 1.0) / 24000 - 0.25;
    double brightness = Math.clamp(Math.cos(phase * Math.TAU) * 2 + 0.2, 0, 1);
    if (!ProjectRuntime.envProxy.biomeHasType(
        BiomeUtil.getBiome(world, position), BiomeType.SANDY)) {
      brightness *= 1 - world.getRainLevel(1) * 5.0 / 16;
      brightness *= 1 - world.getThunderLevel(1) * 5.0 / 16;
    }
    return (float)
        (world.getBrightness(LightLayer.SKY, position) / 15.0 * Math.clamp(brightness, 0, 1));
  }

  @Override
  public boolean gainFuel() {
    return false;
  }

  @Override
  public boolean needsFuel() {
    return false;
  }

  @Override
  protected boolean delayActiveUpdate() {
    return true;
  }

  public boolean isSunlight() {
    return sunlight;
  }

  @Override
  public OwnedMenu<SolarCollector> createServerScreenHandler(int id, Player player) {
    return new SolarCollectorMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new SolarCollectorMenu(id, inventory, this);
  }
}
