package ic2ce.neoforge.registry;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.agriculture.AgricultureBlocks;
import ic2ce.neoforge.cable.CableBlocks;
import ic2ce.neoforge.crafting.CraftingBlocks;
import ic2ce.neoforge.energy.EnergyBlocks;
import ic2ce.neoforge.explosion.ExplosionBlocks;
import ic2ce.neoforge.kinetic.KineticBlocks;
import ic2ce.neoforge.mining.MiningBlocks;
import ic2ce.neoforge.personal.PersonalBlocks;
import ic2ce.neoforge.power.PowerBlocks;
import ic2ce.neoforge.processing.ProcessingBlocks;
import ic2ce.neoforge.reactor.ReactorBlocks;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.storage.StorageBlocks;
import ic2ce.neoforge.terrain.TerrainBlocks;
import ic2ce.neoforge.thermal.ThermalBlocks;
import ic2ce.neoforge.transport.TransportBlocks;
import ic2ce.neoforge.utility.UtilityBlocks;
import ic2ce.neoforge.world.WorldBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public final class BlockDefinitions {
  /** Loads every domain block holder; flammability is declared once all blocks exist. */
  public static void init() {
    WorldBlocks.init();
    ReactorBlocks.init();
    UtilityBlocks.init();
    MiningBlocks.init();
    ExplosionBlocks.init();
    PowerBlocks.init();
    ThermalBlocks.init();
    KineticBlocks.init();
    ProcessingBlocks.init();
    TransportBlocks.init();
    TerrainBlocks.init();
    AgricultureBlocks.init();
    CraftingBlocks.init();
    PersonalBlocks.init();
    CableBlocks.init();
    EnergyBlocks.init();
    StorageBlocks.init();
    declareFlammability();
  }

  /** 26.1.2: blocks need their registry id set on Properties before construction. */
  public static Properties withId(java.util.function.Supplier<Properties> settings, String name) {
    return settings
        .get()
        .setId(
            net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.BLOCK,
                ProjectIds.id(name)));
  }

  public static <T extends Block> T register(String name, T block) {
    EnvProxy.get().registerBlock(ProjectIds.id(name), block);
    return block;
  }

  private static void declareFlammability() {
    EnvProxy.get().registerFlammableBlock(WorldBlocks.RUBBER_LEAVES, 20, 30);
    EnvProxy.get().registerFlammableBlock(WorldBlocks.RUBBER_LOG, 20, 4);
    EnvProxy.get().registerFlammableBlock(WorldBlocks.WOODEN_SCAFFOLD, 8, 20);
    EnvProxy.get().registerFlammableBlock(WorldBlocks.REINFORCED_WOODEN_SCAFFOLD, 8, 20);
  }
}
