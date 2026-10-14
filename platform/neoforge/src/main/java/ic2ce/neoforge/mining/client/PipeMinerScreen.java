package ic2ce.neoforge.mining.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.mining.menu.PipeMinerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Native view of this device's own synchronized ledger and inventory layout. */
public class PipeMinerScreen extends MachineScreen<PipeMinerMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guiminer.png");

  public PipeMinerScreen(PipeMinerMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 176, 166);
    addElement(ElectricalGauge.asBolt(this, 155, 41, menu.base));
  }

  @Override
  public Identifier getTexture() {
    return TEXTURE;
  }
}
