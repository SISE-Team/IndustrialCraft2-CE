package ic2ce.neoforge.client.widget;

import ic2ce.api.upgrade.*;
import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Compatibility is sampled from the actual device and live addon registry when a tooltip is
 * requested.
 */
public class UpgradeHint extends ScreenWidget<UpgradeHint> {
  private final IUpgradableBlock owner;

  public UpgradeHint(MachineScreen<?> gui, int x, int y, IUpgradableBlock owner) {
    super(gui, x, y, 10, 10);
    this.owner = Objects.requireNonNull(owner);
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    bindCommonTexture();
    gui.drawTexturedRect(graphics, x, y, width, height, 96, 128);
  }

  @Override
  protected List<Component> getToolTip() {
    var lines = new ArrayList<Component>();
    lines.add(Component.translatable("ic2.generic.text.upgrade"));
    var properties = owner.getUpgradableProperties();
    for (var stack : UpgradeRegistry.getUpgrades())
      if (stack.getItem() instanceof IUpgradeItem upgrade
          && upgrade.isSuitableFor(stack, properties)) lines.add(stack.getHoverName().copy());
    return lines;
  }
}
