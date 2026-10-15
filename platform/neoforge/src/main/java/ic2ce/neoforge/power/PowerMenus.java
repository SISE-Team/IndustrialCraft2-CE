package ic2ce.neoforge.power;

import ic2ce.neoforge.power.menu.SolarCollectorMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Power menu types, staged in declaration order. */
public final class PowerMenus {
   public static final MenuType<SolarCollectorMenu> SOLAR_GENERATOR = MenuDefinitions.registerManagedBe("solar_generator");

   private PowerMenus() {}

   public static void init() {}
}
