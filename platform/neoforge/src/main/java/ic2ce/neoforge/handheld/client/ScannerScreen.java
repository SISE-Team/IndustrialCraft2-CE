package ic2ce.neoforge.handheld.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.handheld.menu.ScannerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Loaded ore result names and prototypes render from one coherent published menu generation. */
public class ScannerScreen extends MachineScreen<ScannerMenu> {
  public ScannerScreen(ScannerMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 230);
  }

  @Override
  protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    super.drawForegroundLayer(graphics, mouseX, mouseY);
    drawString(graphics, 10, 20, LocalizedText.translate("ic2.itemScanner.found"), 0x20EBBE);
    var rows = getContainer().scanResults;
    if (rows == null) return;
    for (int index = 0; index < Math.min(10, rows.size()); index++) {
      var row = rows.get(index);
      drawString(
          graphics,
          10,
          34 + 11 * index,
          row.b + "x " + row.a.getItem().getName(row.a).getString(),
          0x57C4FA);
    }
  }

  @Override
  public void extractBackground(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
    super.extractBackground(graphics, mouseX, mouseY, partial);
    var rows = getContainer().scanResults;
    if (rows == null) return;
    for (int index = 0; index < Math.min(10, rows.size()); index++)
      drawItem(135 + (index & 1) * 15, 28 + 11 * index, rows.get(index).a);
  }

  @Override
  public Identifier getTexture() {
    return Identifier.fromNamespaceAndPath("ic2", "textures/gui/guitoolscanner.png");
  }
}
