package ic2ce.integration.jeirei;

import ic2ce.neoforge.client.widget.InventoryGrid;
import java.util.Objects;

/** Immutable independent recipe slot geometry with checked coordinate translation. */
public record SlotPosition(int x, int y, InventoryGrid.SlotStyle style) {
  public SlotPosition {
    Objects.requireNonNull(style);
  }

  public SlotPosition(int x, int y) {
    this(x, y, InventoryGrid.SlotStyle.Normal);
  }

  public SlotPosition(SlotPosition origin, int dx, int dy) {
    this(Math.addExact(origin.x, dx), Math.addExact(origin.y, dy), origin.style);
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public InventoryGrid.SlotStyle getStyle() {
    return style;
  }
}
