package ic2ce.neoforge.personal.menu;

import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.personal.EnergyTradeTerminal;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.entity.player.Inventory;

/** Public offer observations are read-only while actual customer bills remain physical. */
public class PublicEnergyTradeMenu extends PhysicalInventoryMenu<EnergyTradeTerminal> {
  private static final java.util.List<String> FIELDS = java.util.List.of("paidFor", "euOffer");

  public PublicEnergyTradeMenu(int id, Inventory inventory, EnergyTradeTerminal machine) {
    super(MenuDefinitions.ENERGY_O_MAT_CLOSED, id, inventory, machine, 166);
    addSlot(new ic2ce.neoforge.menu.slot.DisplaySlot(machine.demandSlot, 0, 50, 17));
    cell(machine.inputSlot, 0, 143, 17);
    cell(machine.chargeSlot, 0, 143, 53);
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
}
