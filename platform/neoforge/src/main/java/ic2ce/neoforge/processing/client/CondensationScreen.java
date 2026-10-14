package ic2ce.neoforge.processing.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Two real steam/water reservoirs, four vent sockets and paid condensation progress. */
public class CondensationScreen extends MachineScreen<CondensationMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guicondenser.png");

  public CondensationScreen(CondensationMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 184);
    var vents =
        (java.util.function.Supplier<String>)
            () -> LocalizedText.translate("ic2.Condenser.gui.tooltipvent", 2);
    for (int x : new int[] {25, 133})
      addElement(
          new InventoryGrid(this, x, 25, 1, 2, InventoryGrid.SlotStyle.Normal).withTooltip(vents));
    addElement(ElectricalGauge.asBolt(this, 12, 26, menu.base));
    addElement(ReservoirGauge.createPlain(this, 46, 27, 84, 33, menu.base.getInputTank()));
    addElement(ReservoirGauge.createPlain(this, 46, 74, 84, 15, menu.base.getOutputTank()));
    addElement(
        new DeviceGauge(
            this, 47, 63, menu.base, "progress", FillGauge.GaugeStyle.ProgressCondenser));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
