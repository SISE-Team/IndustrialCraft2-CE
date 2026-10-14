package ic2ce.neoforge.processing.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Solar availability projects an actual nonzero artwork rectangle without double screen offsets.
 */
public class DistillationScreen extends MachineScreen<DistillationMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guisolardestiller.png");

  public DistillationScreen(DistillationMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 184);
    addElement(ReservoirGauge.createPlain(this, 37, 43, 53, 18, menu.base.inputTank));
    addElement(ReservoirGauge.createPlain(this, 115, 55, 17, 43, menu.base.outputTank));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float fraction) {
    super.extractBackground(graphics, mx, my, fraction);
    bindTexture();
    if (menu.base.canWork()) drawTexturedRect(graphics, 36, 26, 97, 29, 0, 184);
  }
}
