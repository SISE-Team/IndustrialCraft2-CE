package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.List;

/** Ordinary rows own their individual click behavior. */
public class ClickableRows extends ScrollViewport<ClickableRows, ClickableRows.IListItem> {
  public ClickableRows(MachineScreen<?> gui, int x, int y, int width, int height) {
    super(gui, x, y, width, height);
  }

  public ClickableRows(
      MachineScreen<?> gui, int x, int y, int width, int height, List<IListItem> items) {
    super(gui, x, y, width, height, items);
  }

  @Override
  protected boolean onItemClick(IListItem item, PointerButton button, int x, int y) {
    return item.onClick(button, x, y);
  }

  public interface IListItem extends ScrollViewport.IListItem {
    boolean onClick(PointerButton button, int x, int y);
  }
}
