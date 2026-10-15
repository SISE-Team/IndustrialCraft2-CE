package ic2ce.neoforge.reactor.part;

import ic2ce.neoforge.item.StackTag;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Owned bounded wear/heat counters share persistence and display, not component behavior. */
public abstract class WearableReactorPart extends ReactorPart {
  public static final String TOOLTIP_DURABILITY = "ic2.reactoritem.durability";
  private final int maxUse;

  protected WearableReactorPart(Properties properties, int maximum) {
    super(properties);
    if (maximum <= 0) throw new IllegalArgumentException("Invalid reactor-part lifetime");
    maxUse = maximum;
  }

  protected int getUse(ItemStack part) {
    var data = StackTag.get(part);
    return data == null ? 0 : Math.clamp(data.getIntOr("use", 0), 0, maxUse);
  }

  public void setUse(ItemStack part, int used) {
    int bounded = Math.clamp(used, 0, maxUse);
    StackTag.update(
        part,
        data -> {
          data.putInt("use", bounded);
          return data;
        });
  }

  protected void incrementUse(ItemStack part) {
    int used = getUse(part);
    if (used < maxUse) setUse(part, used + 1);
  }

  protected int getMaxUse() {
    return maxUse;
  }

  public double getUseFraction(ItemStack part) {
    return (double) getUse(part) / maxUse;
  }

  @Override
  public boolean isBarVisible(ItemStack part) {
    return true;
  }

  @Override
  public int getBarWidth(ItemStack part) {
    return (int) Math.round((1 - getUseFraction(part)) * 13);
  }

  @Override
  public int getBarColor(ItemStack part) {
    return Mth.hsvToRgb((float) ((1 - getUseFraction(part)) / 3), 1, 1);
  }

  @Override
  public void appendHoverText(
      ItemStack part,
      TooltipContext context,
      TooltipDisplay display,
      Consumer<Component> tooltip,
      TooltipFlag flags) {
    tooltip.accept(
        Component.translatable(TOOLTIP_DURABILITY)
            .append(" " + (maxUse - getUse(part)) + "/" + maxUse)
            .withStyle(ChatFormatting.GRAY));
  }
}
