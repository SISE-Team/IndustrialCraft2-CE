package ic2ce.neoforge.reactor.menu;

import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.slot.MachineSlot;
import ic2ce.neoforge.reactor.NuclearCore;
import ic2ce.neoforge.registry.MenuDefinitions;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/** Reactor slots are a width-specific view; dismantling a chamber invalidates the open menu. */
public class ReactorMenu extends OwnedMenu<NuclearCore> {
  private final int columns;

  public ReactorMenu(int syncId, Inventory inventory, NuclearCore core) {
    super(MenuDefinitions.NUCLEAR_REACTOR, syncId, inventory, core);
    columns = core.getReactorSize();
    for (int row = 0; row < 6; row++)
      for (int column = 0; column < columns; column++)
        addSlot(
            new MachineSlot(
                core.reactorSlot, row * columns + column, 26 + 18 * column, 25 + 18 * row));
    addPlayerInventorySlots(inventory, 214, 243);
    addSlot(new MachineSlot(core.coolantinputSlot, 0, 8, 25));
    addSlot(new MachineSlot(core.hotcoolinputSlot, 0, 188, 25));
    addSlot(new MachineSlot(core.coolantoutputSlot, 0, 8, 115));
    addSlot(new MachineSlot(core.hotcoolantoutputSlot, 0, 188, 115));
  }

  @Override
  public boolean stillValid(Player player) {
    return base.getReactorSize() == columns && super.stillValid(player);
  }

  @Override
  public List<String> getNetworkedFields() {
    return ic2ce.neoforge.menu.MenuFields.extend(
        super.getNetworkedFields(),
        "heat",
        "maxHeat",
        "output",
        "EmitHeat",
        "inputTank",
        "outputTank",
        "fluidCooled");
  }
}
