package ic2ce.neoforge.transport.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.transport.OrderedOutlets;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Actual synchronized order is the sole selection state; server updates cannot leave stale checked
 * buttons.
 */
public abstract class OutletOrderScreen<T extends OwnedMenu<? extends OrderedOutlets>>
    extends MachineScreen<T> {
  protected final ModeButton[][] buttons = new ModeButton[5][6];

  public OutletOrderScreen(T menu, Inventory inventory, Component title, int height) {
    super(menu, inventory, title, height);
    for (int row = 0; row < 5; row++) {
      final int target = row;
      for (int column = 0; column < 6; column++) {
        var side = Direction.from3DDataValue(facingOffset(column));
        buttons[row][column] =
            new ModeButton(
                this, 63 + 18 * column, 17 + 18 * row, 16, 16, button -> select(target, side));
        addElement(
            buttons[row][column]
                .withDisableHandler(() -> menu.base.getFacing() != side)
                .withText(side.getSerializedName().substring(0, 1).toUpperCase(Locale.ROOT))
                .withTooltip(sideName(side)));
      }
      var label =
          switch (row) {
            case 0 -> LiveText.ofTranslated("ic2.WeightedDistributor.gui.highest");
            case 1 -> LiveText.of("↑");
            case 2 -> LiveText.ofTranslated("ic2.WeightedDistributor.gui.priority");
            case 3 -> LiveText.of("↓");
            default -> LiveText.ofTranslated("ic2.WeightedDistributor.gui.lowest");
          };
      addElement(LiveLabel.create(this, 8, 21 + 18 * row, label, 0x404040, false));
    }
    refreshSelection();
  }

  private void select(int row, Direction side) {
    if (side == menu.base.getFacing()) return;
    var current = menu.base.getPriority();
    var order = new ArrayList<>(current);
    int previous = order.indexOf(side);
    boolean deselect = previous == row;
    order.remove(side);
    if (!deselect) {
      int destination = Math.min(row, order.size());
      if (destination == order.size()) order.add(side);
      else order.set(destination, side);
    }
    current.clear();
    current.addAll(order);
    menu.base.updatePriority(false);
    refreshSelection();
  }

  private void refreshSelection() {
    for (var row : buttons) for (var button : row) button.setOn(false);
    var seen = EnumSet.noneOf(Direction.class);
    int row = 0;
    for (var side : menu.base.getPriority()) {
      if (side == null || side == menu.base.getFacing() || !seen.add(side)) continue;
      if (row == buttons.length) break;
      buttons[row++][buttonOffset(side.get3DDataValue())].setOn(true);
    }
  }

  @Override
  public void containerTick() {
    super.containerTick();
    refreshSelection();
  }

  static int facingOffset(int column) {
    return Math.floorMod(column + 1, 6);
  }

  static int buttonOffset(int side) {
    return Math.floorMod(side + 5, 6);
  }

  private static String sideName(Direction side) {
    return switch (side) {
      case DOWN -> "ic2.dir.Bottom";
      case UP -> "ic2.dir.Top";
      case NORTH -> "ic2.dir.North";
      case SOUTH -> "ic2.dir.South";
      case WEST -> "ic2.dir.West";
      case EAST -> "ic2.dir.East";
    };
  }
}
