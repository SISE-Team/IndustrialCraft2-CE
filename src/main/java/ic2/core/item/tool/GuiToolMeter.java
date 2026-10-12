package ic2.core.item.tool;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ic2.core.Ic2Gui;
import ic2.core.gui.CustomButton;
import ic2.core.gui.IClickHandler;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiToolMeter extends Ic2Gui<ContainerMeter> {
   public GuiToolMeter(ContainerMeter container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 217);
      this.addElement(
         new CustomButton(this, 112, 55, 20, 20, this.createModeSetter(ContainerMeter.Mode.EnergyIn))
            .withTooltip("ic2.meter.mode.switch\nic2.meter.mode.EnergyIn")
      );
      this.addElement(
         new CustomButton(this, 132, 55, 20, 20, this.createModeSetter(ContainerMeter.Mode.EnergyOut))
            .withTooltip("ic2.meter.mode.switch\nic2.meter.mode.EnergyOut")
      );
      this.addElement(
         new CustomButton(this, 112, 75, 20, 20, this.createModeSetter(ContainerMeter.Mode.EnergyGain))
            .withTooltip("ic2.meter.mode.switch\nic2.meter.mode.EnergyGain")
      );
      this.addElement(
         new CustomButton(this, 132, 75, 20, 20, this.createModeSetter(ContainerMeter.Mode.Voltage))
            .withTooltip("ic2.meter.mode.switch\nic2.meter.mode.Voltage")
      );
   }

   private IClickHandler createModeSetter(ContainerMeter.Mode mode) {
      return button -> this.getContainer().setMode(mode);
   }

   @Override
   public void containerTick() {
      super.containerTick();
      this.getContainer().broadcastChanges();
   }

   @Override
   public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
double mouseX = event.x();
         double mouseY = event.y();
      int xMin = (this.width - this.imageWidth) / 2;
      int yMin = (this.height - this.imageHeight) / 2;
      int x = (int)(mouseX - xMin);
      int y = (int)(mouseY - yMin);
      if (x >= 26 && y >= 111 && x <= 83 && y <= 123) {
         this.getContainer().reset();
      }

      return super.mouseClicked(event, doubleClick);
   }

   @Override
   protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      super.drawForegroundLayer(graphics, mouseX, mouseY);
      ContainerMeter container = this.getContainer();
      String unit = container.getMode() == ContainerMeter.Mode.Voltage ? "ic2.generic.text.v" : "ic2.generic.text.EUt";
      unit = Localization.translate(unit);
      this.drawString(graphics, 115, 43, Localization.translate("ic2.meter.mode"), 2157374);
      this.drawString(graphics, 15, 42, Localization.translate("ic2.meter.avg"), 2157374);
      this.drawString(graphics, 15, 52, Util.toSiString(container.getResultAvg(), 6) + unit, 2157374);
      this.drawString(graphics, 15, 66, Localization.translate("ic2.meter.max/min"), 2157374);
      this.drawString(graphics, 15, 76, Util.toSiString(container.getResultMax(), 6) + unit, 2157374);
      this.drawString(graphics, 15, 86, Util.toSiString(container.getResultMin(), 6) + unit, 2157374);
      this.drawString(graphics, 15, 100, Localization.translate("ic2.meter.cycle", container.getResultCount() / 20), 2157374);
      this.drawString(graphics, 39, 114, Localization.translate("ic2.meter.mode.reset"), 2157374);
      switch (container.getMode()) {
         case EnergyIn:
            this.drawString(graphics, 105, 1236, Localization.translate("ic2.meter.mode.EnergyIn"), 2157374);
            break;
         case EnergyOut:
            this.drawString(graphics, 105, 1236, Localization.translate("ic2.meter.mode.EnergyOut"), 2157374);
            break;
         case EnergyGain:
            this.drawString(graphics, 105, 1236, Localization.translate("ic2.meter.mode.EnergyGain"), 2157374);
            break;
         case Voltage:
            this.drawString(graphics, 105, 1236, Localization.translate("ic2.meter.mode.Voltage"), 2157374);
      }
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      super.extractBackground(graphics, mouseX, mouseY, delta);
      this.bindTexture();
      ContainerMeter container = this.getContainer();
      switch (container.getMode()) {
         case EnergyIn:
            this.drawTexturedRect(graphics, 112.0, 55.0, 40.0, 40.0, 176.0, 0.0);
            break;
         case EnergyOut:
            this.drawTexturedRect(graphics, 112.0, 55.0, 40.0, 40.0, 176.0, 40.0);
            break;
         case EnergyGain:
            this.drawTexturedRect(graphics, 112.0, 55.0, 40.0, 40.0, 176.0, 120.0);
            break;
         case Voltage:
            this.drawTexturedRect(graphics, 112.0, 55.0, 40.0, 40.0, 176.0, 80.0);
      }
   }

   @Override
   protected Identifier getTexture() {
      return Identifier.fromNamespaceAndPath("ic2", "textures/gui/guitooleumeter.png");
   }
}
