package ic2ce.neoforge.client.widget;

/** Explicit native actions accepted by machine controls. */
public enum PointerButton {
  left(0),
  right(1);
  public final int id;

  PointerButton(int id) {
    this.id = id;
  }

  public static PointerButton get(int id) {
    return switch (id) {
      case 0 -> left;
      case 1 -> right;
      default -> null;
    };
  }
}
