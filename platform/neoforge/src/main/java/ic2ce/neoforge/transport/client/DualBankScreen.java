package ic2ce.neoforge.transport.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.transport.menu.DualBankMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Native view of this device's own synchronized ledger and inventory layout. */
public class DualBankScreen extends MachineScreen<DualBankMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guiitembuffer.png");

  public DualBankScreen(DualBankMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 176, 232);
  }

  @Override
  public Identifier getTexture() {
    return TEXTURE;
  }
}
