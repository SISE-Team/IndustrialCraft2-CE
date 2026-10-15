package ic2ce.neoforge.agriculture;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.agriculture.item.CropSupportItem;
import ic2ce.neoforge.agriculture.item.EncodedSeed;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/** Agriculture items, staged in declaration order. */
public final class AgricultureItems {
   public static final Item CROP_HARVESTER = ItemDefinitions.register("crop_harvester", new BlockItem(AgricultureBlocks.CROP_HARVESTER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("crop_harvester")))));
   public static final Item CROPMATRON = ItemDefinitions.register("cropmatron", new BlockItem(AgricultureBlocks.CROPMATRON, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("cropmatron")))));
   public static final Item CLASSIC_CROPMATRON = ItemDefinitions.register(
      "classic_cropmatron", new BlockItem(AgricultureBlocks.CLASSIC_CROPMATRON, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("classic_cropmatron"))))
   );
   public static final Item CROP_STICK = ItemDefinitions.register("crop_stick", new CropSupportItem(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("crop_stick")))));
   public static final Item CROP_SEED_BACK = ItemDefinitions.register("crop_seed_bag", new EncodedSeed(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("crop_seed_bag"))).stacksTo(1)));
   public static final Item COFFEE_BEANS = ItemDefinitions.register("coffee_beans", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("coffee_beans")))));
   public static final Item COFFEE_POWDER = ItemDefinitions.register("coffee_powder", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("coffee_powder")))));
   public static final Item FERTILIZER = ItemDefinitions.register("fertilizer", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("fertilizer")))));
   public static final Item GRIN_POWDER = ItemDefinitions.register("grin_powder", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("grin_powder")))));
   public static final Item HOPS = ItemDefinitions.register("hops", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("hops")))));
   public static final Item WEED = ItemDefinitions.register("weed", new Item(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("weed")))));

   private AgricultureItems() {}

   public static void init() {}
}
