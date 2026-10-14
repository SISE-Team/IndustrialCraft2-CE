package ic2ce.neoforge.reactor.part;


import ic2ce.api.reactor.IReactor;
import ic2ce.api.reactor.IReactorComponent;
import ic2ce.engine.reactor.HeatBalance;
import ic2ce.neoforge.damage.RadiationEffect;
import ic2ce.neoforge.item.armor.SealedGarment;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Fuel pulse, neighbor heating and wear phases use four local recipients without per-pulse queues
 * or coordinate objects.
 */
public class UraniumFuel extends WearableReactorPart {
  public final int numberOfCells;

  public UraniumFuel(Properties properties, int cells) {
    this(properties, cells, 20000);
  }

  protected UraniumFuel(Properties properties, int cells, int duration) {
    super(properties, duration);
    if (cells != 1 && cells != 2 && cells != 4)
      throw new IllegalArgumentException("Unsupported fuel rod cell count");
    numberOfCells = cells;
  }

  @Override
  public void processChamber(ItemStack fuel, IReactor reactor, int x, int y, boolean thermalPhase) {
    if (!reactor.produceEnergy()) return;
    int base = 1 + numberOfCells / 2;
    for (int cell = 0; cell < numberOfCells; cell++) {
      if (!thermalPhase)
        for (int pulse = 0; pulse < base; pulse++)
          acceptUraniumPulse(fuel, reactor, fuel, x, y, x, y, false);
      int pulses =
          base
              + checkPulseable(reactor, x - 1, y, fuel, x, y, thermalPhase)
              + checkPulseable(reactor, x + 1, y, fuel, x, y, thermalPhase)
              + checkPulseable(reactor, x, y - 1, fuel, x, y, thermalPhase)
              + checkPulseable(reactor, x, y + 1, fuel, x, y, thermalPhase);
      if (thermalPhase)
        heatNeighbors(
            fuel, reactor, x, y, getFinalHeat(fuel, reactor, x, y, triangularNumber(pulses) * 4));
    }
    if (!thermalPhase) {
      if (getUse(fuel) >= getMaxUse() - 1) reactor.setItemAt(x, y, getDepletedStack(fuel, reactor));
      else incrementUse(fuel);
    }
  }

  private static ItemStack recipient(IReactor reactor, int x, int y) {
    var part = reactor.getItemAt(x, y);
    return part != null
            && !part.isEmpty()
            && part.getItem() instanceof IReactorComponent component
            && component.canStoreHeat(part, reactor, x, y)
        ? part
        : ItemStack.EMPTY;
  }

  private void heatNeighbors(ItemStack fuel, IReactor reactor, int x, int y, int heat) {
    var left = recipient(reactor, x - 1, y);
    var right = recipient(reactor, x + 1, y);
    var up = recipient(reactor, x, y - 1);
    var down = recipient(reactor, x, y + 1);
    int count =
        (left.isEmpty() ? 0 : 1)
            + (right.isEmpty() ? 0 : 1)
            + (up.isEmpty() ? 0 : 1)
            + (down.isEmpty() ? 0 : 1);
    if (!left.isEmpty()) heat = distribute(left, reactor, x - 1, y, heat, count--);
    if (!right.isEmpty()) heat = distribute(right, reactor, x + 1, y, heat, count--);
    if (!up.isEmpty()) heat = distribute(up, reactor, x, y - 1, heat, count--);
    if (!down.isEmpty()) heat = distribute(down, reactor, x, y + 1, heat, count);
    if (heat > 0) reactor.addHeat(heat);
  }

  private static int distribute(
      ItemStack part, IReactor reactor, int x, int y, int heat, int recipients) {
    if (heat <= 0 || recipients <= 0 || reactor.getItemAt(x, y) != part) return heat;
    int share = heat / recipients;
    int rejected = ((IReactorComponent) part.getItem()).alterHeat(part, reactor, x, y, share);
    return heat - share + Math.clamp(rejected, 0, share);
  }

  protected int getFinalHeat(ItemStack fuel, IReactor reactor, int x, int y, int heat) {
    return heat;
  }

  protected ItemStack getDepletedStack(ItemStack fuel, IReactor reactor) {
    return new ItemStack(
        switch (numberOfCells) {
          case 1 -> ItemDefinitions.DEPLETED_URANIUM_FUEL_ROD;
          case 2 -> ItemDefinitions.DEPLETED_DUAL_URANIUM_FUEL_ROD;
          default -> ItemDefinitions.DEPLETED_QUAD_URANIUM_FUEL_ROD;
        });
  }

  protected static int checkPulseable(
      IReactor reactor, int x, int y, ItemStack fuel, int fuelX, int fuelY, boolean thermalPhase) {
    var neighbor = reactor.getItemAt(x, y);
    return neighbor != null
            && !neighbor.isEmpty()
            && neighbor.getItem() instanceof IReactorComponent component
            && component.acceptUraniumPulse(
                neighbor, reactor, fuel, x, y, fuelX, fuelY, thermalPhase)
        ? 1
        : 0;
  }

  protected static int triangularNumber(int pulses) {
    return HeatBalance.triangular(pulses);
  }

  @Override
  public boolean acceptUraniumPulse(
      ItemStack fuel,
      IReactor reactor,
      ItemStack source,
      int x,
      int y,
      int sourceX,
      int sourceY,
      boolean thermalPhase) {
    if (!thermalPhase) reactor.addOutput(1);
    return true;
  }

  @Override
  public float influenceExplosion(ItemStack fuel, IReactor reactor) {
    return numberOfCells * 2;
  }

  @Override
  public void inventoryTick(ItemStack fuel, ServerLevel world, Entity holder, EquipmentSlot slot) {
    if (holder instanceof LivingEntity living && !SealedGarment.hasCompleteHazmat(living))
      RadiationEffect.radiation.applyTo(living, 200, 100);
  }
}
