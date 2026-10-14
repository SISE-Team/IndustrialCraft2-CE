package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * An optional atlas background, a distinct hover overlay and the common consumed-click behavior.
 */
public class AtlasButton extends ActionButton<AtlasButton> {
  private final Identifier texture;
  private final AtlasRegion overlay;

  public AtlasButton(
      MachineScreen<?> gui, int x, int y, int width, int height, ClickAction handler) {
    this(gui, x, y, width, height, 0, 0, null, handler);
  }

  public AtlasButton(
      MachineScreen<?> gui,
      int x,
      int y,
      int width,
      int height,
      int u,
      int v,
      Identifier texture,
      ClickAction handler) {
    this(
        gui,
        x,
        y,
        width,
        height,
        new FixedAtlasRegion(u, v, Math.addExact(u, width), Math.addExact(v, height)),
        texture,
        handler);
  }

  public AtlasButton(
      MachineScreen<?> gui,
      int x,
      int y,
      int width,
      int height,
      AtlasRegion overlay,
      Identifier texture,
      ClickAction handler) {
    super(gui, x, y, width, height, handler);
    this.overlay = Objects.requireNonNull(overlay);
    this.texture = texture;
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    if (texture != null) {
      bindTexture(texture);
      gui.drawTexturedRect(
          graphics,
          x,
          y,
          width,
          height,
          overlay.getUS() / 256.0,
          overlay.getVS() / 256.0,
          overlay.getUE() / 256.0,
          overlay.getVE() / 256.0,
          false);
    }
    if (contains(mx, my)) gui.drawColoredRect(graphics, x, y, width, height, hoverColor);
    super.drawBackground(graphics, mx, my);
  }
}
