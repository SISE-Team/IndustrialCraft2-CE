package ic2ce.neoforge.component;

import ic2ce.neoforge.block.NativeBlockEntity;
import java.util.function.IntSupplier;

/** Bounded native output signal updates only when its observable value changes. */
public abstract class SignalOutput extends NativeComponent {
  private int signal;
  private IntSupplier update;

  public SignalOutput(NativeBlockEntity parent) {
    super(parent);
  }

  public int getLevel() {
    return signal;
  }

  public void setLevel(int value) {
    int next = Math.clamp(value, 0, 15);
    if (next == signal) return;
    signal = next;
    onChange();
  }

  public abstract void onChange();

  @Override
  public boolean enableWorldTick() {
    return update != null;
  }

  @Override
  public void onWorldTick() {
    var supplier = update;
    if (supplier != null && parent.getLevel() != null && !parent.getLevel().isClientSide())
      setLevel(supplier.getAsInt());
  }

  public void setUpdate(IntSupplier supplier) {
    update = supplier;
  }
}
