package ic2.core.recipe;

import ic2.core.IC2;
import ic2.core.item.tool.ItemToolCrafting;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

final class CraftingRemainders {
   private CraftingRemainders() {}

   static NonNullList<ItemStack> get(CraftingInput input, boolean consuming) {
      NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
      if (consuming) return result;
      for (int slot = 0; slot < input.size(); slot++) {
         ItemStack stack = input.getItem(slot);
         if (stack.isEmpty()) continue;
         if (stack.getItem() instanceof ItemToolCrafting) {
            ItemStack remainder = stack.copyWithCount(1);
            remainder.setDamageValue(remainder.getDamageValue() + 1);
            if (remainder.getDamageValue() < remainder.getMaxDamage()) result.set(slot, remainder);
         } else {
            result.set(slot, IC2.envProxy.getRecipeRemainder(stack));
         }
      }
      return result;
   }
}
