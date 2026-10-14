package ic2ce.neoforge.transport.client;

import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.transport.menu.PriorityItemMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Independent device projection over the synchronized priority controls. */
public class PriorityItemScreen extends OutletOrderScreen<PriorityItemMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guiweighteditemdistributor.png");

  public PriorityItemScreen(PriorityItemMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 211);
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }
}
