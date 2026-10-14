package ic2ce.neoforge.agriculture.client;

import ic2ce.neoforge.agriculture.menu.FluidCareMenu;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Native view of this device's own synchronized ledger and inventory layout. */
public class FluidCareScreen extends MachineScreen<FluidCareMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guicropmatron.png");

  public FluidCareScreen(FluidCareMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 176, 192);
    addElement(ElectricalGauge.asBolt(this, 138, 82, menu.base));
    addElement(ReservoirGauge.createPlain(this, 11, 26, 24, 47, menu.base.getWaterTank()));
    addElement(ReservoirGauge.createPlain(this, 105, 26, 24, 47, menu.base.getExTank()));
  }

  @Override
  public Identifier getTexture() {
    return TEXTURE;
  }
}
