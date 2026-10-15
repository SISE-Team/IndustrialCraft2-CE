package ic2ce.neoforge.item.upgrade;

import ic2ce.neoforge.client.ClientEnvProxy;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.NativeButton;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.menu.document.DocumentScreen;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Metadata comparison is an ordinary usable control, with current translated policy feedback. */
public final class AdvancedFilterScreenFactory
    implements ClientEnvProxy.ScreenFactory<DocumentMenu<AdvancedFilterInventory>> {
  @Override
  public AbstractContainerScreen<DocumentMenu<AdvancedFilterInventory>> create(
      DocumentMenu<AdvancedFilterInventory> container, Inventory inventory, Component title) {
    var screen = DocumentScreen.create(container, inventory, title);
    screen.addElement(
        new NativeButton(
                screen,
                10,
                62,
                50,
                20,
                ignored -> {
                  container.base.nbt =
                      MetadataMatch.VALUES[
                          (container.base.nbt.ordinal() + 1) % MetadataMatch.VALUES.length];
                  ProjectRuntime.network
                      .get(false)
                      .sendHandHeldInvField(screen.getContainer(), "nbt");
                })
            .withText(() -> LocalizedText.translate("ic2.upgrade.advancedGUI.nbt"))
            .withTooltip(() -> LocalizedText.translate(container.base.nbt.name)));
    return screen;
  }
}
