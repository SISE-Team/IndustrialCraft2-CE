package ic2ce.neoforge.energy;

import ic2ce.neoforge.energy.menu.BankMenu;
import ic2ce.neoforge.energy.menu.ChargingPadMenu;
import ic2ce.neoforge.energy.menu.TransformerMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Energy menu types, staged in declaration order. */
public final class EnergyMenus {
   public static final MenuType<ChargingPadMenu> CHARGEPAD = MenuDefinitions.registerManagedBe("chargepad");
   public static final MenuType<BankMenu> ENERGY_STORAGE = MenuDefinitions.registerManagedBe("energy_storage");
   public static final MenuType<TransformerMenu> TRANSFORMER = MenuDefinitions.registerManagedBe("transformer");

   private EnergyMenus() {}

   public static void init() {}
}
