package ic2ce.neoforge.world;

import ic2ce.neoforge.block.NativeBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Native self-drops pass through the owning tile's distinct retained-data policy. */
public final class BlockDrops {
  private BlockDrops() {}

  public static List<ItemStack> retain(
      BlockEntity entity, Item blockItem, List<ItemStack> sampled) {
    if (!(entity instanceof NativeBlockEntity tile)) return sampled;
    var result = new ArrayList<ItemStack>(sampled.size());
    for (var stack : sampled) {
      var adjusted = stack.is(blockItem) ? tile.adjustDrop(stack, false) : stack;
      if (adjusted != null && !adjusted.isEmpty()) result.add(adjusted);
    }
    return result;
  }
}
