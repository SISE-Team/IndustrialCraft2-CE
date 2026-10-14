package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.menu.document.LiveText;
import java.util.Objects;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Live label placement: alignment is recomputed from the current text, including translations and
 * owner changes.
 */
public class LiveLabel extends ScreenWidget<LiveLabel> {
  private final LiveText.ITextProvider textProvider;
  private final IntSupplier color;
  private final boolean shadow, fixedWidth, fixedHeight, centerX, centerY, right;
  private final int anchorX, anchorY;

  private LiveLabel(
      MachineScreen<?> gui,
      int x,
      int y,
      int width,
      int height,
      LiveText.ITextProvider text,
      IntSupplier color,
      boolean shadow,
      int dx,
      int dy,
      boolean centerX,
      boolean centerY,
      boolean right) {
    super(gui, x, y, Math.max(0, width), Math.max(0, height));
    textProvider = Objects.requireNonNull(text);
    this.color = Objects.requireNonNull(color);
    this.shadow = shadow;
    fixedWidth = width >= 0;
    fixedHeight = height >= 0;
    this.centerX = centerX;
    this.centerY = centerY;
    this.right = right;
    anchorX = x + dx + (centerX && fixedWidth ? width / 2 : 0);
    anchorY = y + dy + (centerY && fixedHeight ? (height + 1) / 2 : 0);
  }

  public static LiveLabel create(
      MachineScreen<?> gui, int x, int y, String text, int color, boolean shadow) {
    return create(gui, x, y, LiveText.of(text), color, shadow);
  }

  public static LiveLabel create(
      MachineScreen<?> gui, int x, int y, LiveText.ITextProvider text, int color, boolean shadow) {
    return create(gui, x, y, text, color, shadow, false, false);
  }

  public static LiveLabel create(
      MachineScreen<?> gui,
      int x,
      int y,
      String text,
      int color,
      boolean shadow,
      boolean centerX,
      boolean centerY) {
    return create(gui, x, y, LiveText.of(text), color, shadow, centerX, centerY);
  }

  public static LiveLabel create(
      MachineScreen<?> gui,
      int x,
      int y,
      LiveText.ITextProvider text,
      int color,
      boolean shadow,
      boolean centerX,
      boolean centerY) {
    return create(gui, x, y, -1, -1, text, color, shadow, centerX, centerY);
  }

  public static LiveLabel create(
      MachineScreen<?> gui,
      int x,
      int y,
      int width,
      int height,
      LiveText.ITextProvider text,
      int color,
      boolean shadow,
      boolean centerX,
      boolean centerY) {
    return create(gui, x, y, width, height, text, color, shadow, 0, 0, centerX, centerY);
  }

  public static LiveLabel create(
      MachineScreen<?> gui,
      int x,
      int y,
      int width,
      int height,
      LiveText.ITextProvider text,
      int color,
      boolean shadow,
      int dx,
      int dy,
      boolean centerX,
      boolean centerY) {
    return create(gui, x, y, width, height, text, () -> color, shadow, dx, dy, centerX, centerY);
  }

  public static LiveLabel create(
      MachineScreen<?> gui,
      int x,
      int y,
      int width,
      int height,
      LiveText.ITextProvider text,
      IntSupplier color,
      boolean shadow,
      int dx,
      int dy,
      boolean centerX,
      boolean centerY) {
    return new LiveLabel(
        gui, x, y, width, height, text, color, shadow, dx, dy, centerX, centerY, false);
  }

  public static LiveLabel createRightAligned(
      MachineScreen<?> gui,
      int x,
      int y,
      int width,
      int height,
      LiveText.ITextProvider text,
      int color,
      boolean shadow,
      int dx,
      int dy,
      boolean centerX,
      boolean centerY) {
    return new LiveLabel(
        gui, x, y, width, height, text, () -> color, shadow, dx, dy, centerX, centerY, true);
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    String text = textProvider.get(getBase(), getTokens());
    if (text == null) text = "";
    int textWidth = text.isEmpty() ? 0 : gui.getStringWidth(text),
        textHeight = text.isEmpty() ? 0 : 8;
    int tx = anchorX - (right ? textWidth : centerX ? textWidth / 2 : 0),
        ty = anchorY - (centerY ? textHeight / 2 : 0);
    if (!fixedWidth) {
      x = tx;
      width = textWidth;
    }
    if (!fixedHeight) {
      y = ty;
      height = textHeight;
    }
    if (!text.isEmpty()) gui.drawString(graphics, tx, ty, text, color.getAsInt(), shadow);
  }

  public enum TextAlignment {
    Start("start"),
    Center("center"),
    End("end");
    public final String name;

    TextAlignment(String name) {
      this.name = name;
    }

    public static TextAlignment get(String value) {
      if (value == null) return null;
      return switch (value) {
        case "start" -> Start;
        case "center" -> Center;
        case "end" -> End;
        default -> null;
      };
    }
  }
}
