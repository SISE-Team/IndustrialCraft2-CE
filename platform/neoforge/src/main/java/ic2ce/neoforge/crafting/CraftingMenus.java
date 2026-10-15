package ic2ce.neoforge.crafting;

import ic2ce.neoforge.crafting.menu.AutomaticCraftingMenu;
import ic2ce.neoforge.crafting.menu.WorkbenchMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Crafting menu types, staged in declaration order. */
public final class CraftingMenus {
   public static final MenuType<WorkbenchMenu> INDUSTRIAL_WORKBENCH = MenuDefinitions.registerManagedBe("industrial_workbench");
   public static final MenuType<AutomaticCraftingMenu> BATCH_CRAFTER = MenuDefinitions.registerManagedBe("batch_crafter");

   private CraftingMenus() {}

   public static void init() {}
}
