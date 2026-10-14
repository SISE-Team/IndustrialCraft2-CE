package ic2ce.core.uu;

import java.util.List;

public interface ILateRecipeResolver {
   List<RecipeTransformation> getTransformations(Iterable<LeanItemStack> var1);
}
