package ic2ce.neoforge.personal.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.personal.menu.OwnerItemTradeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** This role exposes only its actual owner/customer permissions and synchronized invoice state. */
public class OwnerItemTradeScreen extends MachineScreen<OwnerItemTradeMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guitradeomatopen.png");

  public OwnerItemTradeScreen(OwnerItemTradeMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    addElement(
        LiveLabel.create(
            this, 8, 72, LiveText.ofTranslated("container.inventory"), 0x404040, false));
    if (menu.canToggleInfinite)
      addElement(new NativeButton(this, 152, 4, 20, 20, createEventSender(0)).withText("∞"));
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
            57,
            LiveText.ofTranslated("ic2.container.personalTrader.offer"),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            108,
            28,
            LiveText.ofTranslated("ic2.container.personalTrader.totalTrades0"),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            108,
            36,
            LiveText.ofTranslated("ic2.container.personalTrader.totalTrades1"),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            112,
            44,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () -> Long.toString(menu.base.totalTradeCount)),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            108,
            60,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        LocalizedText.translate("ic2.container.personalTrader.stock")
                            + " "
                            + (menu.base.stock < 0 ? "∞" : Integer.toString(menu.base.stock))),
            0x404040,
            false));
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }
}
