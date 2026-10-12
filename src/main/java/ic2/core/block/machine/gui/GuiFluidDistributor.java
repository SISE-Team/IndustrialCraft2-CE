package ic2.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidDistributor extends Ic2Gui<ContainerFluidDistributor> {
   public GuiFluidDistributor(ContainerFluidDistributor container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 184);
      this.addElement(TankGauge.createPlain(this, 29, 38, 55, 47, container.base.fluidTank));
   }

   @Override
   protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      super.drawForegroundLayer(graphics, mouseX, mouseY);
      this.drawString(graphics, 112, 56, Localization.translate("ic2.FluidDistributor.gui.mode.info"), 5752026);
      if (((ContainerFluidDistributor)this.menu).base.getActive()) {
         this.drawString(graphics, 95, 80, Localization.translate("ic2.FluidDistributor.gui.mode.concentrate"), 5752026);
      } else {
         this.drawString(graphics, 95, 80, Localization.translate("ic2.FluidDistributor.gui.mode.distribute"), 5752026);
      }
   }

   @Override
   public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
      double mouseX = event.x() - this.leftPos;
      double mouseY = event.y() - this.topPos;
      if (mouseX >= 117.0 && mouseY >= 58.0 && mouseX <= 135.0 && mouseY <= 66.0) {
         IC2.network.get(false).initiateClientTileEntityEvent(((ContainerFluidDistributor)this.menu).base, 1);
         return true;
      } else {
         mouseX += this.leftPos;
         mouseY += this.topPos;
         return super.mouseClicked(event, doubleClick);
      }
   }

   @Override
   protected Identifier getTexture() {
      return Identifier.fromNamespaceAndPath("ic2", "textures/gui/guifluiddistributor.png");
   }
}
