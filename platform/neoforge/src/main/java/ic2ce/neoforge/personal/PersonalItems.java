package ic2ce.neoforge.personal;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Personal items, staged in declaration order. */
public final class PersonalItems {
   public static final Item ENERGY_O_MAT = ItemDefinitions.register("energy_o_mat", new BlockItem(PersonalBlocks.ENERGY_O_MAT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("energy_o_mat")))));
   public static final Item PERSONAL_CHEST = ItemDefinitions.register(
      "personal_chest", new BlockItem(PersonalBlocks.PERSONAL_CHEST, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("personal_chest"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item TRADE_O_MAT = ItemDefinitions.register("trade_o_mat", new BlockItem(PersonalBlocks.TRADE_O_MAT, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("trade_o_mat")))));

   private PersonalItems() {}

   public static void init() {}
}
