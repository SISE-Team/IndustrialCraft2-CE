package ic2ce.neoforge.processing.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Three packaging arrows use actual finite progress and correct local atlas geometry. */
public class FluidPackagingScreen extends MachineScreen<FluidPackagingMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guibottler.png");

  public FluidPackagingScreen(FluidPackagingMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 184);
    addElement(ElectricalGauge.asBolt(this, 12, 35, menu.base));
    addElement(ReservoirGauge.createNormal(this, 78, 34, menu.base.fluidTank));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float fraction) {
    super.extractBackground(graphics, mx, my, fraction);
    bindTexture();
    float progress = menu.base.getProgress();
    int width = Float.isNaN(progress) ? 0 : Math.round(Math.clamp(progress, 0, 1) * 16);
    if (width > 0) {
      drawTexturedRect(graphics, 61, 36, width, 13, 198, 0);
      drawTexturedRect(graphics, 61, 73, width, 13, 198, 0);
      drawTexturedRect(graphics, 99, 55, width, 13, 198, 0);
    }
  }
}
