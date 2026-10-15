package ic2ce.neoforge.transport;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Transport block entity types, staged in declaration order. */
public final class TransportBlockEntities {
  public static final BlockEntityType<FairFluidRouter> FLUID_DISTRIBUTOR =
      BlockEntityDefinitions.register("fluid_distributor", FairFluidRouter::new, TransportBlocks.FLUID_DISTRIBUTOR);
  public static final BlockEntityType<MeteredFluidOutlet> FLUID_REGULATOR =
      BlockEntityDefinitions.register("fluid_regulator", MeteredFluidOutlet::new, TransportBlocks.FLUID_REGULATOR);
  public static final BlockEntityType<DualItemBuffer> ITEM_BUFFER =
      BlockEntityDefinitions.register("item_buffer", DualItemBuffer::new, TransportBlocks.ITEM_BUFFER);
  public static final BlockEntityType<BatchItemSorter> SORTING_MACHINE =
      BlockEntityDefinitions.register("sorting_machine", BatchItemSorter::new, TransportBlocks.SORTING_MACHINE);
  public static final BlockEntityType<PriorityFluidRouter> WEIGHTED_FLUID_DISTRIBUTOR =
      BlockEntityDefinitions.register(
          "weighted_fluid_distributor",
          PriorityFluidRouter::new,
          TransportBlocks.WEIGHTED_FLUID_DISTRIBUTOR);
  public static final BlockEntityType<PriorityItemRouter> WEIGHTED_ITEM_DISTRIBUTOR =
      BlockEntityDefinitions.register(
          "weighted_item_distributor",
          PriorityItemRouter::new,
          TransportBlocks.WEIGHTED_ITEM_DISTRIBUTOR);

  private TransportBlockEntities() {}

  public static void init() {}
}
