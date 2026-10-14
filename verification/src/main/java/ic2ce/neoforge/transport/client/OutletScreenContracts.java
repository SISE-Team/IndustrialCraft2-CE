package ic2ce.neoforge.transport.client;

import ic2ce.neoforge.client.widget.PointerButton;
import java.util.*;
import net.minecraft.core.Direction;

/** Executes the actual native button handlers against the synchronized client outlet list. */
public final class OutletScreenContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run(OutletOrderScreen<?> screen) {
    var failures = new ArrayList<String>();
    var device = screen.getContainer().base;
    var saved = new ArrayList<>(device.getPriority());
    var candidates =
        Arrays.stream(Direction.values()).filter(side -> side != device.getFacing()).toList();
    try {
      var order = device.getPriority();
      order.clear();
      device.updatePriority(false);
      screen.containerTick();
      click(screen, 0, candidates.get(0));
      if (!device.getPriority().equals(List.of(candidates.get(0))))
        failures.add("outlet screen: first physical selection did not publish actual order");
      click(screen, 4, candidates.get(1));
      if (!device.getPriority().equals(List.of(candidates.get(0), candidates.get(1))))
        failures.add("outlet screen: selecting a sparse row did not compact to the actual end");
      click(screen, 0, candidates.get(1));
      if (!device.getPriority().equals(List.of(candidates.get(1))))
        failures.add(
            "outlet screen: moving an existing side did not remove its old position and replace the"
                + " destination");
      click(screen, 0, candidates.get(1));
      if (!device.getPriority().isEmpty())
        failures.add("outlet screen: selecting the checked row did not remove it");
      order = device.getPriority();
      order.add(candidates.get(2));
      device.updatePriority(false);
      screen.containerTick();
      boolean checked =
          screen.buttons[0][OutletOrderScreen.buttonOffset(candidates.get(2).get3DDataValue())]
              .isOn();
      if (!checked)
        failures.add(
            "outlet screen: actual synchronized external order left stale native button state");
    } finally {
      var order = device.getPriority();
      order.clear();
      order.addAll(saved);
      device.updatePriority(false);
      screen.containerTick();
    }
    return new Result(5, List.copyOf(failures));
  }

  private static void click(OutletOrderScreen<?> screen, int row, Direction side) {
    int column = OutletOrderScreen.buttonOffset(side.get3DDataValue());
    screen.buttons[row][column].onMouseClick(
        64 + 18 * column, 18 + 18 * row, PointerButton.left, true);
  }
}
