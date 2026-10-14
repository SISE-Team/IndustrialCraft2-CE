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
 * Ten physical heat conductors and two conversion reservoirs display actual transmitted and maximum
 * heat.
 */
public class HeatExchangeScreen extends MachineScreen<HeatExchangeMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guiheatsourcefluid.png");

  public HeatExchangeScreen(HeatExchangeMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 204);
    for (int y : new int[] {50, 72})
      addElement(
          new InventoryGrid(this, 46, y, 5, 1, InventoryGrid.SlotStyle.Plain, 1, 1)
              .withTooltip("ic2.LiquidHeatExchanger.gui.tooltipvent"));
    addElement(ReservoirGauge.createPlain(this, 19, 47, 12, 44, menu.base.getInputTank()));
    addElement(ReservoirGauge.createPlain(this, 145, 47, 12, 44, menu.base.getOutputTank()));
    addElement(
        LiveLabel.create(
                this,
                20,
                28,
                138,
                13,
                LiveText.of(
                    (com.google.common.base.Supplier<String>)
                        () ->
                            LocalizedText.translate(
                                "ic2.ElectricHeatGenerator.gui.hUmax",
                                menu.base.gettransmitHeat(),
                                menu.base.getMaxHeatEmittedPerTick())),
                0x57c4da,
                false,
                true,
                true)
            .withTooltip("ic2.LiquidHeatExchanger.gui.tooltipheat"));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
