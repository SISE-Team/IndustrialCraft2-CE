package ic2ce.neoforge.recipe.input;

import com.mojang.serialization.MapCodec;
import ic2ce.neoforge.material.CommonMaterialTags;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** Registry-only descriptor with live material membership across tag publication. */
public final class MaterialTagIngredient implements ICustomIngredient {
  public static final MapCodec<MaterialTagIngredient> CODEC =
      Identifier.CODEC
          .fieldOf("tag")
          .xmap(MaterialTagIngredient::new, MaterialTagIngredient::tagId);
  public static final IngredientType<MaterialTagIngredient> TYPE = new IngredientType<>(CODEC);

  private final Identifier tagId;
  private final java.util.List<TagKey<Item>> tags;

  public MaterialTagIngredient(Identifier tagId) {
    this.tagId = java.util.Objects.requireNonNull(tagId);
    tags =
        CommonMaterialTags.itemAliases(
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, tagId));
  }

  public Identifier tagId() {
    return tagId;
  }

  @Override
  public boolean test(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return false;
    for (int index = 0; index < tags.size(); index++) if (stack.is(tags.get(index))) return true;
    return false;
  }

  @Override
  public Stream<Holder<Item>> items() {
    return tags.stream()
        .flatMap(key -> BuiltInRegistries.ITEM.get(key).stream().flatMap(set -> set.stream()))
        .distinct();
  }

  // Simple native membership is cached; direct testing must see newly published tags.
  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public SlotDisplay display() {
    return new SlotDisplay.Composite(
        tags.stream().<SlotDisplay>map(SlotDisplay.TagSlotDisplay::new).toList());
  }

  @Override
  public boolean equals(Object value) {
    return value instanceof MaterialTagIngredient other && tagId.equals(other.tagId);
  }

  @Override
  public int hashCode() {
    return tagId.hashCode();
  }

  @Override
  public String toString() {
    return "MaterialTagIngredient[tagId=" + tagId + "]";
  }
}
