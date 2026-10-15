package ic2ce.neoforge.transport;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.engine.fluid.FairAllocation;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.transport.menu.FairFluidMenu;
import ic2ce.neoforge.world.DirectionSets;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fair fan-out and inverted front consolidation have separate connectivity and routing policies.
 */
@NotClassic
public class FairFluidRouter extends InventoryBlockEntity
    implements IHasGui, INetworkClientTileEntityEventListener {
  private static final Direction[] DIRECTIONS = Direction.values();
  public final TankContainerInput inputSlot;
  public final OutputInventory OutputSlot;
  protected final FluidPorts fluids;
  @GuiSynced public final FluidPorts.InternalFluidTank fluidTank;
  private int[] demands, allocations;
  private int rotation;

  public FairFluidRouter(BlockPos position, BlockState state) {
    this(BlockEntityDefinitions.FLUID_DISTRIBUTOR, position, state);
  }

  protected FairFluidRouter(
      BlockEntityType<? extends FairFluidRouter> type, BlockPos position, BlockState state) {
    super(type, position, state);
    fluids = addComponent(new FluidPorts(this));
    fluidTank = fluids.addTank("fluidTank", 1000);
    inputSlot =
        new TankContainerInput(
            this,
            "inputSlot",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.BOTTOM,
            FluidContainerInventory.OpType.Fill,
            fluidTank);
    OutputSlot = new OutputInventory(this, "OutputSlot", 1);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    updateConnectivity();
  }

  @Override
  public void setActive(boolean active) {
    super.setActive(active);
    if (fluids != null) updateConnectivity();
  }

  @Override
  protected void setFacing(Level world, Direction facing) {
    super.setFacing(world, facing);
    if (fluids != null) updateConnectivity();
  }

  protected void updateConnectivity() {
    var front = getFacing();
    fluids.changeConnectivity(
        fluidTank,
        getActive() ? DirectionSets.except(front) : DirectionSets.ofMask(1 << front.ordinal()),
        Set.of());
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (inputSlot.processFromTank(fluidTank, OutputSlot)) super.setChanged();
    if (!fluidTank.isEmpty()) moveFluid();
  }

  protected void moveFluid() {
    if (getActive()) {
      FluidOutlet.send(this, fluidTank, getFacing(), fluidTank.getFluidAmount());
      return;
    }
    if (demands == null) {
      demands = new int[6];
      allocations = new int[6];
    }
    var world = getLevel();
    var offered = fluidTank.getFluidStack();
    for (var direction : DIRECTIONS) {
      int index = direction.ordinal();
      demands[index] = 0;
      var position = worldPosition.relative(direction);
      if (direction == getFacing() || !world.hasChunkAt(position)) continue;
      var target = world.getBlockEntity(position);
      if (target == null) continue;
      int capacity = LiquidUtil.fillTile(target, direction.getOpposite(), offered.copy(), true);
      if (capacity < 0 || capacity > offered.getAmountMb())
        throw new IllegalStateException("Receiver exceeded offered fluid");
      demands[index] = capacity;
    }
    FairAllocation.plan(fluidTank.getFluidAmount(), demands, allocations, rotation++);
    for (var direction : DIRECTIONS)
      if (allocations[direction.ordinal()] > 0)
        FluidOutlet.send(this, fluidTank, direction, allocations[direction.ordinal()]);
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if ((event == 0 || event == 1) && getLevel() != null && !getLevel().isClientSide()) {
      setActive(!getActive());
      super.setChanged();
    }
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new FairFluidMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new FairFluidMenu(syncId, inventory, this);
  }
}
