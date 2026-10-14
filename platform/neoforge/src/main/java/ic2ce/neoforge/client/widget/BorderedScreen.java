package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.menu.OwnedMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

/** Tiled original border artwork supports arbitrary validated native menu extents. */
public abstract class BorderedScreen<T extends OwnedMenu<? extends Container>>
    extends MachineScreen<T> {
  public BorderedScreen(T menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
  }

  public BorderedScreen(T menu, Inventory inventory, Component title, int height) {
    super(menu, inventory, title, height);
  }

  public BorderedScreen(T menu, Inventory inventory, Component title, int width, int height) {
    super(menu, inventory, title, width, height);
  }

  @Override
  protected Identifier getTexture() {
    return ScreenWidget.commonTexture;
  }

  @Override
  protected void drawBackgroundAndTitle(
      GuiGraphicsExtractor graphics, float fraction, int mx, int my) {
    bindCommon(graphics);
    for (int row = 0; row < 2; row++)
      for (int col = 0; col < 2; col++)
        drawTexturedRect(
            graphics,
            col == 0 ? -16 : imageWidth - 16,
            row == 0 ? -16 : imageHeight - 16,
            32,
            32,
            col * 64,
            row * 64);
    for (int x = 16; x < imageWidth - 16; x += 32) {
      int w = Math.min(32, imageWidth - 16 - x);
      drawTexturedRect(graphics, x, -16, w, 32, 32, 0);
      drawTexturedRect(graphics, x, imageHeight - 16, w, 32, 32, 64);
    }
    for (int y = 16; y < imageHeight - 16; y += 32) {
      int h = Math.min(32, imageHeight - 16 - y);
      drawTexturedRect(graphics, -16, y, 32, h, 0, 32);
      drawTexturedRect(graphics, imageWidth - 16, y, 32, h, 64, 32);
      for (int x = 16; x < imageWidth - 16; x += 32)
        drawTexturedRect(graphics, x, y, Math.min(32, imageWidth - 16 - x), h, 32, 32);
    }
  }

  private void bindCommon(GuiGraphicsExtractor graphics) {
    bindTexture(ScreenWidget.commonTexture);
  }
}
