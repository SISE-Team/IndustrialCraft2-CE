package ic2.core.gui;

import com.google.common.base.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import ic2.core.Ic2Gui;
import ic2.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;

public class ItemStackImage extends GuiElement<ItemStackImage> {
   private final Supplier<ItemStack> itemSupplier;

   public ItemStackImage(Ic2Gui<?> gui, int x, int y, Supplier<ItemStack> itemSupplier) {
      super(gui, x, y, 16, 16);
      this.itemSupplier = itemSupplier;
   }

   @Override
   public void drawBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      super.drawBackground(graphics, mouseX, mouseY);
      ItemStack stack = (ItemStack)this.itemSupplier.get();
      if (!StackUtil.isEmpty(stack)) {
         this.gui.drawItemStack(this.x, this.y, stack);
      }
   }

   @Override
   public void drawForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      if (this.contains(mouseX, mouseY)) {
         ItemStack stack = (ItemStack)this.itemSupplier.get();
         if (!StackUtil.isEmpty(stack)) {
            this.gui.drawTooltip(graphics, mouseX, mouseY, stack);
         }
      }
   }
}
