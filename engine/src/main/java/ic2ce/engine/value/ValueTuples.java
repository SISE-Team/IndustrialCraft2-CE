package ic2ce.engine.value;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable value pairs retain public field access while preventing escaped structural mutation.
 */
public final class ValueTuples {
  private ValueTuples() {}

  public static <K, V> List<T2<K, V>> fromMap(Map<K, V> source) {
    return source.entrySet().stream()
        .map(entry -> new T2<>(entry.getKey(), entry.getValue()))
        .toList();
  }

  public static final class T2<A, B> {
    public final A a;
    public final B b;

    public T2(A first, B second) {
      a = first;
      b = second;
    }

    @Override
    public boolean equals(Object value) {
      return value instanceof T2<?, ?> other
          && Objects.equals(a, other.a)
          && Objects.equals(b, other.b);
    }

    @Override
    public int hashCode() {
      return 31 * Objects.hashCode(a) + Objects.hashCode(b);
    }

    @Override
    public String toString() {
      return "Pair[" + a + ", " + b + "]";
    }
  }

  public static final class T3<A, B, C> {
    public final A a;
    public final B b;
    public final C c;

    public T3(A first, B second, C third) {
      a = first;
      b = second;
      c = third;
    }

    @Override
    public boolean equals(Object value) {
      return value instanceof T3<?, ?, ?> other
          && Objects.equals(a, other.a)
          && Objects.equals(b, other.b)
          && Objects.equals(c, other.c);
    }

    @Override
    public int hashCode() {
      return 31 * (31 * Objects.hashCode(a) + Objects.hashCode(b)) + Objects.hashCode(c);
    }

    @Override
    public String toString() {
      return "Triple[" + a + ", " + b + ", " + c + "]";
    }
  }
}
