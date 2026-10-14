package ic2ce.api.event;

import java.util.Objects;
import net.minecraft.core.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * Server retexture proposal owns its block coordinate and tint array before observers can apply it.
 */
public class RetextureEvent extends LevelEvent implements net.neoforged.bus.api.ICancellableEvent {
  public final BlockPos pos;
  public final BlockState state, refState;
  public final Direction side, refSide;
  public final Player player;
  public final String refVariant;
  public final int[] refColorMultipliers;
  public boolean applied;

  public RetextureEvent(
      Level world,
      BlockPos pos,
      BlockState state,
      Direction side,
      Player player,
      BlockState reference,
      String variant,
      Direction referenceSide,
      int[] tints) {
    super(Objects.requireNonNull(world));
    if (world.isClientSide())
      throw new IllegalStateException("Retexture proposals require a server world");
    this.pos = Objects.requireNonNull(pos).immutable();
    this.state = Objects.requireNonNull(state);
    this.side = Objects.requireNonNull(side);
    this.player = player;
    refState = Objects.requireNonNull(reference);
    refVariant = Objects.requireNonNull(variant);
    refSide = Objects.requireNonNull(referenceSide);
    Objects.requireNonNull(tints);
    if (tints.length == 0 || tints.length > 64 || variant.length() > 4096)
      throw new IllegalArgumentException("Invalid retexture material bounds");
    refColorMultipliers = tints.clone();
  }
}
