package ic2ce.neoforge.world.surface;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Registered native states have immutable canonical variant text; malformed selections cannot
 * silently become a different state.
 */
public final class StateVariants {
  private static final Map<BlockState, String> VARIANTS = new ConcurrentHashMap<>();

  private StateVariants() {}

  public static String getVariantString(BlockState state) {
    Objects.requireNonNull(state);
    return VARIANTS.computeIfAbsent(
        state,
        value -> {
          if (value.getProperties().isEmpty()) return "normal";
          var text = new StringJoiner(",");
          for (var property : value.getProperties())
            text.add(property.getName() + "=" + name(value, property));
          return text.toString();
        });
  }

  private static <V extends Comparable<V>> String name(BlockState state, Property<V> property) {
    return property.getName(state.getValue(property));
  }

  public static BlockState getState(Block block, String variant) {
    if (block == null || variant == null || variant.length() > 4096) return null;
    var state = block.defaultBlockState();
    if (variant.isEmpty() || variant.equals("normal")) return state;
    var used = new HashSet<String>();
    for (String field : variant.split(",", -1)) {
      int equals = field.indexOf('=');
      if (equals <= 0 || equals == field.length() - 1 || field.indexOf('=', equals + 1) >= 0)
        return null;
      String key = field.substring(0, equals);
      if (!used.add(key)) return null;
      var property = block.getStateDefinition().getProperty(key);
      if (property == null) return null;
      state = apply(state, property, field.substring(equals + 1));
      if (state == null) return null;
    }
    return state;
  }

  private static <V extends Comparable<V>> BlockState apply(
      BlockState state, Property<V> property, String text) {
    var value = property.getValue(text);
    return value.map(parsed -> state.setValue(property, parsed)).orElse(null);
  }
}
