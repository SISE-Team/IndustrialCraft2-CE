package ic2ce.neoforge.reactor;

import ic2ce.api.reactor.IReactorChamber;
import ic2ce.neoforge.block.NativeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Native vessel endpoint; lookup restricts ownership to the enclosing shell. */
public abstract class VesselEndpoint extends NativeBlockEntity implements IReactorChamber {
  protected final FluidCoreLookup lookup = addComponent(new FluidCoreLookup(this));

  protected VesselEndpoint(
      BlockEntityType<? extends VesselEndpoint> type, BlockPos position, BlockState state) {
    super(type, position, state);
  }

  public final NuclearCore getReactorInstance() {
    return lookup.getReactor();
  }

  @Override
  public final boolean isWall() {
    return true;
  }
}
