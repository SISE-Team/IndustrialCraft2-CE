package ic2.core.recipe.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class RecipeInputIngredient extends RecipeInputBase {
   private final Ingredient ingredient;
   private final int amount;

   public RecipeInputIngredient(Ingredient ingredient, int amount) {
      this.ingredient = ingredient;
      this.amount = amount;
   }

   @Override
   public boolean matches(ItemStack subject) {
      return !subject.isEmpty() && this.ingredient.test(subject);
   }

   @Override
   protected List<ItemStack> listStacks() {
      return this.ingredient.items().map(net.minecraft.world.item.ItemStack::new).toList();
   }

   @Override
   public int getAmount() {
      return this.amount;
   }

   @Override
   public Ingredient getIngredient() {
      return this.ingredient;
   }

   @Override
   public JsonElement toJson() {
      JsonElement element = com.mojang.serialization.JsonOps.INSTANCE instanceof com.mojang.serialization.DynamicOps<JsonElement> ops
            ? net.minecraft.world.item.crafting.Ingredient.CODEC.encodeStart(ops, this.ingredient).result().orElse(null)
            : null;
      if (this.amount == 1) {
         return element;
      } else if (element.isJsonObject()) {
         JsonObject obj = element.getAsJsonObject();
         obj.addProperty("count", this.amount);
         return obj;
      } else {
         JsonObject obj = new JsonObject();
         obj.add("any", element);
         obj.addProperty("count", this.amount);
         return obj;
      }
   }
}
