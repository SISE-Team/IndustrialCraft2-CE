package ic2ce.neoforge.registry;


import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.power.menu.SolarCollectorMenu;
import ic2ce.neoforge.thermal.menu.ElectricHeaterMenu;
import ic2ce.neoforge.thermal.menu.FluidHeaterMenu;
import ic2ce.neoforge.thermal.menu.IsotopeHeaterMenu;
import ic2ce.neoforge.kinetic.menu.MotorMenu;
import ic2ce.neoforge.kinetic.menu.SteamTurbineMenu;
import ic2ce.neoforge.kinetic.menu.StirlingEngineMenu;
import ic2ce.neoforge.kinetic.menu.WaterTurbineMenu;
import ic2ce.neoforge.kinetic.menu.WindTurbineMenu;
import ic2ce.neoforge.mining.menu.AreaMinerMenu;
import ic2ce.neoforge.crafting.menu.AutomaticCraftingMenu;
import ic2ce.neoforge.processing.menu.CanningMenu;
import ic2ce.neoforge.utility.menu.ChunkAnchorMenu;
import ic2ce.neoforge.processing.menu.LegacyCanningMenu;
import ic2ce.neoforge.agriculture.menu.CellCareMenu;
import ic2ce.neoforge.processing.menu.CondensationMenu;
import ic2ce.neoforge.agriculture.menu.HarvesterMenu;
import ic2ce.neoforge.agriculture.menu.FluidCareMenu;
import ic2ce.neoforge.processing.menu.ElectrolysisMenu;
import ic2ce.neoforge.processing.menu.FermentationMenu;
import ic2ce.neoforge.processing.menu.FluidPackagingMenu;
import ic2ce.neoforge.transport.menu.FairFluidMenu;
import ic2ce.neoforge.transport.menu.MeteredFluidMenu;
import ic2ce.neoforge.crafting.menu.WorkbenchMenu;
import ic2ce.neoforge.transport.menu.DualBankMenu;
import ic2ce.neoforge.processing.menu.HeatExchangeMenu;
import ic2ce.neoforge.utility.menu.PoleDriveMenu;
import ic2ce.neoforge.processing.menu.MatterMenu;
import ic2ce.neoforge.processing.menu.MetalFormingMenu;
import ic2ce.neoforge.mining.menu.PipeMinerMenu;
import ic2ce.neoforge.processing.menu.PatternLibraryMenu;
import ic2ce.neoforge.processing.menu.ReplicationMenu;
import ic2ce.neoforge.processing.menu.ScanningMenu;
import ic2ce.neoforge.processing.menu.DistillationMenu;
import ic2ce.neoforge.transport.menu.BatchItemMenu;
import ic2ce.neoforge.processing.menu.BoilerMenu;
import ic2ce.neoforge.transport.menu.PriorityFluidMenu;
import ic2ce.neoforge.transport.menu.PriorityItemMenu;
import ic2ce.neoforge.personal.menu.PublicEnergyTradeMenu;
import ic2ce.neoforge.personal.menu.OwnerEnergyTradeMenu;
import ic2ce.neoforge.personal.menu.PublicItemTradeMenu;
import ic2ce.neoforge.personal.menu.OwnerItemTradeMenu;
import ic2ce.neoforge.reactor.menu.ReactorMenu;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.energy.menu.ChargingPadMenu;
import ic2ce.neoforge.energy.menu.BankMenu;
import ic2ce.neoforge.energy.menu.TransformerMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.core.item.IHandHeldInventory;
import ic2ce.core.item.IHandHeldSubInventory;
import ic2ce.neoforge.handheld.menu.EnergyMeterMenu;
import ic2ce.neoforge.handheld.menu.ScannerMenu;
import ic2ce.neoforge.handheld.menu.ToolboxMenu;
import ic2ce.neoforge.handheld.HandheldStorage;
import ic2ce.neoforge.item.upgrade.AdvancedFilterInventory;
import ic2ce.neoforge.item.upgrade.MaterialTagEditor;
import ic2ce.neoforge.item.upgrade.NumericFilterEditor;
import ic2ce.neoforge.network.DataEncoder;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.core.proxy.EnvProxy;
import ic2ce.core.util.Util;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.function.BiFunction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MenuDefinitions {
   public static final MenuType<DocumentMenu<InventoryBlockEntity>> DYNAMIC_BE = registerManagedBe("dynamic_be");
   public static final MenuType<DocumentMenu<HandheldStorage>> DYNAMIC_ITEM = registerManagedItem("dynamic_item");
   public static final MenuType<SolarCollectorMenu> SOLAR_GENERATOR = registerManagedBe("solar_generator");
   public static final MenuType<ElectricHeaterMenu> ELECTRIC_HEAT_GENERATOR = registerManagedBe("electric_heat_generator");
   public static final MenuType<FluidHeaterMenu> FLUID_HEAT_GENERATOR = registerManagedBe("fluid_heat_generator");
   public static final MenuType<IsotopeHeaterMenu> RT_HEAT_GENERATOR = registerManagedBe("rt_heat_generator");
   public static final MenuType<MotorMenu> ELECTRIC_KINETIC_GENERATOR = registerManagedBe("electric_kinetic_generator");
   public static final MenuType<SteamTurbineMenu> STEAM_KINETIC_GENERATOR = registerManagedBe("steam_kinetic_generator");
   public static final MenuType<StirlingEngineMenu> STIRLING_KINETIC_GENERATOR = registerManagedBe("stirling_kinetic_generator");
   public static final MenuType<WaterTurbineMenu> WATER_KINETIC_GENERATOR = registerManagedBe("water_kinetic_generator");
   public static final MenuType<WindTurbineMenu> WIND_KINETIC_GENERATOR = registerManagedBe("wind_kinetic_generator");
   public static final MenuType<ReactorMenu> NUCLEAR_REACTOR = registerManagedBe("nuclear_reactor");
   public static final MenuType<CondensationMenu> CONDENSER = registerManagedBe("condenser");
   public static final MenuType<FluidPackagingMenu> FLUID_BOTTLER = registerManagedBe("fluid_bottler");
   public static final MenuType<FairFluidMenu> FLUID_DISTRIBUTOR = registerManagedBe("fluid_distributor");
   public static final MenuType<MeteredFluidMenu> FLUID_REGULATOR = registerManagedBe("fluid_regulator");
   public static final MenuType<HeatExchangeMenu> LIQUID_HEAT_EXCHANGER = registerManagedBe("liquid_heat_exchanger");
   public static final MenuType<DistillationMenu> SOLAR_DISTILLER = registerManagedBe("solar_distiller");
   public static final MenuType<BoilerMenu> STEAM_GENERATOR = registerManagedBe("steam_generator");
   public static final MenuType<DualBankMenu> ITEM_BUFFER = registerManagedBe("item_buffer");
   public static final MenuType<PoleDriveMenu> MAGNETIZER = registerManagedBe("magnetizer");
   public static final MenuType<BatchItemMenu> SORTING_MACHINE = registerManagedBe("sorting_machine");
   public static final MenuType<CanningMenu> CANNER = registerManagedBe("canner");
   public static final MenuType<LegacyCanningMenu> CLASSIC_CANNER = registerManagedBe("classic_canner");
   public static final MenuType<FermentationMenu> FERMENTER = registerManagedBe("fermenter");
   public static final MenuType<MetalFormingMenu> METAL_FORMER = registerManagedBe("metal_former");
   public static final MenuType<AreaMinerMenu> ADVANCED_MINER = registerManagedBe("advanced_miner");
   public static final MenuType<HarvesterMenu> CROP_HARVESTER = registerManagedBe("crop_harvester");
   public static final MenuType<FluidCareMenu> CROPMATRON = registerManagedBe("cropmatron");
   public static final MenuType<CellCareMenu> CLASSIC_CROPMATRON = registerManagedBe("classic_cropmatron");
   public static final MenuType<PipeMinerMenu> MINER = registerManagedBe("miner");
   public static final MenuType<MatterMenu> MATTER_GENERATOR = registerManagedBe("matter_generator");
   public static final MenuType<PatternLibraryMenu> PATTERN_STORAGE = registerManagedBe("pattern_storage");
   public static final MenuType<ReplicationMenu> REPLICATOR = registerManagedBe("replicator");
   public static final MenuType<ScanningMenu> UU_SCANNER = registerManagedBe("uu_scanner");
   public static final MenuType<PublicEnergyTradeMenu> ENERGY_O_MAT_CLOSED = registerManagedBe("energy_o_mat_closed");
   public static final MenuType<OwnerEnergyTradeMenu> ENERGY_O_MAT_OPEN = registerManagedBe("energy_o_mat_open");
   public static final MenuType<PublicItemTradeMenu> TRADE_O_MAT_CLOSED = registerManagedBe("trade_o_mat_closed");
   public static final MenuType<OwnerItemTradeMenu> TRADE_O_MAT_OPEN = registerManagedBe("trade_o_mat_open");
   public static final MenuType<ChargingPadMenu> CHARGEPAD = registerManagedBe("chargepad");
   public static final MenuType<BankMenu> ENERGY_STORAGE = registerManagedBe("energy_storage");
   public static final MenuType<ElectrolysisMenu> ELECTROLYZER = registerManagedBe("electrolyzer");
   public static final MenuType<TransformerMenu> TRANSFORMER = registerManagedBe("transformer");
   public static final MenuType<ChunkAnchorMenu> CHUNK_LOADER = registerManagedBe("chunk_loader");
   public static final MenuType<PriorityFluidMenu> WEIGHTED_FLUID_DISTRIBUTOR = registerManagedBe("weighted_fluid_distributor");
   public static final MenuType<PriorityItemMenu> WEIGHTED_ITEM_DISTRIBUTOR = registerManagedBe("weighted_item_distributor");
   public static final MenuType<WorkbenchMenu> INDUSTRIAL_WORKBENCH = registerManagedBe("industrial_workbench");
   public static final MenuType<AutomaticCraftingMenu> BATCH_CRAFTER = registerManagedBe("batch_crafter");
   public static final MenuType<DocumentMenu<AdvancedFilterInventory>> ADVANCED_UPGRADE = registerManagedItem("advanced_upgrade");
   public static final MenuType<MaterialTagEditor.ContainerEditOre> ADVANCED_UPGRADE_EDIT_ORE = registerManagedItem("advanced_upgrade/edit_ore");
   public static final MenuType<NumericFilterEditor.ContainerValueConfig> ADVANCED_UPGRADE_VALUE_CONFIG = registerManagedItem("advanced_upgrade/value_config");
   public static final MenuType<ScannerMenu> SCANNER = registerManagedItem("scanner");
   public static final MenuType<EnergyMeterMenu> METER = registerManagedItem("meter");
   public static final MenuType<ToolboxMenu> TOOL_BOX = registerManagedItem("tool_box");

   public static void init() {
   }

   private static <T extends AbstractContainerMenu> MenuType<T> register(String name, BiFunction<Integer, Inventory, T> factory) {
      return ProjectRuntime.envProxy.registerScreenHandler(ProjectRuntime.getIdentifier(name), factory);
   }

   private static <T extends AbstractContainerMenu> MenuType<T> registerExtended(String name, EnvProxy.ExtendedClientScreenHandlerFactory<T> factory) {
      return ProjectRuntime.envProxy.registerExtendedScreenHandler(ProjectRuntime.getIdentifier(name), factory);
   }

   public static <T extends AbstractContainerMenu> MenuType<T> registerManagedBe(String name){return ic2ce.neoforge.menu.ManagedMenus.block(name);}
   private static <T extends AbstractContainerMenu> MenuType<T> registerManagedItem(String name){return ic2ce.neoforge.menu.ManagedMenus.item(name);}
   public static void writeManagedBeData(BlockEntity entity,GrowingBuffer buffer)throws IOException{ic2ce.neoforge.menu.ManagedMenus.writeBlock(entity,buffer);}
   public static void writeManagedItemData(Player player,InteractionHand hand,Integer nestedId,GrowingBuffer buffer){ic2ce.neoforge.menu.ManagedMenus.writeItem(player,hand,nestedId,buffer);}
}
