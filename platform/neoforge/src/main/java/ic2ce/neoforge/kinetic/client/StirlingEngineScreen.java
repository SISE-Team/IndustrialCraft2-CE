package ic2ce.neoforge.kinetic.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.kinetic.menu.StirlingEngineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Stirling hot/cold circulation owns two actual independent reservoirs. */
public class StirlingEngineScreen extends MachineScreen<StirlingEngineMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guistirlingkineticgenerator.png");

  public StirlingEngineScreen(StirlingEngineMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 204);
    addElement(ReservoirGauge.createPlain(this, 19, 47, 12, 44, menu.base.getInputTank()));
    addElement(ReservoirGauge.createPlain(this, 145, 47, 12, 44, menu.base.getOutputTank()));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
