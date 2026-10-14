package ic2ce.neoforge.recipe.processing;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.*;
import ic2ce.neoforge.recipe.input.ExactStackInput;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;

/**
 * Native two-input registries exercise actual ownership, independent quantities, replacement and
 * legacy API paths.
 */
public final class CanningRegistryContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native canning registry: " + label);
  }

  public static Result run() {
    return new CanningRegistryContracts().exercise();
  }

  private Result exercise() {
    var manager = new SolidCanningRecipes();
    var container = new ExactStackInput(new ItemStack(Items.BUCKET, 2));
    var resource = new ExactStackInput(new ItemStack(Items.BREAD, 3));
    var prototype = new ItemStack(Items.DIAMOND, 4);
    var metadata = new CompoundTag();
    metadata.putInt("value", 17);
    check(
        manager.addRecipe(
            new ICannerBottleRecipeManager.Input(container, resource), prototype, metadata, false),
        "actual dual native quantities register once");
    prototype.setCount(1);
    metadata.putInt("value", 0);
    var source =
        new ICannerBottleRecipeManager.RawInput(
            new ItemStack(Items.BUCKET, 5), new ItemStack(Items.BREAD, 7));
    var result = manager.apply(source, false);
    check(
        result != null
            && result.getOutput().getCount() == 4
            && result.getAdjustedInput().container.getCount() == 3
            && result.getAdjustedInput().fill.getCount() == 4
            && source.container.getCount() == 5
            && source.fill.getCount() == 7
            && result.getRecipe().getMetaData().getIntOr("value", 0) == 17,
        "actual item bills own caller quantities, output and metadata");
    result.getOutput().setCount(64);
    check(
        manager.apply(source, false).getOutput().getCount() == 4,
        "caller changes to a recipe result cannot overwrite the registered output prototype");
    check(
        manager.apply(
                new ICannerBottleRecipeManager.RawInput(
                    new ItemStack(Items.BUCKET), new ItemStack(Items.BREAD, 3)),
                false)
            == null,
        "insufficient first bill refuses normal native lookup");
    var buckets = new ItemStack(Items.BUCKET, 5);
    var bread = new ItemStack(Items.BREAD, 7);
    var old = manager.getOutputFor(buckets, bread, true, false);
    check(
        old != null
            && old.items.getFirst().getCount() == 4
            && buckets.getCount() == 3
            && bread.getCount() == 4,
        "deprecated native lookup returns the actual output and pays both independent bills");
    old = manager.getOutputFor(ItemStack.EMPTY, bread, true, true);
    check(
        old != null && bread.getCount() == 4,
        "one-sided acceptance hint never physically consumes an input");
    check(
        !manager.addRecipe(container, resource, new ItemStack(Items.EMERALD), false)
            && manager.addRecipe(container, resource, new ItemStack(Items.EMERALD), true)
            && manager.apply(source, false).getOutput().is(Items.EMERALD),
        "replace=false is nonmutating and accepted replacement invalidates old cached output");
    var iterator = manager.getRecipes().iterator();
    iterator.next();
    iterator.remove();
    check(
        manager.apply(source, false) == null,
        "native recipe iterator removal invalidates the actual ordered registry generation");
    var enrich = new EnrichmentRecipes();
    var fluid = Ic2FluidStack.create(Fluids.WATER, 500);
    var product = Ic2FluidStack.create(Fluids.LAVA, 250);
    var additive = new ExactStackInput(new ItemStack(Items.COAL, 2));
    enrich.addRecipe(fluid, additive, product);
    fluid.setAmountMb(1);
    product.setAmountMb(1);
    var raw =
        new ICannerEnrichRecipeManager.RawInput(
            Ic2FluidStack.create(Fluids.WATER, 1000), new ItemStack(Items.COAL, 5));
    var enriched = enrich.apply(raw, false);
    check(
        enriched != null
            && enriched.getOutput().getAmountMb() == 250
            && enriched.getAdjustedInput().fluid.getAmountMb() == 500
            && enriched.getAdjustedInput().additive.getCount() == 3
            && raw.fluid.getAmountMb() == 1000
            && raw.additive.getCount() == 5,
        "actual enrichment owns both fluid prototypes and separate additive/payment quantities");
    enriched.getOutput().setAmountMb(0);
    check(
        enrich.apply(raw, false).getOutput().getAmountMb() == 250,
        "borrowed native enrichment output cannot mutate later processing results");
    var shown = enrich.getRecipes().iterator().next();
    shown.getInput().fluid.setAmountMb(1);
    shown.getOutput().setAmountMb(1);
    check(
        enrich.apply(raw, false).getAdjustedInput().fluid.getAmountMb() == 500
            && enrich.apply(raw, false).getOutput().getAmountMb() == 250,
        "display recipe views cannot modify actual registered fluid payment or output");
    var exact =
        enrich.apply(
            new ICannerEnrichRecipeManager.RawInput(
                Ic2FluidStack.create(Fluids.WATER, 500), new ItemStack(Items.COAL, 2)),
            false);
    check(
        exact != null
            && exact.getAdjustedInput().fluid == null
            && exact.getAdjustedInput().additive.isEmpty(),
        "exhausted native enrichment uses its required null fluid sentinel and empty actual solid"
            + " remainder");
    check(
        enrich.apply(
                new ICannerEnrichRecipeManager.RawInput(
                    Ic2FluidStack.create(Fluids.LAVA, 1000), new ItemStack(Items.COAL, 5)),
                false)
            == null,
        "incompatible actual fluid identity refuses enrichment");
    var hint = enrich.getOutputFor(null, new ItemStack(Items.COAL, 1), true, true);
    check(hint != null, "legacy one-sided fluid acceptance avoids null-input mutation");
    return new Result(checks, List.copyOf(failures));
  }
}
