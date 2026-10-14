package ic2ce.neoforge.client.widget;

import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.proxy.SideProxyClient;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Distinct normal, plain and mirrored gauges sample actual tank capacity with bounded zero-capacity
 * geometry.
 */
public class ReservoirGauge extends ScreenWidget<ReservoirGauge> {
  public static final int filledBackgroundU = 6,
      filledScaleU = 38,
      emptyU = 70,
      v = 100,
      normalWidth = 20,
      normalHeight = 55,
      fluidOffsetX = 4,
      fluidOffsetY = 4,
      fluidNetWidth = 12,
      fluidNetHeight = 47;
  private final Ic2FluidTank tank;
  private final Style style;

  private enum Style {
    NORMAL(true, true, false),
    PLAIN(false, false, false),
    BORDERLESS(false, true, false),
    MIRRORED(false, true, true);
    final boolean border, gauge, mirror;

    Style(boolean border, boolean gauge, boolean mirror) {
      this.border = border;
      this.gauge = gauge;
      this.mirror = mirror;
    }
  }

  private ReservoirGauge(
      MachineScreen<?> gui, int x, int y, int width, int height, Ic2FluidTank tank, Style style) {
    super(gui, x, y, width, height);
    this.tank = Objects.requireNonNull(tank);
    this.style = style;
  }

  public static ReservoirGauge createNormal(MachineScreen<?> gui, int x, int y, Ic2FluidTank tank) {
    return new ReservoirGauge(gui, x, y, 20, 55, tank, Style.NORMAL);
  }

  public static ReservoirGauge createPlain(
      MachineScreen<?> gui, int x, int y, int width, int height, Ic2FluidTank tank) {
    return new ReservoirGauge(gui, x, y, width, height, tank, Style.PLAIN);
  }

  public static ReservoirGauge createBorderless(
      MachineScreen<?> gui, int x, int y, Ic2FluidTank tank, boolean mirrored) {
    return new ReservoirGauge(
        gui, x, y, 12, 47, tank, mirrored ? Style.MIRRORED : Style.BORDERLESS);
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    bindCommonTexture();
    var contents = tank.getFluidStack();
    boolean filled = contents != null && !contents.isEmpty() && contents.getFluid() != null;
    if (style.border)
      gui.drawTexturedRect(
          graphics, x, y, width, height, filled ? filledBackgroundU : emptyU, v, style.mirror);
    if (filled) {
      int px = x + (style.border ? 4 : 0),
          py = y + (style.border ? 4 : 0),
          w = style.border ? 12 : width,
          h = style.border ? 47 : height;
      double ratio =
          tank.getCapacity() <= 0
              ? 0
              : Math.clamp((double) contents.getAmountMb() / tank.getCapacity(), 0, 1);
      double rendered = h * ratio;
      if (rendered > 0) {
        bindBlockTexture();
        gui.drawSprite(
            graphics,
            px,
            py + h - rendered,
            w,
            rendered,
            SideProxyClient.envProxy.getFluidStillSprite(contents),
            SideProxyClient.envProxy.getFluidColor(contents),
            1,
            false,
            true);
      }
    }
    if (style.gauge) {
      bindCommonTexture();
      if (filled)
        gui.drawTexturedRect(
            graphics,
            x - (style.border ? 0 : 4),
            y - (style.border ? 0 : 4),
            20,
            55,
            filledScaleU,
            v,
            style.mirror);
      else if (!style.border)
        gui.drawTexturedRect(graphics, x, y, width, height, 74, 104, style.mirror);
    }
  }

  @Override
  protected List<Component> getToolTip() {
    var contents = tank.getFluidStack();
    if (contents == null || contents.isEmpty())
      return List.of(Component.translatable("ic2.generic.text.empty"));
    if (contents.getFluid() == null) return List.of(Component.translatable("ic2.fluid.invalid"));
    return List.of(
        Component.literal(
            SideProxyClient.envProxy.getFluidName(contents)
                + ": "
                + contents.getAmountMb()
                + " "
                + LocalizedText.translate("ic2.generic.text.mb")));
  }
}
