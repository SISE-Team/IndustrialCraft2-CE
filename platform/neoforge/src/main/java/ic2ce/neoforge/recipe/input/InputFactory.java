package ic2ce.neoforge.recipe.input;

import ic2ce.api.recipe.IRecipeInput;
import ic2ce.api.recipe.IRecipeInputFactory;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

/** Factory operations retain the distinct item, tag, native-predicate and container protocols. */
public final class InputFactory implements IRecipeInputFactory {
  @Override
  public IRecipeInput forItem(ItemLike item) {
    return forStack(new ItemStack(Objects.requireNonNull(item)));
  }

  @Override
  public IRecipeInput forStack(ItemStack stack) {
    return new ExactStackInput(stack);
  }

  @Override
  public IRecipeInput forStack(ItemStack stack, int amount) {
    return forStack(Objects.requireNonNull(stack).copyWithCount(InputDescriptor.positive(amount)));
  }

  @Override
  public IRecipeInput forTag(String name, int amount) {
    return forIngredient(new MaterialTagIngredient(Identifier.parse(name)).toVanilla(), amount);
  }

  @Override
  public IRecipeInput forFluidContainer(Fluid fluid, int amount) {
    return new FluidContentsInput(fluid, amount);
  }

  @Override
  public Ingredient getIngredient(IRecipeInput input) {
    return Objects.requireNonNull(input).getIngredient();
  }

  @Override
  public IRecipeInput forIngredient(Ingredient ingredient, int amount) {
    return new NativeIngredientInput(ingredient, amount);
  }
}
