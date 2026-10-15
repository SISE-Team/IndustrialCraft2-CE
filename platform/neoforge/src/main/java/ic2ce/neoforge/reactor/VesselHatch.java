package ic2ce.neoforge.reactor;

import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Shell hatch delegates its live inventory to the sole enclosed fluid core. */
@NotClassic
public class VesselHatch extends VesselEndpoint implements ReactorInventoryView {
  public VesselHatch(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.REACTOR_ACCESS_HATCH, position, state);
  }

  @Override
  public void setChanged() {
    super.setChanged();
    var core = getReactorInstance();
    if (core != null) core.setChanged();
  }

  @Override
  protected InteractionResult onActivated(
      Player player, InteractionHand hand, Direction face, Vec3 hit) {
    var core = getReactorInstance();
    return core == null ? InteractionResult.PASS : core.onActivated(player, hand, face, hit);
  }
}
