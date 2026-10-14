package ic2ce.neoforge.processing.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Legacy food, staged fuel and foam progress follow the actual automatic input mode. */
public class LegacyCanningScreen extends MachineScreen<LegacyCanningMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/gui_canner_classic.png");

  public LegacyCanningScreen(LegacyCanningMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(
        new DeviceGauge(
            this, 74, 36, menu.base, "progress", FillGauge.GaugeStyle.ProgressLongArrow));
    addElement(ElectricalGauge.asBolt(this, 34, 28, menu.base));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
