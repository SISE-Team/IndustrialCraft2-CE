package ic2ce.neoforge.config;

import ic2ce.api.recipe.*;
import ic2ce.engine.config.Config;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Configuration item/tag/fluid grammar is parsed once into a checked independent value description.
 */
public final class NativeConfigValues {
  private NativeConfigValues() {}

  private record InputSpec(String name, int amount, boolean quantified) {}

  public static List<String> asList(String text) {
    Objects.requireNonNull(text);
    var values = new ArrayList<String>();
    int start = 0;
    for (int end = 0; end <= text.length(); end++)
      if (end == text.length() || text.charAt(end) == ',') {
        var part = text.substring(start, end).trim();
        if (!part.isEmpty()) values.add(part);
        else if (end < text.length() && start > 0) values.add("");
        start = end + 1;
      }
    return values;
  }

  private static InputSpec parse(String text, boolean allowAmount) throws ParseException {
    if (text == null) throw new ParseException("Missing configuration input", 0);
    text = text.trim();
    int marker = text.indexOf('*');
    String name = marker < 0 ? text : text.substring(0, marker).trim();
    if (name.isEmpty()) throw new ParseException("Empty configuration identifier", 0);
    int amount = 1;
    if (marker >= 0) {
      if (!allowAmount)
        throw new ParseException("Quantity is not supported in this configuration field", marker);
      if (text.indexOf('*', marker + 1) >= 0)
        throw new ParseException("Repeated quantity suffix", marker);
      try {
        amount = Integer.parseInt(text.substring(marker + 1).trim());
      } catch (NumberFormatException error) {
        var failure = new ParseException("Invalid configuration quantity", marker + 1);
        failure.initCause(error);
        throw failure;
      }
      if (amount <= 0)
        throw new ParseException("Configuration quantity must be positive", marker + 1);
    }
    return new InputSpec(name, amount, marker >= 0);
  }

  private static ItemStack stack(InputSpec spec) throws ParseException {
    var id = Identifier.tryParse(spec.name());
    if (id == null) throw new ParseException("Invalid item identifier: " + spec.name(), 0);
    var item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
    return item == null ? null : new ItemStack(item, spec.amount());
  }

  private static IRecipeInput input(InputSpec spec) throws ParseException {
    if (spec.name().startsWith("Tag:")) {
      var tag = spec.name().substring(4);
      if (Identifier.tryParse(tag) == null) throw new ParseException("Invalid item tag: " + tag, 4);
      return Recipes.inputFactory.forTag(tag, spec.amount());
    }
    if (spec.name().startsWith("Fluid:")) {
      var id = Identifier.tryParse(spec.name().substring(6));
      if (id == null) throw new ParseException("Invalid fluid identifier", 6);
      var fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(null);
      return fluid == null
          ? null
          : Recipes.inputFactory.forFluidContainer(fluid, spec.quantified() ? spec.amount() : 1000);
    }
    var item = stack(spec);
    return item == null ? null : Recipes.inputFactory.forStack(item);
  }

  public static ItemStack asStack(String text) throws ParseException {
    return stack(parse(text, false));
  }

  public static ItemStack asStackWithAmount(String text) throws ParseException {
    return stack(parse(text, true));
  }

  public static IRecipeInput asRecipeInput(String text) throws ParseException {
    return input(parse(text, false));
  }

  public static IRecipeInput asRecipeInputWithAmount(String text) throws ParseException {
    return input(parse(text, true));
  }

  public static List<ItemStack> asStackList(String text) throws ParseException {
    var result = new ArrayList<ItemStack>();
    for (var part : asList(text)) result.add(asStack(part));
    return result;
  }

  public static List<IRecipeInput> asRecipeInputList(String text) throws ParseException {
    return asRecipeInputList(text, false);
  }

  public static List<IRecipeInput> asRecipeInputList(String text, boolean allowMissing)
      throws ParseException {
    var result = new ArrayList<IRecipeInput>();
    for (var part : asList(text)) {
      var input = asRecipeInput(part);
      if (input == null && !allowMissing)
        throw new ParseException("Unknown item input: " + part, 0);
      result.add(input);
    }
    return result;
  }

  public static String fromStack(ItemStack stack) {
    return BuiltInRegistries.ITEM.getKey(Objects.requireNonNull(stack).getItem()).toString();
  }

  public static String fromStackWithAmount(ItemStack stack) {
    return fromStack(stack) + "*" + stack.getCount();
  }

  public static IRecipeInput asRecipeInput(Config.Value value) {
    try {
      return asRecipeInput(value.getString());
    } catch (ParseException error) {
      throw new Config.ParseException("Invalid recipe input", value, error);
    }
  }

  private static <T> T read(
      Config config, String key, Function<Config.Value, T> convert, T fallback) {
    var value = config.get(key);
    if (value == null) throw new IllegalArgumentException("Missing configuration key: " + key);
    try {
      return convert.apply(value);
    } catch (Config.ParseException error) {
      displayError(error, key);
      return fallback;
    }
  }

  public static String getString(Config config, String key) {
    return read(config, key, Config.Value::getString, "");
  }

  public static boolean getBool(Config config, String key) {
    return read(config, key, Config.Value::getBool, false);
  }

  public static int getInt(Config config, String key) {
    return read(config, key, Config.Value::getInt, 0);
  }

  public static float getFloat(Config config, String key) {
    return read(config, key, Config.Value::getFloat, 0F);
  }

  public static double getDouble(Config config, String key) {
    return read(config, key, Config.Value::getDouble, 0D);
  }

  @FunctionalInterface
  private interface Parser<T> {
    T parse(String text) throws ParseException;
  }

  private static <T> T parsed(Config config, String key, Parser<T> parser) {
    return read(
        config,
        key,
        value -> {
          try {
            return parser.parse(value.getString());
          } catch (ParseException error) {
            throw new Config.ParseException("Invalid configuration input", value, error);
          }
        },
        null);
  }

  public static ItemStack asStack(Config config, String key) {
    return parsed(config, key, NativeConfigValues::asStack);
  }

  public static List<ItemStack> asStackList(Config config, String key) {
    return parsed(config, key, NativeConfigValues::asStackList);
  }

  public static List<IRecipeInput> asRecipeInputList(Config config, String key) {
    return parsed(config, key, NativeConfigValues::asRecipeInputList);
  }

  public static int[] asIntArray(Config config, String key) {
    return read(
        config,
        key,
        value -> {
          var entries = asList(value.getString());
          var result = new int[entries.size()];
          try {
            for (int index = 0; index < result.length; index++)
              result[index] = Integer.parseInt(entries.get(index));
          } catch (NumberFormatException error) {
            throw new Config.ParseException("Invalid integer list", value, error);
          }
          return result;
        },
        new int[0]);
  }

  private static void displayError(Config.ParseException error, String key) {
    ProjectRuntime.sideProxy.displayError(
        "The IndustrialCraft2-CE configuration contains an invalid entry for %s.\n\n%s%s",
        key,
        error.getMessage(),
        error.getCause() == null ? "" : "\n\n" + error.getCause().getMessage());
  }
}
