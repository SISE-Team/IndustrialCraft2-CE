package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.component.AddonProcessor;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Progress samples the actual addon processing ledger; non-finite external values cannot corrupt
 * GUI geometry.
 */
public class ProcessGauge extends ScreenWidget<ProcessGauge> {
  private final AddonProcessor process;
  private final ProgressBarType type;

  public ProcessGauge(
      MachineScreen<?> gui, int x, int y, NativeBlockEntity owner, ProgressBarType type) {
    super(gui, x, y, Objects.requireNonNull(type).width, type.height);
    this.type = type;
    process =
        Objects.requireNonNull(
            owner.getComponent(AddonProcessor.class), "Missing processing ledger");
  }

  protected double getProgressRatio() {
    return process.getProgressRatio();
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    bindCommonTexture();
    gui.drawTexturedRect(graphics, x, y, width, height, type.emptyX, type.emptyY);
    double ratio = getProgressRatio();
    int filled = Double.isNaN(ratio) ? 0 : (int) Math.round(Math.clamp(ratio, 0, 1) * width);
    if (filled > 0) gui.drawTexturedRect(graphics, x, y, filled, height, type.fullX, type.fullY);
  }

  public enum ProgressBarType {
    type_1(165, 0, 165, 16, 22, 15),
    type_2(165, 35, 165, 52, 21, 11),
    type_3(165, 64, 165, 80, 22, 15),
    type_4(165, 96, 165, 112, 22, 15),
    type_5(133, 64, 133, 80, 18, 15);
    final int emptyX, emptyY, fullX, fullY, width, height;

    ProgressBarType(int x, int y, int fx, int fy, int w, int h) {
      emptyX = x;
      emptyY = y;
      fullX = fx;
      fullY = fy;
      width = w;
      height = h;
    }
  }
}
