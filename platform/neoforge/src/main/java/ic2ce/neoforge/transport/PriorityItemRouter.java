package ic2ce.neoforge.transport;

import ic2ce.api.network.ClientModifiable;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.network.ClientFieldValidator;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.neoforge.transport.menu.PriorityItemMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ordered item overflow exhausts the first inventory's real capacity before using another target.
 */
@NotClassic
public class PriorityItemRouter extends InventoryBlockEntity
    implements IHasGui, OrderedOutlets, ClientFieldValidator {
  @ClientModifiable protected List<Direction> priority = new ArrayList<>(5);
  public final InventorySlot buffer = new InventorySlot(this, "buffer", InventorySlot.Access.I, 9);

  public PriorityItemRouter(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.WEIGHTED_ITEM_DISTRIBUTOR, position, state);
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
  protected void onLoaded() {
    super.onLoaded();
    updateConnectivity();
  }

  @Override
  protected void setFacing(Level world, Direction facing) {
    super.setFacing(world, facing);
    updateConnectivity();
  }

  protected void updateConnectivity() {
    priority = OutletOrder.normalize(priority, getFacing());
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (buffer.isEmpty()) return;
    boolean changed = false;
    for (var direction : getPriority()) {
      if (!getLevel().hasChunkAt(worldPosition.relative(direction))) continue;
      var target = StackUtil.ENV.getAdjacentInventory(this, direction);
      if (target == null) continue;
      for (int index = 0; index < buffer.size(); index++) {
        while (!buffer.isEmpty(index)) {
          var snapshot = buffer.get(index).copy();
          int offered = StackUtil.ENV.deposit(target, snapshot.copy(), true);
          if (offered < 0 || offered > snapshot.getCount())
            throw new IllegalStateException("Inventory target exceeded its preview quantity");
          if (offered == 0) break;
          int delivered = StackUtil.ENV.deposit(target, snapshot.copyWithCount(offered), false);
          if (delivered < 0 || delivered > offered)
            throw new IllegalStateException("Inventory target exceeded its reserved quantity");
          if (delivered == 0) break;
          var current = buffer.get(index);
          if (current.getCount() != snapshot.getCount()
              || !ItemStack.isSameItemSameComponents(current, snapshot))
            throw new IllegalStateException("Source inventory changed during transfer");
          buffer.put(
              index,
              delivered == snapshot.getCount()
                  ? ItemStack.EMPTY
                  : snapshot.copyWithCount(snapshot.getCount() - delivered));
          changed = true;
        }
      }
      if (buffer.isEmpty()) break;
    }
    if (changed) super.setChanged();
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
    ProjectRuntime.network.get(server).updateTileEntityField(this, "priority");
  }

  @Override
  public void acceptClientField(String field) {
    if (field.equals("priority")) updatePriority(true);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new PriorityItemMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new PriorityItemMenu(syncId, inventory, this);
  }
}
