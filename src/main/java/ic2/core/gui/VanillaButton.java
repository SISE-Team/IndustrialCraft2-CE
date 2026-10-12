package ic2.core.gui;

import ic2.core.Ic2Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Modern GUI sprites provide the normal, highlighted and disabled nine-slice buttons. */
public class VanillaButton extends Button<VanillaButton> {
   protected IEnableHandler disableHandler;
   private static final Identifier NORMAL = Identifier.withDefaultNamespace("widget/button");
   private static final Identifier HOVER = Identifier.withDefaultNamespace("widget/button_highlighted");
   private static final Identifier DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");

   public VanillaButton(Ic2Gui<?> gui, int x, int y, int width, int height, IClickHandler handler) {
      super(gui, x, y, width, height, handler);
   }

   public VanillaButton withDisableHandler(IEnableHandler handler) {
      this.disableHandler = handler;
      return this;
   }

   public boolean isDisabled() { return this.disableHandler != null && !this.disableHandler.isEnabled(); }
   protected boolean isActive(int mouseX, int mouseY) { return this.contains(mouseX, mouseY); }

   @Override
   public void drawBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      Identifier sprite = this.isDisabled() ? DISABLED : this.isActive(mouseX, mouseY) ? HOVER : NORMAL;
      graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite,
         this.gui.getX() + this.x, this.gui.getY() + this.y, this.width, this.height);
      super.drawBackground(graphics, mouseX, mouseY);
   }

   @Override
   protected int getTextColor(int mouseX, int mouseY) {
      return this.isDisabled() ? 10526880 : this.isActive(mouseX, mouseY) ? 16777120 : 14737632;
   }

   @Override
   protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button) {
      return !this.isDisabled() && super.onMouseClick(mouseX, mouseY, button);
   }
}
