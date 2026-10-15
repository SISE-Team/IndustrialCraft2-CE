package ic2ce.neoforge.kinetic;

import ic2ce.api.item.IKineticRotor;
import ic2ce.core.event.WorldData;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.kinetic.menu.WindTurbineMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Wind-specific altitude/obstruction sampling and overspeed wear over a conserved shaft budget. */
@NotClassic
public class WindTurbine extends RotorGearbox {
  private double windStrength;
  private int obstructedCrossSection,
      crossSection,
      updateTicker = ProjectRuntime.random.nextInt(32);
  public static final float outputModifier =
      10
          * NativeConfigValues.getFloat(
              ProjectConfiguration.get(), "balance/energy/kineticgenerator/wind");

  public WindTurbine(BlockPos position, BlockState block) {
    super(
        BlockEntityDefinitions.WIND_KINETIC_GENERATOR,
        position,
        block,
        IKineticRotor.GearboxType.WIND);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (++updateTicker >= getTickRate()) {
      updateTicker = 0;
      boolean ready = hasRotor() && rotorHasSpace();
      setActive(ready);
      if (!ready) {
        windStrength = 0;
        speed(0);
      } else {
        int span = getRotorDiameter() / 2 * 4 + 1;
        crossSection = span * span;
        obstructedCrossSection = checkSpace(getRotorDiameter() * 3, false);
        if (obstructedCrossSection > 0 && obstructedCrossSection <= (getRotorDiameter() + 1) / 2)
          obstructedCrossSection = 0;
        windStrength = obstructedCrossSection < 0 ? 0 : calcWindStrength();
        speed(
            (float)
                Math.clamp(
                    (windStrength - getMinWindStrength()) / Math.max(1, getMaxWindStrength()),
                    0,
                    2));
        if (isWindStrongEnough() && hasRotor()) {
          rotorSlot.damage(windStrength > getMaxWindStrength() ? 4 : 1, false);
          setChanged();
        }
      }
    }
    budget.reset(production());
  }

  public boolean facingMatchesDirection(Direction side) {
    return side == getFacing();
  }

  public int getTickRate() {
    return 32;
  }

  public int checkSpace(int depth, boolean planeOnly) {
    return RotorVolume.scan(
        getLevel(),
        worldPosition,
        getFacing(),
        getRotorDiameter(),
        depth,
        planeOnly,
        BlockState::isAir,
        entity -> entity instanceof WindTurbine);
  }

  public boolean rotorHasSpace() {
    return checkSpace(1, true) == 0;
  }

  public double calcWindStrength() {
    var world = getLevel();
    if (world == null || world.isClientSide() || crossSection <= 0 || obstructedCrossSection < 0)
      return 0;
    var simulation = WorldData.get(world).windSim;
    if (simulation == null) return 0;
    double blocked = Math.clamp((double) obstructedCrossSection / crossSection, 0, 1);
    return Math.max(0, simulation.getWindAt(worldPosition.getY()) * (1 - blocked * blocked));
  }

  public int getMinWindStrength() {
    var rotor = rotor();
    return rotor == null ? 0 : Math.max(0, rotor.getMinWindStrength(rotorSlot.get()));
  }

  public int getMaxWindStrength() {
    var rotor = rotor();
    return rotor == null ? 0 : Math.max(0, rotor.getMaxWindStrength(rotorSlot.get()));
  }

  public boolean isWindStrongEnough() {
    return hasRotor() && windStrength >= getMinWindStrength();
  }

  public boolean isRotorOverloaded() {
    return isWindStrongEnough() && rotorHasSpace() && windStrength > getMaxWindStrength();
  }

  public int getKuOutput() {
    return production();
  }

  @Override
  protected int production() {
    return getActive()
            && isWindStrongEnough()
            && Float.isFinite(outputModifier)
            && outputModifier > 0
        ? (int) Math.clamp(windStrength * outputModifier * getEfficiency(), 0, Integer.MAX_VALUE)
        : 0;
  }

  public int getWindStrength() {
    return (int) windStrength;
  }

  public int getObstructions() {
    return obstructedCrossSection;
  }

  public String getRotorHealth() {
    return health("ic2.WindKineticGenerator.gui.rotorhealth");
  }

  @Override
  public void setActive(boolean value) {
    if (value != getActive() && getLevel() != null && !getLevel().isClientSide())
      ProjectRuntime.network.get(true).updateTileEntityField(this, "rotorSlot");
    super.setActive(value);
  }

  @Override
  public OwnedMenu<WindTurbine> createServerScreenHandler(int id, Player player) {
    return new WindTurbineMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new WindTurbineMenu(id, inventory, this);
  }
}
