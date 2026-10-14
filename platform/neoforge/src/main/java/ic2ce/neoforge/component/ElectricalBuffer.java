package ic2ce.neoforge.component;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.*;
import ic2ce.api.info.ILocatable;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.engine.energy.EnergyBuffer;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.world.DirectionSets;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** Native machine energy component over an independent conserved primitive ledger. */
public class ElectricalBuffer extends NativeComponent {
  private final EnergyBuffer buffer;
  private final List<InventorySlot> managed = new ArrayList<>(2);
  private final boolean fullEnergy;
  private Set<Direction> sinks, sources;
  private int sinkMask, sourceMask, sinkTier, sourceTier, packets = 1;
  private boolean loaded, receiving = true, sending = true, multiple;
  private Endpoint delegate;

  public static ElectricalBuffer asBasicSink(NativeBlockEntity parent, double capacity) {
    return asBasicSink(parent, capacity, 1);
  }

  public static ElectricalBuffer asBasicSink(NativeBlockEntity parent, double capacity, int tier) {
    return new ElectricalBuffer(parent, capacity, DirectionSets.all(), Set.of(), tier);
  }

  public static ElectricalBuffer asBasicSource(NativeBlockEntity parent, double capacity) {
    return asBasicSource(parent, capacity, 1);
  }

  public static ElectricalBuffer asBasicSource(
      NativeBlockEntity parent, double capacity, int tier) {
    return new ElectricalBuffer(parent, capacity, Set.of(), DirectionSets.all(), tier);
  }

  public ElectricalBuffer(NativeBlockEntity parent, double capacity) {
    this(parent, capacity, Set.of(), Set.of(), 1);
  }

  public ElectricalBuffer(
      NativeBlockEntity parent,
      double capacity,
      Set<Direction> input,
      Set<Direction> output,
      int tier) {
    this(parent, capacity, input, output, tier, tier, false);
  }

  public ElectricalBuffer(
      NativeBlockEntity parent,
      double capacity,
      Set<Direction> input,
      Set<Direction> output,
      int inputTier,
      int outputTier,
      boolean requireFullPackets) {
    super(parent);
    buffer = new EnergyBuffer(capacity);
    fullEnergy = requireFullPackets;
    setSinkTier(inputTier);
    setSourceTier(outputTier);
    directions(input, output);
  }

  public ElectricalBuffer addManagedSlot(InventorySlot slot) {
    if (!(slot instanceof IChargingSlot) && !(slot instanceof IDischargingSlot))
      throw new IllegalArgumentException("Slot has no charging/discharging contract");
    if (!managed.contains(slot)) managed.add(slot);
    return this;
  }

  public ElectricalBuffer setMultiSource(boolean enabled) {
    multiple = enabled;
    if (!enabled) packets = 1;
    return this;
  }

  public boolean isMultiSource() {
    return multiple;
  }

  public void setPacketOutput(int count) {
    if (count < 0) throw new IllegalArgumentException("Negative packet count");
    if (multiple) packets = count;
  }

  public int getPacketOutput() {
    return packets;
  }

  public double getCapacity() {
    return buffer.capacity();
  }

  public void setCapacity(double value) {
    buffer.setCapacity(value);
  }

  public double getEnergy() {
    return buffer.stored();
  }

  public double getFreeEnergy() {
    return buffer.free();
  }

  public double getFillRatio() {
    return buffer.capacity() == 0 ? 0 : buffer.stored() / buffer.capacity();
  }

  public int getComparatorValue() {
    return Math.clamp((int) (getFillRatio() * 15), 0, 15);
  }

  public double addEnergy(double delta) {
    return buffer.add(delta);
  }

  public void forceAddEnergy(double delta) {
    buffer.forceDelta(delta);
  }

  public boolean canUseEnergy(double amount) {
    return buffer.canSpend(amount);
  }

  public boolean useEnergy(double amount) {
    return buffer.spendExact(amount);
  }

  public double useEnergy(double amount, boolean simulate) {
    return buffer.withdraw(amount, simulate);
  }

  public int getSinkTier() {
    return sinkTier;
  }

  public int getSourceTier() {
    return sourceTier;
  }

  public void setSinkTier(int tier) {
    if (tier < 0) throw new IllegalArgumentException("Negative sink tier");
    sinkTier = tier;
  }

  public void setSourceTier(int tier) {
    if (tier < 0) throw new IllegalArgumentException("Negative source tier");
    sourceTier = tier;
  }

  public void setEnabled(boolean enabled) {
    receiving = sending = enabled;
  }

  public void setReceivingEnabled(boolean enabled) {
    receiving = enabled;
  }

  public void setSendingEnabled(boolean enabled) {
    sending = enabled;
  }

  public Set<Direction> getSourceDirs() {
    return sources;
  }

  public Set<Direction> getSinkDirs() {
    return sinks;
  }

  public IEnergyTile getDelegate() {
    return delegate;
  }

  @Override
  public void readFromNbt(CompoundTag data) {
    buffer.restoreStored(data.getDoubleOr("storage", 0));
  }

  @Override
  public CompoundTag writeToNbt() {
    var data = new CompoundTag();
    data.putDouble("storage", buffer.stored());
    return data;
  }

  @Override
  public void onContainerUpdate(ServerPlayer player) {
    var data = new GrowingBuffer(16);
    data.writeDouble(buffer.capacity());
    data.writeDouble(buffer.stored());
    data.flip();
    setNetworkUpdate(player, data);
  }

  @Override
  public void onNetworkUpdate(DataInput input) throws IOException {
    buffer.setCapacity(input.readDouble());
    buffer.restoreStored(input.readDouble());
  }

  @Override
  public void onLoaded() {
    if (parent.getLevel().isClientSide()) return;
    if (loaded) return;
    loaded = true;
    attach();
  }

  @Override
  public void onUnloaded() {
    detach();
    loaded = false;
  }

  private void attach() {
    if (!loaded || sinkMask == 0 && sourceMask == 0) return;
    delegate =
        sourceMask == 0
            ? new EnergyNetDelegateSink()
            : sinkMask == 0 ? new EnergyNetDelegateSource() : new EnergyNetDelegateDual();
    EnergyNet.instance.addLocatableTile(delegate);
  }

  private void detach() {
    Endpoint previous = delegate;
    delegate = null;
    if (previous != null) EnergyNet.instance.removeTile(previous);
  }

  public void setDirections(Set<Direction> input, Set<Direction> output) {
    int incoming = DirectionSets.mask(input), outgoing = DirectionSets.mask(output);
    if (sinkMask == incoming && sourceMask == outgoing) return;
    detach();
    directions(input, output);
    attach();
  }

  private void directions(Set<Direction> input, Set<Direction> output) {
    sinkMask = DirectionSets.mask(input);
    sourceMask = DirectionSets.mask(output);
    sinks = DirectionSets.ofMask(sinkMask);
    sources = DirectionSets.ofMask(sourceMask);
  }

  private static boolean side(int mask, Direction direction) {
    return direction != null && (mask & 1 << direction.ordinal()) != 0;
  }

  @Override
  public boolean enableWorldTick() {
    return !parent.getLevel().isClientSide() && !managed.isEmpty();
  }

  @Override
  public void onWorldTick() {
    for (int i = 0; i < managed.size(); i++) {
      InventorySlot slot = managed.get(i);
      if (slot instanceof IChargingSlot charger) {
        if (buffer.stored() > 0) {
          double consumed = charger.charge(buffer.stored());
          if (consumed > 0) buffer.withdraw(consumed, false);
        }
      } else if (slot instanceof IDischargingSlot discharger && buffer.free() > 0) {
        double received = discharger.discharge(buffer.free(), false);
        // Single-use fuels deliver an indivisible amount; retain their
        // overflow until the machine spends it instead of deleting it.
        if (received > 0) buffer.forceDelta(received);
      }
    }
  }

  private double offered() {
    if (!sending || sourceMask == 0) return 0;
    return fullEnergy && buffer.stored() < EnergyNet.instance.getPowerFromTier(sourceTier)
        ? 0
        : buffer.stored();
  }

  private int packetCount() {
    return fullEnergy
        ? Math.min(
            packets,
            (int) Math.floor(buffer.stored() / EnergyNet.instance.getPowerFromTier(sourceTier)))
        : packets;
  }

  private abstract class Endpoint implements ILocatable, IEnergyTile {
    @Override
    public Level getWorldObj() {
      return parent.getLevel();
    }

    @Override
    public BlockPos getPosition() {
      return parent.getBlockPos();
    }

    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction direction) {
      return side(sinkMask, direction);
    }

    public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction direction) {
      return side(sourceMask, direction);
    }

    public double getDemandedEnergy() {
      return receiving && sinkMask != 0 ? buffer.free() : 0;
    }

    public double getOfferedEnergy() {
      return offered();
    }

    public int getSinkTier() {
      return sinkTier;
    }

    public int getSourceTier() {
      return sourceTier;
    }

    public double injectEnergy(Direction direction, double amount, double voltage) {
      if (!receiving || sinkMask == 0 || direction != null && !side(sinkMask, direction))
        return amount;
      return amount - buffer.add(amount);
    }

    public void drawEnergy(double amount) {
      buffer.withdraw(amount, false);
    }

    public boolean sendMultipleEnergyPackets() {
      return multiple;
    }

    public int getMultipleEnergyPacketAmount() {
      return packetCount();
    }
  }

  private final class EnergyNetDelegateSink extends Endpoint implements IEnergySink {}

  private final class EnergyNetDelegateSource extends Endpoint implements IMultiEnergySource {}

  private final class EnergyNetDelegateDual extends Endpoint
      implements IEnergySink, IMultiEnergySource {}
}
