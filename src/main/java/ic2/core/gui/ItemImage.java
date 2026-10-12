package ic2.core.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ic2.core.Ic2Gui;
import ic2.core.util.StackUtil;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public class ItemImage extends GuiElement<ItemImage> {
   private final Supplier<ItemStack> itemSupplier;

   public ItemImage(Ic2Gui<?> gui, int x, int y, Supplier<ItemStack> itemSupplier) {
      super(gui, x, y, 16, 16);
      this.itemSupplier = itemSupplier;
   }

   @Override
   public void drawBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      super.drawBackground(graphics, mouseX, mouseY);
      ItemStack stack = this.itemSupplier.get();
      if (!StackUtil.isEmpty(stack)) {
         this.gui.drawItem(this.x, this.y, stack);
      }
   }
}
