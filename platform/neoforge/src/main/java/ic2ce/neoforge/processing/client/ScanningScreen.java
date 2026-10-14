package ic2ce.neoforge.processing.client;

import ic2ce.core.util.Util;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Every scanning state has distinct live messages and legal controls; scan animation uses native
 * local coordinates.
 */
public class ScanningScreen extends MachineScreen<ScanningMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guiscanner.png");

  public ScanningScreen(ScanningMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(ElectricalGauge.asBolt(this, 12, 25, menu.base));
    addElement(
        new AtlasButton(this, 102, 49, 12, 12, 176, 57, BACKGROUND, createEventSender(0))
            .withEnableHandler(
                () ->
                    switch (menu.base.getState()) {
                      case COMPLETED, TRANSFER_ERROR, FAILED -> true;
                      default -> false;
                    })
            .withTooltip("ic2.Scanner.gui.button.delete"));
    addElement(
        new AtlasButton(this, 143, 49, 24, 12, 176, 69, BACKGROUND, createEventSender(1))
            .withEnableHandler(
                () ->
                    switch (menu.base.getState()) {
                      case COMPLETED, TRANSFER_ERROR -> true;
                      default -> false;
                    })
            .withTooltip("ic2.Scanner.gui.button.save"));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  @Override
  protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mx, int my) {
    super.drawForegroundLayer(graphics, mx, my);
    var owner = menu.base;
    var state = owner.getState();
    drawString(graphics, 105, 6, LocalizedText.translate("ic2.Scanner.gui.info5") + ":", 0x404040);
    String key =
        switch (state) {
          case IDLE -> "ic2.Scanner.gui.idle";
          case SCANNING -> "ic2.Scanner.gui.info1";
          case NO_STORAGE -> "ic2.Scanner.gui.info2";
          case NO_ENERGY -> "ic2.Scanner.gui.info3";
          case COMPLETED, FAILED -> "ic2.Scanner.gui.info4";
          case TRANSFER_ERROR -> "ic2.Scanner.gui.info7";
          case ALREADY_RECORDED -> "ic2.Scanner.gui.info8";
        };
    int color =
        switch (state) {
          case NO_ENERGY, TRANSFER_ERROR, ALREADY_RECORDED -> 0xd71010;
          case IDLE, NO_STORAGE -> 0xebea20;
          default -> 0x20ebbe;
        };
    drawString(graphics, 10, 69, LocalizedText.translate(key), color);
    if (state == ic2ce.neoforge.processing.fabrication.PatternScanner.State.SCANNING)
      drawString(graphics, 125, 69, owner.getPercentageDone() + "%", 0x20ebbe);
    if (state == ic2ce.neoforge.processing.fabrication.PatternScanner.State.FAILED)
      drawString(graphics, 110, 30, LocalizedText.translate("ic2.Scanner.gui.info6"), 0xd71010);
    if (state == ic2ce.neoforge.processing.fabrication.PatternScanner.State.COMPLETED
        || state == ic2ce.neoforge.processing.fabrication.PatternScanner.State.TRANSFER_ERROR) {
      drawString(graphics, 105, 25, Util.toSiString(owner.patternUu, 4) + "B UUM", 0xffffff);
      drawString(graphics, 105, 36, Util.toSiString(owner.patternEu, 4) + "EU", 0xffffff);
    }
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float fraction) {
    super.extractBackground(graphics, mx, my, fraction);
    bindTexture();
    int progress = Math.clamp(menu.base.getSubPercentageDoneScaled(66), 0, 66);
    if (progress > 0) drawTexturedRect(graphics, 30, 20, progress, 14, 176, 43);
  }
}
