package ic2ce.neoforge.personal;

import ic2ce.neoforge.personal.menu.OwnerEnergyTradeMenu;
import ic2ce.neoforge.personal.menu.OwnerItemTradeMenu;
import ic2ce.neoforge.personal.menu.PublicEnergyTradeMenu;
import ic2ce.neoforge.personal.menu.PublicItemTradeMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Personal menu types, staged in declaration order. */
public final class PersonalMenus {
   public static final MenuType<PublicEnergyTradeMenu> ENERGY_O_MAT_CLOSED = MenuDefinitions.registerManagedBe("energy_o_mat_closed");
   public static final MenuType<OwnerEnergyTradeMenu> ENERGY_O_MAT_OPEN = MenuDefinitions.registerManagedBe("energy_o_mat_open");
   public static final MenuType<PublicItemTradeMenu> TRADE_O_MAT_CLOSED = MenuDefinitions.registerManagedBe("trade_o_mat_closed");
   public static final MenuType<OwnerItemTradeMenu> TRADE_O_MAT_OPEN = MenuDefinitions.registerManagedBe("trade_o_mat_open");

   private PersonalMenus() {}

   public static void init() {}
}
