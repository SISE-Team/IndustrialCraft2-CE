package ic2ce.api.recipe;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** A weighted operation samples one owned native output; an empty distribution produces no item. */
public class MachineRecipeWeighted<I> extends MachineRecipe<I, Collection<ItemStack>> {
  private final RecipeOutputWeighted distribution;

  public MachineRecipeWeighted(I input, RecipeOutputWeighted distribution) {
    this(input, distribution, null);
  }

  public MachineRecipeWeighted(I input, RecipeOutputWeighted distribution, CompoundTag metadata) {
    super(input, null, metadata);
    this.distribution = Objects.requireNonNull(distribution);
  }

  @Override
  public Collection<ItemStack> getOutput() {
    var chosen = getOutputWeighted().drawOutput();
    return chosen == null || chosen.isEmpty() ? List.of() : List.of(chosen);
  }

  public RecipeOutputWeighted getOutputWeighted() {
    return distribution;
  }
}
