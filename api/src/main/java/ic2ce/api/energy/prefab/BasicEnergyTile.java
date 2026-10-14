package ic2ce.api.energy.prefab;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.IEnergyNet;
import ic2ce.api.energy.tile.IEnergyTile;
import ic2ce.api.info.ILocatable;
import ic2ce.api.item.ElectricItem;
import ic2ce.engine.energy.ChargeMath;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Finite addon buffer with native location reconciliation and debit-before-credit item transfers.
 */
public abstract class BasicEnergyTile implements ILocatable, IEnergyTile {
  private final Object locationProvider;
  protected Level world;
  protected BlockPos pos;
  protected double capacity, energyStored;
  protected boolean addedToEnet;
  private IEnergyNet registeredNetwork;
  private Level registeredWorld;
  private BlockPos registeredPosition;
  private boolean detaching, transferring;

  protected BasicEnergyTile(BlockEntity owner, double capacity) {
    locationProvider = Objects.requireNonNull(owner);
    setCapacity(capacity);
  }

  protected BasicEnergyTile(ILocatable owner, double capacity) {
    locationProvider = Objects.requireNonNull(owner);
    setCapacity(capacity);
  }

  protected BasicEnergyTile(Level world, BlockPos position, double capacity) {
    locationProvider = null;
    this.world = Objects.requireNonNull(world);
    pos = Objects.requireNonNull(position).immutable();
    setCapacity(capacity);
  }

  public void update() {
    var currentWorld = getWorldObj();
    var currentPosition = getPosition();
    if (addedToEnet
        && (currentWorld != registeredWorld
            || !Objects.equals(currentPosition, registeredPosition)
            || registeredNetwork != EnergyNet.instance)) detach();
    if (!addedToEnet) onLoad();
  }

  public void onLoad() {
    var currentWorld = getWorldObj();
    var currentPosition = getPosition();
    var network = EnergyNet.instance;
    if (addedToEnet
        || currentWorld == null
        || currentPosition == null
        || currentWorld.isClientSide()
        || network == null) return;
    registeredWorld = currentWorld;
    registeredPosition = currentPosition.immutable();
    registeredNetwork = network;
    try {
      network.addLocatableTile(this);
      addedToEnet = true;
    } catch (RuntimeException | Error error) {
      registeredNetwork = null;
      registeredWorld = null;
      registeredPosition = null;
      throw error;
    }
  }

  private void detach() {
    if (!addedToEnet) return;
    detaching = true;
    try {
      registeredNetwork.removeTile(this);
    } finally {
      detaching = false;
      addedToEnet = false;
      registeredNetwork = null;
      registeredWorld = null;
      registeredPosition = null;
    }
  }

  public void invalidate() {
    detach();
  }

  public void onChunkUnload() {
    detach();
  }

  public void readFromNBT(CompoundTag data) {
    setEnergyStored(
        Objects.requireNonNull(data).getCompoundOrEmpty(getNbtTagName()).getDoubleOr("energy", 0));
  }

  public CompoundTag writeToNBT(CompoundTag data) {
    var buffer = new CompoundTag();
    buffer.putDouble("energy", getEnergyStored());
    Objects.requireNonNull(data).put(getNbtTagName(), buffer);
    return data;
  }

  public double getCapacity() {
    return capacity;
  }

  public void setCapacity(double capacity) {
    if (!Double.isFinite(capacity) || capacity < 0)
      throw new IllegalArgumentException("Finite nonnegative energy capacity required");
    this.capacity = capacity;
    energyStored = ChargeMath.stored(energyStored, capacity);
  }

  public double getEnergyStored() {
    return ChargeMath.stored(energyStored, capacity);
  }

  public void setEnergyStored(double energy) {
    energyStored = ChargeMath.stored(energy, capacity);
  }

  public double getFreeCapacity() {
    return capacity - getEnergyStored();
  }

  protected boolean writable() {
    var currentWorld = getWorldObj();
    return currentWorld != null && !currentWorld.isClientSide();
  }

  public double addEnergy(double amount) {
    if (!writable() || !Double.isFinite(amount) || amount <= 0) return 0;
    double accepted = Math.min(amount, getFreeCapacity());
    setEnergyStored(getEnergyStored() + accepted);
    return accepted;
  }

  public boolean canUseEnergy(double amount) {
    return Double.isFinite(amount) && amount >= 0 && getEnergyStored() >= amount;
  }

  public boolean useEnergy(double amount) {
    if (!writable() || !canUseEnergy(amount)) return false;
    setEnergyStored(getEnergyStored() - amount);
    return true;
  }

  public boolean charge(ItemStack stack) {
    if (stack == null
        || stack.isEmpty()
        || !writable()
        || transferring
        || ElectricItem.manager == null) return false;
    double reserve = getEnergyStored();
    if (reserve <= 0) return false;
    transferring = true;
    setEnergyStored(0);
    try {
      double credited =
          ElectricItem.manager.charge(
              stack, reserve, Math.max(getSinkTier(), getSourceTier()), false, false);
      credited = Double.isFinite(credited) ? Math.clamp(credited, 0, reserve) : 0;
      setEnergyStored(getEnergyStored() + reserve - credited);
      return credited > 0;
    } catch (RuntimeException | Error error) {
      addEnergy(reserve);
      throw error;
    } finally {
      transferring = false;
    }
  }

  public boolean discharge(ItemStack stack, double limit) {
    if (stack == null
        || stack.isEmpty()
        || !writable()
        || transferring
        || ElectricItem.manager == null
        || Double.isNaN(limit)
        || limit < 0) return false;
    double requested = limit == 0 ? getFreeCapacity() : Math.min(limit, getFreeCapacity());
    if (requested <= 0) return false;
    transferring = true;
    try {
      double paid =
          ElectricItem.manager.discharge(
              stack, requested, Math.max(getSinkTier(), getSourceTier()), limit > 0, true, false);
      paid = Double.isFinite(paid) ? Math.clamp(paid, 0, requested) : 0;
      return addEnergy(paid) > 0;
    } finally {
      transferring = false;
    }
  }

  @Override
  public Level getWorldObj() {
    if (detaching) return registeredWorld;
    return switch (locationProvider) {
      case BlockEntity owner -> owner.getLevel();
      case ILocatable owner -> owner.getWorldObj();
      case null -> world;
      default -> throw new IllegalStateException("Unsupported energy location");
    };
  }

  @Override
  public BlockPos getPosition() {
    if (detaching) return registeredPosition;
    return switch (locationProvider) {
      case BlockEntity owner -> owner.getBlockPos();
      case ILocatable owner -> owner.getPosition();
      case null -> pos;
      default -> throw new IllegalStateException("Unsupported energy location");
    };
  }

  protected static int tier(int tier) {
    if (tier < 0) throw new IllegalArgumentException("Negative energy tier");
    return tier;
  }

  protected void reservePacketCapacity(int tier) {
    double packet =
        EnergyNet.instance == null
            ? (tier < 30 ? Math.scalb(8, tier * 2) : 9.223372E18F)
            : EnergyNet.instance.getPowerFromTier(tier);
    if (Double.isFinite(packet) && packet > capacity) setCapacity(packet);
  }

  protected abstract String getNbtTagName();

  protected int getSinkTier() {
    return 0;
  }

  protected int getSourceTier() {
    return 0;
  }
}
