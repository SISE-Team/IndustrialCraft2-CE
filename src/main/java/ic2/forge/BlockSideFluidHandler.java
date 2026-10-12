package ic2.forge;

import ic2.core.block.comp.Fluids;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.fluid.Ic2FluidStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/** Sided, transactional access to the actual IC2 tank component. */
final class BlockSideFluidHandler extends SnapshotJournal<List<BlockSideFluidHandler.TankSnapshot>> implements ResourceHandler<FluidResource> {
   private final Fluids component;
   private final Ic2TileEntity owner;
   private final @Nullable Direction side;

   BlockSideFluidHandler(Fluids component, Ic2TileEntity owner, @Nullable Direction side) {
      this.component = component;
      this.owner = owner;
      this.side = side;
   }

   private boolean canInsert(Fluids.InternalFluidTank tank) {
      if (this.side != null) return tank.canFill(this.side);
      for (Direction direction : Direction.values()) if (tank.canFill(direction)) return true;
      return false;
   }

   private boolean canExtract(Fluids.InternalFluidTank tank) {
      if (this.side != null) return tank.canDrain(this.side);
      for (Direction direction : Direction.values()) if (tank.canDrain(direction)) return true;
      return false;
   }

   private List<Fluids.InternalFluidTank> tanks() {
      List<Fluids.InternalFluidTank> tanks = new ArrayList<>();
      for (var tank : this.component.getAllTanks()) {
         if (this.canInsert(tank) || this.canExtract(tank)) tanks.add(tank);
      }
      return tanks;
   }

   private Fluids.InternalFluidTank tank(int index) {
      var tanks = this.tanks();
      Objects.checkIndex(index, tanks.size());
      return tanks.get(index);
   }

   @Override public int size() { return this.tanks().size(); }
   @Override public FluidResource getResource(int index) {
      return FluidResource.of(EnvFluidHandlerForge.getForgeFs(this.tank(index).getFluidStack()));
   }
   @Override public long getAmountAsLong(int index) { return this.tank(index).getFluidAmount(); }
   @Override public long getCapacityAsLong(int index, FluidResource resource) {
      return this.tank(index).getCapacity();
   }
   @Override public boolean isValid(int index, FluidResource resource) {
      var tank = this.tank(index);
      return !resource.isEmpty() && this.canInsert(tank) && tank.canFill(resource.getFluid());
   }

   @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
      var tank = this.tank(index);
      if (amount <= 0 || resource.isEmpty() || !this.canInsert(tank) || !tank.canFill(resource.getFluid())) return 0;
      var contents = new Ic2FluidStackImpl(resource.toStack(amount));
      if (tank.fillMb(contents, true) <= 0) return 0;
      this.updateSnapshots(transaction);
      return tank.fillMb(contents, false);
   }

   @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
      var tank = this.tank(index);
      var contents = tank.getFluidStack();
      if (amount <= 0 || resource.isEmpty() || !this.canExtract(tank) || contents == null ||
          !resource.equals(FluidResource.of(EnvFluidHandlerForge.getForgeFs(contents)))) return 0;
      if (tank.drainMb(amount, true).isEmpty()) return 0;
      this.updateSnapshots(transaction);
      return tank.drainMb(amount, false).getAmountMb();
   }

   record TankSnapshot(Fluids.InternalFluidTank tank, @Nullable Ic2FluidStack contents) {}

   @Override protected List<TankSnapshot> createSnapshot() {
      List<TankSnapshot> snapshots = new ArrayList<>();
      for (var tank : this.component.getAllTanks()) {
         snapshots.add(new TankSnapshot(tank, tank.getFluidStack() == null ? null : tank.getFluidStack().copy()));
      }
      return snapshots;
   }

   @Override protected void revertToSnapshot(List<TankSnapshot> snapshots) {
      for (var snapshot : snapshots) {
         snapshot.tank().setFluidStack(snapshot.contents() == null ? null : snapshot.contents().copy());
      }
   }

   @Override protected void onRootCommit(List<TankSnapshot> originalState) {
      if (!this.owner.isRemoved()) this.owner.setChanged();
   }
}
