package ic2ce.neoforge.component;

import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.io.*;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

/**
 * Native component lifetime is owned by one block entity; updates remain bound to its actual server
 * world.
 */
public abstract class NativeComponent {
  protected final NativeBlockEntity parent;

  public NativeComponent(NativeBlockEntity owner) {
    parent = Objects.requireNonNull(owner);
  }

  public NativeBlockEntity getParent() {
    return parent;
  }

  public void readFromNbt(CompoundTag data) {}

  public CompoundTag writeToNbt() {
    return null;
  }

  public void onLoaded() {}

  public void onUnloaded() {}

  public void onNeighborChange(Block block) {}

  public void onContainerUpdate(ServerPlayer actor) {}

  public void onNetworkUpdate(DataInput input) throws IOException {}

  public boolean enableWorldTick() {
    return false;
  }

  public void onWorldTick() {}

  protected void setNetworkUpdate(ServerPlayer actor, GrowingBuffer data) {
    Objects.requireNonNull(actor);
    Objects.requireNonNull(data);
    var world = parent.getLevel();
    if (world == null || world.isClientSide() || actor.level() != world || parent.isRemoved())
      return;
    var id = ComponentTypes.getId(getClass());
    if (id == null)
      throw new IllegalStateException("Unregistered native component: " + getClass().getName());
    ProjectRuntime.network.get(true).sendComponentUpdate(parent, id, actor, data);
  }
}
