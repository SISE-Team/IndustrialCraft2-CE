package ic2ce.neoforge.personal.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.personal.menu.OwnerEnergyTradeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** This role exposes only its actual owner/customer permissions and synchronized invoice state. */
public class OwnerEnergyTradeScreen extends MachineScreen<OwnerEnergyTradeMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guienergyomatopen.png");

  public OwnerEnergyTradeScreen(OwnerEnergyTradeMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    addElement(
        LiveLabel.create(
            this, 8, 72, LiveText.ofTranslated("container.inventory"), 0x404040, false));
    String[] amounts = {"100k", "10k", "1k", "100"};
    for (int row = 0; row < amounts.length; row++) {
      addElement(
          new NativeButton(this, 102, 16 + 10 * row, 32, 10, createEventSender(row))
              .withText("-" + amounts[row]));
      addElement(
          new NativeButton(this, 134, 16 + 10 * row, 32, 10, createEventSender(row + 4))
              .withText("+" + amounts[row]));
    }
    addElement(
        LiveLabel.create(
            this,
            100,
            60,
            LiveText.ofTranslated("ic2.container.personalTrader.offer"),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            100,
            68,
            LiveText.of((com.google.common.base.Supplier<String>) () -> menu.base.euOffer + " EU"),
            0x404040,
            false));
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }
}
