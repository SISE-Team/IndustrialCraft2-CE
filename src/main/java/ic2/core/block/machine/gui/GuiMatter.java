package ic2.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerMatter;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiMatter extends Ic2Gui<ContainerMatter> {
   public String progressLabel;
   public String amplifierLabel;

   public GuiMatter(ContainerMatter container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title);
      this.addElement(TankGauge.createNormal(this, 96, 22, container.base.fluidTank));
      this.progressLabel = Localization.translate("ic2.Matter.gui.info.progress");
      this.amplifierLabel = Localization.translate("ic2.Matter.gui.info.amplifier");
   }

   @Override
   protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      super.drawForegroundLayer(graphics, mouseX, mouseY);
      this.drawString(graphics, 8, 22, this.progressLabel, 4210752);
      this.drawString(graphics, 18, 31, ((ContainerMatter)this.menu).base.getProgressAsString(), 4210752);
      if (((ContainerMatter)this.menu).base.scrap > 0) {
         this.drawString(graphics, 8, 46, this.amplifierLabel, 4210752);
         this.drawString(graphics, 8, 58, ((ContainerMatter)this.menu).base.scrap + "", 4210752);
      }
   }

   @Override
   public Identifier getTexture() {
      return Identifier.fromNamespaceAndPath("ic2", "textures/gui/guimatter.png");
   }
}
