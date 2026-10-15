package ic2ce.neoforge.mining;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Mining block entity types, staged in declaration order. */
public final class MiningBlockEntities {
  public static final BlockEntityType<AreaMiner> ADVANCED_MINER =
      BlockEntityDefinitions.register("advanced_miner", AreaMiner::new, MiningBlocks.ADVANCED_MINER);
  public static final BlockEntityType<PipeMiner> MINER =
      BlockEntityDefinitions.register("miner", PipeMiner::new, MiningBlocks.MINER);

  private MiningBlockEntities() {}

  public static void init() {}
}
