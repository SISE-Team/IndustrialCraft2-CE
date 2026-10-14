package ic2ce.neoforge.reactor.part;

import ic2ce.api.reactor.IReactor;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Heat cells absorb signed heat and return a remainder with the same sign as the request. */
public class HeatCell extends WearableReactorPart {
  public HeatCell(Properties properties, int capacity) {
    super(properties, capacity);
  }

  @Override
  public boolean canStoreHeat(ItemStack part, IReactor reactor, int x, int y) {
    return true;
  }

  @Override
  public int getMaxHeat(ItemStack part, IReactor reactor, int x, int y) {
    return getMaxUse();
  }

  @Override
  public int getCurrentHeat(ItemStack part, IReactor reactor, int x, int y) {
    return getUse(part);
  }

  @Override
  public int alterHeat(ItemStack part, IReactor reactor, int x, int y, int requested) {
    long total = (long) getUse(part) + requested;
    int capacity = getMaxHeat(part, reactor, x, y);
    if (total > capacity) {
      reactor.setItemAt(x, y, ItemStack.EMPTY);
      return (int) Math.min(Integer.MAX_VALUE, total - capacity - 1);
    }
    if (total < 0) {
      setUse(part, 0);
      return (int) Math.max(Integer.MIN_VALUE, total);
    }
    setUse(part, (int) total);
    return 0;
  }

  @Override
  public void appendHoverText(
      ItemStack part,
      TooltipContext context,
      TooltipDisplay display,
      Consumer<Component> tooltip,
      TooltipFlag flags) {
    if (getUse(part) > 0) {
      tooltip.accept(
          Component.translatable("ic2.reactoritem.heatwarning.line1")
              .withStyle(ChatFormatting.GRAY));
      tooltip.accept(
          Component.translatable("ic2.reactoritem.heatwarning.line2")
              .withStyle(ChatFormatting.GRAY));
    }
  }
}
