package ic2ce.neoforge.reactor.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.reactor.menu.ReactorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Reactor presentation reads nominal output; drawing energy cannot change the displayed rate. */
public class ReactorScreen extends MachineScreen<ReactorMenu> {
  private static final Identifier ELECTRIC =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guinuclearreactor.png");
  private static final Identifier FLUID =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guinuclearreactorfluid.png");

  public ReactorScreen(ReactorMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 212, 243);
    WidgetCondition fluidMode = () -> menu.base.isFluidCooled();
    addElement(
        ReservoirGauge.createBorderless(this, 10, 54, menu.base.inputTank, true)
            .withEnableHandler(fluidMode));
    addElement(
        ReservoirGauge.createBorderless(this, 190, 54, menu.base.outputTank, false)
            .withEnableHandler(fluidMode));
    addElement(
        new DeviceGauge(this, 7, 136, menu.base, "heat", FillGauge.GaugeStyle.HeatNuclearReactor)
            .withTooltip(
                () ->
                    LocalizedText.translate(
                        "ic2.NuclearReactor.gui.info.temp", menu.base.getGuiValue("heat") * 100)));
    addElement(
        LiveLabel.create(
            this,
            107,
            136,
            200,
            13,
            LiveText.of(
                () ->
                    menu.base.isFluidCooled()
                        ? LocalizedText.translate(
                            "ic2.NuclearReactor.gui.info.HUoutput", menu.base.EmitHeat)
                        : LocalizedText.translate(
                            "ic2.NuclearReactor.gui.info.EUoutput",
                            Math.round(menu.base.getReactorEUEnergyOutput()))),
            5752026,
            false,
            4,
            0,
            false,
            true));
    addElement(
        new HoverRegion(this, 5, 160, 18, 18)
            .withTooltip(
                () ->
                    menu.base.isFluidCooled()
                        ? "ic2.NuclearReactor.gui.mode.fluid"
                        : "ic2.NuclearReactor.gui.mode.electric"));
  }

  @Override
  protected Identifier getTexture() {
    return menu.base.isFluidCooled() ? FLUID : ELECTRIC;
  }

  @Override
  public void extractBackground(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    super.extractBackground(graphics, mouseX, mouseY, delta);
    bindTexture();
    for (int row = 0; row < 6; row++)
      for (int column = menu.base.getReactorSize(); column < 9; column++)
        drawTexturedRect(graphics, 26 + column * 18, 25 + row * 18, 16, 16, 213, 1);
    if (menu.base.isFluidCooled()) {
      int heat = Math.clamp(menu.base.gaugeHeatScaled(160), 0, 160);
      for (int row = 0; row < 7; row++)
        drawTexturedRect(graphics, 186 - heat, 23 + row * 18, 0, 243, heat, 2);
    }
  }
}
