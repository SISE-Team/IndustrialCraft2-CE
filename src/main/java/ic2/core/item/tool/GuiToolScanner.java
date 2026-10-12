package ic2.core.item.tool;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ic2.core.Ic2Gui;
import ic2.core.init.Localization;
import ic2.core.util.Tuple;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiToolScanner extends Ic2Gui<ContainerToolScanner> {
   public GuiToolScanner(ContainerToolScanner container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 230);
   }

   @Override
   protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      super.drawForegroundLayer(graphics, mouseX, mouseY);
      this.drawString(graphics, 10, 20, Localization.translate("ic2.itemScanner.found"), 2157374);
      if (((ContainerToolScanner)this.menu).scanResults != null) {
         int count = 0;

         for (Tuple.T2<ItemStack, Integer> result : ((ContainerToolScanner)this.menu).scanResults) {
            String name = result.a.getItem().getName(result.a).getString();
            this.drawString(graphics, 10, 34 + count * 11, result.b + "x " + name, 5752026);
            if (++count == 10) {
               break;
            }
         }
      }
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      super.extractBackground(graphics, mouseX, mouseY, delta);
      if (((ContainerToolScanner)this.menu).scanResults != null) {
         int count = 0;

         for (Tuple.T2<ItemStack, Integer> result : ((ContainerToolScanner)this.menu).scanResults) {
            int xPos = 135 + (count & 1) * 15;
            this.drawItem(xPos, 11 * count + 28, result.a);
            if (++count == 10) {
               break;
            }
         }
      }
   }

   @Override
   public Identifier getTexture() {
      return Identifier.fromNamespaceAndPath("ic2", "textures/gui/guitoolscanner.png");
   }
}
