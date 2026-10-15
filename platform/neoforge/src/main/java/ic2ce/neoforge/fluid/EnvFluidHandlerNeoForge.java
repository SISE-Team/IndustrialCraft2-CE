package ic2ce.neoforge.fluid;

import ic2ce.api.fluid.Ic2FluidItem;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.registry.CreativeContents;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apache.commons.lang3.mutable.Mutable;

/**
 * NeoForge transactional capabilities; container identity is separate from stored fluid content.
 */
public class EnvFluidHandlerNeoForge implements EnvFluidHandler {
  public static final Map<Identifier, Fluid> pendingFluids = new LinkedHashMap<>();
  public static final Map<Identifier, FluidType> pendingFluidTypes = new LinkedHashMap<>();
  static final Map<Fluid, FluidRefs> fluidMaterials = new IdentityHashMap<>();

  private static final class Definition {
    FluidRefs references;
  }

  @Override
  public FluidRefs createFluid(
      Identifier id,
      int density,
      int viscosity,
      int light,
      int temperature,
      Identifier stillSprite,
      Identifier flowingSprite,
      int color) {
    if (pendingFluids.containsKey(id))
      throw new IllegalArgumentException("Duplicate fluid identifier: " + id);
    var definition = new Definition();
    var type =
        new FluidType(
            FluidType.Properties.create()
                .descriptionId("block." + id.getNamespace() + "." + id.getPath())
                .density(density)
                .viscosity(viscosity)
                .lightLevel(light)
                .temperature(temperature));
    var properties =
        new BaseFlowingFluid.Properties(
                () -> type, () -> definition.references.still, () -> definition.references.flowing)
            .bucket(() -> definition.references.bucket);
    Fluid still = new BaseFlowingFluid.Source(properties),
        flowing = flowingSprite == null ? null : new BaseFlowingFluid.Flowing(properties);
    var bucketId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_bucket");
    var bucket =
        new BucketItem(
            still,
            new net.minecraft.world.item.Item.Properties()
                .setId(
                    net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.ITEM, bucketId))
                .stacksTo(1)) {
          @Override
          public net.minecraft.world.item.ItemStackTemplate getCraftingRemainder(
              net.minecraft.world.item.ItemInstance instance) {
            return new net.minecraft.world.item.ItemStackTemplate(
                net.minecraft.world.item.Items.BUCKET);
          }
        };
    var references = new FluidRefs(null, still, flowing, bucket, stillSprite, flowingSprite, color);
    definition.references = references;
    pendingFluidTypes.put(id, type);
    pendingFluids.put(id, still);
    fluidMaterials.put(still, references);
    if (flowing != null) {
      pendingFluids.put(
          Identifier.fromNamespaceAndPath(id.getNamespace(), "flowing_" + id.getPath()), flowing);
      fluidMaterials.put(flowing, references);
    }
    ProjectRuntime.envProxy.registerItem(bucketId, bucket);
    CreativeContents.assignDynamic(bucket, ProjectRuntime.tabIc2ToolsAndUtilities);
    return references;
  }

  @Override
  public Collection<Fluid> getAllFluids() {
    return BuiltInRegistries.FLUID.stream().toList();
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
    throw new UnsupportedOperationException("Fluid rendering requires a client service");
  }

  @Override
  public Identifier getFlowingSpriteId(Fluid fluid) {
    throw new UnsupportedOperationException("Fluid rendering requires a client service");
  }

  @Override
  public int getColor(Fluid fluid) {
    throw new UnsupportedOperationException("Fluid rendering requires a client service");
  }

  @Override
  public Ic2FluidStack createFluidStackMb(Fluid fluid, int amount, CompoundTag data) {
    if (amount < 0) throw new IllegalArgumentException("Negative fluid quantity");
    var stack = new FluidStack(Objects.requireNonNull(fluid), amount);
    if (data != null && !data.isEmpty()) ic2ce.neoforge.fluid.FluidTag.set(stack, data.copy());
    return new Ic2FluidStackImpl(stack);
  }

  private static ResourceHandler<FluidResource> readItemHandler(ItemStack stack) {
    return ItemAccess.forStack(stack.getCount() == 1 ? stack : stack.copyWithCount(1))
        .getCapability(Capabilities.Fluid.ITEM);
  }

  @Override
  public Ic2FluidStack getFluidStack(ItemStack stack) {
    var all = getFluidStacks(stack);
    if (all == null) return null;
    for (var content : all) if (!content.isEmpty()) return content;
    return Ic2FluidStack.EMPTY;
  }

  @Override
  public Ic2FluidStack[] getFluidStacks(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return null;
    if (stack.getItem() instanceof Ic2FluidItem container) {
      var content = container.getFluidStack(stack.getCount() == 1 ? stack : stack.copyWithCount(1));
      return new Ic2FluidStack[] {
        content == null || content.isEmpty() ? Ic2FluidStack.EMPTY : content.copy()
      };
    }
    var handler = readItemHandler(stack);
    if (handler == null || handler.size() == 0) return null;
    var contents = new Ic2FluidStack[handler.size()];
    for (int index = 0; index < contents.length; index++) {
      var content = net.neoforged.neoforge.transfer.fluid.FluidUtil.getStack(handler, index);
      contents[index] =
          content.isEmpty() ? Ic2FluidStack.EMPTY : new Ic2FluidStackImpl(content.copy());
    }
    return contents;
  }

  @Override
  public Ic2FluidStack readFluidStack(CompoundTag data) {
    return Ic2FluidStackImpl.read(data);
  }

  @Override
  public CompoundTag getFluidStackNbt(Ic2FluidStack fluid) {
    return fluid instanceof Ic2FluidStackImpl nativeFluid
        ? ic2ce.neoforge.fluid.FluidTag.get(nativeFluid.parent)
        : null;
  }

  private static void validateSingle(ItemStack stack, Mutable<ItemStack> replacement) {
    Objects.requireNonNull(stack);
    if (stack.getCount() != 1)
      throw new IllegalArgumentException("Fluid operation requires one container");
    if (replacement != null) replacement.setValue(stack);
  }

  private static final class ItemSession extends ItemStackResourceHandler {
    private ItemStack current;

    ItemSession(ItemStack source) {
      current = source.copy();
    }

    @Override
    protected ItemStack getStack() {
      return current;
    }

    @Override
    protected void setStack(ItemStack stack) {
      current = stack;
    }

    ResourceHandler<FluidResource> fluids() {
      return ItemAccess.forHandlerIndexStrict(this, 0).getCapability(Capabilities.Fluid.ITEM);
    }

    boolean canPublish(ItemStack original, Mutable<ItemStack> replacement) {
      return replacement != null || original.getItem() == current.getItem();
    }

    void publish(ItemStack original, Mutable<ItemStack> replacement) {
      if (replacement != null) replacement.setValue(current.copy());
      else {
        original.applyComponents(current.getComponentsPatch());
        original.setCount(current.getCount());
      }
    }
  }

  private static Ic2FluidStack extractFirst(
      ResourceHandler<FluidResource> handler, int maximum, Transaction transaction) {
    for (int index = 0; index < handler.size(); index++) {
      var content = handler.getResource(index);
      if (content.isEmpty()) continue;
      int removed = handler.extract(content, maximum, transaction);
      if (removed > 0) return new Ic2FluidStackImpl(content.toStack(removed));
    }
    return Ic2FluidStack.EMPTY;
  }

  @Override
  public Ic2FluidStack drainMb(
      ItemStack stack, int maximum, boolean simulate, Mutable<ItemStack> replacement) {
    if (maximum < 0) throw new IllegalArgumentException("Negative drain quantity");
    if (maximum == 0) return Ic2FluidStack.EMPTY;
    validateSingle(stack, replacement);
    if (stack.getItem() instanceof Ic2FluidItem container)
      return container.drainMb(stack, maximum, simulate, replacement);
    var session = new ItemSession(stack);
    var handler = session.fluids();
    if (handler == null) return null;
    try (var transaction = Transaction.openRoot()) {
      var extracted = extractFirst(handler, maximum, transaction);
      if (!simulate && !extracted.isEmpty()) {
        if (!session.canPublish(stack, replacement)) return Ic2FluidStack.EMPTY;
        transaction.commit();
        session.publish(stack, replacement);
      }
      return extracted;
    }
  }

  private int modifyItem(
      ItemStack stack,
      Ic2FluidStack fluid,
      boolean insert,
      boolean simulate,
      Mutable<ItemStack> replacement) {
    Objects.requireNonNull(fluid);
    if (fluid.isEmpty()) return 0;
    validateSingle(stack, replacement);
    if (stack.getItem() instanceof Ic2FluidItem container)
      return insert
          ? container.fillMb(stack, fluid, simulate, replacement)
          : container.drainMb(stack, fluid, simulate, replacement);
    var session = new ItemSession(stack);
    var handler = session.fluids();
    if (handler == null) return 0;
    try (var transaction = Transaction.openRoot()) {
      var resource = FluidResource.of(getForgeFs(fluid));
      int quantity =
          insert
              ? handler.insert(resource, fluid.getAmountMb(), transaction)
              : handler.extract(resource, fluid.getAmountMb(), transaction);
      if (!simulate && quantity > 0) {
        if (!session.canPublish(stack, replacement)) return 0;
        transaction.commit();
        session.publish(stack, replacement);
      }
      return quantity;
    }
  }

  @Override
  public int drainMb(
      ItemStack stack, Ic2FluidStack fluid, boolean simulate, Mutable<ItemStack> replacement) {
    return modifyItem(stack, fluid, false, simulate, replacement);
  }

  @Override
  public int fillMb(
      ItemStack stack, Ic2FluidStack fluid, boolean simulate, Mutable<ItemStack> replacement) {
    return modifyItem(stack, fluid, true, simulate, replacement);
  }

  private static ResourceHandler<FluidResource> blockHandler(
      Level world, BlockPos position, BlockEntity tile, Direction side) {
    if (tile != null) {
      if (world == null) world = tile.getLevel();
      if (position == null) position = tile.getBlockPos();
    }
    return world == null || position == null
        ? null
        : world.getCapability(Capabilities.Fluid.BLOCK, position, side);
  }

  @Override
  public boolean isFluidBlock(
      BlockState state, Level world, BlockPos position, BlockEntity tile, Direction side) {
    return blockHandler(world, position, tile, side) != null;
  }

  @Override
  public Ic2FluidStack drainMb(
      BlockState state,
      Level world,
      BlockPos position,
      BlockEntity tile,
      Direction side,
      int maximum,
      boolean simulate) {
    if (maximum < 0) throw new IllegalArgumentException("Negative drain quantity");
    if (maximum == 0) return Ic2FluidStack.EMPTY;
    var handler = blockHandler(world, position, tile, side);
    if (handler == null) return null;
    try (var transaction = Transaction.openRoot()) {
      var extracted = extractFirst(handler, maximum, transaction);
      if (!simulate && !extracted.isEmpty()) transaction.commit();
      return extracted;
    }
  }

  private int modifyBlock(
      Level world,
      BlockPos position,
      BlockEntity tile,
      Direction side,
      Ic2FluidStack fluid,
      boolean insert,
      boolean simulate) {
    Objects.requireNonNull(fluid);
    if (fluid.isEmpty()) return 0;
    var handler = blockHandler(world, position, tile, side);
    if (handler == null) return 0;
    try (var transaction = Transaction.openRoot()) {
      var resource = FluidResource.of(getForgeFs(fluid));
      int quantity =
          insert
              ? handler.insert(resource, fluid.getAmountMb(), transaction)
              : handler.extract(resource, fluid.getAmountMb(), transaction);
      if (!simulate && quantity > 0) transaction.commit();
      return quantity;
    }
  }

  @Override
  public int drainMb(
      BlockState state,
      Level world,
      BlockPos position,
      BlockEntity tile,
      Direction side,
      Ic2FluidStack fluid,
      boolean simulate) {
    return modifyBlock(world, position, tile, side, fluid, false, simulate);
  }

  @Override
  public int fillMb(
      BlockState state,
      Level world,
      BlockPos position,
      BlockEntity tile,
      Direction side,
      Ic2FluidStack fluid,
      boolean simulate) {
    return modifyBlock(world, position, tile, side, fluid, true, simulate);
  }

  @Override
  public Fluid getWorldFluid(BlockState state, Level world, BlockPos position) {
    return state.getFluidState().isEmpty() ? null : state.getFluidState().getType();
  }

  @Override
  public int getWorldFluidLevel(BlockState state, Level world, BlockPos position) {
    if (state.getFluidState().isEmpty()) return -1;
    var legacy = state.getFluidState().createLegacyBlock();
    return legacy.hasProperty(net.minecraft.world.level.block.LiquidBlock.LEVEL)
        ? legacy.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL)
        : -1;
  }

  @Override
  public Ic2FluidStack drainWorldFluid(
      BlockState state, Level world, BlockPos position, boolean simulate) {
    if (state.getFluidState().isEmpty()) return null;
    var result = drainMb(state, world, position, null, null, Integer.MAX_VALUE, simulate);
    return result == null || result.isEmpty() ? null : result;
  }

  static FluidStack getForgeFs(Ic2FluidStack fluid) {
    if (fluid == null || fluid.isEmpty()) return FluidStack.EMPTY;
    if (fluid instanceof Ic2FluidStackImpl nativeFluid) return nativeFluid.parent;
    var serialized = new CompoundTag();
    fluid.toNbt(serialized);
    var decoded = Ic2FluidStackImpl.read(serialized);
    return decoded instanceof Ic2FluidStackImpl nativeFluid
        ? nativeFluid.parent
        : new FluidStack(fluid.getFluid(), fluid.getAmountMb());
  }
}
