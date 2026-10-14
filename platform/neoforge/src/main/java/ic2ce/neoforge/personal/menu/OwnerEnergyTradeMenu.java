package ic2ce.neoforge.personal.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.personal.EnergyTradeTerminal;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Owner access follows actual profile authorization on every native validity check. */
public class OwnerEnergyTradeMenu extends PhysicalInventoryMenu<EnergyTradeTerminal> {
  private static final java.util.List<String> FIELDS =
      java.util.List.of("paidFor", "euBuffer", "euOffer");

  public OwnerEnergyTradeMenu(int id, Inventory inventory, EnergyTradeTerminal machine) {
    super(MenuDefinitions.ENERGY_O_MAT_OPEN, id, inventory, machine, 166);
    cell(machine.demandSlot, 0, 24, 17);
    cell(machine.upgradeSlot, 0, 24, 53);
    cell(machine.inputSlot, 0, 60, 17);
    cell(machine.chargeSlot, 0, 60, 53);
    addDataSlot(
        new net.minecraft.world.inventory.DataSlot() {
          public int get() {
            return base.chargeSlot.tier;
          }

          public void set(int value) {
            if (base.getLevel() != null && base.getLevel().isClientSide())
              base.chargeSlot.tier = Math.clamp(value, 0, 14);
          }
        });
  }

  @Override
  public java.util.List<String> getNetworkedFields() {
    return FIELDS;
  }

  @Override
  public boolean stillValid(net.minecraft.world.entity.player.Player actor) {
    return super.stillValid(actor) && base.permitsAccess(actor.getGameProfile());
  }
}
