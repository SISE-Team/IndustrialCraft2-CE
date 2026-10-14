package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.Objects;

/** Value suppliers share atlas metadata while retaining independent device calculations. */
public class SupplierGauge extends FillGauge<SupplierGauge> {
  private final IGaugeRatioProvider provider;

  public SupplierGauge(
      MachineScreen<?> gui, int x, int y, IGaugeRatioProvider provider, GaugeProperties style) {
    super(gui, x, y, style);
    this.provider = Objects.requireNonNull(provider);
  }

  public static SupplierGauge asFuel(
      MachineScreen<?> gui, int x, int y, IGaugeRatioProvider provider) {
    return create(gui, x, y, provider, GaugeStyle.Fuel);
  }

  public static SupplierGauge create(
      MachineScreen<?> gui, int x, int y, IGaugeRatioProvider provider, GaugeStyle style) {
    return new SupplierGauge(gui, x, y, provider, style.properties);
  }

  @Override
  protected double getRatio() {
    return provider.getRatio();
  }

  @FunctionalInterface
  public interface IGaugeRatioProvider {
    double getRatio();
  }
}
