package ic2ce.neoforge.registry;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.agriculture.AgricultureBlockEntities;
import ic2ce.neoforge.crafting.CraftingBlockEntities;
import ic2ce.neoforge.energy.EnergyBlockEntities;
import ic2ce.neoforge.explosion.ExplosionBlockEntities;
import ic2ce.neoforge.kinetic.KineticBlockEntities;
import ic2ce.neoforge.mining.MiningBlockEntities;
import ic2ce.neoforge.personal.PersonalBlockEntities;
import ic2ce.neoforge.power.PowerBlockEntities;
import ic2ce.neoforge.processing.ProcessingBlockEntities;
import ic2ce.neoforge.reactor.ReactorBlockEntities;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.storage.StorageBlockEntities;
import ic2ce.neoforge.terrain.TerrainBlockEntities;
import ic2ce.neoforge.thermal.ThermalBlockEntities;
import ic2ce.neoforge.transport.TransportBlockEntities;
import ic2ce.neoforge.utility.UtilityBlockEntities;
import ic2ce.neoforge.world.WorldBlockEntities;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityDefinitions {
  private static final java.util.concurrent.ConcurrentHashMap<Identifier, BlockEntityType<?>>
      TYPES = new java.util.concurrent.ConcurrentHashMap<>();

  /** Loads every domain block entity holder after all blocks. */
  public static void init() {
    WorldBlockEntities.init();
    ExplosionBlockEntities.init();
    PowerBlockEntities.init();
    ThermalBlockEntities.init();
    KineticBlockEntities.init();
    ReactorBlockEntities.init();
    ProcessingBlockEntities.init();
    TransportBlockEntities.init();
    UtilityBlockEntities.init();
    TerrainBlockEntities.init();
    MiningBlockEntities.init();
    AgricultureBlockEntities.init();
    CraftingBlockEntities.init();
    PersonalBlockEntities.init();
    EnergyBlockEntities.init();
    StorageBlockEntities.init();
  }

  public static <T extends BlockEntity> BlockEntityType<T> register(
      String name, BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
    var id = ProjectIds.id(name);
    var type = EnvProxy.get().registerBlockEntity(id, factory, blocks);
    if (TYPES.putIfAbsent(id, type) != null)
      throw new IllegalStateException("Repeated project block entity: " + id);
    return type;
  }

  public static BlockEntityType<?> get(Identifier id) {
    return id == null ? null : TYPES.get(id);
  }
}
