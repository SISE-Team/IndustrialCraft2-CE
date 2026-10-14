package ic2ce.neoforge.item.tool;

import ic2ce.api.item.IBoxable;
import ic2ce.api.item.IItemHudInfo;
import ic2ce.neoforge.client.text.LocalizedText;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Shared finite tool display; tool operations and wear protocols belong to each concrete item. */
public class DurableCraftingTool extends Item implements IBoxable, IItemHudInfo {
  public static final String TOOLTIP_USES_LEFT = "ic2.tooltip.tool.uses_left";

  public DurableCraftingTool(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      net.minecraft.world.item.component.TooltipDisplay display,
      java.util.function.Consumer<Component> text,
      TooltipFlag flags) {
    text.accept(Component.translatable(TOOLTIP_USES_LEFT, getRemainingUses(stack)));
  }

  @Override
  public boolean canBeStoredInToolbox(ItemStack stack) {
    return stack != null && !stack.isEmpty();
  }

  @Override
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    return List.of(
        ChatFormatting.GRAY + LocalizedText.translate(TOOLTIP_USES_LEFT, getRemainingUses(stack)));
  }

  protected static int getRemainingUses(ItemStack stack) {
    return stack == null || stack.isEmpty()
        ? 0
        : Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
  }
}
