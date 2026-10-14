package ic2ce.neoforge.processing.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Distinct water input and pressure digits send exact bounded events; live heat, scale, output and
 * fluid identity remain separate.
 */
public class BoilerScreen extends MachineScreen<BoilerMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guisteamgenerator.png");

  public BoilerScreen(BoilerMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 220);
    addElement(ReservoirGauge.createPlain(this, 10, 155, 75, 47, menu.base.waterTank));
    addElement(
        new DeviceGauge(this, 13, 70, menu.base, "heat", FillGauge.GaugeStyle.HeatSteamGenerator)
            .withTooltip(
                () ->
                    LocalizedText.translate(
                        "ic2.SteamGenerator.gui.systemheat", menu.base.getSystemHeat())));
    addElement(
        new DeviceGauge(
                this,
                155,
                61,
                menu.base,
                "calcification",
                FillGauge.GaugeStyle.CalcificationSteamGenerator)
            .withTooltip(
                () ->
                    LocalizedText.translate(
                            "ic2.SteamGenerator.gui.calcification", menu.base.getCalcification())
                        + "%"));
    label(
        91,
        172,
        59,
        13,
        () ->
            menu.base.getInputMB()
                + LocalizedText.translate("ic2.generic.text.mb")
                + LocalizedText.translate("ic2.generic.text.tick"),
        "ic2.SteamGenerator.gui.info.waterinput");
    label(
        31,
        133,
        111,
        13,
        () -> LocalizedText.translate("ic2.SteamGenerator.gui.heatInput", menu.base.getHeatInput()),
        "ic2.SteamGenerator.gui.info.heatinput");
    label(
        22,
        35,
        42,
        13,
        () ->
            LocalizedText.translate(
                "ic2.SteamGenerator.gui.pressurevalve", menu.base.getPressure()),
        "ic2.SteamGenerator.gui.info.pressvalve");
    label(
        66,
        25,
        81,
        13,
        () ->
            menu.base.getOutputMB()
                + LocalizedText.translate("ic2.generic.text.mb")
                + LocalizedText.translate("ic2.generic.text.tick"),
        "ic2.SteamGenerator.gui.info.fluidoutput");
    label(66, 45, 100, 13, () -> LocalizedText.translate(menu.base.getOutputFluidName()), null);
    int[] magnitudes = {1000, 100, 10, 1};
    for (int digit = 0; digit < 4; digit++) {
      int step = magnitudes[digit];
      adjust(92 + 10 * digit, 162, step);
      adjust(92 + 10 * digit, 186, -step);
      if (digit < 3) {
        int pressure = magnitudes[digit + 1];
        adjust(23 + 10 * digit, 25, 2000 + pressure);
        adjust(23 + 10 * digit, 49, -(2000 + pressure));
      }
    }
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  private void adjust(int x, int y, int event) {
    addElement(
        new AtlasButton(
            this,
            x,
            y,
            9,
            9,
            button -> {
              if (button == PointerButton.left)
                ic2ce.neoforge.runtime.ProjectRuntime.network
                    .get(false)
                    .initiateClientTileEntityEvent(menu.base, event);
            }));
  }

  private void label(
      int x,
      int y,
      int width,
      int height,
      com.google.common.base.Supplier<String> text,
      String tooltip) {
    var label =
        LiveLabel.create(
            this, x, y, width, height, LiveText.of(text), 0x20ebbe, false, 4, 0, false, true);
    if (tooltip != null) label.withTooltip(tooltip);
    addElement(label);
  }
}
