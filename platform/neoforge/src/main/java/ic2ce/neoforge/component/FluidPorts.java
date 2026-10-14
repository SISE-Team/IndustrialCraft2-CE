package ic2ce.neoforge.component;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.ILiquidAcceptManager;
import ic2ce.core.fluid.FluidTankInfo;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.inventory.InventorySlot;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/** Native tank ownership, sided access and aggregate transfer without mutating requests. */
public class FluidPorts extends NativeComponent {
  protected final List<InternalFluidTank> managedTanks = new ArrayList<>();
  protected final List<Supplier<? extends Collection<InternalFluidTank>>> unmanagedTanks =
      new ArrayList<>();
  private static final Predicate<Fluid> ANY_FLUID = _ -> true;
  private final Iterable<InternalFluidTank> combined = this::tankIterator;

  public FluidPorts(NativeBlockEntity parent) {
    super(parent);
  }

  public InternalFluidTank addTankInsert(String name, int capacity) {
    return addTankInsert(name, capacity, ANY_FLUID);
  }

  public InternalFluidTank addTankInsert(String name, int capacity, Predicate<Fluid> accepted) {
    return addTankInsert(name, capacity, InventorySlot.InvSide.ANY, accepted);
  }

  public InternalFluidTank addTankInsert(String name, int capacity, InventorySlot.InvSide side) {
    return addTankInsert(name, capacity, side, ANY_FLUID);
  }

  public InternalFluidTank addTankInsert(
      String name, int capacity, InventorySlot.InvSide side, Predicate<Fluid> accepted) {
    return addTank(name, capacity, InventorySlot.Access.I, side, accepted);
  }

  public InternalFluidTank addTankExtract(String name, int capacity) {
    return addTankExtract(name, capacity, InventorySlot.InvSide.ANY);
  }

  public InternalFluidTank addTankExtract(String name, int capacity, InventorySlot.InvSide side) {
    return addTank(name, capacity, InventorySlot.Access.O, side);
  }

  public InternalFluidTank addTank(String name, int capacity) {
    return addTank(name, capacity, InventorySlot.Access.IO);
  }

  public InternalFluidTank addTank(String name, int capacity, InventorySlot.Access access) {
    return addTank(name, capacity, access, InventorySlot.InvSide.ANY);
  }

  public InternalFluidTank addTank(String name, int capacity, Predicate<Fluid> accepted) {
    return addTank(name, capacity, InventorySlot.Access.IO, InventorySlot.InvSide.ANY, accepted);
  }

  public InternalFluidTank addTank(
      String name, int capacity, InventorySlot.Access access, InventorySlot.InvSide side) {
    return addTank(name, capacity, access, side, ANY_FLUID);
  }

  public InternalFluidTank addTank(
      String name,
      int capacity,
      InventorySlot.Access access,
      InventorySlot.InvSide side,
      Predicate<Fluid> accepted) {
    return addTank(
        name,
        capacity,
        access.isInput() ? side.getAcceptedSides() : Set.of(),
        access.isOutput() ? side.getAcceptedSides() : Set.of(),
        accepted);
  }

  public InternalFluidTank addTank(
      String name,
      int capacity,
      Collection<Direction> input,
      Collection<Direction> output,
      Predicate<Fluid> accepted) {
    return addTank(new InternalFluidTank(name, input, output, accepted, capacity));
  }

  public InternalFluidTank addTank(InternalFluidTank tank) {
    Objects.requireNonNull(tank);
    for (InternalFluidTank previous : managedTanks)
      if (previous.identifier.equals(tank.identifier))
        throw new IllegalArgumentException("Duplicate fluid tank name");
    managedTanks.add(tank);
    return tank;
  }

  public void addUnmanagedTanks(InternalFluidTank tank) {
    addUnmanagedTanks(List.of(tank));
  }

  public void addUnmanagedTanks(Collection<InternalFluidTank> tanks) {
    addUnmanagedTankHook(() -> tanks);
  }

  public void addUnmanagedTankHook(Supplier<? extends Collection<InternalFluidTank>> supplier) {
    unmanagedTanks.add(Objects.requireNonNull(supplier));
  }

  public void changeConnectivity(
      InternalFluidTank tank, InventorySlot.Access access, InventorySlot.InvSide side) {
    changeConnectivity(
        tank,
        access.isInput() ? side.getAcceptedSides() : Set.of(),
        access.isOutput() ? side.getAcceptedSides() : Set.of());
  }

  public void changeConnectivity(
      InternalFluidTank tank, Collection<Direction> input, Collection<Direction> output) {
    if (!managedTanks.contains(tank))
      throw new IllegalArgumentException("Tank is not owned by this component");
    tank.connect(input, output);
  }

  public Ic2FluidTank getFluidTank(String name) {
    for (InternalFluidTank tank : getAllTanks()) if (tank.identifier.equals(name)) return tank;
    throw new IllegalArgumentException("Unknown fluid tank: " + name);
  }

  @Override
  public void readFromNbt(CompoundTag data) {
    for (InternalFluidTank tank : managedTanks)
      if (data.contains(tank.identifier)) tank.fromNbt(data.getCompoundOrEmpty(tank.identifier));
  }

  @Override
  public CompoundTag writeToNbt() {
    var data = new CompoundTag();
    for (InternalFluidTank tank : managedTanks)
      data.put(tank.identifier, tank.toNbt(new CompoundTag()));
    return data;
  }

  public FluidTankInfo[] getTankInfos() {
    var result = new ArrayList<FluidTankInfo>(managedTanks.size());
    for (InternalFluidTank tank : managedTanks)
      if ((tank.inputMask | tank.outputMask) != 0)
        result.add(
            new FluidTankInfo(
                tank.outputMask, tank.inputMask, tank.getCapacity(), tank.getFluidStack()));
    return result.isEmpty() ? null : result.toArray(FluidTankInfo[]::new);
  }

  public Ic2FluidStack drainMb(Direction side, int maximum, boolean simulate) {
    if (maximum <= 0) return Ic2FluidStack.EMPTY;
    for (InternalFluidTank tank : getAllTanks())
      if (tank.canDrain(side)) {
        Ic2FluidStack sample = tank.drainMb(maximum, true);
        if (sample.isEmpty()) continue;
        Ic2FluidStack request = sample.copyWithAmountMb(maximum);
        int drained = drainMb(side, request, simulate);
        return drained == 0 ? Ic2FluidStack.EMPTY : sample.copyWithAmountMb(drained);
      }
    return Ic2FluidStack.EMPTY;
  }

  public int drainMb(Direction side, Ic2FluidStack request, boolean simulate) {
    if (request == null || request.isEmpty()) return 0;
    int remaining = request.getAmountMb();
    for (InternalFluidTank tank : getAllTanks()) {
      if (tank.canDrain(side)) remaining -= tank.drainMbLimited(request, remaining, simulate);
      if (remaining == 0) break;
    }
    return request.getAmountMb() - remaining;
  }

  public int fillMb(Direction side, Ic2FluidStack request, boolean simulate) {
    if (request == null || request.isEmpty()) return 0;
    int remaining = request.getAmountMb();
    for (InternalFluidTank tank : getAllTanks()) {
      if (tank.canFill(request.getFluid(), side))
        remaining -= tank.fillMbLimited(request, remaining, simulate);
      if (remaining == 0) break;
    }
    return request.getAmountMb() - remaining;
  }

  public static Predicate<Fluid> fluidPredicate(Fluid... values) {
    var allowed = Collections.newSetFromMap(new IdentityHashMap<Fluid, Boolean>());
    Collections.addAll(allowed, values);
    return allowed::contains;
  }

  public static Predicate<Fluid> fluidPredicate(TagKey<Fluid> tag) {
    return fluid -> fluid != null && fluid.is(tag);
  }

  public static Predicate<Fluid> fluidPredicate(ILiquidAcceptManager manager) {
    return manager::acceptsFluid;
  }

  public Iterable<InternalFluidTank> getAllTanks() {
    return unmanagedTanks.isEmpty() ? managedTanks : combined;
  }

  private Iterator<InternalFluidTank> tankIterator() {
    return new Iterator<>() {
      private Iterator<InternalFluidTank> current = managedTanks.iterator();
      private int hook;
      private InternalFluidTank ready;
      private final Set<InternalFluidTank> seen =
          Collections.newSetFromMap(new IdentityHashMap<>());

      public boolean hasNext() {
        if (ready != null) return true;
        while (true) {
          while (current.hasNext()) {
            InternalFluidTank candidate = current.next();
            if (candidate != null && seen.add(candidate)) {
              ready = candidate;
              return true;
            }
          }
          if (hook >= unmanagedTanks.size()) return false;
          Collection<InternalFluidTank> next = unmanagedTanks.get(hook++).get();
          current = next == null ? Collections.emptyIterator() : next.iterator();
        }
      }

      public InternalFluidTank next() {
        if (!hasNext()) throw new NoSuchElementException();
        InternalFluidTank result = ready;
        ready = null;
        return result;
      }
    };
  }

  public static class InternalFluidTank extends Ic2FluidTank {
    protected final String identifier;
    private final Predicate<Fluid> accepted;
    private int inputMask, outputMask;

    protected InternalFluidTank(
        String name,
        Collection<Direction> input,
        Collection<Direction> output,
        Predicate<Fluid> accepted,
        int capacity) {
      super(capacity);
      identifier = Objects.requireNonNull(name);
      this.accepted = Objects.requireNonNull(accepted);
      connect(input, output);
    }

    private void connect(Collection<Direction> input, Collection<Direction> output) {
      inputMask = mask(input);
      outputMask = mask(output);
    }

    private static int mask(Collection<Direction> directions) {
      int mask = 0;
      for (Direction direction : directions) mask |= 1 << direction.ordinal();
      return mask;
    }

    @Override
    public boolean canFill(Fluid fluid) {
      return fluid != null && accepted.test(fluid);
    }

    public boolean canFill(Direction side) {
      return side != null && (inputMask & 1 << side.ordinal()) != 0;
    }

    public boolean canFill(Fluid fluid, Direction side) {
      return canFill(side) && canFill(fluid);
    }

    public boolean canDrain(Direction side) {
      return side != null && (outputMask & 1 << side.ordinal()) != 0;
    }
  }
}
