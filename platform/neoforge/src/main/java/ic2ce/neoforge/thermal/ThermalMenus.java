package ic2ce.neoforge.thermal;

import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.thermal.menu.ElectricHeaterMenu;
import ic2ce.neoforge.thermal.menu.FluidHeaterMenu;
import ic2ce.neoforge.thermal.menu.IsotopeHeaterMenu;
import net.minecraft.world.inventory.MenuType;

/** Thermal menu types, staged in declaration order. */
public final class ThermalMenus {
   public static final MenuType<ElectricHeaterMenu> ELECTRIC_HEAT_GENERATOR = MenuDefinitions.registerManagedBe("electric_heat_generator");
   public static final MenuType<FluidHeaterMenu> FLUID_HEAT_GENERATOR = MenuDefinitions.registerManagedBe("fluid_heat_generator");
   public static final MenuType<IsotopeHeaterMenu> RT_HEAT_GENERATOR = MenuDefinitions.registerManagedBe("rt_heat_generator");

   private ThermalMenus() {}

   public static void init() {}
}
