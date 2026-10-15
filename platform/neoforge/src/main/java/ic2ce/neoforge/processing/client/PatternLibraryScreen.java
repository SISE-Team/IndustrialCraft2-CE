package ic2ce.neoforge.processing.client;

import ic2ce.engine.text.SiFormat;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Pattern import/export and bounded selection show actual nonempty ownership and separate UU/EU
 * costs.
 */
public class PatternLibraryScreen extends MachineScreen<PatternLibraryMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guipatternstorage.png");

  public PatternLibraryScreen(PatternLibraryMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    button(7, 19, 9, 18, 0, "ic2.PatternStorage.gui.info.last");
    button(36, 19, 9, 18, 1, "ic2.PatternStorage.gui.info.next");
    button(10, 37, 16, 8, 2, "ic2.PatternStorage.gui.info.export");
    button(26, 37, 16, 8, 3, "ic2.PatternStorage.gui.info.import");
    addElement(
        LiveLabel.create(
            this,
            imageWidth / 2,
            30,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        Math.min(menu.base.index + 1, menu.base.maxIndex)
                            + " / "
                            + menu.base.maxIndex),
            0x404040,
            false,
            true,
            false));
    for (int index = 0; index < 3; index++) {
      final int field = index;
      String key =
          switch (field) {
            case 0 -> "ic2.generic.text.Name";
            case 1 -> "ic2.generic.text.UUMatte";
            default -> "ic2.generic.text.Energy";
          };
      addElement(
          LiveLabel.create(this, 10, 48 + 11 * field, LiveText.ofTranslated(key), 0xffffff, false));
      addElement(
          LiveLabel.create(
                  this,
                  80,
                  48 + 11 * field,
                  LiveText.of(
                      (com.google.common.base.Supplier<String>)
                          () ->
                              switch (field) {
                                case 0 ->
                                    hasPattern()
                                        ? menu.base.pattern.getHoverName().getString()
                                        : "";
                                case 1 ->
                                    SiFormat.format(menu.base.patternUu, 4)
                                        + LocalizedText.translate("ic2.generic.text.bucketUnit");
                                default ->
                                    SiFormat.format(menu.base.patternEu, 4)
                                        + LocalizedText.translate("ic2.generic.text.EU");
                              }),
                  0xffffff,
                  false)
              .withEnableHandler(this::hasPattern));
    }
    addElement(new ItemIcon(this, 152, 29, () -> menu.base.pattern));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  private boolean hasPattern() {
    return menu.base.pattern != null && !menu.base.pattern.isEmpty();
  }

  private void button(int x, int y, int width, int height, int event, String tooltip) {
    addElement(
        new AtlasButton(this, x, y, width, height, createEventSender(event)).withTooltip(tooltip));
  }
}
