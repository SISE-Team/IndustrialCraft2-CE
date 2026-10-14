package ic2ce.neoforge.agriculture.client;

import ic2ce.neoforge.agriculture.menu.CellCareMenu;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Native view of this device's own synchronized ledger and inventory layout. */
public class CellCareScreen extends MachineScreen<CellCareMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/gui_cropmatron_classic.png");

  public CellCareScreen(CellCareMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 176, 166);
    addElement(ElectricalGauge.asBolt(this, 29, 39, menu.base));
  }

  @Override
  public Identifier getTexture() {
    return TEXTURE;
  }
}
