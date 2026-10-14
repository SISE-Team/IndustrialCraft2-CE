package ic2ce.core.util;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.util.FluidContainerOutputMode;
import ic2ce.core.fluid.FluidHandler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableObject;

/** Pure container plans and bounded world-thread fluid transfers. */
public final class LiquidUtil {
  private LiquidUtil() {}

  public static List<Fluid> getAllFluidsSorted() {
    var sorted = new ArrayList<>(FluidHandler.getAllFluids());
    sorted.sort(Comparator.comparing(BuiltInRegistries.FLUID::getKey));
    return sorted;
  }

  public static LiquidData getLiquid(Level world, BlockPos position) {
    var state = world.getBlockState(position);
    var block = state.getBlock();
    if (block == Blocks.WATER || block == Blocks.LAVA)
      return new LiquidData(
          block == Blocks.WATER ? Fluids.WATER : Fluids.LAVA,
          state.getValue(LiquidBlock.LEVEL) == 0);
    Fluid fluid = FluidHandler.getWorldFluid(state, world, position);
    if (fluid == null || fluid == Fluids.EMPTY) return null;
    var drained = FluidHandler.drainWorldFluid(state, world, position, true);
    return new LiquidData(fluid, drained != null && drained.getAmountMb() >= 1000);
  }

  public static boolean isFluidContainer(ItemStack stack) {
    return stack != null && !stack.isEmpty() && Ic2FluidStack.get(stack) != null;
  }

  public static boolean isDrainableFluidContainer(ItemStack stack) {
    var fluid = stack == null || stack.isEmpty() ? null : Ic2FluidStack.get(stack);
    return fluid != null && !fluid.isEmpty();
  }

  public static boolean isFillableFluidContainer(ItemStack stack) {
    return isFillableFluidContainer(stack, null);
  }

  public static boolean isFillableFluidContainer(ItemStack stack, Iterable<Fluid> allowed) {
    if (!isFluidContainer(stack)) return false;
    var single = stack.copyWithCount(1);
    if (allowed == null) {
      var contents = Ic2FluidStack.getAll(single);
      if (contents != null)
        for (var content : contents)
          if (!content.isEmpty()
              && FluidHandler.fillMb(
                      single, content.copyWithAmountMb(Integer.MAX_VALUE), true, null)
                  > 0) return true;
    }
    for (var fluid : allowed == null ? FluidHandler.getAllFluids() : allowed)
      if (fluid != null
          && fluid != Fluids.EMPTY
          && FluidHandler.fillMb(single, Ic2FluidStack.create(fluid, Integer.MAX_VALUE), true, null)
              > 0) return true;
    return false;
  }

  private static FluidOperationResult routeContainer(
      ItemStack original,
      ItemStack changed,
      Ic2FluidStack fluid,
      boolean finished,
      FluidContainerOutputMode mode) {
    Objects.requireNonNull(mode);
    if (changed == null || changed.isEmpty())
      return new FluidOperationResult(fluid, original.copyWithCount(original.getCount() - 1), null);
    if (changed.getCount() != 1)
      throw new IllegalStateException("Fluid handler returned more than one converted container");
    boolean separate =
        switch (mode) {
          case AnyToOutput -> true;
          case EmptyFullToOutput -> finished;
          case InPlacePreferred -> original.getCount() > 1;
          case InPlace -> false;
        };
    if (!separate && original.getCount() > 1) return null;
    return separate
        ? new FluidOperationResult(fluid, original.copyWithCount(original.getCount() - 1), changed)
        : new FluidOperationResult(fluid, changed, null);
  }

  public static FluidOperationResult drainContainer(
      ItemStack original, Fluid requested, int maximum, FluidContainerOutputMode mode) {
    if (!isFluidContainer(original) || maximum <= 0) return null;
    var single = original.copyWithCount(1);
    var changed = new MutableObject<>(single);
    Ic2FluidStack drained;
    if (requested == null) drained = FluidHandler.drainMb(single, maximum, false, changed);
    else {
      drained = null;
      var contents = Ic2FluidStack.getAll(single);
      if (contents != null)
        for (var candidate : contents)
          if (!candidate.isEmpty() && candidate.getFluid() == requested) {
            var quantity = candidate.copyWithAmountMb(Math.min(maximum, candidate.getAmountMb()));
            int removed = FluidHandler.drainMb(single, quantity, false, changed);
            if (removed > 0) {
              drained = quantity.copyWithAmountMb(removed);
              break;
            }
          }
    }
    if (drained == null || drained.isEmpty()) return null;
    if (drained.getAmountMb() > maximum)
      throw new IllegalStateException("Container drained more than requested");
    var result = changed.getValue();
    var remaining =
        result == null || result.isEmpty()
            ? null
            : FluidHandler.drainMb(result, Integer.MAX_VALUE, true, null);
    return routeContainer(
        original, result, drained, remaining == null || remaining.isEmpty(), mode);
  }

  public static FluidOperationResult fillContainer(
      ItemStack original, Ic2FluidStack offered, FluidContainerOutputMode mode) {
    if (!isFluidContainer(original) || offered == null || offered.isEmpty()) return null;
    var single = original.copyWithCount(1);
    var changed = new MutableObject<>(single);
    int accepted = FluidHandler.fillMb(single, offered.copy(), false, changed);
    if (accepted <= 0) return null;
    if (accepted > offered.getAmountMb())
      throw new IllegalStateException("Container filled more than offered");
    var result = changed.getValue();
    boolean full =
        result == null
            || result.isEmpty()
            || FluidHandler.fillMb(result, offered.copyWithAmountMb(Integer.MAX_VALUE), true, null)
                == 0;
    return routeContainer(original, result, offered.copyWithAmountMb(accepted), full, mode);
  }

  private static boolean hasPlayerOutputSpace(FluidOperationResult plan, Player player) {
    return plan != null
        && (plan.extraOutput == null
            || plan.extraOutput.isEmpty()
            || StackUtil.storeInventoryItem(plan.extraOutput, player, true));
  }

  private static void commitPlayer(FluidOperationResult plan, Player player, InteractionHand hand) {
    if (plan.extraOutput != null
        && !plan.extraOutput.isEmpty()
        && !StackUtil.storeInventoryItem(plan.extraOutput, player, false))
      throw new IllegalStateException("Fluid container inventory capacity changed before commit");
    StackUtil.set(player, hand, plan.inPlaceOutput);
  }

  public static Ic2FluidStack drainContainer(
      Player player,
      InteractionHand hand,
      Fluid fluid,
      int maximum,
      FluidContainerOutputMode mode,
      boolean simulate) {
    var plan = drainContainer(StackUtil.get(player, hand), fluid, maximum, mode);
    if (!hasPlayerOutputSpace(plan, player)) return null;
    if (!simulate) commitPlayer(plan, player, hand);
    return plan.fluidChange;
  }

  public static int fillContainer(
      Player player,
      InteractionHand hand,
      Ic2FluidStack fluid,
      FluidContainerOutputMode mode,
      boolean simulate) {
    var plan = fillContainer(StackUtil.get(player, hand), fluid, mode);
    if (!hasPlayerOutputSpace(plan, player)) return 0;
    if (!simulate) commitPlayer(plan, player, hand);
    return plan.fluidChange.getAmountMb();
  }

  public static boolean isFluidTile(BlockEntity tile, Direction side) {
    return tile != null && FluidHandler.isFluidBlock(tile, side);
  }

  public static boolean isFluidTile(BlockState state, BlockEntity tile, Direction side) {
    return tile != null && FluidHandler.isFluidBlock(state, tile, side);
  }

  public static Ic2FluidStack drainTile(
      BlockState state, BlockEntity tile, Direction side, int maximum, boolean simulate) {
    return tile == null || maximum <= 0
        ? null
        : FluidHandler.drainMb(state, tile, side, maximum, simulate);
  }

  public static Ic2FluidStack drainTile(
      BlockState state,
      Level world,
      BlockPos position,
      Direction side,
      int maximum,
      boolean simulate) {
    return maximum <= 0
        ? null
        : FluidHandler.drainMb(state, world, position, side, maximum, simulate);
  }

  public static int drainTile(
      BlockEntity tile, Direction side, Fluid fluid, int maximum, boolean simulate) {
    return tile == null || fluid == null || maximum <= 0
        ? 0
        : FluidHandler.drainMb(tile, side, Ic2FluidStack.create(fluid, maximum), simulate);
  }

  public static int fillTile(
      BlockEntity tile, Direction side, Ic2FluidStack fluid, boolean simulate) {
    return tile == null || fluid == null || fluid.isEmpty()
        ? 0
        : FluidHandler.fillMb(tile, side, fluid, simulate);
  }

  public static AdjacentFluidHandler getAdjacentHandler(BlockEntity source, Direction direction) {
    if (source == null || source.getLevel() == null) return null;
    var target = source.getLevel().getBlockEntity(source.getBlockPos().relative(direction));
    return isFluidTile(target, direction.getOpposite())
        ? new AdjacentFluidHandler(target, direction)
        : null;
  }

  public static List<AdjacentFluidHandler> getAdjacentHandlers(BlockEntity source) {
    var result = new ArrayList<AdjacentFluidHandler>(6);
    for (var direction : Direction.values()) {
      var adjacent = getAdjacentHandler(source, direction);
      if (adjacent != null) result.add(adjacent);
    }
    return result;
  }

  public static int distribute(BlockEntity source, Ic2FluidStack offered, boolean simulate) {
    if (offered == null || offered.isEmpty()) return 0;
    int remaining = offered.getAmountMb();
    for (var direction : Direction.values()) {
      var adjacent = getAdjacentHandler(source, direction);
      if (adjacent == null) continue;
      int accepted =
          fillTile(
              adjacent.handler,
              direction.getOpposite(),
              offered.copyWithAmountMb(remaining),
              simulate);
      if (accepted < 0 || accepted > remaining)
        throw new IllegalStateException("Adjacent handler violated fluid quantity bounds");
      remaining -= accepted;
      if (remaining == 0) break;
    }
    return offered.getAmountMb() - remaining;
  }

  public static Ic2FluidStack transfer(
      BlockEntity source, Direction direction, BlockEntity target, int maximum) {
    if (source == null || target == null || maximum <= 0) return null;
    var opposite = direction == null ? null : direction.getOpposite();
    for (int attempt = 0; attempt < 64; attempt++) {
      var offered = drainTile(source.getBlockState(), source, direction, maximum, true);
      if (offered == null || offered.isEmpty()) return null;
      if (offered.getAmountMb() > maximum)
        throw new IllegalStateException("Source drained more than requested");
      int accepted = fillTile(target, opposite, offered.copy(), true);
      if (accepted < 0 || accepted > offered.getAmountMb())
        throw new IllegalStateException("Target accepted more than offered");
      if (accepted == 0) return null;
      if (accepted != offered.getAmountMb()) {
        maximum = accepted;
        continue;
      }
      var actual = drainTile(source.getBlockState(), source, direction, accepted, false);
      if (actual == null || actual.getAmountMb() != accepted || !actual.hasExactFluid(offered))
        throw new IllegalStateException("Source fluid changed between planning and commit");
      if (fillTile(target, opposite, actual.copy(), false) != accepted)
        throw new IllegalStateException("Target fluid changed between planning and commit");
      return actual;
    }
    return null;
  }

  public static int distributeAll(BlockEntity source, int maximum) {
    Objects.requireNonNull(source);
    if (maximum <= 0) return 0;
    int remaining = maximum;
    for (var direction : Direction.values()) {
      var adjacent = getAdjacentHandler(source, direction);
      if (adjacent == null) continue;
      var transferred = transfer(source, direction, adjacent.handler, remaining);
      if (transferred != null) {
        remaining -= transferred.getAmountMb();
        if (remaining == 0) break;
      }
    }
    return maximum - remaining;
  }

  public static boolean check(Ic2FluidStack fluid) {
    return fluid != null && !fluid.isEmpty() && fluid.getFluid() != Fluids.EMPTY;
  }

  public static Ic2FluidStack drainWorldFluidBlock(
      Level world, BlockPos position, boolean simulate) {
    var state = world.getBlockState(position);
    var block = state.getBlock();
    if (block == Blocks.WATER || block == Blocks.LAVA) {
      if (state.getValue(LiquidBlock.LEVEL) != 0) return null;
      var source = Ic2FluidStack.create(block == Blocks.WATER ? Fluids.WATER : Fluids.LAVA, 1000);
      if (!simulate && !world.setBlockAndUpdate(position, Blocks.AIR.defaultBlockState()))
        return null;
      return source;
    }
    return FluidHandler.drainWorldFluid(state, world, position, simulate);
  }

  public static boolean drainWorldFluidBlockToContainer(
      Level world, BlockPos position, Player player, InteractionHand hand) {
    var available = drainWorldFluidBlock(world, position, true);
    if (!check(available)) return false;
    var plan =
        fillContainer(
            StackUtil.get(player, hand), available, FluidContainerOutputMode.InPlacePreferred);
    if (!hasPlayerOutputSpace(plan, player)
        || plan.fluidChange.getAmountMb() != available.getAmountMb()) return false;
    var actual = drainWorldFluidBlock(world, position, false);
    if (actual == null || !actual.equals(plan.fluidChange)) return false;
    commitPlayer(plan, player, hand);
    return true;
  }

  public static boolean fillWorldFluidBlock(
      Ic2FluidStack fluid, Level world, BlockPos position, boolean simulate) {
    if (!check(fluid) || fluid.getAmountMb() < 1000) return false;
    var block = fluid.getFluid().defaultFluidState().createLegacyBlock().getBlock();
    if (block == Blocks.AIR) return false;
    var current = world.getBlockState(position);
    if (!current.isAir() && current.isSolid()) return false;
    var present = getLiquid(world, position);
    if (current.getBlock() == block && present != null && present.isSource) return false;
    if (simulate) return true;
    if (world.dimension() == Level.NETHER
        && fluid.getFluid().defaultFluidState().is(net.minecraft.tags.FluidTags.WATER)) {
      world.playSound(
          null,
          position,
          SoundEvents.FIRE_EXTINGUISH,
          SoundSource.BLOCKS,
          0.5F,
          2.6F + (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.8F);
      for (int i = 0; i < 8; i++)
        world.addParticle(
            ParticleTypes.LARGE_SMOKE,
            position.getX() + world.getRandom().nextDouble(),
            position.getY() + world.getRandom().nextDouble(),
            position.getZ() + world.getRandom().nextDouble(),
            0,
            0,
            0);
    } else if (!world.setBlockAndUpdate(position, block.defaultBlockState())) return false;
    fluid.decreaseMb(1000);
    return true;
  }

  public static boolean fillWorldFluidBlockFromContainer(
      Level world, BlockPos position, Player player, InteractionHand hand) {
    var plan =
        drainContainer(
            StackUtil.get(player, hand), null, 1000, FluidContainerOutputMode.InPlacePreferred);
    if (!hasPlayerOutputSpace(plan, player) || plan.fluidChange.getAmountMb() != 1000) return false;
    if (!fillWorldFluidBlock(plan.fluidChange.copy(), world, position, false)) return false;
    commitPlayer(plan, player, hand);
    return true;
  }

  public static boolean storeOutputContainer(MutableObject<ItemStack> output, Player player) {
    return output.getValue() == null
        || output.getValue().isEmpty()
        || StackUtil.storeInventoryItem(output.getValue(), player, false);
  }

  public static final class AdjacentFluidHandler {
    public final BlockEntity handler;
    public final Direction dir;

    AdjacentFluidHandler(BlockEntity handler, Direction direction) {
      this.handler = handler;
      dir = direction;
    }
  }

  public static final class FluidOperationResult {
    public final Ic2FluidStack fluidChange;
    public final ItemStack inPlaceOutput, extraOutput;

    FluidOperationResult(Ic2FluidStack fluid, ItemStack input, ItemStack extra) {
      fluidChange = Objects.requireNonNull(fluid);
      inPlaceOutput = input == null ? ItemStack.EMPTY : input;
      extraOutput = extra;
    }
  }

  public static final class LiquidData {
    public final Fluid liquid;
    public final boolean isSource;

    LiquidData(Fluid fluid, boolean source) {
      liquid = fluid;
      isSource = source;
    }
  }
}
