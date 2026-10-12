package ic2.core.item.tool;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ic2.core.Ic2Gui;
import ic2.core.gui.TextLabel;
import ic2.core.ref.Ic2Items;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiToolbox extends Ic2Gui<ContainerToolbox> {
   private static final Identifier background = Identifier.fromNamespaceAndPath("ic2", "textures/gui/guitoolbox.png");

   public GuiToolbox(ContainerToolbox container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title);
      this.addElement(TextLabel.create(this, 65, 11, net.minecraft.network.chat.Component.translatable(Ic2Items.TOOL_BOX.getDescriptionId()).getString(), 0, false));
   }

   @Override
   protected void drawBackgroundAndTitle(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
      this.bindTexture();
      this.blit(graphics, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
   }

   @Override
   protected Identifier getTexture() {
      return background;
   }
}
