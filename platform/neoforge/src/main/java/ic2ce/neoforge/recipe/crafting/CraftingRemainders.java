package ic2ce.neoforge.recipe.crafting;

import ic2ce.neoforge.item.tool.DurableCraftingTool;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

/**
 * Consumable recipes, finite tool wear and native container remainders follow separate protocols.
 */
final class CraftingRemainders {
  private CraftingRemainders() {}

  static NonNullList<ItemStack> get(CraftingInput input, boolean consuming) {
    var result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
    if (consuming) return result;
    for (int slot = 0; slot < input.size(); slot++)
      result.set(slot, remainder(input.getItem(slot)));
    return result;
  }

  private static ItemStack remainder(ItemStack source) {
    if (source == null || source.isEmpty()) return ItemStack.EMPTY;
    if (source.getItem() instanceof DurableCraftingTool) {
      var tool = source.copyWithCount(1);
      if (tool.has(DataComponents.UNBREAKABLE)) return tool;
      int maximum = tool.getMaxDamage();
      long next = (long) Math.max(0, tool.getDamageValue()) + 1;
      if (maximum <= 0 || next >= maximum) return ItemStack.EMPTY;
      tool.setDamageValue((int) next);
      return tool;
    }
    var nativeRemainder = source.getItem().getCraftingRemainder(source);
    return nativeRemainder == null ? ItemStack.EMPTY : nativeRemainder.create();
  }
}
