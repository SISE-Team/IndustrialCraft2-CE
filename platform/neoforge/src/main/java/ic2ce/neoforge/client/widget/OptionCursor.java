package ic2ce.neoforge.client.widget;

import java.util.Objects;

/** A bounded option cursor with exact forward/backward wrap and explicit atlas extent. */
public class OptionCursor implements ClickAction, AtlasRegion {
  private final int uS, vS, uE, vE, step, options;
  private final boolean vertical;
  private final OptionValue handler;

  public OptionCursor(
      int uS,
      int vS,
      int uE,
      int vE,
      int step,
      boolean vertical,
      int options,
      OptionValue handler) {
    if (options <= 0 || step < 0) throw new IllegalArgumentException("Invalid cycle extent");
    this.uS = uS;
    this.vS = vS;
    this.uE = uE;
    this.vE = vE;
    this.step = step;
    this.vertical = vertical;
    this.options = options;
    this.handler = Objects.requireNonNull(handler);
  }

  protected int getValue() {
    int value = handler.getValue();
    if (value < 0 || value >= options)
      throw new IllegalStateException("Invalid option cursor: " + value);
    return value;
  }

  public void onClick(PointerButton button) {
    if (button == null) return;
    int value = getValue();
    handler.onChange(
        button == PointerButton.left
            ? (value == options - 1 ? 0 : value + 1)
            : (value == 0 ? options - 1 : value - 1));
  }

  private int shifted(int origin) {
    return Math.addExact(origin, Math.multiplyExact(step, getValue()));
  }

  public int getUS() {
    return vertical ? uS : shifted(uS);
  }

  public int getVS() {
    return vertical ? shifted(vS) : vS;
  }

  public int getUE() {
    return vertical ? uE : shifted(uE);
  }

  public int getVE() {
    return vertical ? shifted(vE) : vE;
  }
}
