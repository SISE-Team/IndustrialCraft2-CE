package ic2ce.neoforge.item.tooltip;

import ic2ce.api.item.ElectricItem;
import ic2ce.core.item.*;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Charge descriptions use public electrical contracts and avoid client class loading on dedicated
 * servers.
 */
public final class ElectricTooltip {
  private ElectricTooltip() {}

  public static void addTooltip(ItemStack stack, Consumer<Component> text) {
    addTooltip(stack, text, TooltipFlag.NORMAL);
  }

  public static void addTooltip(ItemStack stack, Consumer<Component> text, TooltipFlag flags) {
    if (stack == null || stack.isEmpty() || ElectricItem.manager.getMaxCharge(stack) <= 0) return;
    var description = ElectricItem.manager.getToolTip(stack);
    if (description == null || description.isBlank()) return;
    text.accept(Component.literal(description));
    if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient() && flags.hasShiftDown())
      text.accept(
          Component.translatable("ic2.item.tooltip.PowerTier", ElectricItem.manager.getTier(stack))
              .withStyle(ChatFormatting.GRAY));
  }
}
