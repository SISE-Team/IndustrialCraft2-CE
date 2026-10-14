package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Native nine-slice sprites select distinct disabled, hovered and idle states. */
public class NativeButton extends ActionButton<NativeButton> {
  private static final Identifier IDLE = Identifier.withDefaultNamespace("widget/button"),
      HOVER = Identifier.withDefaultNamespace("widget/button_highlighted"),
      DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");
  protected WidgetCondition disableHandler;

  public NativeButton(
      MachineScreen<?> gui, int x, int y, int width, int height, ClickAction handler) {
    super(gui, x, y, width, height, handler);
  }

  public NativeButton withDisableHandler(WidgetCondition condition) {
    disableHandler = condition;
    return this;
  }

  public boolean isDisabled() {
    return disableHandler != null && !disableHandler.isEnabled();
  }

  protected boolean isActive(int mx, int my) {
    return contains(mx, my);
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    graphics.blitSprite(
        RenderPipelines.GUI_TEXTURED,
        isDisabled() ? DISABLED : isActive(mx, my) ? HOVER : IDLE,
        gui.getX() + x,
        gui.getY() + y,
        width,
        height);
    super.drawBackground(graphics, mx, my);
  }

  @Override
  protected int getTextColor(int mx, int my) {
    return isDisabled() ? 0xa0a0a0 : isActive(mx, my) ? 0xffffa0 : 0xe0e0e0;
  }

  @Override
  protected boolean onMouseClick(int mx, int my, PointerButton button) {
    return !isDisabled() && super.onMouseClick(mx, my, button);
  }
}
