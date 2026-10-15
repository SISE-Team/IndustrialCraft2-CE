package ic2ce.neoforge.kinetic;

import ic2ce.api.energy.tile.IKineticSource;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Player-driven bounded kinetic buffer with a server tick click budget and food cost. */
@NotClassic
public class ManualCrank extends NativeBlockEntity implements IKineticSource {
  public int clicks, currentKU;
  public static final int maxClicksPerTick = 10;
  public final int maxKU = 1000;
  private static final double MULTIPLIER =
      Math.round(
          NativeConfigValues.getDouble(
              ProjectConfiguration.get(), "balance/energy/kineticgenerator/manual"));

  public ManualCrank(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.MANUAL_KINETIC_GENERATOR, position, block);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    clicks = 0;
  }

  @Override
  protected InteractionResult onActivated(
      Player player, InteractionHand hand, Direction side, Vec3 hit) {
    if (player instanceof ServerPlayer actor
        && actor.getFoodData().getFoodLevel() > 6
        && clicks < maxClicksPerTick
        && Double.isFinite(MULTIPLIER)
        && MULTIPLIER > 0) {
      int base = EnvProxy.get().isFakePlayer(actor) ? 20 : 400;
      long created = Math.min(maxKU, Math.round(base * MULTIPLIER));
      currentKU = (int) Math.clamp((long) currentKU + created, 0, maxKU);
      actor.causeFoodExhaustion(0.25F);
      clicks++;
      setChanged();
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public int maxrequestkineticenergyTick(Direction side) {
    return drawKineticEnergy(side, Integer.MAX_VALUE, true);
  }

  @Override
  public int getConnectionBandwidth(Direction side) {
    return maxKU;
  }

  @Override
  public int requestkineticenergy(Direction side, int request) {
    return drawKineticEnergy(side, request, false);
  }

  @Override
  public int drawKineticEnergy(Direction side, int request, boolean simulate) {
    int amount = Math.min(Math.max(0, request), currentKU);
    if (!simulate && amount > 0) {
      currentKU -= amount;
      setChanged();
    }
    return amount;
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    currentKU = Math.clamp(data.getIntOr("currentKU", 0), 0, maxKU);
    clicks = 0;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("currentKU", currentKU);
  }
}
