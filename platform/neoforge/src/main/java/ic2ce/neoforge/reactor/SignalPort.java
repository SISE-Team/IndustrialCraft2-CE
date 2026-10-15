package ic2ce.neoforge.reactor;

import ic2ce.neoforge.component.SignalInput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Redstone forwarding follows the current enclosing fluid core and unlinks on dismantling. */
public class SignalPort extends VesselEndpoint {
  public final SignalInput redstone = addComponent(new SignalInput(this));

  public SignalPort(BlockPos position, BlockState state) {
    super(ReactorBlockEntities.REACTOR_REDSTONE_PORT, position, state);
  }

  private void relink() {
    if (getLevel() == null || getLevel().isClientSide()) return;
    var core = getReactorInstance();
    var receiver = core == null ? null : core.redstone;
    if (redstone.getLinkReceiver() == receiver) return;
    redstone.unlinkOutbound();
    if (receiver != null) redstone.linkTo(receiver);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    relink();
  }

  @Override
  protected void onNeighborChange(Block changed) {
    super.onNeighborChange(changed);
    relink();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    relink();
  }
}
