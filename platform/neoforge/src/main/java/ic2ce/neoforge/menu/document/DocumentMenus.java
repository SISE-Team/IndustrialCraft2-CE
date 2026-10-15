package ic2ce.neoforge.menu.document;

import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.handheld.HandheldStorage;
import ic2ce.neoforge.item.upgrade.AdvancedFilterInventory;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Document menu types, staged in declaration order. */
public final class DocumentMenus {
   public static final MenuType<DocumentMenu<InventoryBlockEntity>> DYNAMIC_BE = MenuDefinitions.registerManagedBe("dynamic_be");
   public static final MenuType<DocumentMenu<HandheldStorage>> DYNAMIC_ITEM = MenuDefinitions.registerManagedItem("dynamic_item");
   public static final MenuType<DocumentMenu<AdvancedFilterInventory>> ADVANCED_UPGRADE = MenuDefinitions.registerManagedItem("advanced_upgrade");

   private DocumentMenus() {}

   public static void init() {}
}
