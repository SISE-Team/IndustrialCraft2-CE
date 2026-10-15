package ic2ce.neoforge.mining;

import ic2ce.neoforge.mining.menu.AreaMinerMenu;
import ic2ce.neoforge.mining.menu.PipeMinerMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Mining menu types, staged in declaration order. */
public final class MiningMenus {
   public static final MenuType<AreaMinerMenu> ADVANCED_MINER = MenuDefinitions.registerManagedBe("advanced_miner");
   public static final MenuType<PipeMinerMenu> MINER = MenuDefinitions.registerManagedBe("miner");

   private MiningMenus() {}

   public static void init() {}
}
