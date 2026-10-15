package ic2ce.neoforge.item.upgrade;

import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Upgrade menu types, staged in declaration order. */
public final class UpgradeMenus {
   public static final MenuType<MaterialTagEditor.ContainerEditOre> ADVANCED_UPGRADE_EDIT_ORE = MenuDefinitions.registerManagedItem("advanced_upgrade/edit_ore");
   public static final MenuType<NumericFilterEditor.ContainerValueConfig> ADVANCED_UPGRADE_VALUE_CONFIG = MenuDefinitions.registerManagedItem("advanced_upgrade/value_config");

   private UpgradeMenus() {}

   public static void init() {}
}
