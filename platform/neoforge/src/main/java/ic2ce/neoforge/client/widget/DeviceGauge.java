package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.menu.document.ScreenValues;
import java.util.Objects;

/** Samples the named device value on the current owner every render. */
public class DeviceGauge extends FillGauge<DeviceGauge> {
  private final ScreenValues provider;
  protected final String name;

  public DeviceGauge(
      MachineScreen<?> gui, int x, int y, ScreenValues provider, String name, IGaugeStyle style) {
    super(gui, x, y, Objects.requireNonNull(style).getProperties());
    this.provider = Objects.requireNonNull(provider);
    this.name = Objects.requireNonNull(name);
  }

  @Override
  protected double getRatio() {
    return provider.getGuiValue(name);
  }
}
