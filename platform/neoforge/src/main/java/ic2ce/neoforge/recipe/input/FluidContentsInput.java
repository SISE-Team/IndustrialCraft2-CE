package ic2ce.neoforge.recipe.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.core.fluid.FluidHandler;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableObject;

/** Actual fluid contents determine eligibility; one physical container is the item bill. */
public final class FluidContentsInput extends InputDescriptor {
  public final Fluid fluid;
  public final int amount;

  private record Catalog(long generation, List<ItemStack> empty) {}

  private record StackKey(
      net.minecraft.world.item.Item item,
      net.minecraft.core.component.DataComponentMap components) {}

  private static volatile long generation;
  private static volatile Catalog catalog;
  private static final Map<Fluid, List<ItemStack>> filled = new IdentityHashMap<>();

  public FluidContentsInput(Fluid fluid) {
    this(fluid, 1000);
  }

  public FluidContentsInput(Fluid fluid, int amount) {
    this.fluid = fluid == Fluids.EMPTY ? null : fluid;
    this.amount = positive(amount);
  }

  @Override
  public boolean matches(ItemStack stack) {
    return matchesContents(stack, fluid, amount);
  }

  static boolean matchesContents(ItemStack stack, Fluid fluid, int amount) {
    if (stack == null || stack.isEmpty()) return false;
    var contents = Ic2FluidStack.getAll(stack);
    if (contents == null || contents.length == 0) {
      // The single-fluid contract distinguishes an empty supported container from a plain item.
      var content = Ic2FluidStack.get(stack);
      return content != null
          && (fluid == null
              ? content.isEmpty()
              : content.hasExactFluid(fluid) && content.getAmountMb() >= amount);
    }
    if (fluid == null) {
      for (var content : contents) if (content != null && !content.isEmpty()) return false;
      return true;
    }
    for (var content : contents)
      if (content != null && content.hasExactFluid(fluid) && content.getAmountMb() >= amount)
        return true;
    return false;
  }

  @Override
  public int getAmount() {
    return 1;
  }

  @Override
  protected List<ItemStack> listStacks() {
    return getFluidContainer(fluid).stream().filter(this::matches).toList();
  }

  @Override
  public Ingredient getIngredient() {
    return new FluidContentsIngredient(
            BuiltInRegistries.FLUID.getKey(fluid == null ? Fluids.EMPTY : fluid), amount)
        .toVanilla();
  }

  @Override
  public JsonElement toJson() {
    var object = new JsonObject();
    object.addProperty(
        "fluid", BuiltInRegistries.FLUID.getKey(fluid == null ? Fluids.EMPTY : fluid).toString());
    object.addProperty("amount", amount);
    return object;
  }

  @Override
  public boolean equals(Object value) {
    return value instanceof FluidContentsInput other
        && fluid == other.fluid
        && amount == other.amount;
  }

  @Override
  public int hashCode() {
    return 31 * System.identityHashCode(fluid) + amount;
  }

  @Override
  public String toString() {
    return "FluidInput[" + fluid + ", " + amount + "mB]";
  }

  public static synchronized void invalidateContainers() {
    generation++;
    catalog = null;
    filled.clear();
  }

  private static Catalog emptyCatalog() {
    var current = catalog;
    if (current != null && current.generation == generation) return current;
    synchronized (FluidContentsInput.class) {
      current = catalog;
      if (current != null && current.generation == generation) return current;
      var unique = new LinkedHashMap<StackKey, ItemStack>();
      for (var item : BuiltInRegistries.ITEM) {
        var stack = new ItemStack(item);
        if (stack.isEmpty()) continue;
        var content = Ic2FluidStack.get(stack);
        if (content == null) continue;
        if (!content.isEmpty()) {
          var replacement = new MutableObject<ItemStack>();
          FluidHandler.drainMb(stack, Integer.MAX_VALUE, false, replacement);
          stack = replacement.getValue();
        }
        if (stack != null && matchesContents(stack, null, 1)) {
          var owned = stack.copyWithCount(1);
          unique.putIfAbsent(new StackKey(owned.getItem(), owned.getComponents()), owned);
        }
      }
      return catalog = new Catalog(generation, List.copyOf(unique.values()));
    }
  }

  public static List<ItemStack> getFluidContainer(Fluid fluid) {
    synchronized (FluidContentsInput.class) {
      var empty = emptyCatalog().empty;
      if (fluid == null || fluid == Fluids.EMPTY)
        return empty.stream().map(ItemStack::copy).toList();
      var samples = filled.get(fluid);
      if (samples == null) {
        var result = new ArrayList<ItemStack>();
        for (var prototype : empty) {
          var replacement = new MutableObject<ItemStack>();
          var source = prototype.copy();
          if (FluidHandler.fillMb(
                  source, Ic2FluidStack.create(fluid, Integer.MAX_VALUE), false, replacement)
              > 0) {
            var actual = replacement.getValue();
            if (actual != null && matchesContents(actual, fluid, 1))
              result.add(actual.copyWithCount(1));
          }
        }
        samples = List.copyOf(result);
        filled.put(fluid, samples);
      }
      return samples.stream().map(ItemStack::copy).toList();
    }
  }
}
