package ic2ce.neoforge.reactor;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Reactor block entity types, staged in declaration order. */
public final class ReactorBlockEntities {
  public static final BlockEntityType<NuclearCore> NUCLEAR_REACTOR =
      BlockEntityDefinitions.register("nuclear_reactor", NuclearCore::new, ReactorBlocks.NUCLEAR_REACTOR);
  public static final BlockEntityType<VesselHatch> REACTOR_ACCESS_HATCH =
      BlockEntityDefinitions.register("reactor_access_hatch", VesselHatch::new, ReactorBlocks.REACTOR_ACCESS_HATCH);
  public static final BlockEntityType<ReactorChamber> REACTOR_CHAMBER =
      BlockEntityDefinitions.register("reactor_chamber", ReactorChamber::new, ReactorBlocks.REACTOR_CHAMBER);
  public static final BlockEntityType<CoolantPort> REACTOR_FLUID_PORT =
      BlockEntityDefinitions.register("reactor_fluid_port", CoolantPort::new, ReactorBlocks.REACTOR_FLUID_PORT);
  public static final BlockEntityType<SignalPort> REACTOR_REDSTONE_PORT =
      BlockEntityDefinitions.register("reactor_redstone_port", SignalPort::new, ReactorBlocks.REACTOR_REDSTONE_PORT);
  public static final BlockEntityType<RedstoneCondensatorService> RCI_RSH =
      BlockEntityDefinitions.register("rci_rsh", RedstoneCondensatorService::new, ReactorBlocks.RCI_RSH);
  public static final BlockEntityType<LapisCondensatorService> RCI_LZH =
      BlockEntityDefinitions.register("rci_lzh", LapisCondensatorService::new, ReactorBlocks.RCI_LZH);

  private ReactorBlockEntities() {}

  public static void init() {}
}
