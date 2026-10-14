package ic2ce.neoforge.client.widget;

/** Direction in content space, independent of the operating system wheel magnitude. */
public enum ContentScroll {
  up(-1),
  down(1),
  stopped(0);
  public final int multiplier;

  ContentScroll(int multiplier) {
    this.multiplier = multiplier;
  }
}
