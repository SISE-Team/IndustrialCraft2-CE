package ic2.forge;

import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.core.fluid.Ic2FluidItem;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Central registration of IC2 capabilities for the 26.1.2 capability system,
 * replacing the per-object ICapabilityProvider/LazyOptional model.
 */
public final class Ic2Capabilities {
   private Ic2Capabilities() {}

   @SubscribeEvent
   public static void registerCapabilities(RegisterCapabilitiesEvent event) {
      // --- fluid cells (items implementing Ic2FluidItem) ---
      List<Item> fluidItems = new ArrayList<>();
      for (Item item : BuiltInRegistries.ITEM) {
         if (item instanceof Ic2FluidItem && item.getClass().getName().startsWith("ic2.")) {
            fluidItems.add(item);
         }
      }
      for (Item item : fluidItems) {
         event.registerItem(Capabilities.Fluid.ITEM, Ic2Capabilities::itemFluidHandler, item);
      }
      java.util.Set<Item> buckets = new java.util.HashSet<>();
      for (var fluid : EnvFluidHandlerForge.pendingFluids.values()) {
         buckets.add(fluid.getBucket());
      }
      for (Item bucket : buckets) {
         event.registerItem(Capabilities.Fluid.ITEM,
            (stack, access) -> new net.neoforged.neoforge.transfer.fluid.BucketResourceHandler(access), bucket);
      }

      // --- block entities: fluid sides + item container sides ---
      for (Field field : Ic2BlockEntities.class.getFields()) {
         if (!Modifier.isStatic(field.getModifiers()) || field.getType() != BlockEntityType.class) {
            continue;
         }
         BlockEntityType<?> type;
         try {
            type = (BlockEntityType<?>)field.get(null);
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
         }
         event.registerBlockEntity(Capabilities.Fluid.BLOCK, type, Ic2Capabilities::blockFluidHandler);
         event.registerBlockEntity(Capabilities.Item.BLOCK, type, Ic2Capabilities::blockItemHandler);
      }
   }

   @Nullable
   private static ResourceHandler<FluidResource> itemFluidHandler(ItemStack stack, ItemAccess access) {
      if (stack.getCount() != 1 || !(stack.getItem() instanceof Ic2FluidItem)) {
         return null;
      }
      return new CellFluidHandler(stack, access);
   }

   @Nullable
   private static ResourceHandler<FluidResource> blockFluidHandler(BlockEntity be, @Nullable Direction side) {
      if (!(be instanceof Ic2TileEntity tile) || !tile.hasComponent(ic2.core.block.comp.Fluids.class)) {
         return null;
      }
      return new BlockSideFluidHandler(tile.getComponent(ic2.core.block.comp.Fluids.class), tile, side);
   }

   @Nullable
   private static ResourceHandler<ItemResource> blockItemHandler(BlockEntity be, @Nullable Direction side) {
      if (!(be instanceof Container container) || !(be instanceof Ic2TileEntity)) {
         return null;
      }
      return new ContainerItemHandler(container, side);
   }
}
