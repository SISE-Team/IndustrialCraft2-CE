package ic2ce.neoforge.client.widget;

import java.util.*;

/** Owned enum selections retain caller order and reject missing/duplicate start values. */
public class EnumCursor<E extends Enum<E>> extends OptionCursor {
  private final List<E> choices;

  public EnumCursor(E[] set) {
    this(set, first(set));
  }

  public EnumCursor(E[] set, E start) {
    this(0, 0, 0, 0, 0, false, set, start);
  }

  public EnumCursor(int uS, int vS, int uE, int vE, int step, boolean vertical, E[] set, E start) {
    var owned = List.copyOf(Arrays.asList(set.clone()));
    if (owned.isEmpty() || new HashSet<>(owned).size() != owned.size() || !owned.contains(start))
      throw new IllegalArgumentException("Invalid enum cursor");
    var cursor = new Cursor(owned.indexOf(start), owned.size());
    super(uS, vS, uE, vE, step, vertical, owned.size(), cursor);
    choices = owned;
  }

  private static <E> E first(E[] choices) {
    Objects.requireNonNull(choices);
    if (choices.length == 0) throw new IllegalArgumentException("Empty enum choices");
    return choices[0];
  }

  public E getCurrentValue() {
    return choices.get(getValue());
  }

  private static final class Cursor implements OptionValue {
    private int value;
    private final int size;

    Cursor(int value, int size) {
      this.value = value;
      this.size = size;
    }

    public int getValue() {
      return value;
    }

    public void onChange(int next) {
      Objects.checkIndex(next, size);
      value = next;
    }
  }
}
