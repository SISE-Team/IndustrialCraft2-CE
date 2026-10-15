package ic2ce.neoforge.kinetic;

import ic2ce.api.item.IKineticRotor;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.core.util.BiomeUtil;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.kinetic.menu.WaterTurbineMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Water-specific biome/tidal/river flow and submerged geometry over a conserved shaft budget. */
@NotClassic
public class WaterTurbine extends RotorGearbox {
  public BiomeState type = BiomeState.UNKNOWN;
  protected int updateTicker = ProjectRuntime.random.nextInt(20);
  private boolean rightFacing;
  private int distanceToNormalBiome, crossSection, obstructedCrossSection, waterFlow;
  private static final double MULTIPLIER =
      0.2
          * NativeConfigValues.getDouble(
              ProjectConfiguration.get(), "balance/energy/kineticgenerator/water");

  public WaterTurbine(BlockPos position, BlockState block) {
    super(
        BlockEntityDefinitions.WATER_KINETIC_GENERATOR,
        position,
        block,
        IKineticRotor.GearboxType.WATER);
  }

  protected int getTickRate() {
    return 20;
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (getLevel() != null && !getLevel().isClientSide()) updateSeaInfo();
  }

  @Override
  protected void setFacing(Level world, Direction facing) {
    super.setFacing(world, facing);
    if (getLevel() != null && !getLevel().isClientSide()) updateSeaInfo();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (++updateTicker >= getTickRate()) {
      updateTicker = 0;
      sampleFlow();
    }
    budget.reset(production());
  }

  private void sampleFlow() {
    var world = getLevel();
    var biome = BiomeUtil.getBiome(world, worldPosition);
    type =
        biome.is(BiomeTags.IS_DEEP_OCEAN)
            ? BiomeState.DEAP_OCEAN
            : biome.is(BiomeTags.IS_OCEAN)
                ? BiomeState.OCEAN
                : biome.is(BiomeTags.IS_RIVER) ? BiomeState.RIVER : BiomeState.INVALID;
    boolean ready = type != BiomeState.INVALID && hasRotor() && checkSpace(1, true) == 0;
    setActive(ready);
    if (!ready) {
      stopSpinning();
      return;
    }
    int span = getRotorDiameter() / 2 * 4 + 1;
    crossSection = span * span;
    obstructedCrossSection = checkSpace(getRotorDiameter() * 3, false);
    if (obstructedCrossSection > 0 && obstructedCrossSection <= (getRotorDiameter() + 1) / 2)
      obstructedCrossSection = 0;
    if (obstructedCrossSection < 0) {
      stopSpinning();
      return;
    }
    double obstruction =
        Math.clamp((double) obstructedCrossSection / Math.max(1, crossSection), 0, 1);
    int wear;
    if (type == BiomeState.RIVER) {
      float flow = Math.clamp(distanceToNormalBiome, 20, 50) / 50.0F;
      speed(getFacing() == Direction.EAST || getFacing() == Direction.NORTH ? -flow : flow);
      waterFlow =
          (int)
              (flow
                  * 1000
                  * getEfficiency()
                  * Math.max(0, 1 - 0.3 * world.getRandom().nextFloat() - 0.1 * obstruction));
      wear = 1;
    } else {
      double tide = Math.sin(world.getGameTime() * Math.PI / 6000);
      tide *= Math.abs(tide);
      float flow = (float) (tide * distanceToNormalBiome / 100 * (1 - obstruction * obstruction));
      speed(rightFacing ? -flow : flow);
      waterFlow = (int) (flow * (type == BiomeState.DEAP_OCEAN ? 4000 : 3000) * getEfficiency());
      wear = type == BiomeState.DEAP_OCEAN ? 3 : 2;
    }
    rotorSlot.damage(wear, false);
    setChanged();
  }

  protected void stopSpinning() {
    speed(0);
    waterFlow = 0;
    budget.reset(0);
  }

  @Override
  public int getRotorDiameter() {
    int base = super.getRotorDiameter();
    return type == BiomeState.OCEAN ? base : (base + 1) * 2 / 3;
  }

  public int checkSpace(int depth, boolean planeOnly) {
    return RotorVolume.scan(
        getLevel(),
        worldPosition,
        getFacing(),
        getRotorDiameter(),
        depth,
        planeOnly,
        state ->
            state.getBlock() instanceof LiquidBlock && state.getFluidState().is(FluidTags.WATER),
        entity -> entity instanceof WaterTurbine);
  }

  public void updateSeaInfo() {
    var world = getLevel();
    if (world == null || getFacing().getAxis() == Direction.Axis.Y) {
      distanceToNormalBiome = 0;
      return;
    }
    rightFacing = true;
    distanceToNormalBiome = 200;
    for (int distance = 1; distance < 200; distance++) {
      if (!isValidBiome(BiomeUtil.getBiome(world, worldPosition.relative(getFacing(), distance)))) {
        distanceToNormalBiome = distance;
        return;
      }
      if (!isValidBiome(
          BiomeUtil.getBiome(world, worldPosition.relative(getFacing(), -distance)))) {
        distanceToNormalBiome = distance;
        rightFacing = false;
        return;
      }
    }
  }

  public boolean isValidBiome(Holder<Biome> biome) {
    return biome.is(BiomeTags.IS_RIVER)
        || biome.is(BiomeTags.IS_OCEAN)
        || biome.is(BiomeTags.IS_DEEP_OCEAN);
  }

  public int getKuOutput() {
    return production();
  }

  @Override
  protected int production() {
    return getActive() && hasRotor() && Double.isFinite(MULTIPLIER) && MULTIPLIER > 0
        ? (int) Math.clamp(Math.abs(waterFlow * MULTIPLIER), 0, Integer.MAX_VALUE)
        : 0;
  }

  @Override
  protected float animationScale() {
    return 0.1F;
  }

  public String getRotorHealth() {
    return health("ic2.WaterKineticGenerator.gui.rotorhealth");
  }

  @Override
  public OwnedMenu<WaterTurbine> createServerScreenHandler(int id, Player player) {
    return new WaterTurbineMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new WaterTurbineMenu(id, inventory, this);
  }

  public enum BiomeState {
    UNKNOWN,
    OCEAN,
    DEAP_OCEAN,
    RIVER,
    INVALID
  }
}
