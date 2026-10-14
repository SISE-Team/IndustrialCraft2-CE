package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.menu.OwnedMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

/** Full native player-inventory decoration anchors to the menu's actual lower edge. */
public class PlayerInventoryScreen<T extends OwnedMenu<? extends Container>>
    extends BorderedScreen<T> {
  public PlayerInventoryScreen(T menu, Inventory inventory, Component title) {
    this(menu, inventory, title, 176, 166);
  }

  public PlayerInventoryScreen(T menu, Inventory inventory, Component title, int height) {
    this(menu, inventory, title, 176, height);
  }

  public PlayerInventoryScreen(
      T menu, Inventory inventory, Component title, int width, int height) {
    super(menu, inventory, title, width, height);
    addElement(new InventoryGrid(this, 7, height - 83, 9, 3, InventoryGrid.SlotStyle.Normal));
    addElement(new InventoryGrid(this, 7, height - 25, 9, 1, InventoryGrid.SlotStyle.Normal));
  }

  @Override
  protected void drawBackgroundAndTitle(
      GuiGraphicsExtractor graphics, float fraction, int mx, int my) {
    super.drawBackgroundAndTitle(graphics, fraction, mx, my);
    drawXCenteredString(graphics, imageWidth / 2, 6, title, 0x404040, false);
  }
}
