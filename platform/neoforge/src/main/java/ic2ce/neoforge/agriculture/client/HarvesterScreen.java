package ic2ce.neoforge.agriculture.client;

import ic2ce.neoforge.agriculture.menu.HarvesterMenu;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Native view of this device's own synchronized ledger and inventory layout. */
public class HarvesterScreen extends MachineScreen<HarvesterMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guicropharvester.png");

  public HarvesterScreen(HarvesterMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 176, 166);
    addElement(ElectricalGauge.asBolt(this, 19, 37, menu.base));
  }

  @Override
  public Identifier getTexture() {
    return TEXTURE;
  }
}
