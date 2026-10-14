package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * Explicit row/column projection with validated stride and atomically published visual variants.
 */
public class InventoryGrid extends ScreenWidget<InventoryGrid> {
  private final SlotStyle style;
  private final int columns, rows, border, spacing;

  public InventoryGrid(MachineScreen<?> gui, int x, int y, SlotStyle style) {
    this(gui, x, y, 1, 1, style);
  }

  public InventoryGrid(MachineScreen<?> gui, int x, int y, int columns, int rows, SlotStyle style) {
    this(gui, x, y, columns, rows, style, 0, 0);
  }

  public InventoryGrid(MachineScreen<?> gui, int x, int y, SlotStyle style, int border) {
    this(gui, x, y, 1, 1, style, border, 0);
  }

  public InventoryGrid(
      MachineScreen<?> gui,
      int x,
      int y,
      int columns,
      int rows,
      SlotStyle style,
      int border,
      int spacing) {
    super(
        gui,
        x - border,
        y - border,
        extent(columns, style.width, border, spacing),
        extent(rows, style.height, border, spacing));
    this.style = Objects.requireNonNull(style);
    this.columns = columns;
    this.rows = rows;
    this.border = border;
    this.spacing = spacing;
  }

  private static int extent(int count, int size, int border, int spacing) {
    if (count < 0 || count > 4096 || border < 0 || spacing < 0)
      throw new IllegalArgumentException("Invalid slot grid");
    if (count == 0) return 2 * border;
    return Math.addExact(
        Math.addExact(Math.multiplyExact(count, size), Math.multiplyExact(count - 1, spacing)),
        Math.multiplyExact(border, 2));
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    if (style.background == null) return;
    bindTexture(style.background);
    for (int row = 0; row < rows; row++)
      for (int col = 0; col < columns; col++)
        gui.drawTexturedRect(
            graphics,
            x + border + col * (style.width + spacing),
            y + border + row * (style.height + spacing),
            style.width,
            style.height,
            style.u,
            style.v);
  }

  @Override
  protected boolean suppressTooltip(int mx, int my) {
    var slot = gui.getFocusedSlot();
    return gui.getContainer().getCarried().isEmpty() && slot != null && slot.hasItem();
  }

  public static final class SlotStyle {
    public static final SlotStyle Normal = new SlotStyle(103, 7, 18, 18),
        Large = new SlotStyle(99, 35, 26, 26),
        Plain = new SlotStyle(16, 16);
    public static final int refSize = 16;
    public final int u, v, width, height;
    public final Identifier background;
    private static volatile Map<String, SlotStyle> styles =
        Map.of("normal", Normal, "large", Large, "plain", Plain);

    public SlotStyle(int u, int v, int width, int height) {
      this(u, v, width, height, ScreenWidget.commonTexture);
    }

    public SlotStyle(int width, int height) {
      this(0, 0, width, height, null);
    }

    public SlotStyle(int u, int v, int width, int height, Identifier background) {
      if (width <= 0 || height <= 0 || width > 4096 || height > 4096)
        throw new IllegalArgumentException("Invalid slot style dimensions");
      this.u = u;
      this.v = v;
      this.width = width;
      this.height = height;
      this.background = background;
    }

    public static synchronized void registerVarient(String name, SlotStyle style) {
      Objects.requireNonNull(name);
      Objects.requireNonNull(style);
      name = name.toLowerCase(Locale.ROOT);
      if (name.isBlank() || styles.containsKey(name))
        throw new IllegalArgumentException("Duplicate/empty slot style: " + name);
      var next = new LinkedHashMap<>(styles);
      next.put(name, style);
      styles = Map.copyOf(next);
    }

    public static SlotStyle get(String name) {
      return name == null ? null : styles.get(name.toLowerCase(Locale.ROOT));
    }
  }
}
