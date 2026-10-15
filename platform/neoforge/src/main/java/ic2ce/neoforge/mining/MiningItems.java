package ic2ce.neoforge.mining;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/** Mining items, staged in declaration order. */
public final class MiningItems {
   public static final Item MINING_PIPE = ItemDefinitions.register("mining_pipe", new BlockItem(MiningBlocks.MINING_PIPE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mining_pipe")))));
   public static final Item ADVANCED_MINER = ItemDefinitions.register("advanced_miner", new BlockItem(MiningBlocks.ADVANCED_MINER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("advanced_miner")))));
   public static final Item MINER = ItemDefinitions.register("miner", new BlockItem(MiningBlocks.MINER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("miner")))));

   private MiningItems() {}

   public static void init() {}
}
