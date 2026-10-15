package ic2ce.neoforge.transport;

import ic2ce.api.network.ClientModifiable;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.runtime.SideServices;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.network.ClientFieldValidator;
import ic2ce.neoforge.transport.menu.PriorityFluidMenu;
import ic2ce.neoforge.world.DirectionSets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Ordered fluid overflow visits the next outlet only after the earlier outlet stops accepting. */
@NotClassic
public class PriorityFluidRouter extends FairFluidRouter
    implements OrderedOutlets, ClientFieldValidator {
  @ClientModifiable protected List<Direction> priority = new ArrayList<>(5);

  public PriorityFluidRouter(BlockPos position, BlockState state) {
    super(TransportBlockEntities.WEIGHTED_FLUID_DISTRIBUTOR, position, state);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    priority = OutletOrder.read(data, getFacing());
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    OutletOrder.write(data, getPriority());
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.add("priority");
    return fields;
  }

  @Override
  protected void updateConnectivity() {
    priority = OutletOrder.normalize(priority, getFacing());
    fluids.changeConnectivity(
        fluidTank, DirectionSets.ofMask(1 << getFacing().ordinal()), Set.of());
  }

  @Override
  protected void moveFluid() {
    for (var direction : getPriority()) {
      FluidOutlet.send(this, fluidTank, direction, fluidTank.getFluidAmount());
      if (fluidTank.isEmpty()) break;
    }
  }

  @Override
  public List<Direction> getPriority() {
    priority = OutletOrder.normalize(priority, getFacing());
    return priority;
  }

  @Override
  public void updatePriority(boolean server) {
    getPriority();
    if (server) super.setChanged();
    SideServices.network(server).updateTileEntityField(this, "priority");
  }

  @Override
  public void acceptClientField(String field) {
    if (field.equals("priority")) updatePriority(true);
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event < 0 || getLevel() == null || getLevel().isClientSide()) return;
    int index = event / 10, code = event % 10;
    var order = getPriority();
    if (index > order.size() || index >= 5 || code >= 6) return;
    var direction = Direction.from3DDataValue(code);
    if (direction == getFacing()
        || order.contains(direction) && (index == order.size() || order.get(index) != direction))
      return;
    if (index == order.size()) order.add(direction);
    else order.set(index, direction);
    updatePriority(true);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new PriorityFluidMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new PriorityFluidMenu(syncId, inventory, this);
  }
}
