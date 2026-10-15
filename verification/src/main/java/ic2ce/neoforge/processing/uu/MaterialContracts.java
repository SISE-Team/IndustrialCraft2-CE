package ic2ce.neoforge.processing.uu;

import ic2ce.core.util.StackTag;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Native ingredient quantities, immutable custom-data identities and side-effect-free graph reads.
 */
public final class MaterialContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native materials: " + label);
  }

  public static Result run() {
    return new MaterialContracts().exercise();
  }

  private Result exercise() {
    var converted = RecipeUtil.convertIngredients(List.of(Ingredient.of(Items.RAW_IRON)));
    check(
        converted.size() == 1
            && converted.getFirst().size() == 1
            && converted.getFirst().getFirst().getItem() == Items.RAW_IRON,
        "vanilla smelting conversion retains its real ingredient rather than producing a free"
            + " transformation");
    var data = new CompoundTag();
    data.putInt("test", 1);
    var stack = new ItemStack(Items.IRON_INGOT);
    StackTag.set(stack, data);
    var key = new LeanItemStack(stack);
    int hash = key.hashCode();
    data.putInt("test", 2);
    key.getNbt().putInt("test", 3);
    check(
        key.hashCode() == hash && key.getNbt().getIntOr("test", 0) == 1,
        "source and returned custom data cannot mutate a graph key");
    var produced = key.toMcStack();
    StackTag.getOrCreate(produced).putInt("test", 4);
    check(
        key.getNbt().getIntOr("test", 0) == 1, "native output does not alias graph identity data");
    var rule =
        new RecipeTransformation(
            1,
            List.of(
                List.of(new LeanItemStack(Items.COAL, 1), new LeanItemStack(Items.DIAMOND, 3)),
                List.of(new LeanItemStack(Items.COAL, 3), new LeanItemStack(Items.DIAMOND, 1))),
            new LeanItemStack(Items.STONE));
    rule.merge();
    check(
        rule.inputs.size() == 2,
        "independent ingredient choices with different quantities are not conflated");
    int before = count();
    var unknown = new ItemStack(Items.DIAMOND);
    var unknownTag = new CompoundTag();
    unknownTag.putInt("unknown_material_probe", 12345);
    StackTag.set(unknown, unknownTag);
    check(
        UuGraph.get(unknown) == Double.POSITIVE_INFINITY
            && UuGraph.find(unknown).isEmpty()
            && count() == before,
        "unknown material reads never add new graph nodes");
    check(
        Double.isFinite(UuIndex.instance.get(new ItemStack(Items.IRON_ORE))),
        "server graph retains a finite calibrated iron-ore cost");

    var manual = new ManualRecipeResolver();
    var rods = manual.getTransformations();
    check(
        rods.size() == 6,
        "all single, double and quadruple uranium/MOX spent material mappings are present");
    for (var rod : rods)
      check(
          rod.transformCost == 0
              && rod.inputs.size() == 1
              && rod.inputs.getFirst().size() == 1
              && rod.inputs.getFirst().getFirst().getSize() == 1
              && rod.outputs.size() == 1
              && rod.outputs.getFirst().getSize() == 1
              && rod.inputs.getFirst().getFirst().getItem() != rod.outputs.getFirst().getItem(),
          "actual spent rod transformation is a complete forward-only one-rod bill");
    rods.getFirst().inputs.clear();
    check(
        manual.getTransformations().getFirst().inputs.size() == 1,
        "borrowed resolver rules cannot edit a later material-cost generation");
    return new Result(checks, List.copyOf(failures));
  }

  private static int count() {
    var entries = UuGraph.iterator();
    int count = 0;
    while (entries.hasNext()) {
      entries.next();
      count++;
    }
    return count;
  }
}
