package ic2ce.neoforge.explosion;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Explosion block entity types, staged in declaration order. */
public final class ExplosionBlockEntities {
  public static final BlockEntityType<DemolitionBlockEntity> ITNT =
      BlockEntityDefinitions.register("itnt", DemolitionBlockEntity::new, ExplosionBlocks.ITNT);
  public static final BlockEntityType<ConfiguredNuclearCharge> NUKE =
      BlockEntityDefinitions.register("nuke", ConfiguredNuclearCharge::new, ExplosionBlocks.NUKE);
  public static final BlockEntityType<NuclearBlockEntity.ClassicNuclearCharge> CLASSIC_NUKE =
      BlockEntityDefinitions.register(
          "classic_nuke",
          NuclearBlockEntity.ClassicNuclearCharge::new,
          ExplosionBlocks.CLASSIC_NUKE);

  private ExplosionBlockEntities() {}

  public static void init() {}
}
