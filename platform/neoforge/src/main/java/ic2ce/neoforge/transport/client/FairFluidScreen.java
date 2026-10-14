package ic2ce.neoforge.transport.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.transport.menu.FairFluidMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Fair division/concentration has an ordinary consumed widget click and a live actual mode label.
 */
public class FairFluidScreen extends MachineScreen<FairFluidMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guifluiddistributor.png");

  public FairFluidScreen(FairFluidMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 184);
    addElement(ReservoirGauge.createPlain(this, 29, 38, 55, 47, menu.base.fluidTank));
    addElement(
        LiveLabel.create(
            this,
            112,
            56,
            LiveText.ofTranslated("ic2.FluidDistributor.gui.mode.info"),
            5752026,
            false));
    addElement(
        LiveLabel.create(
            this,
            95,
            80,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        LocalizedText.translate(
                            menu.base.getActive()
                                ? "ic2.FluidDistributor.gui.mode.concentrate"
                                : "ic2.FluidDistributor.gui.mode.distribute")),
            5752026,
            false));
    addElement(new AtlasButton(this, 117, 58, 18, 8, createEventSender(1)));
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }
}
