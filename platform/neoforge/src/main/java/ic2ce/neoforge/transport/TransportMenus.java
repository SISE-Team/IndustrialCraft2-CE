package ic2ce.neoforge.transport;

import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.transport.menu.BatchItemMenu;
import ic2ce.neoforge.transport.menu.DualBankMenu;
import ic2ce.neoforge.transport.menu.FairFluidMenu;
import ic2ce.neoforge.transport.menu.MeteredFluidMenu;
import ic2ce.neoforge.transport.menu.PriorityFluidMenu;
import ic2ce.neoforge.transport.menu.PriorityItemMenu;
import net.minecraft.world.inventory.MenuType;

/** Transport menu types, staged in declaration order. */
public final class TransportMenus {
   public static final MenuType<FairFluidMenu> FLUID_DISTRIBUTOR = MenuDefinitions.registerManagedBe("fluid_distributor");
   public static final MenuType<MeteredFluidMenu> FLUID_REGULATOR = MenuDefinitions.registerManagedBe("fluid_regulator");
   public static final MenuType<DualBankMenu> ITEM_BUFFER = MenuDefinitions.registerManagedBe("item_buffer");
   public static final MenuType<BatchItemMenu> SORTING_MACHINE = MenuDefinitions.registerManagedBe("sorting_machine");
   public static final MenuType<PriorityFluidMenu> WEIGHTED_FLUID_DISTRIBUTOR = MenuDefinitions.registerManagedBe("weighted_fluid_distributor");
   public static final MenuType<PriorityItemMenu> WEIGHTED_ITEM_DISTRIBUTOR = MenuDefinitions.registerManagedBe("weighted_item_distributor");

   private TransportMenus() {}

   public static void init() {}
}
