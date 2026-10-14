package ic2ce.neoforge.personal.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.personal.menu.PublicItemTradeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** This role exposes only its actual owner/customer permissions and synchronized invoice state. */
public class PublicItemTradeScreen extends MachineScreen<PublicItemTradeMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guitradeomatclosed.png");

  public PublicItemTradeScreen(PublicItemTradeMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    addElement(
        LiveLabel.create(
            this, 8, 72, LiveText.ofTranslated("container.inventory"), 0x404040, false));
    addElement(
        LiveLabel.create(
            this,
            12,
            23,
            LiveText.ofTranslated("ic2.container.personalTrader.want"),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            12,
            42,
            LiveText.ofTranslated("ic2.container.personalTrader.offer"),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            12,
            60,
            LiveText.ofTranslated("ic2.container.personalTrader.stock"),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            50,
            60,
            -1,
            -1,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () -> menu.base.stock < 0 ? "∞" : Integer.toString(menu.base.stock)),
            () -> menu.base.stock == 0 ? 0xff5555 : 0x404040,
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
