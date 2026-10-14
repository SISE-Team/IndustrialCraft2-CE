package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;

/** A validated hit region for tooltips or recipe overlays, with no background extraction. */
public class HoverRegion extends ScreenWidget<HoverRegion> {
  public HoverRegion(MachineScreen<?> gui, int x, int y, int width, int height) {
    super(gui, x, y, width, height);
  }
}
