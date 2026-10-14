package ic2ce.neoforge.handheld.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.handheld.menu.ToolboxMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Native toolbox uses the retained material and live translated title. */
public class ToolboxScreen extends MachineScreen<ToolboxMenu> {
  private static final Identifier MATERIAL =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guitoolbox.png");

  public ToolboxScreen(ToolboxMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
  }

  @Override
  protected void drawBackgroundAndTitle(
      GuiGraphicsExtractor graphics, float partial, int mouseX, int mouseY) {
    bindTexture();
    blit(graphics, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    drawString(graphics, 65, 11, title.getString(), 0, false);
  }

  @Override
  protected Identifier getTexture() {
    return MATERIAL;
  }
}
