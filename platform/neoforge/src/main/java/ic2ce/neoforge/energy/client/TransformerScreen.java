package ic2ce.neoforge.energy.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.energy.menu.*;
import ic2ce.neoforge.menu.document.LiveText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;

/**
 * Three distinct conversion policies retain exact server events and live input/output packet
 * limits.
 */
public class TransformerScreen extends MachineScreen<TransformerMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guitransfomer.png");

  public TransformerScreen(TransformerMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 219);
    addElement(
        LiveLabel.create(
            this, 6, 34, LiveText.ofTranslated("ic2.Transformer.gui.Output"), 0x404040, false));
    addElement(
        LiveLabel.create(
            this, 6, 48, LiveText.ofTranslated("ic2.Transformer.gui.Input"), 0x404040, false));
    addElement(
        LiveLabel.create(
            this,
            52,
            34,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        menu.base.getoutputflow()
                            + " "
                            + LocalizedText.translate("ic2.generic.text.EUt")),
            0x20ebbe,
            false));
    addElement(
        LiveLabel.create(
            this,
            52,
            48,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        menu.base.getinputflow()
                            + " "
                            + LocalizedText.translate("ic2.generic.text.EUt")),
            0x20ebbe,
            false));
    var icon = new ItemStack(ic2ce.neoforge.registry.ItemDefinitions.WRENCH);
    var policies = ic2ce.neoforge.energy.transformer.VoltageTransformer.Mode.values();
    for (int index = 0; index < 3; index++) {
      int selected = index;
      addElement(
          new NativeButton(this, 7, 65 + 20 * index, 144, 20, createEventSender(index))
              .withText("ic2.Transformer.gui.switch.mode" + (index + 1)));
      addElement(
          new ItemIcon(this, 152, 67 + 20 * index, () -> icon)
              .withEnableHandler(() -> menu.base.getMode() == policies[selected]));
    }
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
