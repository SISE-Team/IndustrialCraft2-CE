package ic2ce.api.recipe;

import java.util.Map;
import net.minecraft.world.level.material.Fluid;

public interface ISemiFluidFuelManager extends ILiquidAcceptManager {
  void addFluid(Fluid var1, int var2, double var3);

  ISemiFluidFuelManager.BurnProperty getBurnProperty(Fluid var1);

  Map<Fluid, ISemiFluidFuelManager.BurnProperty> getBurnProperties();

  public static final class BurnProperty {
    public final int amount;
    public final double power;

    public BurnProperty(int amount, double power) {
      if (amount <= 0 || !Double.isFinite(power) || power <= 0)
        throw new IllegalArgumentException(
            "Electrical fluid fuel requires positive volume and finite positive power");
      this.amount = amount;
      this.power = power;
    }
  }
}
