package ic2ce.api.crops;

import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Native soil policy avoids allocating enum arrays during neighbor and placement checks. */
public enum CropSoilType {
  FARMLAND(Blocks.FARMLAND),
  MYCELIUM(Blocks.MYCELIUM),
  SAND(Blocks.SAND),
  SOULSAND(Blocks.SOUL_SAND);
  private final Block block;
  private static final Set<Block> ALLOWED =
      Set.of(Blocks.FARMLAND, Blocks.MYCELIUM, Blocks.SAND, Blocks.SOUL_SAND);

  CropSoilType(Block block) {
    this.block = java.util.Objects.requireNonNull(block);
  }

  public Block getBlock() {
    return block;
  }

  public static boolean contains(Block block) {
    return block != null && ALLOWED.contains(block);
  }
}
