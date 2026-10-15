package ic2ce.neoforge.agriculture;

import ic2ce.neoforge.agriculture.menu.CellCareMenu;
import ic2ce.neoforge.agriculture.menu.FluidCareMenu;
import ic2ce.neoforge.agriculture.menu.HarvesterMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Agriculture menu types, staged in declaration order. */
public final class AgricultureMenus {
   public static final MenuType<HarvesterMenu> CROP_HARVESTER = MenuDefinitions.registerManagedBe("crop_harvester");
   public static final MenuType<FluidCareMenu> CROPMATRON = MenuDefinitions.registerManagedBe("cropmatron");
   public static final MenuType<CellCareMenu> CLASSIC_CROPMATRON = MenuDefinitions.registerManagedBe("classic_cropmatron");

   private AgricultureMenus() {}

   public static void init() {}
}
