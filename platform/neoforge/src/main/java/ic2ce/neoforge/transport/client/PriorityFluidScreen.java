package ic2ce.neoforge.transport.client;

import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.transport.menu.PriorityFluidMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Independent device projection over the synchronized priority controls. */
public class PriorityFluidScreen extends OutletOrderScreen<PriorityFluidMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guiweightedfluiddistributor.png");

  public PriorityFluidScreen(PriorityFluidMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 211);
    addElement(ReservoirGauge.createPlain(this, 33, 111, 110, 10, menu.base.fluidTank));
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }
}
