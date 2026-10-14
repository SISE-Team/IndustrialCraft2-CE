package ic2ce.neoforge.energy.client;

import ic2ce.core.util.Util;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.energy.menu.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;

/**
 * Stored EU and rated output are distinct observations; the redstone mode icon has one screen
 * owner.
 */
public class BankScreen extends MachineScreen<BankMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guielectricblock.png");

  public BankScreen(BankMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 196);
    addElement(ElectricalGauge.asBar(this, 79, 38, menu.base));
    var redstone = new ItemStack(Items.REDSTONE);
    addElement(
        new NativeButton(this, 152, 4, 20, 20, createEventSender(0))
            .withIcon(() -> redstone)
            .withTooltip(menu.base::getRedstoneMode));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  @Override
  protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mx, int my) {
    super.drawForegroundLayer(graphics, mx, my);
    drawString(graphics, 8, 74, LocalizedText.translate("ic2.EUStorage.gui.info.armor"), 0x404040);
    drawString(graphics, 79, 40, LocalizedText.translate("ic2.EUStorage.gui.info.level"), 0x404040);
    drawString(
        graphics,
        110,
        50,
        Util.toSiString(Math.min(menu.base.energy.getEnergy(), menu.base.energy.getCapacity()), 6),
        0x404040);
    drawString(
        graphics, 110, 60, "/" + Util.toSiString(menu.base.energy.getCapacity(), 6), 0x404040);
    drawString(
        graphics,
        85,
        75,
        LocalizedText.translate("ic2.EUStorage.gui.info.output", menu.base.getOutput()),
        0x404040);
  }
}
