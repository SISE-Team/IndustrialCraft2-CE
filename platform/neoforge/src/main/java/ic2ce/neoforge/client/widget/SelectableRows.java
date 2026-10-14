package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.*;

/** Selection transitions publish the current owner before invoking callbacks. */
public class SelectableRows
    extends ScrollViewport<SelectableRows, SelectableRows.ISelectableListItem> {
  protected ISelectableListItem currentSelected;

  public SelectableRows(MachineScreen<?> gui, int x, int y, int width, int height) {
    super(gui, x, y, width, height);
  }

  public SelectableRows(
      MachineScreen<?> gui, int x, int y, int width, int height, List<ISelectableListItem> items) {
    super(gui, x, y, width, height, items);
  }

  public SelectableRows setSelected(int index) {
    return setSelected(items.get(index));
  }

  public SelectableRows setSelected(ISelectableListItem item) {
    Objects.requireNonNull(item);
    if (!items.contains(item))
      throw new IllegalArgumentException("Selection belongs to another list");
    if (currentSelected == item) return this;
    var previous = currentSelected;
    currentSelected = item;
    if (previous != null) previous.onDeselected();
    if (currentSelected == item) item.onSelected();
    return this;
  }

  public SelectableRows clearSelected() {
    var previous = currentSelected;
    currentSelected = null;
    if (previous != null) previous.onDeselected();
    return this;
  }

  @Override
  public SelectableRows removeItem(ISelectableListItem item) {
    if (currentSelected == item) clearSelected();
    return super.removeItem(item);
  }

  @Override
  public SelectableRows removeItem(int index) {
    return removeItem(items.get(index));
  }

  @Override
  protected boolean onItemClick(ISelectableListItem item, PointerButton button, int x, int y) {
    if (button == PointerButton.left) {
      setSelected(item);
      return true;
    }
    if (button == PointerButton.right && currentSelected == item) {
      clearSelected();
      return true;
    }
    return false;
  }

  public interface ISelectableListItem extends ScrollViewport.IListItem {
    void onSelected();

    void onDeselected();
  }
}
