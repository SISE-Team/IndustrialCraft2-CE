package ic2ce.neoforge.fluid;

import ic2ce.api.fluid.*;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import org.apache.commons.lang3.mutable.Mutable;

/**
 * Native fluid service preserves actual world/block identities, independent container results and
 * declared gas phases.
 */
public final class FluidHandler {
  private FluidHandler() {}

  static final EnvFluidHandler ENV_HANDLER = ProjectRuntime.envProxy.createFluidStackHandler();

  static {
    FluidStackAccess.install(ENV_HANDLER);
  }

  private static volatile Map<Fluid, Boolean> gasPhases = Map.of();
  private static volatile List<Fluid> sourceCatalog;

  public static void initialize() {}

  public static synchronized EnvFluidHandler.FluidRefs createFluid(
      Identifier id,
      int density,
      int viscosity,
      int light,
      int temperature,
      boolean gaseous,
      Identifier still,
      Identifier flowing,
      int color) {
    var references =
        ENV_HANDLER.createFluid(id, density, viscosity, light, temperature, still, flowing, color);
    var next = new IdentityHashMap<>(gasPhases);
    next.put(references.still, gaseous);
    if (references.flowing != null) next.put(references.flowing, gaseous);
    gasPhases = Collections.unmodifiableMap(next);
    sourceCatalog = null;
    return references;
  }

  public static int getDensity(Fluid fluid) {
    return ENV_HANDLER.getDensity(fluid);
  }

  public static int getTemperature(Fluid fluid) {
    return ENV_HANDLER.getTemperature(fluid);
  }

  public static boolean isGaseous(Fluid fluid) {
    var declared = gasPhases.get(fluid);
    return declared == null ? ENV_HANDLER.isGaseous(fluid) : declared;
  }

  public static Identifier getStillSpriteId(Fluid fluid) {
    return ENV_HANDLER.getStillSpriteId(fluid);
  }

  public static Identifier getFlowingSpriteId(Fluid fluid) {
    return ENV_HANDLER.getFlowingSpriteId(fluid);
  }

  public static int getColor(Fluid fluid) {
    return ENV_HANDLER.getColor(fluid);
  }

  public static Ic2FluidStack drainMb(
      ItemStack stack, int amount, boolean simulate, Mutable<ItemStack> result) {
    if (stack == null || stack.isEmpty() || amount <= 0) return Ic2FluidStack.EMPTY;
    return stack.getItem() instanceof Ic2FluidItem item
        ? item.drainMb(stack, amount, simulate, result)
        : ENV_HANDLER.drainMb(stack, amount, simulate, result);
  }

  public static int drainMb(
      ItemStack stack, Ic2FluidStack requested, boolean simulate, Mutable<ItemStack> result) {
    if (stack == null || stack.isEmpty() || requested == null || requested.isEmpty()) return 0;
    return stack.getItem() instanceof Ic2FluidItem item
        ? item.drainMb(stack, requested, simulate, result)
        : ENV_HANDLER.drainMb(stack, requested, simulate, result);
  }

  public static int fillMb(
      ItemStack stack, Ic2FluidStack offered, boolean simulate, Mutable<ItemStack> result) {
    if (stack == null || stack.isEmpty() || offered == null || offered.isEmpty()) return 0;
    return stack.getItem() instanceof Ic2FluidItem item
        ? item.fillMb(stack, offered, simulate, result)
        : ENV_HANDLER.fillMb(stack, offered, simulate, result);
  }

  public static boolean isFluidBlock(
      BlockState state, Level world, BlockPos position, Direction side) {
    return state.getBlock() instanceof Ic2FluidBlock block
        ? block.isFluidBlock(
            state, world, position, world == null ? null : world.getBlockEntity(position))
        : ENV_HANDLER.isFluidBlock(state, world, position, null, side);
  }

  public static boolean isFluidBlock(BlockEntity tile, Direction side) {
    return tile != null && !tile.isRemoved() && isFluidBlock(tile.getBlockState(), tile, side);
  }

  public static boolean isFluidBlock(BlockState state, BlockEntity tile, Direction side) {
    return tile != null
        && !tile.isRemoved()
        && (state.getBlock() instanceof Ic2FluidBlock block
            ? block.isFluidBlock(state, tile.getLevel(), tile.getBlockPos(), tile)
            : ENV_HANDLER.isFluidBlock(state, tile.getLevel(), tile.getBlockPos(), tile, side));
  }

  public static Ic2FluidStack drainMb(
      BlockState state,
      Level world,
      BlockPos position,
      Direction side,
      int amount,
      boolean simulate) {
    if (amount <= 0) return Ic2FluidStack.EMPTY;
    return state.getBlock() instanceof Ic2FluidBlock block
        ? block.drainMb(
            state,
            world,
            position,
            world == null ? null : world.getBlockEntity(position),
            side,
            amount,
            simulate)
        : ENV_HANDLER.drainMb(state, world, position, null, side, amount, simulate);
  }

  public static Ic2FluidStack drainMb(
      BlockEntity tile, Direction side, int amount, boolean simulate) {
    return tile == null
        ? Ic2FluidStack.EMPTY
        : drainMb(tile.getBlockState(), tile, side, amount, simulate);
  }

  public static Ic2FluidStack drainMb(
      BlockState state, BlockEntity tile, Direction side, int amount, boolean simulate) {
    if (tile == null || tile.isRemoved() || amount <= 0) return Ic2FluidStack.EMPTY;
    return state.getBlock() instanceof Ic2FluidBlock block
        ? block.drainMb(state, tile.getLevel(), tile.getBlockPos(), tile, side, amount, simulate)
        : ENV_HANDLER.drainMb(
            state, tile.getLevel(), tile.getBlockPos(), tile, side, amount, simulate);
  }

  public static int drainMb(
      BlockState state,
      Level world,
      BlockPos position,
      Direction side,
      Ic2FluidStack requested,
      boolean simulate) {
    if (requested == null || requested.isEmpty()) return 0;
    return state.getBlock() instanceof Ic2FluidBlock block
        ? block.drainMb(
            state,
            world,
            position,
            world == null ? null : world.getBlockEntity(position),
            side,
            requested,
            simulate)
        : ENV_HANDLER.drainMb(state, world, position, null, side, requested, simulate);
  }

  public static int drainMb(
      BlockEntity tile, Direction side, Ic2FluidStack requested, boolean simulate) {
    if (tile == null || tile.isRemoved() || requested == null || requested.isEmpty()) return 0;
    var state = tile.getBlockState();
    return state.getBlock() instanceof Ic2FluidBlock block
        ? block.drainMb(state, tile.getLevel(), tile.getBlockPos(), tile, side, requested, simulate)
        : ENV_HANDLER.drainMb(
            state, tile.getLevel(), tile.getBlockPos(), tile, side, requested, simulate);
  }

  public static int fillMb(
      BlockState state,
      Level world,
      BlockPos position,
      Direction side,
      Ic2FluidStack offered,
      boolean simulate) {
    if (offered == null || offered.isEmpty()) return 0;
    return state.getBlock() instanceof Ic2FluidBlock block
        ? block.fillMb(
            state,
            world,
            position,
            world == null ? null : world.getBlockEntity(position),
            side,
            offered,
            simulate)
        : ENV_HANDLER.fillMb(state, world, position, null, side, offered, simulate);
  }

  public static int fillMb(
      BlockEntity tile, Direction side, Ic2FluidStack offered, boolean simulate) {
    if (tile == null || tile.isRemoved() || offered == null || offered.isEmpty()) return 0;
    var state = tile.getBlockState();
    return state.getBlock() instanceof Ic2FluidBlock block
        ? block.fillMb(state, tile.getLevel(), tile.getBlockPos(), tile, side, offered, simulate)
        : ENV_HANDLER.fillMb(
            state, tile.getLevel(), tile.getBlockPos(), tile, side, offered, simulate);
  }

  public static Fluid getWorldFluid(BlockState state) {
    return ENV_HANDLER.getWorldFluid(state, null, null);
  }

  public static Fluid getWorldFluid(BlockState state, Level world, BlockPos position) {
    return ENV_HANDLER.getWorldFluid(state, world, position);
  }

  public static int getWorldFluidLevel(BlockState state, Level world, BlockPos position) {
    return ENV_HANDLER.getWorldFluidLevel(state, world, position);
  }

  public static Ic2FluidStack drainWorldFluid(
      BlockState state, Level world, BlockPos position, boolean simulate) {
    return ENV_HANDLER.drainWorldFluid(state, world, position, simulate);
  }

  public static CompoundTag getFluidStackNbt(Ic2FluidStack fluid) {
    return ENV_HANDLER.getFluidStackNbt(fluid);
  }

  public static Ic2FluidStack createFluidStackMb(Fluid fluid, int amount, CompoundTag data) {
    return ENV_HANDLER.createFluidStackMb(fluid, amount, data);
  }

  public static Collection<Fluid> getAllFluids() {
    var current = sourceCatalog;
    if (current != null) return current;
    synchronized (FluidHandler.class) {
      if (sourceCatalog == null)
        sourceCatalog =
            BuiltInRegistries.FLUID.stream()
                .filter(fluid -> fluid != Fluids.EMPTY && fluid.isSource(fluid.defaultFluidState()))
                .toList();
      return sourceCatalog;
    }
  }
}
