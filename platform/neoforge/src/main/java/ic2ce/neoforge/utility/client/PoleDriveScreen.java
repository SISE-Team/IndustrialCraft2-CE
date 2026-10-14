package ic2ce.neoforge.utility.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.utility.IndustrialFence;
import ic2ce.neoforge.utility.menu.PoleDriveMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Actual worn footwear selects the pole-drive safety status and color. */
public class PoleDriveScreen extends MachineScreen<PoleDriveMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guimagnetizer.png");

  public PoleDriveScreen(PoleDriveMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    addElement(ElectricalGauge.asBolt(this, 11, 28, menu.base));
    addElement(
        LiveLabel.create(
            this,
            18,
            66,
            -1,
            -1,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        LocalizedText.translate(
                            IndustrialFence.hasMetalShoes(menu.getPlayer())
                                ? "ic2.Magnetizer.gui.hasMetalShoes"
                                : "ic2.Magnetizer.gui.noMetalShoes")),
            () -> IndustrialFence.hasMetalShoes(menu.getPlayer()) ? 4259648 : 16728128,
            false,
            0,
            0,
            false,
            false));
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }
}
