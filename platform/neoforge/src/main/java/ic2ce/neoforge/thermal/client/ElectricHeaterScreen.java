package ic2ce.neoforge.thermal.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.thermal.menu.ElectricHeaterMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Actual installed coils determine electrical conversion capacity and transmitted heat. */
public class ElectricHeaterScreen extends MachineScreen<ElectricHeaterMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guielectricheatgenerator.png");

  public ElectricHeaterScreen(ElectricHeaterMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(
        new InventoryGrid(this, 43, 26, 5, 2, InventoryGrid.SlotStyle.Normal)
            .withTooltip("ic2.ElectricHeatGenerator.gui.coils"));
    addElement(ElectricalGauge.asBolt(this, 12, 44, menu.base));
    addElement(
        LiveLabel.create(
                this,
                34,
                66,
                109,
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
            .withTooltip("ic2.ElectricHeatGenerator.gui.tooltipheat"));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
