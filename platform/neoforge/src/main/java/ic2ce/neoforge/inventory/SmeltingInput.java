package ic2ce.neoforge.inventory;

import ic2ce.api.recipe.Recipes;
import ic2ce.core.block.IInventorySlotHolder;
import net.minecraft.world.item.ItemStack;

/** Furnace processing uses the furnace manager, not the generic IC2 recipe index. */
public class SmeltingInput extends RecipeInputInventory<ItemStack, ItemStack, ItemStack> {
  public SmeltingInput(IInventorySlotHolder<?> holder, String name, int count) {
    super(holder, name, count, _ -> Recipes.furnace);
  }

  @Override
  public boolean accepts(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return false;
    var world = base.getParent().getLevel();
    return world != null && world.isClientSide()
        ? world
            .recipeAccess()
            .propertySet(net.minecraft.world.item.crafting.RecipePropertySet.FURNACE_INPUT)
            .test(stack)
        : super.accepts(stack);
  }

  @Override
  protected ItemStack getInput(ItemStack stack) {
    return stack;
  }

  @Override
  protected void setInput(ItemStack adjusted) {
    put(adjusted);
  }
}
