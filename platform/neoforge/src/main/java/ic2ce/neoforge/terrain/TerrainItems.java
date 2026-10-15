package ic2ce.neoforge.terrain;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.terrain.program.AridProgram;
import ic2ce.neoforge.terrain.program.CultivationProgram;
import ic2ce.neoforge.terrain.program.FrostProgram;
import ic2ce.neoforge.terrain.program.FungalProgram;
import ic2ce.neoforge.terrain.program.IrrigationProgram;
import ic2ce.neoforge.terrain.program.LevelingProgram;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Terrain items, staged in declaration order. */
public final class TerrainItems {
   public static final Item TERRAFORMER = ItemDefinitions.register(
      "terraformer", new BlockItem(TerrainBlocks.TERRAFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("terraformer"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item BLANK_TFBP = ItemDefinitions.register("blank_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("blank_tfbp"))).stacksTo(1), 0.0, 0, null));
   public static final Item CHILLING_TFBP = ItemDefinitions.register(
      "chilling_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("chilling_tfbp"))).stacksTo(1), 2000.0, 50, new FrostProgram())
   );
   public static final Item CULTIVATION_TFBP = ItemDefinitions.register(
      "cultivation_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("cultivation_tfbp"))).stacksTo(1), 4000.0, 40, new CultivationProgram())
   );
   public static final Item DESERTIFICATION_TFBP = ItemDefinitions.register(
      "desertification_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("desertification_tfbp"))).stacksTo(1), 2500.0, 40, new AridProgram())
   );
   public static final Item FLATIFICATION_TFBP = ItemDefinitions.register(
      "flatification_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("flatification_tfbp"))).stacksTo(1), 4000.0, 40, new LevelingProgram())
   );
   public static final Item IRRIGATION_TFBP = ItemDefinitions.register(
      "irrigation_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("irrigation_tfbp"))).stacksTo(1), 3000.0, 60, new IrrigationProgram())
   );
   public static final Item MUSHROOM_TFBP = ItemDefinitions.register(
      "mushroom_tfbp", new TerrainBlueprint(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mushroom_tfbp"))).stacksTo(1), 8000.0, 25, new FungalProgram())
   );

   private TerrainItems() {}

   public static void init() {}
}
