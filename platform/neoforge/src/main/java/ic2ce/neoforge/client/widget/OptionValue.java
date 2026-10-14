package ic2ce.neoforge.client.widget;

/** Mutable option cursor used by explicit bounded controls. */
public interface OptionValue {
  int getValue();

  void onChange(int value);
}
