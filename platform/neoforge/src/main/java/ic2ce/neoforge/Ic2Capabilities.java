package ic2ce.neoforge;

import ic2ce.api.fluid.Ic2FluidItem;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.transfer.TransactionalItemAdapter;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.BucketResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * Capabilities bind typed project registrations; no reflective field enumeration or package-name
 * matching.
 */
public final class Ic2Capabilities {
  private Ic2Capabilities() {}

  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    for (var item : EnvProxyNeoForge.ITEMS.values())
      if (item instanceof Ic2FluidItem)
        event.registerItem(Capabilities.Fluid.ITEM, Ic2Capabilities::cell, item);
    Set<Item> buckets = Collections.newSetFromMap(new IdentityHashMap<>());
    for (var fluid : EnvFluidHandlerNeoForge.pendingFluids.values()) {
      var bucket = fluid.getBucket();
      if (bucket != null && bucket != net.minecraft.world.item.Items.AIR) buckets.add(bucket);
    }
    for (var bucket : buckets)
      event.registerItem(
          Capabilities.Fluid.ITEM, (stack, access) -> new BucketResourceHandler(access), bucket);
    for (var type : EnvProxyNeoForge.BLOCK_ENTITIES.values()) {
      event.registerBlockEntity(Capabilities.Fluid.BLOCK, type, Ic2Capabilities::fluid);
      event.registerBlockEntity(Capabilities.Item.BLOCK, type, Ic2Capabilities::items);
    }
  }

  private static @Nullable ResourceHandler<FluidResource> cell(ItemStack stack, ItemAccess access) {
    return stack.getCount() == 1 && stack.getItem() instanceof Ic2FluidItem
        ? new CellFluidHandler(stack, access)
        : null;
  }

  private static @Nullable ResourceHandler<FluidResource> fluid(
      BlockEntity entity, @Nullable Direction side) {
    return entity instanceof NativeBlockEntity tile
            && !tile.isRemoved()
            && tile.hasComponent(FluidPorts.class)
        ? new BlockSideFluidHandler(tile.getComponent(FluidPorts.class), tile, side)
        : null;
  }

  private static @Nullable ResourceHandler<ItemResource> items(
      BlockEntity entity, @Nullable Direction side) {
    return entity instanceof NativeBlockEntity tile
            && !tile.isRemoved()
            && entity instanceof Container container
        ? new TransactionalItemAdapter(container, side)
        : null;
  }
}
