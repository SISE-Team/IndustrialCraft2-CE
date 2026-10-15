package ic2ce.neoforge.power;

import ic2ce.core.event.WorldData;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.core.util.Util;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/** Windmill production samples altitude, weather and a cached obstruction volume. */
public class WindMill extends RotorGenerator implements ScreenValues.IActiveGuiValueProvider {
  private static final double PER_WIND =
      0.1
          * NativeConfigValues.getDouble(
              ProjectConfiguration.get(), "balance/energy/generator/wind");
  private int sampleTicks = ProjectRuntime.random.nextInt(128), obstructedBlockCount;
  @GuiSynced private double overheatRatio;

  public WindMill(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.WIND_GENERATOR, position, block, 0, 1, 32, 2);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    updateObscuratedBlockCount();
    if (getLevel() != null && !getLevel().isClientSide()) sampleWind();
  }

  private boolean sampleWind() {
    var world = getLevel();
    if (world == null || world.isClientSide()) return false;
    production = overheatRatio = 0;
    if (!Double.isFinite(PER_WIND) || PER_WIND <= 0) return false;
    var simulation = WorldData.get(world).windSim;
    if (simulation == null) return false;
    double wind = simulation.getWindAt(worldPosition.getY()) * (1 - obstructedBlockCount / 567.0);
    double maximum = simulation.getMaxWind();
    if (!Double.isFinite(wind) || wind <= 0 || maximum <= 0) return false;
    double next = wind * PER_WIND;
    overheatRatio = Math.max(0, (wind / maximum - 0.5) * 2);
    if (wind > maximum * 0.5 && next > 5 && world.getRandom().nextInt(5000) <= next - 5) {
      if (Util.harvestBlock(world, worldPosition))
        for (int n = world.getRandom().nextInt(5); n > 0; n--)
          StackUtil.dropAsEntity(world, worldPosition, new ItemStack(Items.IRON_INGOT));
      return false;
    }
    production = next;
    return true;
  }

  @Override
  public boolean gainEnergy() {
    if ((++sampleTicks & 127) == 0) {
      if ((sampleTicks & 1023) == 0) updateObscuratedBlockCount();
      if (!sampleWind()) return false;
    }
    return super.gainEnergy();
  }

  public void updateObscuratedBlockCount() {
    var world = getLevel();
    if (world == null) {
      obstructedBlockCount = 0;
      return;
    }
    int count = 0;
    var cursor = new BlockPos.MutableBlockPos();
    for (int x = -4; x <= 4; x++)
      for (int y = -2; y <= 4; y++)
        for (int z = -4; z <= 4; z++) {
          if (x == 0 && y == 0 && z == 0) continue;
          cursor.set(worldPosition.getX() + x, worldPosition.getY() + y, worldPosition.getZ() + z);
          if (!world.isEmptyBlock(cursor)) count++;
        }
    obstructedBlockCount = Math.clamp(count, 0, 566);
  }

  public int getObstructions() {
    return obstructedBlockCount;
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
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.GENERATOR_WIND_LOOP;
  }

  @Override
  protected boolean delayActiveUpdate() {
    return true;
  }

  @Override
  protected boolean shouldRotorRotate() {
    return production > 0;
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.add("production");
    return fields;
  }

  @Override
  public boolean isGuiValueActive(String name) {
    if ("wind".equals(name)) return production > 0;
    throw new IllegalArgumentException("Unknown windmill state: " + name);
  }

  @Override
  public double getGuiValue(String name) {
    if ("wind".equals(name)) return overheatRatio;
    throw new IllegalArgumentException("Unknown windmill value: " + name);
  }
}
