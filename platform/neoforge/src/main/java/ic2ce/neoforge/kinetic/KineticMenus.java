package ic2ce.neoforge.kinetic;

import ic2ce.neoforge.kinetic.menu.MotorMenu;
import ic2ce.neoforge.kinetic.menu.SteamTurbineMenu;
import ic2ce.neoforge.kinetic.menu.StirlingEngineMenu;
import ic2ce.neoforge.kinetic.menu.WaterTurbineMenu;
import ic2ce.neoforge.kinetic.menu.WindTurbineMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Kinetic menu types, staged in declaration order. */
public final class KineticMenus {
   public static final MenuType<MotorMenu> ELECTRIC_KINETIC_GENERATOR = MenuDefinitions.registerManagedBe("electric_kinetic_generator");
   public static final MenuType<SteamTurbineMenu> STEAM_KINETIC_GENERATOR = MenuDefinitions.registerManagedBe("steam_kinetic_generator");
   public static final MenuType<StirlingEngineMenu> STIRLING_KINETIC_GENERATOR = MenuDefinitions.registerManagedBe("stirling_kinetic_generator");
   public static final MenuType<WaterTurbineMenu> WATER_KINETIC_GENERATOR = MenuDefinitions.registerManagedBe("water_kinetic_generator");
   public static final MenuType<WindTurbineMenu> WIND_KINETIC_GENERATOR = MenuDefinitions.registerManagedBe("wind_kinetic_generator");

   private KineticMenus() {}

   public static void init() {}
}
