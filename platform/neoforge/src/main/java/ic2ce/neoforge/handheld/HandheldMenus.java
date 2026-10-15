package ic2ce.neoforge.handheld;

import ic2ce.neoforge.handheld.menu.EnergyMeterMenu;
import ic2ce.neoforge.handheld.menu.ScannerMenu;
import ic2ce.neoforge.handheld.menu.ToolboxMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Handheld menu types, staged in declaration order. */
public final class HandheldMenus {
   public static final MenuType<ScannerMenu> SCANNER = MenuDefinitions.registerManagedItem("scanner");
   public static final MenuType<EnergyMeterMenu> METER = MenuDefinitions.registerManagedItem("meter");
   public static final MenuType<ToolboxMenu> TOOL_BOX = MenuDefinitions.registerManagedItem("tool_box");

   private HandheldMenus() {}

   public static void init() {}
}
