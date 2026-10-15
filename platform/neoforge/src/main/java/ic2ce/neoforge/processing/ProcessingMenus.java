package ic2ce.neoforge.processing;

import ic2ce.neoforge.processing.menu.BoilerMenu;
import ic2ce.neoforge.processing.menu.CanningMenu;
import ic2ce.neoforge.processing.menu.CondensationMenu;
import ic2ce.neoforge.processing.menu.DistillationMenu;
import ic2ce.neoforge.processing.menu.ElectrolysisMenu;
import ic2ce.neoforge.processing.menu.FermentationMenu;
import ic2ce.neoforge.processing.menu.FluidPackagingMenu;
import ic2ce.neoforge.processing.menu.HeatExchangeMenu;
import ic2ce.neoforge.processing.menu.LegacyCanningMenu;
import ic2ce.neoforge.processing.menu.MatterMenu;
import ic2ce.neoforge.processing.menu.MetalFormingMenu;
import ic2ce.neoforge.processing.menu.PatternLibraryMenu;
import ic2ce.neoforge.processing.menu.ReplicationMenu;
import ic2ce.neoforge.processing.menu.ScanningMenu;
import ic2ce.neoforge.registry.MenuDefinitions;
import net.minecraft.world.inventory.MenuType;

/** Processing menu types, staged in declaration order. */
public final class ProcessingMenus {
   public static final MenuType<CondensationMenu> CONDENSER = MenuDefinitions.registerManagedBe("condenser");
   public static final MenuType<FluidPackagingMenu> FLUID_BOTTLER = MenuDefinitions.registerManagedBe("fluid_bottler");
   public static final MenuType<HeatExchangeMenu> LIQUID_HEAT_EXCHANGER = MenuDefinitions.registerManagedBe("liquid_heat_exchanger");
   public static final MenuType<DistillationMenu> SOLAR_DISTILLER = MenuDefinitions.registerManagedBe("solar_distiller");
   public static final MenuType<BoilerMenu> STEAM_GENERATOR = MenuDefinitions.registerManagedBe("steam_generator");
   public static final MenuType<CanningMenu> CANNER = MenuDefinitions.registerManagedBe("canner");
   public static final MenuType<LegacyCanningMenu> CLASSIC_CANNER = MenuDefinitions.registerManagedBe("classic_canner");
   public static final MenuType<FermentationMenu> FERMENTER = MenuDefinitions.registerManagedBe("fermenter");
   public static final MenuType<MetalFormingMenu> METAL_FORMER = MenuDefinitions.registerManagedBe("metal_former");
   public static final MenuType<MatterMenu> MATTER_GENERATOR = MenuDefinitions.registerManagedBe("matter_generator");
   public static final MenuType<PatternLibraryMenu> PATTERN_STORAGE = MenuDefinitions.registerManagedBe("pattern_storage");
   public static final MenuType<ReplicationMenu> REPLICATOR = MenuDefinitions.registerManagedBe("replicator");
   public static final MenuType<ScanningMenu> UU_SCANNER = MenuDefinitions.registerManagedBe("uu_scanner");
   public static final MenuType<ElectrolysisMenu> ELECTROLYZER = MenuDefinitions.registerManagedBe("electrolyzer");

   private ProcessingMenus() {}

   public static void init() {}
}
