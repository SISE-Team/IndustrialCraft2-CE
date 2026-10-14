package ic2ce.core.util;

import ic2ce.core.network.DataEncoder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Class-lifetime metadata caches for native field synchronization and compatibility lookup. */
public final class ReflectionUtil {
  private ReflectionUtil() {}

  private record Fields(
      Map<String, Field> direct,
      Map<String, Field> inherited,
      List<Field> declaredOrder,
      List<Field> hierarchyOrder) {}

  private static final ClassValue<Fields> metadata =
      new ClassValue<>() {
        @Override
        protected Fields computeValue(Class<?> type) {
          var direct = new LinkedHashMap<String, Field>();
          var inherited = new LinkedHashMap<String, Field>();
          var declared = new ArrayList<Field>();
          var hierarchy = new ArrayList<Field>();
          for (var current = type; current != null; current = current.getSuperclass()) {
            for (var field : current.getDeclaredFields()) {
              if (!field.trySetAccessible()) continue;
              if (current == type) {
                direct.put(field.getName(), field);
                declared.add(field);
              }
              inherited.putIfAbsent(field.getName(), field);
              hierarchy.add(field);
            }
          }
          return new Fields(
              Map.copyOf(direct),
              Map.copyOf(inherited),
              List.copyOf(declared),
              List.copyOf(hierarchy));
        }
      };

  public static Field getField(Class<?> type, String name) {
    var field = metadata.get(Objects.requireNonNull(type)).direct().get(name);
    if (field == null)
      throw new IllegalStateException("Missing accessible field " + type.getName() + "." + name);
    return field;
  }

  public static Field getField(Class<?> type, String... names) {
    var fields = metadata.get(type).direct();
    for (var name : names) {
      var field = fields.get(name);
      if (field != null) return field;
    }
    return null;
  }

  private static Field byType(List<Field> fields, Class<?> type, boolean unique) {
    Field result = null;
    for (var field : fields)
      if (type.isAssignableFrom(field.getType())) {
        if (!unique) return field;
        if (result != null) return null;
        result = field;
      }
    return result;
  }

  public static Field getField(Class<?> owner, Class<?> type) {
    return byType(metadata.get(owner).declaredOrder(), type, true);
  }

  public static Field getFieldRecursive(Class<?> type, String name) {
    return metadata.get(type).inherited().get(name);
  }

  public static Field getFieldRecursive(Class<?> owner, Class<?> type, boolean unique) {
    return byType(metadata.get(owner).hierarchyOrder(), type, unique);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getFieldValue(Field field, Object owner) {
    try {
      return (T) field.get(owner);
    } catch (IllegalAccessException error) {
      throw new IllegalStateException("Cannot read " + field, error);
    }
  }

  public static <T> T getValue(Object owner, Class<?> type) {
    var field = getField(owner.getClass(), type);
    return field == null ? null : getFieldValue(field, owner);
  }

  public static <T> T getValueRecursive(Object owner, String name) throws NoSuchFieldException {
    var field = getFieldRecursive(owner.getClass(), name);
    if (field == null) throw new NoSuchFieldException(name);
    return getFieldValue(field, owner);
  }

  public static <T> T getValueRecursive(Object owner, Class<?> type, boolean unique)
      throws NoSuchFieldException {
    var field = getFieldRecursive(owner.getClass(), type, unique);
    if (field == null) throw new NoSuchFieldException(type.getName());
    return getFieldValue(field, owner);
  }

  public static void setValue(Object owner, Field field, Object value) {
    if (field.getType().isEnum() && value instanceof Integer ordinal) {
      var constants = field.getType().getEnumConstants();
      if (ordinal < 0 || ordinal >= constants.length)
        throw new IllegalArgumentException("Invalid enum ordinal for " + field);
      value = constants[ordinal];
    }
    try {
      var previous = field.get(owner);
      if (!DataEncoder.copyValue(value, previous)) field.set(owner, value);
    } catch (ReflectiveOperationException | IllegalArgumentException error) {
      throw new IllegalStateException("Cannot update " + field, error);
    }
  }

  public static boolean setValueRecursive(Object owner, String name, Object value) {
    var field = getFieldRecursive(owner.getClass(), name);
    if (field == null) return false;
    setValue(owner, field, value);
    return true;
  }

  public static Method getMethod(Class<?> owner, String[] names, Class<?>... parameters) {
    for (var name : names) {
      try {
        var method = owner.getDeclaredMethod(name, parameters);
        if (method.trySetAccessible()) return method;
      } catch (NoSuchMethodException ignored) {
      }
    }
    return null;
  }
}
