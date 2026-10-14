package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * A finite normalized fill clips the original visual atlas without allocating per-frame geometry.
 */
public abstract class FillGauge<T extends FillGauge<T>> extends ScreenWidget<T> {
  protected final GaugeProperties properties;

  protected FillGauge(MachineScreen<?> gui, int x, int y, GaugeProperties style) {
    super(gui, x + style.hoverXOffset, y + style.hoverYOffset, style.hoverWidth, style.hoverHeight);
    properties = Objects.requireNonNull(style);
  }

  protected abstract double getRatio();

  protected boolean isActive(double ratio) {
    return ratio > 0;
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    double value = getRatio();
    double ratio = Double.isNaN(value) ? 0 : Math.clamp(value, 0, 1);
    var p = properties;
    if (ratio <= 0 && p.bgWidth <= 0) return;
    bindTexture(p.texture);
    double px = x - p.hoverXOffset, py = y - p.hoverYOffset;
    if (p.bgWidth > 0 && p.bgHeight > 0) {
      boolean active = isActive(ratio);
      gui.drawTexturedRect(
          graphics,
          px + p.bgXOffset,
          py + p.bgYOffset,
          p.bgWidth,
          p.bgHeight,
          active ? p.uBgActive : p.uBgInactive,
          active ? p.vBgActive : p.vBgInactive);
    }
    if (ratio <= 0) return;
    double u = p.uInner, v = p.vInner, w = p.innerWidth, h = p.innerHeight;
    double full = p.vertical ? h : w, filled = ratio * full;
    if (!p.smooth) filled = Math.round(filled);
    if (filled <= 0) return;
    if (p.vertical) {
      if (p.reverse) {
        py += h - filled;
        v += h - filled;
      }
      h = filled;
    } else {
      if (p.reverse) {
        px += w - filled;
        u += w - filled;
      }
      w = filled;
    }
    gui.drawTexturedRect(graphics, px, py, w, h, u, v);
  }

  private static short coordinate(int value) {
    if (value < Short.MIN_VALUE || value > Short.MAX_VALUE)
      throw new IllegalArgumentException("Gauge coordinate overflow");
    return (short) value;
  }

  private static short size(int value) {
    if (value < 0) throw new IllegalArgumentException("Negative gauge dimension");
    return coordinate(value);
  }

  public static final class GaugeProperties {
    public final short uInner,
        vInner,
        innerWidth,
        innerHeight,
        hoverXOffset,
        hoverYOffset,
        hoverWidth,
        hoverHeight,
        bgXOffset,
        bgYOffset,
        bgWidth,
        bgHeight,
        uBgInactive,
        vBgInactive,
        uBgActive,
        vBgActive;
    public final boolean vertical, reverse, smooth;
    public final Identifier texture;

    public GaugeProperties(
        int u,
        int v,
        int width,
        int height,
        int hoverX,
        int hoverY,
        int hoverW,
        int hoverH,
        int bgX,
        int bgY,
        int bgW,
        int bgH,
        int inactiveU,
        int inactiveV,
        int activeU,
        int activeV,
        boolean vertical,
        boolean reverse,
        boolean smooth,
        Identifier texture) {
      uInner = coordinate(u);
      vInner = coordinate(v);
      innerWidth = size(width);
      innerHeight = size(height);
      hoverXOffset = coordinate(hoverX);
      hoverYOffset = coordinate(hoverY);
      hoverWidth = size(hoverW);
      hoverHeight = size(hoverH);
      bgXOffset = coordinate(bgX);
      bgYOffset = coordinate(bgY);
      bgWidth = coordinate(bgW);
      bgHeight = coordinate(bgH);
      uBgInactive = coordinate(inactiveU);
      vBgInactive = coordinate(inactiveV);
      uBgActive = coordinate(activeU);
      vBgActive = coordinate(activeV);
      this.vertical = vertical;
      this.reverse = reverse;
      this.smooth = smooth;
      this.texture = Objects.requireNonNull(texture);
    }
  }

  public static final class GaugePropertyBuilder {
    private final int u, v, width, height;
    private int hoverX,
        hoverY,
        hoverW,
        hoverH,
        bgX,
        bgY,
        bgW = -1,
        bgH = -1,
        inactiveU,
        inactiveV,
        activeU,
        activeV;
    private final GaugeOrientation direction;
    private boolean smooth = true;
    private Identifier texture = ScreenWidget.commonTexture;

    public GaugePropertyBuilder(int u, int v, int width, int height, GaugeOrientation direction) {
      coordinate(u);
      coordinate(v);
      size(width);
      size(height);
      this.u = u;
      this.v = v;
      this.width = width;
      this.height = height;
      hoverW = width;
      hoverH = height;
      this.direction = Objects.requireNonNull(direction);
    }

    public GaugePropertyBuilder withHoverBorder(int border) {
      if (border < 0) throw new IllegalArgumentException("Negative hover border");
      return withHover(
          -border,
          -border,
          Math.addExact(width, Math.multiplyExact(border, 2)),
          Math.addExact(height, Math.multiplyExact(border, 2)));
    }

    public GaugePropertyBuilder withHover(int x, int y, int width, int height) {
      coordinate(x);
      coordinate(y);
      size(width);
      size(height);
      hoverX = x;
      hoverY = y;
      hoverW = width;
      hoverH = height;
      return this;
    }

    public GaugePropertyBuilder withBackground(int u, int v) {
      return withBackground(0, 0, width, height, u, v);
    }

    public GaugePropertyBuilder withBackground(int x, int y, int width, int height, int u, int v) {
      return withBackground(x, y, width, height, u, v, u, v);
    }

    public GaugePropertyBuilder withBackground(
        int inactiveU, int inactiveV, int activeU, int activeV) {
      return withBackground(0, 0, width, height, inactiveU, inactiveV, activeU, activeV);
    }

    public GaugePropertyBuilder withBackground(
        int x, int y, int width, int height, int iu, int iv, int au, int av) {
      coordinate(x);
      coordinate(y);
      size(width);
      size(height);
      coordinate(iu);
      coordinate(iv);
      coordinate(au);
      coordinate(av);
      bgX = x;
      bgY = y;
      bgW = width;
      bgH = height;
      inactiveU = iu;
      inactiveV = iv;
      activeU = au;
      activeV = av;
      return this;
    }

    public GaugePropertyBuilder withSmooth(boolean enabled) {
      smooth = enabled;
      return this;
    }

    public GaugePropertyBuilder withTexture(Identifier texture) {
      this.texture = Objects.requireNonNull(texture);
      return this;
    }

    public GaugeProperties build() {
      return new GaugeProperties(
          u,
          v,
          width,
          height,
          hoverX,
          hoverY,
          hoverW,
          hoverH,
          bgX,
          bgY,
          bgW,
          bgH,
          inactiveU,
          inactiveV,
          activeU,
          activeV,
          direction.vertical,
          direction.reverse,
          smooth,
          texture);
    }

    public enum GaugeOrientation {
      Up(true, true),
      Down(true, false),
      Left(false, true),
      Right(false, false);
      final boolean vertical, reverse;

      GaugeOrientation(boolean vertical, boolean reverse) {
        this.vertical = vertical;
        this.reverse = reverse;
      }
    }
  }

  public enum GaugeStyle implements IGaugeStyle {
    Fuel(
        new FillGauge.GaugePropertyBuilder(
                112, 80, 13, 13, FillGauge.GaugePropertyBuilder.GaugeOrientation.Up)
            .withHover(0, 0, 14, 14)
            .withBackground(0, 0, 16, 16, 96, 80)
            .build()),
    Bucket(
        new FillGauge.GaugePropertyBuilder(
                110, 111, 14, 16, FillGauge.GaugePropertyBuilder.GaugeOrientation.Up)
            .withBackground(96, 111)
            .build()),
    ProgressWind(
        new FillGauge.GaugePropertyBuilder(
                242, 91, 13, 13, FillGauge.GaugePropertyBuilder.GaugeOrientation.Up)
            .withBackground(242, 63, 242, 77)
            .build()),
    ProgressArrow(
        new FillGauge.GaugePropertyBuilder(
                165, 16, 22, 15, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withBackground(-5, 0, 32, 16, 160, 0)
            .build()),
    ProgressArrowModern(
        new FillGauge.GaugePropertyBuilder(
                86, 234, 16, 10, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withBackground(0, 0, 16, 10, 70, 234)
            .build()),
    ProgressArrowModernReversed(
        new FillGauge.GaugePropertyBuilder(
                70, 244, 16, 10, FillGauge.GaugePropertyBuilder.GaugeOrientation.Left)
            .withBackground(0, 0, 16, 10, 86, 244)
            .build()),
    ProgressCrush(
        new FillGauge.GaugePropertyBuilder(
                165, 52, 21, 11, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withBackground(-5, -3, 32, 16, 160, 32)
            .build()),
    ProgressTriangle(
        new FillGauge.GaugePropertyBuilder(
                165, 80, 22, 15, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withBackground(-5, 0, 32, 16, 160, 64)
            .build()),
    ProgressDrop(
        new FillGauge.GaugePropertyBuilder(
                165, 112, 22, 15, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withBackground(-5, 0, 32, 16, 160, 96)
            .build()),
    ProgressRecycler(
        new FillGauge.GaugePropertyBuilder(
                133, 80, 18, 15, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withBackground(-5, 0, 32, 16, 128, 64)
            .build()),
    ProgressMetalFormer(
        new FillGauge.GaugePropertyBuilder(
                200, 19, 46, 9, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withBackground(-8, -3, 64, 16, 192, 0)
            .build()),
    ProgressCentrifuge(
        new FillGauge.GaugePropertyBuilder(
                252, 33, 3, 28, FillGauge.GaugePropertyBuilder.GaugeOrientation.Up)
            .withBackground(-1, -1, 5, 30, 246, 32)
            .build()),
    HeatCentrifuge(
        new FillGauge.GaugePropertyBuilder(
                225, 54, 20, 4, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withBackground(-1, -1, 22, 6, 224, 47)
            .build()),
    HeatNuclearReactor(
        new FillGauge.GaugePropertyBuilder(
                0, 243, 100, 13, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withHoverBorder(1)
            .withTexture(ProjectRuntime.getIdentifier("textures/gui/guinuclearreactor.png"))
            .build()),
    HeatSteamGenerator(
        new FillGauge.GaugePropertyBuilder(
                177, 1, 7, 76, FillGauge.GaugePropertyBuilder.GaugeOrientation.Up)
            .withHoverBorder(1)
            .withTexture(ProjectRuntime.getIdentifier("textures/gui/guisteamgenerator.png"))
            .build()),
    CalcificationSteamGenerator(
        new FillGauge.GaugePropertyBuilder(
                187, 1, 7, 58, FillGauge.GaugePropertyBuilder.GaugeOrientation.Up)
            .withHoverBorder(1)
            .withTexture(ProjectRuntime.getIdentifier("textures/gui/guisteamgenerator.png"))
            .build()),
    ProgressCondenser(
        new FillGauge.GaugePropertyBuilder(
                1, 185, 82, 7, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withHoverBorder(1)
            .withTexture(ProjectRuntime.getIdentifier("textures/gui/guicondenser.png"))
            .build()),
    HeatFermenter(
        new FillGauge.GaugePropertyBuilder(
                177, 10, 40, 3, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withHoverBorder(1)
            .withTexture(ProjectRuntime.getIdentifier("textures/gui/guifermenter.png"))
            .build()),
    ProgressFermenter(
        new FillGauge.GaugePropertyBuilder(
                177, 1, 40, 7, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withHoverBorder(1)
            .withTexture(ProjectRuntime.getIdentifier("textures/gui/guifermenter.png"))
            .build()),
    ProgressOreWasher(
        new FillGauge.GaugePropertyBuilder(
                177, 118, 18, 18, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withTexture(ProjectRuntime.getIdentifier("textures/gui/guiorewashingplant.png"))
            .withBackground(-1, -1, 20, 19, 102, 38)
            .build()),
    ProgressBlockCutter(
        new FillGauge.GaugePropertyBuilder(
                176, 15, 46, 17, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withTexture(ProjectRuntime.getIdentifier("textures/gui/guiblockcutter.png"))
            .withBackground(55, 33)
            .build()),
    ProgressLongArrow(
        new FillGauge.GaugePropertyBuilder(
                176, 15, 34, 13, FillGauge.GaugePropertyBuilder.GaugeOrientation.Right)
            .withTexture(ProjectRuntime.getIdentifier("textures/gui/gui_canner_classic.png"))
            .withBackground(74, 36)
            .build());
    private static volatile Map<String, IGaugeStyle> styles = initial();
    public final GaugeProperties properties;

    GaugeStyle(GaugeProperties properties) {
      this.properties = Objects.requireNonNull(properties);
    }

    @Override
    public GaugeProperties getProperties() {
      return properties;
    }

    private static Map<String, IGaugeStyle> initial() {
      var result = new LinkedHashMap<String, IGaugeStyle>();
      for (var style : values()) result.put(style.name().toLowerCase(Locale.ROOT), style);
      return Map.copyOf(result);
    }

    public static synchronized void addStyle(String name, IGaugeStyle style) {
      Objects.requireNonNull(name);
      Objects.requireNonNull(style);
      name = name.toLowerCase(Locale.ROOT);
      if (name.isBlank() || styles.containsKey(name))
        throw new IllegalArgumentException("Duplicate/empty gauge style: " + name);
      var next = new LinkedHashMap<>(styles);
      next.put(name, style);
      styles = Map.copyOf(next);
    }

    public static IGaugeStyle get(String name) {
      return name == null ? null : styles.get(name.toLowerCase(Locale.ROOT));
    }
  }

  @FunctionalInterface
  public interface IGaugeStyle {
    GaugeProperties getProperties();
  }
}
