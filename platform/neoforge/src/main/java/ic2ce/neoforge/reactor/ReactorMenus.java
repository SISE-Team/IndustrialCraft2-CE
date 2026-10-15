package ic2ce.neoforge.reactor;

import ic2ce.neoforge.reactor.menu.ReactorMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Reactor menu types, staged in declaration order. */
public final class ReactorMenus {
   public static final MenuType<ReactorMenu> NUCLEAR_REACTOR = MenuDefinitions.registerManagedBe("nuclear_reactor");

   private ReactorMenus() {}

   public static void init() {}
}
