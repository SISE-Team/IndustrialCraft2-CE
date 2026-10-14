package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import ic2ce.core.profile.NotClassic;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.ItemStack;

/**
 * MOX retains its shorter lifetime and temperature-dependent EU, with extra heat only in hot fluid
 * reactors.
 */
@NotClassic
public class MoxFuel extends UraniumFuel {
  public MoxFuel(Properties properties, int cells) {
    super(properties, cells, 10000);
  }

  private static float ratio(IReactor reactor) {
    return reactor.getMaxHeat() <= 0
        ? 0
        : Math.clamp((float) reactor.getHeat() / reactor.getMaxHeat(), 0, 1);
  }

  @Override
  protected int getFinalHeat(ItemStack fuel, IReactor reactor, int x, int y, int heat) {
    return reactor.isFluidCooled() && ratio(reactor) > 0.5F
        ? (int) Math.min(Integer.MAX_VALUE, (long) heat * 2)
        : heat;
  }

  @Override
  protected ItemStack getDepletedStack(ItemStack fuel, IReactor reactor) {
    return new ItemStack(
        switch (numberOfCells) {
          case 1 -> ItemDefinitions.DEPLETED_MOX_FUEL_ROD;
          case 2 -> ItemDefinitions.DEPLETED_DUAL_MOX_FUEL_ROD;
          default -> ItemDefinitions.DEPLETED_QUAD_MOX_FUEL_ROD;
        });
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
    if (!thermalPhase) reactor.addOutput(1 + 4 * ratio(reactor));
    return true;
  }
}
