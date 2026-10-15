package ic2ce.neoforge.processing.uu;

import java.util.List;

public interface ILateRecipeResolver {
   List<RecipeTransformation> getTransformations(Iterable<LeanItemStack> var1);
}
