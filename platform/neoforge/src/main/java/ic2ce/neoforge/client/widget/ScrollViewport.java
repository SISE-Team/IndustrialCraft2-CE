package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A bounded viewport draws visible rows through the native extractor's nested scissor stack. */
public abstract class ScrollViewport<
        T extends ScrollViewport<T, I>, I extends ScrollViewport.IListItem>
    extends ScreenWidget<T> {
  protected float scroll;
  protected boolean scrolling;
  protected int mouseScrollOffset = -1;
  protected final List<I> items;

  public ScrollViewport(MachineScreen<?> gui, int x, int y, int width, int height) {
    this(gui, x, y, width, height, List.of());
  }

  protected ScrollViewport(
      MachineScreen<?> gui, int x, int y, int width, int height, List<I> source) {
    super(gui, x, y, width, height);
    if (width < 9 || height < 3) throw new IllegalArgumentException("Scrolling viewport too small");
    items = new ArrayList<>(source);
    for (var item : items) Objects.requireNonNull(item);
  }

  private void constrain() {
    scroll = Math.clamp(Float.isFinite(scroll) ? scroll : 0, 0, getMaxScroll());
  }

  @SuppressWarnings("unchecked")
  public T addItem(I item) {
    items.add(Objects.requireNonNull(item));
    constrain();
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T addItem(int index, I item) {
    items.add(index, Objects.requireNonNull(item));
    constrain();
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T removeItem(I item) {
    items.remove(Objects.requireNonNull(item));
    constrain();
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T removeItem(int index) {
    items.remove(index);
    constrain();
    return (T) this;
  }

  private int thumbHeight() {
    long content = Math.max(1, (long) items.size() * 11 - 1);
    return Math.clamp((int) ((long) (height - 2) * (height - 2) / content), 1, height - 2);
  }

  private int thumbTop() {
    int range = getMaxScroll();
    return range == 0 ? 0 : Math.round(scroll / range * (height - 2 - thumbHeight()));
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    constrain();
    gui.drawColoredRect(graphics, x, y, width, height, 0xff000000);
    gui.drawColoredRect(graphics, x + 1, y + 1, width - 8, height - 2, 0xffa0a0a0);
    gui.drawColoredRect(graphics, x + width - 6, y + 1, 5, height - 2, 0xffa0a0ff);
    gui.drawColoredRect(graphics, x + width - 6, y + 1 + thumbTop(), 5, thumbHeight(), 0xff000000);
  }

  @Override
  public void drawForeground(GuiGraphicsExtractor graphics, int mx, int my) {
    super.drawForeground(graphics, mx, my);
    constrain();
    graphics.enableScissor(
        gui.getX() + x + 1,
        gui.getY() + y + 1,
        gui.getX() + x + width - 7,
        gui.getY() + y + height - 1);
    try {
      int first = Math.max(0, (int) scroll / 11);
      for (int index = first; index < items.size(); index++) {
        int rowY = y + 1 + index * 11 - (int) scroll;
        if (rowY >= y + height - 1) break;
        items.get(index).draw(graphics, x + 1, rowY, width - 8, 10, mx - x - 1, my - rowY);
        gui.drawColoredRect(graphics, x + 1, rowY + 10, width - 8, 1, 0xff000000);
      }
    } finally {
      graphics.disableScissor();
    }
  }

  @Override
  protected boolean onMouseClick(int mx, int my, PointerButton button) {
    int localX = mx - x, localY = my - y - 1;
    if (localY < 0 || localY >= height - 2) return false;
    if (localX >= width - 6 && localX < width - 1 && button == PointerButton.left) {
      int top = thumbTop();
      if (localY >= top && localY < top + thumbHeight()) {
        mouseScrollOffset = localY - top;
        scrolling = true;
        return true;
      }
      return false;
    }
    if (localX < 1 || localX >= width - 7) return false;
    int relative = localY + (int) scroll, index = relative / 11;
    if (index < items.size() && relative % 11 < 10)
      return onItemClick(items.get(index), button, localX - 1, relative % 11);
    return false;
  }

  protected abstract boolean onItemClick(I item, PointerButton button, int mx, int my);

  @Override
  public boolean onMouseDrag(int mx, int my, PointerButton button, boolean onThis) {
    if (!scrolling) return false;
    int travel = height - 2 - thumbHeight();
    scroll = travel <= 0 ? 0 : (float) (my - y - 1 - mouseScrollOffset) * getMaxScroll() / travel;
    constrain();
    return true;
  }

  @Override
  public boolean onMouseRelease(int mx, int my, PointerButton button, boolean onThis) {
    boolean consumed = scrolling;
    scrolling = false;
    mouseScrollOffset = -1;
    return consumed;
  }

  @Override
  public void onMouseScroll(int mx, int my, ContentScroll direction) {
    scroll += Objects.requireNonNull(direction).multiplier * 3;
    constrain();
  }

  protected int getMaxScroll() {
    return (int)
        Math.min(Integer.MAX_VALUE, Math.max(0, (long) items.size() * 11 - 1 - (height - 2)));
  }

  @FunctionalInterface
  public interface IListItem {
    void draw(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int mx, int my);
  }
}
