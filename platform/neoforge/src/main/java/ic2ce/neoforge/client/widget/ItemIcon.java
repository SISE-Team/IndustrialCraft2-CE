package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/** Samples the actual item supplier without manufacturing placeholder stacks per frame. */
public class ItemIcon extends ScreenWidget<ItemIcon> {
  private final Supplier<ItemStack> supplier;

  public ItemIcon(MachineScreen<?> gui, int x, int y, Supplier<ItemStack> supplier) {
    super(gui, x, y, 16, 16);
    this.supplier = Objects.requireNonNull(supplier);
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    var stack = supplier.get();
    if (stack != null && !stack.isEmpty()) gui.drawItem(x, y, stack);
  }
}
