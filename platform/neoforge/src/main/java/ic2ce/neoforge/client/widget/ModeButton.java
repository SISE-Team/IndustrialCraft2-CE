package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;

/** Selected mode buttons retain their highlight independently of pointer location. */
public class ModeButton extends NativeButton {
  private boolean selected;

  public ModeButton(
      MachineScreen<?> gui, int x, int y, int width, int height, ClickAction handler) {
    super(gui, x, y, width, height, handler);
  }

  public void setOn(boolean value) {
    selected = value;
  }

  public boolean isOn() {
    return selected;
  }

  @Override
  protected boolean isActive(int mx, int my) {
    return selected || super.isActive(mx, my);
  }

  @Override
  public ModeButton withDisableHandler(WidgetCondition condition) {
    super.withDisableHandler(condition);
    return this;
  }

  @Override
  public ModeButton withText(String text) {
    super.withText(text);
    return this;
  }

  @Override
  public ModeButton withTooltip(String text) {
    super.withTooltip(text);
    return this;
  }
}
