package ic2ce.neoforge.utility;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Utility block entity types, staged in declaration order. */
public final class UtilityBlockEntities {
  public static final BlockEntityType<PoleDrive> MAGNETIZER =
      BlockEntityDefinitions.register("magnetizer", PoleDrive::new, UtilityBlocks.MAGNETIZER);
  public static final BlockEntityType<LinkedTeleporter> TELEPORTER =
      BlockEntityDefinitions.register("teleporter", LinkedTeleporter::new, UtilityBlocks.TELEPORTER);
  public static final BlockEntityType<ArcCoil> TESLA_COIL =
      BlockEntityDefinitions.register("tesla_coil", ArcCoil::new, UtilityBlocks.TESLA_COIL);
  public static final BlockEntityType<ChunkAnchor> CHUNK_LOADER =
      BlockEntityDefinitions.register("chunk_loader", ChunkAnchor::new, UtilityBlocks.CHUNK_LOADER);

  private UtilityBlockEntities() {}

  public static void init() {}
}
