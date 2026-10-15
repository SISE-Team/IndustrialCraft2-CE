package ic2ce.neoforge.processing;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.processing.uu.ItemCrystalMemory;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Processing items, staged in declaration order. */
public final class ProcessingItems {
   public static final Item STEAM_REPRESSURIZER = ItemDefinitions.register(
      "steam_repressurizer", new BlockItem(ProcessingBlocks.STEAM_REPRESSURIZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("steam_repressurizer"))))
   );
   public static final Item CONDENSER = ItemDefinitions.register("condenser", new BlockItem(ProcessingBlocks.CONDENSER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("condenser")))));
   public static final Item FLUID_BOTTLER = ItemDefinitions.register("fluid_bottler", new BlockItem(ProcessingBlocks.FLUID_BOTTLER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("fluid_bottler")))));
   public static final Item PUMP = ItemDefinitions.register("pump", new BlockItem(ProcessingBlocks.PUMP, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("pump")))));
   public static final Item SOLAR_DISTILLER = ItemDefinitions.register(
      "solar_distiller", new BlockItem(ProcessingBlocks.SOLAR_DISTILLER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("solar_distiller"))))
   );
   public static final Item STEAM_GENERATOR = ItemDefinitions.register(
      "steam_generator", new BlockItem(ProcessingBlocks.STEAM_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("steam_generator"))))
   );
   public static final Item ELECTROLYZER = ItemDefinitions.register("electrolyzer", new BlockItem(ProcessingBlocks.ELECTROLYZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("electrolyzer")))));
   public static final Item CLASSIC_ELECTROLYZER = ItemDefinitions.register(
      "classic_electrolyzer", new BlockItem(ProcessingBlocks.CLASSIC_ELECTROLYZER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("classic_electrolyzer"))))
   );
   public static final Item CANNER = ItemDefinitions.register("canner", new BlockItem(ProcessingBlocks.CANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("canner")))));
   public static final Item CLASSIC_CANNER = ItemDefinitions.register("classic_canner", new BlockItem(ProcessingBlocks.CLASSIC_CANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("classic_canner")))));
   public static final Item COMPRESSOR = ItemDefinitions.register("compressor", new BlockItem(ProcessingBlocks.COMPRESSOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("compressor")))));
   public static final Item ELECTRIC_FURNACE = ItemDefinitions.register(
      "electric_furnace", new BlockItem(ProcessingBlocks.ELECTRIC_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("electric_furnace"))))
   );
   public static final Item EXTRACTOR = ItemDefinitions.register("extractor", new BlockItem(ProcessingBlocks.EXTRACTOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("extractor")))));
   public static final Item IRON_FURNACE = ItemDefinitions.register("iron_furnace", new BlockItem(ProcessingBlocks.IRON_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("iron_furnace")))));
   public static final Item MACERATOR = ItemDefinitions.register("macerator", new BlockItem(ProcessingBlocks.MACERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("macerator")))));
   public static final Item RECYCLER = ItemDefinitions.register("recycler", new BlockItem(ProcessingBlocks.RECYCLER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("recycler")))));
   public static final Item SOLID_CANNER = ItemDefinitions.register("solid_canner", new BlockItem(ProcessingBlocks.SOLID_CANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("solid_canner")))));
   public static final Item BLAST_FURNACE = ItemDefinitions.register("blast_furnace", new BlockItem(ProcessingBlocks.BLAST_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("blast_furnace")))));
   public static final Item BLOCK_CUTTER = ItemDefinitions.register("block_cutter", new BlockItem(ProcessingBlocks.BLOCK_CUTTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("block_cutter")))));
   public static final Item CENTRIFUGE = ItemDefinitions.register("centrifuge", new BlockItem(ProcessingBlocks.CENTRIFUGE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("centrifuge")))));
   public static final Item FERMENTER = ItemDefinitions.register("fermenter", new BlockItem(ProcessingBlocks.FERMENTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("fermenter")))));
   public static final Item INDUCTION_FURNACE = ItemDefinitions.register(
      "induction_furnace", new BlockItem(ProcessingBlocks.INDUCTION_FURNACE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("induction_furnace"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item METAL_FORMER = ItemDefinitions.register("metal_former", new BlockItem(ProcessingBlocks.METAL_FORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("metal_former")))));
   public static final Item ORE_WASHING_PLANT = ItemDefinitions.register(
      "ore_washing_plant", new BlockItem(ProcessingBlocks.ORE_WASHING_PLANT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("ore_washing_plant"))))
   );
   public static final Item MASS_FABRICATOR = ItemDefinitions.register(
      "mass_fabricator", new BlockItem(ProcessingBlocks.MASS_FABRICATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mass_fabricator"))).rarity(Rarity.RARE))
   );
   public static final Item CLASSIC_MASS_FABRICATOR = ItemDefinitions.register(
      "classic_mass_fabricator", new BlockItem(ProcessingBlocks.CLASSIC_MASS_FABRICATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("classic_mass_fabricator"))).rarity(Rarity.RARE))
   );
   public static final Item MATTER_GENERATOR = ItemDefinitions.register(
      "matter_generator", new BlockItem(ProcessingBlocks.MATTER_GENERATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("matter_generator"))).rarity(Rarity.RARE))
   );
   public static final Item PATTERN_STORAGE = ItemDefinitions.register(
      "pattern_storage", new BlockItem(ProcessingBlocks.PATTERN_STORAGE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("pattern_storage"))))
   );
   public static final Item REPLICATOR = ItemDefinitions.register(
      "replicator", new BlockItem(ProcessingBlocks.REPLICATOR, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("replicator"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item UU_SCANNER = ItemDefinitions.register(
      "uu_scanner", new BlockItem(ProcessingBlocks.UU_SCANNER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("uu_scanner"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item CRYSTAL_MEMORY = ItemDefinitions.register("crystal_memory", new ItemCrystalMemory(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("crystal_memory"))).stacksTo(1)));

   private ProcessingItems() {}

   public static void init() {}
}
