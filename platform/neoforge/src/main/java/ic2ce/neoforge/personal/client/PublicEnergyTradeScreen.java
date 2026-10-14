package ic2ce.neoforge.personal.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.personal.menu.PublicEnergyTradeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** This role exposes only its actual owner/customer permissions and synchronized invoice state. */
public class PublicEnergyTradeScreen extends MachineScreen<PublicEnergyTradeMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guienergyomatclosed.png");

  public PublicEnergyTradeScreen(PublicEnergyTradeMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    addElement(
        LiveLabel.create(
            this, 8, 72, LiveText.ofTranslated("container.inventory"), 0x404040, false));
    addElement(
        LiveLabel.create(
            this,
            12,
            21,
            LiveText.ofTranslated("ic2.container.personalTrader.want"),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            12,
            39,
            LiveText.ofTranslated("ic2.container.personalTrader.offer"),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            50,
            39,
            LiveText.of((com.google.common.base.Supplier<String>) () -> menu.base.euOffer + " EU"),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            12,
            57,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        LocalizedText.translate(
                            "ic2.container.personalTraderEnergy.paidFor", menu.base.paidFor)),
            0x404040,
            false));
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }
}
