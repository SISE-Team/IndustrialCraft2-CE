package ic2ce.neoforge.client.widget;

import com.google.common.base.Supplier;
import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/** Samples the actual item supplier without manufacturing placeholder stacks per frame. */
public class StackIcon extends ScreenWidget<StackIcon> {
  private final Supplier<ItemStack> supplier;

  public StackIcon(MachineScreen<?> gui, int x, int y, Supplier<ItemStack> supplier) {
    super(gui, x, y, 16, 16);
    this.supplier = Objects.requireNonNull(supplier);
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    var stack = supplier.get();
    if (stack != null && !stack.isEmpty()) gui.drawItemStack(x, y, stack);
  }

  @Override
  public void drawForeground(GuiGraphicsExtractor graphics, int mx, int my) {
    if (!contains(mx, my)) return;
    var stack = supplier.get();
    if (stack != null && !stack.isEmpty()) gui.drawTooltip(graphics, mx, my, stack);
  }
}
