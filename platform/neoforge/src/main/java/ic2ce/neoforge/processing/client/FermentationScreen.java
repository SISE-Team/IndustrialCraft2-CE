package ic2ce.neoforge.processing.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Biomass, biogas, supplied heat and fertilizer progress remain distinct live signals. */
public class FermentationScreen extends MachineScreen<FermentationMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guifermenter.png");

  public FermentationScreen(FermentationMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 184);
    addElement(ReservoirGauge.createPlain(this, 38, 49, 48, 30, menu.base.getInputTank()));
    addElement(ReservoirGauge.createNormal(this, 125, 22, menu.base.getOutputTank()));
    addElement(
        new DeviceGauge(this, 42, 41, menu.base, "heat", FillGauge.GaugeStyle.HeatFermenter)
            .withTooltip(
                () ->
                    LocalizedText.translate("ic2.Fermenter.gui.info.conversion")
                        + " "
                        + Math.round(Math.clamp(menu.base.getGuiValue("heat"), 0, 1) * 100)
                        + "%"));
    addElement(
        new DeviceGauge(this, 38, 88, menu.base, "progress", FillGauge.GaugeStyle.ProgressFermenter)
            .withTooltip("ic2.Fermenter.gui.info.waste"));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
