package ic2ce.neoforge.material;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Retained data tags and native common tags describe one material without changing either
 * registry's tags.
 */
public final class CommonMaterialTags {
  private static final ConcurrentHashMap<TagKey<Block>, List<TagKey<Block>>> BLOCK_ALIASES =
      new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<TagKey<Item>, List<TagKey<Item>>> ITEM_ALIASES =
      new ConcurrentHashMap<>();

  private CommonMaterialTags() {}

  private static Identifier alternate(Identifier name, boolean item) {
    var namespace = name.getNamespace();
    if (!namespace.equals("forge") && !namespace.equals("c")) return name;
    String path = name.getPath();
    if (item) {
      if (namespace.equals("forge") && path.startsWith("raw_ores/"))
        path = "raw_materials/" + path.substring(9);
      else if (namespace.equals("c") && path.startsWith("raw_materials/"))
        path = "raw_ores/" + path.substring(14);
    }
    return Identifier.fromNamespaceAndPath(namespace.equals("forge") ? "c" : "forge", path);
  }

  public static List<TagKey<Block>> blockAliases(TagKey<Block> tag) {
    var cached = BLOCK_ALIASES.get(tag);
    if (cached != null) return cached;
    var name = alternate(tag.location(), false);
    var result =
        name.equals(tag.location())
            ? List.of(tag)
            : List.of(tag, TagKey.create(Registries.BLOCK, name));
    if (BLOCK_ALIASES.size() < 2048) BLOCK_ALIASES.putIfAbsent(tag, result);
    return result;
  }

  public static List<TagKey<Item>> itemAliases(TagKey<Item> tag) {
    var cached = ITEM_ALIASES.get(tag);
    if (cached != null) return cached;
    var name = alternate(tag.location(), true);
    var result =
        name.equals(tag.location())
            ? List.of(tag)
            : List.of(tag, TagKey.create(Registries.ITEM, name));
    if (ITEM_ALIASES.size() < 2048) ITEM_ALIASES.putIfAbsent(tag, result);
    return result;
  }

  public static boolean matches(BlockState state, TagKey<Block> tag) {
    var aliases = blockAliases(tag);
    for (int index = 0; index < aliases.size(); index++)
      if (state.is(aliases.get(index))) return true;
    return false;
  }

  public static boolean matches(ItemStack stack, TagKey<Item> tag) {
    var aliases = itemAliases(tag);
    for (int index = 0; index < aliases.size(); index++)
      if (stack.is(aliases.get(index))) return true;
    return false;
  }
}
