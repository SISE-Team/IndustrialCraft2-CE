package ic2ce.neoforge.processing.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** UU production and its consumed amplifier reservoir display live localized state. */
public class MatterScreen extends MachineScreen<MatterMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guimatter.png");

  public MatterScreen(MatterMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(ReservoirGauge.createNormal(this, 96, 22, menu.base.fluidTank));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  @Override
  protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mx, int my) {
    super.drawForegroundLayer(graphics, mx, my);
    drawString(graphics, 8, 22, LocalizedText.translate("ic2.Matter.gui.info.progress"), 0x404040);
    drawString(graphics, 18, 31, menu.base.getProgressAsString(), 0x404040);
    if (menu.base.scrap > 0) {
      drawString(
          graphics, 8, 46, LocalizedText.translate("ic2.Matter.gui.info.amplifier"), 0x404040);
      drawString(graphics, 8, 58, Integer.toString(menu.base.scrap), 0x404040);
    }
  }
}
