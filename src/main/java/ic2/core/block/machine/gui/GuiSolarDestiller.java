package ic2.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiSolarDestiller extends Ic2Gui<ContainerSolarDestiller> {
   public GuiSolarDestiller(ContainerSolarDestiller container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 184);
      this.addElement(TankGauge.createPlain(this, 37, 43, 53, 18, container.base.inputTank));
      this.addElement(TankGauge.createPlain(this, 115, 55, 17, 43, container.base.outputTank));
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      super.extractBackground(graphics, mouseX, mouseY, delta);
      this.bindTexture();
      if (((ContainerSolarDestiller)this.menu).base.canWork()) {
         this.drawTexturedRect(graphics, this.leftPos + 36, this.topPos + 26, 0.0, 184.0, 97.0, 29.0);
      }
   }

   @Override
   protected Identifier getTexture() {
      return Identifier.fromNamespaceAndPath("ic2", "textures/gui/guisolardestiller.png");
   }
}
