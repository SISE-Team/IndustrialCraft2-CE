package ic2ce.neoforge.client.widget;

import ic2ce.core.util.Util;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.network.chat.Component;

/** The actual electrical ledger supplies distinct capacity and stored EU values. */
public class ElectricalGauge extends FillGauge<ElectricalGauge> {
  private final ElectricalBuffer ledger;

  public ElectricalGauge(
      MachineScreen<?> gui, int x, int y, NativeBlockEntity owner, EnergyGaugeStyle style) {
    super(gui, x, y, Objects.requireNonNull(style).properties);
    ledger =
        Objects.requireNonNull(
            owner.getComponent(ElectricalBuffer.class), "Owner has no electrical ledger");
  }

  public static ElectricalGauge asBar(MachineScreen<?> gui, int x, int y, NativeBlockEntity owner) {
    return new ElectricalGauge(gui, x, y, owner, EnergyGaugeStyle.Bar);
  }

  public static ElectricalGauge asBolt(
      MachineScreen<?> gui, int x, int y, NativeBlockEntity owner) {
    return new ElectricalGauge(gui, x, y, owner, EnergyGaugeStyle.Bolt);
  }

  @Override
  protected double getRatio() {
    return ledger.getFillRatio();
  }

  @Override
  protected List<Component> getToolTip() {
    return List.of(
        Component.literal(
            Util.toSiString(ledger.getEnergy(), 4)
                + "/"
                + Util.toSiString(ledger.getCapacity(), 4)
                + " "
                + LocalizedText.translate("ic2.generic.text.EU")));
  }

  public enum EnergyGaugeStyle {
    Bar(
        new GaugePropertyBuilder(132, 43, 24, 9, GaugePropertyBuilder.GaugeOrientation.Right)
            .withBackground(-4, -11, 32, 32, 128, 0)
            .build()),
    Bolt(
        new GaugePropertyBuilder(116, 65, 7, 13, GaugePropertyBuilder.GaugeOrientation.Up)
            .withBackground(-4, -1, 16, 16, 96, 64)
            .build()),
    StirlingBar(
        new GaugePropertyBuilder(176, 15, 58, 14, GaugePropertyBuilder.GaugeOrientation.Right)
            .withTexture(ProjectRuntime.getIdentifier("textures/gui/guistirlinggenerator.png"))
            .withBackground(59, 33)
            .build());
    public final String name;
    public final GaugeProperties properties;

    EnergyGaugeStyle(GaugeProperties properties) {
      this.properties = properties;
      name = name().toLowerCase(Locale.ROOT);
    }

    public static EnergyGaugeStyle get(String value) {
      if (value == null) return null;
      return switch (value) {
        case "bar" -> Bar;
        case "bolt" -> Bolt;
        case "stirlingbar" -> StirlingBar;
        default -> null;
      };
    }
  }
}
