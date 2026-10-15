package ic2ce.neoforge.utility;

import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.utility.menu.ChunkAnchorMenu;
import ic2ce.neoforge.utility.menu.PoleDriveMenu;
import net.minecraft.world.inventory.MenuType;

/** Utility menu types, staged in declaration order. */
public final class UtilityMenus {
   public static final MenuType<PoleDriveMenu> MAGNETIZER = MenuDefinitions.registerManagedBe("magnetizer");
   public static final MenuType<ChunkAnchorMenu> CHUNK_LOADER = MenuDefinitions.registerManagedBe("chunk_loader");

   private UtilityMenus() {}

   public static void init() {}
}
