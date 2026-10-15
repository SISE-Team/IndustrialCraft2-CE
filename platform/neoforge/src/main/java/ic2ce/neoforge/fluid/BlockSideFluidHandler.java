package ic2ce.neoforge.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * Native sided access scans actual tank identities without per-query list copies; each mutated tank
 * journals separately.
 */
public final class BlockSideFluidHandler implements ResourceHandler<FluidResource> {
  private static final Direction[] SIDES = Direction.values();
  private final FluidPorts component;
  private final NativeBlockEntity owner;
  private final @Nullable Direction side;
  private final Map<FluidPorts.InternalFluidTank, TankJournal> journals = new IdentityHashMap<>();

  public BlockSideFluidHandler(FluidPorts component, NativeBlockEntity owner, @Nullable Direction side) {
    this.component = Objects.requireNonNull(component);
    this.owner = Objects.requireNonNull(owner);
    this.side = side;
    if (component.getParent() != owner)
      throw new IllegalArgumentException("Fluid capability owner mismatch");
  }

  private boolean input(FluidPorts.InternalFluidTank tank) {
    if (side != null) return tank.canFill(side);
    for (var candidate : SIDES) if (tank.canFill(candidate)) return true;
    return false;
  }

  private boolean output(FluidPorts.InternalFluidTank tank) {
    if (side != null) return tank.canDrain(side);
    for (var candidate : SIDES) if (tank.canDrain(candidate)) return true;
    return false;
  }

  private FluidPorts.InternalFluidTank tank(int index) {
    if (index < 0) throw new IndexOutOfBoundsException(index);
    int visible = 0;
    for (var tank : component.getAllTanks())
      if (input(tank) || output(tank)) {
        if (visible++ == index) return tank;
      }
    throw new IndexOutOfBoundsException(index);
  }

  @Override
  public int size() {
    int count = 0;
    for (var tank : component.getAllTanks()) if (input(tank) || output(tank)) count++;
    return count;
  }

  private static FluidResource resource(Ic2FluidStack contents) {
    return contents == null || contents.isEmpty()
        ? FluidResource.EMPTY
        : FluidResource.of(EnvFluidHandlerNeoForge.getForgeFs(contents));
  }

  @Override
  public FluidResource getResource(int index) {
    return resource(tank(index).getFluidStack());
  }

  @Override
  public long getAmountAsLong(int index) {
    return tank(index).getFluidAmount();
  }

  @Override
  public long getCapacityAsLong(int index, FluidResource resource) {
    return tank(index).getCapacity();
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    var tank = tank(index);
    return !owner.isRemoved()
        && !resource.isEmpty()
        && input(tank)
        && tank.canFill(resource.getFluid());
  }

  @Override
  public int insert(int index, FluidResource offered, int amount, TransactionContext transaction) {
    var tank = tank(index);
    if (owner.isRemoved()
        || amount <= 0
        || offered.isEmpty()
        || !input(tank)
        || !tank.canFill(offered.getFluid())) return 0;
    int room = Math.max(0, tank.getCapacity() - tank.getFluidAmount());
    if (room == 0) return 0;
    var contents = new Ic2FluidStackImpl(offered.toStack(Math.min(amount, room)));
    if (tank.fillMb(contents, true) == 0) return 0;
    journals.computeIfAbsent(tank, TankJournal::new).updateSnapshots(transaction);
    return tank.fillMb(contents, false);
  }

  @Override
  public int extract(
      int index, FluidResource requested, int amount, TransactionContext transaction) {
    var tank = tank(index);
    if (owner.isRemoved()
        || amount <= 0
        || requested.isEmpty()
        || !output(tank)
        || !requested.equals(resource(tank.getFluidStack()))) return 0;
    if (tank.getFluidAmount() == 0) return 0;
    journals.computeIfAbsent(tank, TankJournal::new).updateSnapshots(transaction);
    return tank.drainMb(amount, false).getAmountMb();
  }

  private final class TankJournal extends SnapshotJournal<@Nullable Ic2FluidStack> {
    private final FluidPorts.InternalFluidTank tank;

    TankJournal(FluidPorts.InternalFluidTank tank) {
      this.tank = tank;
    }

    @Override
    protected @Nullable Ic2FluidStack createSnapshot() {
      var contents = tank.getFluidStack();
      return contents == null ? null : contents.copy();
    }

    @Override
    protected void revertToSnapshot(@Nullable Ic2FluidStack saved) {
      tank.setFluidStack(saved == null ? null : saved.copy());
    }

    @Override
    protected void onRootCommit(@Nullable Ic2FluidStack original) {
      if (!owner.isRemoved()) owner.setChanged();
    }
  }
}
