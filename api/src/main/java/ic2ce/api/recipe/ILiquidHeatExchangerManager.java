package ic2ce.api.recipe;

import java.util.Map;
import net.minecraft.world.level.material.Fluid;

public interface ILiquidHeatExchangerManager extends ILiquidAcceptManager {
  void addFluid(Fluid var1, Fluid var2, int var3);

  ILiquidHeatExchangerManager.HeatExchangeProperty getHeatExchangeProperty(Fluid var1);

  Map<Fluid, ILiquidHeatExchangerManager.HeatExchangeProperty> getHeatExchangeProperties();

  ILiquidAcceptManager getSingleDirectionLiquidManager();

  public static class HeatExchangeProperty {
    public final Fluid outputFluid;
    public final int huPerMB;

    public HeatExchangeProperty(Fluid outputFluid, int huPerMB) {
      if (outputFluid == null
          || outputFluid == net.minecraft.world.level.material.Fluids.EMPTY
          || huPerMB <= 0)
        throw new IllegalArgumentException(
            "Heat exchange requires an actual output fluid and positive heat per volume");
      this.outputFluid = outputFluid;
      this.huPerMB = huPerMB;
    }
  }
}
