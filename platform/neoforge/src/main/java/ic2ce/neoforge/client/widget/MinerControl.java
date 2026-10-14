package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Distinct miner mode controls retain the original atlas metadata. */
public class MinerControl extends ActionButton<MinerControl> {
  private final ButtonStyle style;

  public static MinerControl create(
      MachineScreen<?> gui, int x, int y, ClickAction click, ButtonStyle style) {
    return new MinerControl(gui, x, y, click, style);
  }

  protected MinerControl(MachineScreen<?> gui, int x, int y, ClickAction click, ButtonStyle style) {
    super(gui, x, y, Objects.requireNonNull(style).width, style.height, click);
    this.style = style;
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    bindCommonTexture();
    gui.drawTexturedRect(graphics, x, y, width, height, style.u, style.v);
    super.drawBackground(graphics, mx, my);
  }

  public enum ButtonStyle {
    AdvMinerReset(192, 32, 36, 15),
    AdvMinerMode(228, 32, 18, 15),
    AdvMinerSilkTouch(192, 47, 18, 15);
    final int u, v, width, height;

    ButtonStyle(int u, int v, int width, int height) {
      this.u = u;
      this.v = v;
      this.width = width;
      this.height = height;
    }
  }
}
