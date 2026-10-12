package ic2.core.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 26.1.2 removed block Material; these helpers replicate the material checks the
 * reactor and tools relied on using tags and block identity.
 */
public final class BlockMaterialChecks {
   private static final java.util.Set<Block> CASING_LIKE = java.util.Set.of(
      block("stone"), block("granite"), block("diorite"), block("andesite"), block("polished_granite"),
      block("polished_diorite"), block("polished_andesite"), block("deepslate"), block("tuff"), block("obsidian"),
      block("iron_block"), block("copper_block"), block("gold_block"), block("diamond_block"), block("emerald_block"),
      block("lava"), block("clay"), block("dirt"), block("coarse_dirt"), block("grass_block"), block("podzol"),
      block("bricks"), block("stone_bricks"), block("nether_bricks"), block("reinforced_deepslate")
   );

   private BlockMaterialChecks() {}

   private static Block block(String name) {
      return BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(name));
   }

   /** Replaces {@code Material.STONE/METAL/LAVA/DIRT/CLAY} reactor casing checks. */
   public static boolean isReactorCasingLike(BlockState state) {
      Block block = state.getBlock();
      if (CASING_LIKE.contains(block)) {
         return true;
      }
      return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)
         || state.is(BlockTags.STONE_BRICKS) || state.is(BlockTags.DIRT) || state.is(BlockTags.SAND)
         || (state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.is(BlockTags.LEAVES))
         || state.is(BlockTags.NEEDS_IRON_TOOL) || state.is(BlockTags.NEEDS_DIAMOND_TOOL);
   }

   /** Replaces {@code Material.WOOD/LEAVES/WOOL} burnable checks. */
   public static boolean isFlammableLike(BlockState state) {
      return state.is(BlockTags.PLANKS) || state.is(BlockTags.LOGS_THAT_BURN) || state.is(BlockTags.WOOL)
         || state.is(BlockTags.LEAVES);
   }
}
