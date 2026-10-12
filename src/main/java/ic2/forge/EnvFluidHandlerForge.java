package ic2.forge;

import ic2.core.IC2;
import ic2.core.fluid.EnvFluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.ref.TabAssignments;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

class EnvFluidHandlerForge implements EnvFluidHandler {
   /** 26.1.2 forbids direct registry writes: stage fluid/type registrations and replay on RegisterEvent. */
   static final Map<Identifier, Fluid> pendingFluids = new HashMap<>();
   static final Map<Identifier, FluidType> pendingFluidTypes = new HashMap<>();

   @Override
   public EnvFluidHandler.FluidRefs createFluid(
      Identifier id,
      int density,
      int viscosity,
      int luminosity,
      int temperature,
      Identifier stillSpriteId,
      Identifier flowingSpriteId,
      int color
   ) {
      AtomicReference<EnvFluidHandler.FluidRefs> res = new AtomicReference<>();
      FluidType.Properties attributesBuilder = FluidType.Properties.create()
         .descriptionId("block." + id.getNamespace() + "." + id.getPath())
         .density(density).viscosity(viscosity).lightLevel(luminosity).temperature(temperature);
      FluidType fluidType = new FluidType(attributesBuilder);
      BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(
            () -> fluidType, () -> res.get().still, () -> res.get().flowing
         )
         .bucket(() -> res.get().bucket);
      Fluid still = new BaseFlowingFluid.Source(properties);
      Fluid flowing = flowingSpriteId != null ? new BaseFlowingFluid.Flowing(properties) : null;
      Identifier bucketId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_bucket");
      BucketItem bucket = new BucketItem(
         still,
         new net.minecraft.world.item.Item.Properties()
            .setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, bucketId))
            .stacksTo(1)
      ) {
         // 26.1.2: remainder templates cannot be built before registry freeze; resolve lazily.
         @Override
         public net.minecraft.world.item.ItemStackTemplate getCraftingRemainder(net.minecraft.world.item.ItemInstance instance) {
            return new net.minecraft.world.item.ItemStackTemplate(net.minecraft.world.item.Items.BUCKET);
         }
      };
      EnvFluidHandler.FluidRefs ret = new EnvFluidHandler.FluidRefs(null, still, flowing, bucket, stillSpriteId, flowingSpriteId, color);
      res.set(ret);
      pendingFluidTypes.put(id, fluidType);
      pendingFluids.put(id, still);
      if (ret.flowing != null) {
         pendingFluids.put(Identifier.fromNamespaceAndPath(id.getNamespace(), "flowing_" + id.getPath()), flowing);
      }

      IC2.envProxy.registerItem(bucketId, bucket);
      // items registered at runtime land in their tab through the dynamic assignments
      TabAssignments.assignDynamic(bucket, IC2.tabIc2ToolsAndUtilities);
      return ret;
   }

   @Override
   public Collection<Fluid> getAllFluids() {
      Collection<Fluid> fluids = new ArrayList<>();
      for (Identifier id : BuiltInRegistries.FLUID.keySet()) {
         Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
         if (fluid != null) {
            fluids.add(fluid);
         }
      }
      return fluids;
   }

   @Override
   public int getDensity(Fluid fluid) {
      return fluid.getFluidType().getDensity();
   }

   @Override
   public int getTemperature(Fluid fluid) {
      return fluid.getFluidType().getTemperature();
   }

   @Override
   public boolean isGaseous(Fluid fluid) {
      return fluid.getFluidType().isLighterThanAir();
   }

   @Override
   public Identifier getStillSpriteId(Fluid fluid) {
      throw new UnsupportedOperationException("client only");
   }

   @Override
   public Identifier getFlowingSpriteId(Fluid fluid) {
      throw new UnsupportedOperationException("client only");
   }

   @Override
   public int getColor(Fluid fluid) {
      throw new UnsupportedOperationException("client only");
   }

   @Override
   public Ic2FluidStack createFluidStackMb(Fluid fluid, int amount, CompoundTag nbt) {
      FluidStack stack = new FluidStack(fluid, amount);
      if (nbt != null && !nbt.isEmpty()) {
         ic2.core.util.FluidTag.set(stack, nbt);
      }
      return new Ic2FluidStackImpl(stack);
   }

   @Override
   public Ic2FluidStack getFluidStack(ItemStack stack) {
      if (StackUtil.isEmpty(stack)) {
         return null;
      } else if (stack.getCount() != 1) {
         stack = StackUtil.copyWithSize(stack, 1);
      }

      if (stack.getItem() instanceof ic2.core.fluid.Ic2FluidItem cell) {
         Ic2FluidStack content = cell.getFluidStack(stack);
         return content == null || content.isEmpty() ? null : content;
      }

      ResourceHandler<FluidResource> handler = net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
      if (handler == null || handler.size() <= 0) {
         return null;
      }
      FluidStack fs = net.neoforged.neoforge.transfer.fluid.FluidUtil.getStack(handler, 0);
      return fs != null && !fs.isEmpty() ? new Ic2FluidStackImpl(fs) : null;
   }

   @Override
   public Ic2FluidStack[] getFluidStacks(ItemStack stack) {
      if (StackUtil.isEmpty(stack)) {
         return null;
      } else if (stack.getCount() != 1) {
         stack = StackUtil.copyWithSize(stack, 1);
      }

      if (stack.getItem() instanceof ic2.core.fluid.Ic2FluidItem cell) {
         Ic2FluidStack content = cell.getFluidStack(stack);
         return content == null || content.isEmpty() ? null : new Ic2FluidStack[]{content};
      }

      ResourceHandler<FluidResource> handler = net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
      if (handler == null || handler.size() <= 0) {
         return null;
      }
      FluidStack fs = net.neoforged.neoforge.transfer.fluid.FluidUtil.getStack(handler, 0);
      return fs != null && !fs.isEmpty() ? new Ic2FluidStack[]{new Ic2FluidStackImpl(fs)} : null;
   }

   @Override
   public Ic2FluidStack readFluidStack(CompoundTag nbt) {
      String id = nbt.getStringOr("FluidName", "");
      int amount = nbt.getIntOr("Amount", 0);
      if (id.isEmpty() || amount < 0) {
         return null;
      }
      Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(id));
      if (fluid == null) {
         return null;
      }
      if (nbt.contains("Tag")) {
         FluidStack stack = new FluidStack(fluid, amount);
         ic2.core.util.FluidTag.set(stack, nbt.getCompoundOrEmpty("Tag"));
         return new Ic2FluidStackImpl(stack);
      }
      return Ic2FluidStack.create(fluid, amount);
   }

   @Override
   public CompoundTag getFluidStackNbt(Ic2FluidStack fs) {
      return fs instanceof Ic2FluidStackImpl impl ? ic2.core.util.FluidTag.get(impl.parent) : null;
   }

   @Override
   public Ic2FluidStack drainMb(ItemStack stack, int amount, boolean simulate, Mutable<ItemStack> newStack) {
      if (newStack != null) {
         newStack.setValue(stack);
      }

      if (amount < 0) {
         throw new IllegalArgumentException("negative amount");
      } else if (amount == 0) {
         return Ic2FluidStack.EMPTY;
      } else if (stack.getCount() != 1) {
         throw new IllegalArgumentException("invalid stack size: " + stack.getCount());
      } else {
         if (stack.getItem() instanceof ic2.core.fluid.Ic2FluidItem cell) {
            return cell.drainMb(stack, amount, simulate, newStack);
         }

         MutableContainer container = new MutableContainer(stack);
         ResourceHandler<FluidResource> handler = container.handler();
         if (handler == null) {
            return null;
         }
         try (Transaction tx = Transaction.openRoot()) {
            for (int tank = 0; tank < handler.size(); tank++) {
               FluidStack current = net.neoforged.neoforge.transfer.fluid.FluidUtil.getStack(handler, tank);
               if (current == null || current.isEmpty()) {
                  continue;
               }
               FluidResource resource = FluidResource.of(current);
               int extracted = handler.extract(tank, resource, amount, tx);
               if (extracted > 0) {
                  if (!simulate) {
                     tx.commit();
                     container.writeResult(stack, newStack);
                  }
                  FluidStack result = resource.toStack(extracted);
                  return new Ic2FluidStackImpl(result);
               }
            }
            return Ic2FluidStack.EMPTY;
         }
      }
   }

   @Override
   public int drainMb(ItemStack stack, Ic2FluidStack drainFs, boolean simulate, Mutable<ItemStack> newStack) {
      if (newStack != null) {
         newStack.setValue(stack);
      }

      if (drainFs == null) {
         throw new IllegalArgumentException("invalid drain medium");
      } else if (drainFs.isEmpty()) {
         return 0;
      } else if (stack.getCount() != 1) {
         throw new IllegalArgumentException("invalid stack size: " + stack.getCount());
      } else {
         if (stack.getItem() instanceof ic2.core.fluid.Ic2FluidItem cell) {
            return cell.drainMb(stack, drainFs, simulate, newStack);
         }

         MutableContainer container = new MutableContainer(stack);
         ResourceHandler<FluidResource> handler = container.handler();
         if (handler == null) {
            return 0;
         }
         FluidResource resource = FluidResource.of(getForgeFs(drainFs));
         try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, drainFs.getAmountMb(), tx);
            if (extracted <= 0) {
               return 0;
            }
            if (!simulate) {
               tx.commit();
               container.writeResult(stack, newStack);
            }
            return extracted;
         }
      }
   }

   @Override
   public int fillMb(ItemStack stack, Ic2FluidStack fillFs, boolean simulate, Mutable<ItemStack> newStack) {
      if (newStack != null) {
         newStack.setValue(stack);
      }

      if (fillFs == null) {
         throw new IllegalArgumentException("invalid fill medium");
      } else if (fillFs.isEmpty()) {
         return 0;
      } else if (stack.getCount() != 1) {
         throw new IllegalArgumentException("invalid stack size: " + stack.getCount());
      } else {
         if (stack.getItem() instanceof ic2.core.fluid.Ic2FluidItem cell) {
            return cell.fillMb(stack, fillFs, simulate, newStack);
         }

         MutableContainer container = new MutableContainer(stack);
         ResourceHandler<FluidResource> handler = container.handler();
         if (handler == null) {
            return 0;
         }
         FluidResource resource = FluidResource.of(getForgeFs(fillFs));
         try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(resource, fillFs.getAmountMb(), tx);
            if (!simulate && inserted > 0) {
               tx.commit();
               container.writeResult(stack, newStack);
            }
            return inserted;
         }
      }
   }

   /** A slot allows transactional bucket/cell replacement; ItemAccess.forStack cannot change an Item. */
   private static final class MutableContainer extends net.neoforged.neoforge.transfer.item.ItemStackResourceHandler {
      private ItemStack stack;

      private MutableContainer(ItemStack original) { this.stack = original.copy(); }
      @Override protected ItemStack getStack() { return this.stack; }
      @Override protected void setStack(ItemStack stack) { this.stack = stack; }

      private ResourceHandler<FluidResource> handler() {
         return net.neoforged.neoforge.transfer.access.ItemAccess.forHandlerIndexStrict(this, 0).getCapability(Capabilities.Fluid.ITEM);
      }

      private void writeResult(ItemStack original, Mutable<ItemStack> replacement) {
         if (replacement != null) {
            replacement.setValue(this.stack.copy());
         } else if (original.getItem() == this.stack.getItem()) {
            original.applyComponents(this.stack.getComponentsPatch());
            original.setCount(this.stack.getCount());
         }
      }
   }

   @Override
   public boolean isFluidBlock(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side) {
      return getBlockFluidHandler(world, pos, be, side) != null;
   }

   @Nullable
   private static ResourceHandler<FluidResource> getBlockFluidHandler(Level world, BlockPos pos, @Nullable BlockEntity be, @Nullable Direction side) {
      if (be != null) {
         if (world == null) world = be.getLevel();
         if (pos == null) pos = be.getBlockPos();
      }
      if (world == null || pos == null) return null;
      return world.getCapability(Capabilities.Fluid.BLOCK, pos, side);
   }

   @Override
   public Ic2FluidStack drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, int amount, boolean simulate) {
      if (amount < 0) {
         throw new IllegalArgumentException("negative amount");
      } else if (amount == 0) {
         return Ic2FluidStack.EMPTY;
      } else {
         ResourceHandler<FluidResource> handler = getBlockFluidHandler(world, pos, be, side);
         if (handler == null) {
            return null;
         }
         try (Transaction tx = Transaction.openRoot()) {
            for (int tank = 0; tank < handler.size(); tank++) {
               FluidStack current = net.neoforged.neoforge.transfer.fluid.FluidUtil.getStack(handler, tank);
               if (current == null || current.isEmpty()) {
                  continue;
               }
               FluidResource resource = FluidResource.of(current);
               int extracted = handler.extract(tank, resource, amount, tx);
               if (extracted > 0) {
                  if (!simulate) {
                     tx.commit();
                  }
                  return new Ic2FluidStackImpl(resource.toStack(extracted));
               }
            }
            return Ic2FluidStack.EMPTY;
         }
      }
   }

   @Override
   public int drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2FluidStack drainFs, boolean simulate) {
      if (drainFs == null) {
         throw new IllegalArgumentException("invalid drain medium");
      } else if (drainFs.isEmpty()) {
         return 0;
      } else {
         ResourceHandler<FluidResource> handler = getBlockFluidHandler(world, pos, be, side);
         if (handler == null) {
            return 0;
         }
         FluidResource resource = FluidResource.of(getForgeFs(drainFs));
         try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, drainFs.getAmountMb(), tx);
            if (extracted > 0 && !simulate) {
               tx.commit();
            }
            return extracted;
         }
      }
   }

   @Override
   public int fillMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2FluidStack fillFs, boolean simulate) {
      if (fillFs == null) {
         throw new IllegalArgumentException("invalid fill medium");
      } else if (fillFs.isEmpty()) {
         return 0;
      } else {
         ResourceHandler<FluidResource> handler = getBlockFluidHandler(world, pos, be, side);
         if (handler == null) {
            return 0;
         }
         FluidResource resource = FluidResource.of(getForgeFs(fillFs));
         try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(resource, fillFs.getAmountMb(), tx);
            if (inserted > 0 && !simulate) {
               tx.commit();
            }
            return inserted;
         }
      }
   }

   @Override
   public Fluid getWorldFluid(BlockState state, Level world, BlockPos pos) {
      return state.getFluidState().isEmpty() ? null : state.getFluidState().getType();
   }

   @Override
   public int getWorldFluidLevel(BlockState state, Level world, BlockPos pos) {
      if (state.getFluidState().isEmpty()) return -1;
      // PumpUtil expects legacy flow decay (source=0), not the amount (source=8).
      BlockState liquid = state.getFluidState().createLegacyBlock();
      return liquid.hasProperty(net.minecraft.world.level.block.LiquidBlock.LEVEL)
         ? liquid.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL) : -1;
   }

   @Override
   public Ic2FluidStack drainWorldFluid(BlockState state, Level world, BlockPos pos, boolean simulate) {
      if (state.getFluidState().isEmpty()) {
         return null;
      }
      Ic2FluidStack drained = this.drainMb(state, world, pos, null, null, Integer.MAX_VALUE, simulate);
      return drained == null || drained.isEmpty() ? null : drained;
   }

   static FluidStack getForgeFs(Ic2FluidStack fs) {
      if (fs == null || fs.isEmpty()) {
         return FluidStack.EMPTY;
      } else {
         return fs instanceof Ic2FluidStackImpl ? ((Ic2FluidStackImpl)fs).parent : new FluidStack(fs.getFluid(), fs.getAmountMb());
      }
   }
}
