package ic2ce.neoforge.registry;


import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * 26.1.2 removed the Tier interface; tool materials are ToolMaterial records.
 * Repair tags reference IC2 item tags (data files under tags/item).
 */
public final class ToolProfiles {
   /** Bronze: mining level 2 (iron-equivalent), durability 350, speed 6.0. */
   public static final ToolMaterial BRONZE = new ToolMaterial(
      BlockTags.INCORRECT_FOR_IRON_TOOL, 350, 6.0F, 2.0F, 14, TagKey.create(Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("bronze_ingot"))
   );
   /** Chainsaw: level 3 (diamond-equivalent); no repair items. */
   public static final ToolMaterial CHAINSAW = new ToolMaterial(
      BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 250, 12.0F, 9.0F, 14, TagKey.create(Registries.ITEM, ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier("no_repair"))
   );

   private ToolProfiles() {}

}
