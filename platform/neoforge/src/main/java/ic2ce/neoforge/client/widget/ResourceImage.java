package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/** Resource-sized native image projection; UV denominators follow actual image dimensions. */
public class ResourceImage extends ScreenWidget<ResourceImage> {
  private final Identifier texture;
  private final int baseWidth, baseHeight;
  private final AtlasRegion overlay;
  private final boolean autoWidth, autoHeight;

  public static ResourceImage create(
      MachineScreen<?> gui,
      int x,
      int y,
      int width,
      int height,
      Identifier texture,
      int baseWidth,
      int baseHeight,
      int u,
      int v,
      int endU,
      int endV) {
    return create(
        gui,
        x,
        y,
        width,
        height,
        texture,
        baseWidth,
        baseHeight,
        new FixedAtlasRegion(u, v, endU, endV));
  }

  public static ResourceImage create(
      MachineScreen<?> gui,
      int x,
      int y,
      int width,
      int height,
      Identifier texture,
      int baseWidth,
      int baseHeight,
      AtlasRegion overlay) {
    return new ResourceImage(
        gui,
        x,
        y,
        Math.max(0, width),
        Math.max(0, height),
        texture,
        baseWidth,
        baseHeight,
        overlay,
        width < 0,
        height < 0);
  }

  protected ResourceImage(
      MachineScreen<?> gui,
      int x,
      int y,
      int width,
      int height,
      Identifier texture,
      int baseWidth,
      int baseHeight,
      AtlasRegion overlay,
      boolean autoWidth,
      boolean autoHeight) {
    super(gui, x, y, width, height);
    this.texture = Objects.requireNonNull(texture);
    this.overlay = Objects.requireNonNull(overlay);
    this.baseWidth = baseWidth;
    this.baseHeight = baseHeight;
    this.autoWidth = autoWidth;
    this.autoHeight = autoHeight;
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    var image = ImageDimensions.get(texture);
    if (image == null) {
      if (autoWidth) width = 0;
      if (autoHeight) height = 0;
      return;
    }
    if (autoWidth) width = image.getWidth();
    if (autoHeight) height = image.getHeight();
    if (width <= 0 || height <= 0) return;
    double canvasW = baseWidth > 0 ? baseWidth : image.getCanvasWidth(),
        canvasH = baseHeight > 0 ? baseHeight : image.getCanvasHeight();
    if (canvasW <= 0 || canvasH <= 0) return;
    int u = overlay.getUS(), v = overlay.getVS();
    double endU = overlay.getUE() < 0 ? (long) u + width : overlay.getUE(),
        endV = overlay.getVE() < 0 ? (long) v + height : overlay.getVE();
    bindTexture(texture);
    gui.drawTexturedRect(
        graphics,
        x,
        y,
        width,
        height,
        u / canvasW,
        v / canvasH,
        endU / canvasW,
        endV / canvasH,
        false);
  }
}
