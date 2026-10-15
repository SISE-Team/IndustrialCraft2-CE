package ic2ce.neoforge.energy.grid;

import ic2ce.api.energy.IEnergyNet;
import ic2ce.api.energy.IEnergyNetEventReceiver;
import ic2ce.api.energy.NodeStats;
import ic2ce.api.energy.tile.IEnergyTile;
import ic2ce.api.info.ILocatable;
import ic2ce.neoforge.world.WorldData;
import java.io.PrintStream;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Public API facade; each server world owns its registration and transfer state. */
public final class EnergyNetGlobal implements IEnergyNet {
  private static final CopyOnWriteArrayList<IEnergyNetEventReceiver> receivers =
      new CopyOnWriteArrayList<>();
  private static final IEnergyCalculator calculator = new EnergyCalculatorLeg();

  private EnergyNetGlobal() {}

  public static EnergyNetGlobal create() {
    return new EnergyNetGlobal();
  }

  static Iterable<IEnergyNetEventReceiver> getEventReceivers() {
    return receivers;
  }

  static IEnergyCalculator getCalculator() {
    return calculator;
  }

  public static EnergyNetLocal getLocal(Level world) {
    Objects.requireNonNull(world, "world");
    if (world.isClientSide()) throw new IllegalStateException("Energy network is server-side");
    return WorldData.get(world).energyNet;
  }

  @Override
  public IEnergyTile getTile(Level world, BlockPos position) {
    return getLocal(world).getIoTile(Objects.requireNonNull(position));
  }

  @Override
  public IEnergyTile getSubTile(Level world, BlockPos position) {
    return getLocal(world).getSubTile(Objects.requireNonNull(position));
  }

  @Override
  public <T extends BlockEntity & IEnergyTile> void addBlockEntityTile(T tile) {
    Objects.requireNonNull(tile);
    getLocal(tile.getLevel()).addTile(tile, tile.getBlockPos());
  }

  @Override
  public <T extends ILocatable & IEnergyTile> void addLocatableTile(T tile) {
    Objects.requireNonNull(tile);
    getLocal(tile.getWorldObj()).addTile(tile, tile.getPosition());
  }

  @Override
  public void removeTile(IEnergyTile tile) {
    getLocal(getWorld(tile)).removeTile(tile, getPos(tile));
  }

  @Override
  public Level getWorld(IEnergyTile tile) {
    Objects.requireNonNull(tile);
    return switch (tile) {
      case ILocatable location -> location.getWorldObj();
      case BlockEntity entity -> entity.getLevel();
      default ->
          throw new IllegalArgumentException(
              "Energy tile has no world: " + tile.getClass().getName());
    };
  }

  @Override
  public BlockPos getPos(IEnergyTile tile) {
    Objects.requireNonNull(tile);
    return switch (tile) {
      case ILocatable location -> location.getPosition();
      case BlockEntity entity -> entity.getBlockPos();
      default ->
          throw new IllegalArgumentException(
              "Energy tile has no position: " + tile.getClass().getName());
    };
  }

  @Override
  public NodeStats getNodeStats(IEnergyTile tile) {
    return getLocal(getWorld(tile)).getNodeStats(tile);
  }

  @Override
  public int getAdjacentConnections(IEnergyTile tile) {
    return getLocal(getWorld(tile)).getAdjacentConnections(tile);
  }

  public boolean dumpDebugInfo(
      Level world, BlockPos position, PrintStream console, PrintStream chat) {
    return getLocal(world).dumpDebugInfo(position, console, chat);
  }

  @Override
  public double getPowerFromTier(int tier) {
    if (tier < 0) throw new IllegalArgumentException("Negative energy tier");
    return tier < 30 ? Math.scalb(8.0, tier * 2) : 9.223372E18F;
  }

  @Override
  public int getTierFromPower(double power) {
    return power <= 0 ? 0 : (int) Math.ceil(Math.log(power / 8.0) / Math.log(4.0));
  }

  @Override
  public void registerEventReceiver(IEnergyNetEventReceiver receiver) {
    receivers.addIfAbsent(Objects.requireNonNull(receiver));
  }

  @Override
  public void unregisterEventReceiver(IEnergyNetEventReceiver receiver) {
    receivers.remove(receiver);
  }
}
