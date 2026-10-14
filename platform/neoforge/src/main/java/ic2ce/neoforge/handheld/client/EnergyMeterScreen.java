package ic2ce.neoforge.handheld.client;

import ic2ce.core.util.Util;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.AtlasButton;
import ic2ce.neoforge.handheld.menu.EnergyMeterMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Native energy report draws synchronized fractional statistics within the actual visible window.
 */
public class EnergyMeterScreen extends MachineScreen<EnergyMeterMenu> {
  private static final EnergyMeterMenu.Mode[] MODES = EnergyMeterMenu.Mode.values();

  public EnergyMeterScreen(EnergyMeterMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 217);
    for (int index = 0; index < MODES.length; index++) {
      var mode = MODES[index];
      addElement(
          new AtlasButton(
                  this,
                  112 + (index & 1) * 20,
                  55 + (index >> 1) * 20,
                  20,
                  20,
                  ignored -> menu.setMode(mode))
              .withTooltip("ic2.meter.mode." + mode.name()));
    }
  }

  @Override
  public boolean mouseClicked(
      net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
    double x = event.x() - leftPos, y = event.y() - topPos;
    if (x >= 26 && x <= 83 && y >= 111 && y <= 123) getContainer().reset();
    return super.mouseClicked(event, doubleClick);
  }

  @Override
  protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    super.drawForegroundLayer(graphics, mouseX, mouseY);
    var menu = getContainer();
    String unit =
        LocalizedText.translate(
            menu.getMode() == EnergyMeterMenu.Mode.Voltage
                ? "ic2.generic.text.v"
                : "ic2.generic.text.EUt");
    drawString(graphics, 115, 43, LocalizedText.translate("ic2.meter.mode"), 0x20EBBE);
    drawString(graphics, 15, 42, LocalizedText.translate("ic2.meter.avg"), 0x20EBBE);
    drawString(graphics, 15, 52, Util.toSiString(menu.getResultAvg(), 6) + unit, 0x20EBBE);
    drawString(graphics, 15, 66, LocalizedText.translate("ic2.meter.max/min"), 0x20EBBE);
    drawString(graphics, 15, 76, Util.toSiString(menu.getResultMax(), 6) + unit, 0x20EBBE);
    drawString(graphics, 15, 86, Util.toSiString(menu.getResultMin(), 6) + unit, 0x20EBBE);
    drawString(
        graphics,
        15,
        100,
        LocalizedText.translate("ic2.meter.cycle", menu.getResultCount() / 20),
        0x20EBBE);
    drawString(graphics, 39, 114, LocalizedText.translate("ic2.meter.mode.reset"), 0x20EBBE);
    drawString(
        graphics,
        105,
        136,
        LocalizedText.translate("ic2.meter.mode." + menu.getMode().name()),
        0x20EBBE);
  }

  @Override
  public void extractBackground(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
    super.extractBackground(graphics, mouseX, mouseY, partial);
    bindTexture();
    int v =
        switch (getContainer().getMode()) {
          case EnergyIn -> 0;
          case EnergyOut -> 40;
          case Voltage -> 80;
          case EnergyGain -> 120;
        };
    drawTexturedRect(graphics, 112, 55, 40, 40, 176, v);
  }

  @Override
  protected Identifier getTexture() {
    return Identifier.fromNamespaceAndPath("ic2", "textures/gui/guitooleumeter.png");
  }
}
