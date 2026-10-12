package ic2.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerFluidBottler;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidBottler extends Ic2Gui<ContainerFluidBottler> {
   public GuiFluidBottler(ContainerFluidBottler container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 184);
      this.addElement(EnergyGauge.asBolt(this, 12, 35, container.base));
      this.addElement(TankGauge.createNormal(this, 78, 34, container.base.fluidTank));
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      super.extractBackground(graphics, mouseX, mouseY, delta);
      this.bindTexture();
      int progressSize = Math.round(((ContainerFluidBottler)this.menu).base.getProgress() * 16.0F);
      if (progressSize > 0) {
         this.drawTexturedRect(graphics, this.leftPos + 61, this.topPos + 36, 198.0, 0.0, progressSize, 13.0);
         this.drawTexturedRect(graphics, this.leftPos + 61, this.topPos + 73, 198.0, 0.0, progressSize, 13.0);
         this.drawTexturedRect(graphics, this.leftPos + 99, this.topPos + 55, 198.0, 0.0, progressSize, 13.0);
      }
   }

   @Override
   public Identifier getTexture() {
      return Identifier.fromNamespaceAndPath("ic2", "textures/gui/guibottler.png");
   }
}
