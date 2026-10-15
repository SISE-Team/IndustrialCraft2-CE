package ic2ce.neoforge.kinetic.menu;

import ic2ce.neoforge.kinetic.ElectricMotor;
import ic2ce.neoforge.kinetic.KineticMenus;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import net.minecraft.world.entity.player.Inventory;

/** Motor menu binds actual motor assemblies and charging ledger. */
public class MotorMenu extends PhysicalInventoryMenu<ElectricMotor> {
  public MotorMenu(int id, Inventory inventory, ElectricMotor machine) {
    super(KineticMenus.ELECTRIC_KINETIC_GENERATOR, id, inventory, machine, 166);
    for (int slot = 0; slot < machine.slotMotor.size(); slot++)
      cell(machine.slotMotor, slot, 44 + (slot % 5) * 18, 27 + (slot / 5) * 18);
    cell(machine.dischargeSlot, 0, 8, 62);
  }
}
