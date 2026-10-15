package ic2ce.neoforge.energy.storage;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.tile.IEnergyStorage;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.engine.energy.StorageSignalPolicy;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.component.DirectSignalOutput;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.energy.menu.BankMenu;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.runtime.SideServices;
import ic2ce.neoforge.world.DirectionSets;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Electrical bank output orientation, conserved integer storage API and retained-energy block
 * drops.
 */
public abstract class EnergyBank extends InventoryBlockEntity
    implements IHasGui, INetworkClientTileEntityEventListener, IEnergyStorage {
  protected double output;
  public byte redstoneMode;
  public static byte redstoneModes = 7;
  public final ChargeInventory chargeSlot;
  public final DischargeInventory dischargeSlot;
  public final ElectricalBuffer energy;
  public final SignalInput redstone;
  public final DirectSignalOutput rsEmitter;

  public EnergyBank(
      BlockEntityType<? extends EnergyBank> type,
      BlockPos position,
      BlockState state,
      int tier,
      int packet,
      int capacity) {
    super(type, position, state);
    if (packet <= 0 || capacity < packet)
      throw new IllegalArgumentException(
          "Electrical storage must contain at least one positive output packet");
    output = packet;
    chargeSlot = new ChargeInventory(this, tier);
    dischargeSlot =
        new DischargeInventory(this, InventorySlot.Access.IO, tier, InventorySlot.InvSide.BOTTOM);
    var facing = getFacing();
    energy =
        addComponent(
            new ElectricalBuffer(
                    this,
                    capacity,
                    DirectionSets.except(facing),
                    DirectionSets.ofMask(1 << facing.ordinal()),
                    tier,
                    tier,
                    true)
                .addManagedSlot(chargeSlot)
                .addManagedSlot(dischargeSlot));
    rsEmitter = addComponent(new DirectSignalOutput(this));
    redstone = addComponent(new SignalInput(this));
    comparator.setUpdate(energy::getComparatorValue);
  }

  protected int modeCount() {
    return 7;
  }

  protected final int currentMode() {
    return redstoneMode >= 0 && redstoneMode < modeCount() ? redstoneMode : 0;
  }

  private void updateDirections() {
    var facing = getFacing();
    energy.setDirections(DirectionSets.except(facing), DirectionSets.ofMask(1 << facing.ordinal()));
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    int mode = data.getByteOr("redstoneMode", (byte) 0);
    redstoneMode = (byte) (mode >= 0 && mode < modeCount() ? mode : 0);
    updateDirections();
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putByte("redstoneMode", (byte) currentMode());
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    energy.setSendingEnabled(shouldEmitEnergy());
    rsEmitter.setLevel(shouldEmitRedstone() ? 15 : 0);
  }

  @Override
  protected void setFacing(Level world, Direction facing) {
    super.setFacing(world, facing);
    updateDirections();
  }

  protected final void superSetFacing(Level world, Direction facing) {
    super.setFacing(world, facing);
  }

  protected boolean shouldEmitRedstone() {
    return StorageSignalPolicy.signal(
        currentMode(), energy.getEnergy(), energy.getCapacity(), output);
  }

  protected boolean shouldEmitEnergy() {
    return StorageSignalPolicy.discharge(
        currentMode(),
        redstone.hasRedstoneInput(),
        energy.getEnergy(),
        energy.getCapacity(),
        output);
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    redstoneMode = (byte) ((currentMode() + 1) % modeCount());
    setChanged();
    if (player != null) SideServices.proxy().messagePlayer(player, getRedstoneModeKey());
  }

  public String getRedstoneMode() {
    return LocalizedText.translate(getRedstoneModeKey());
  }

  public String getRedstoneModeKey() {
    return "ic2.EUStorage.gui.mod.redstone" + currentMode();
  }

  @Override
  public void onPlaced(ItemStack blockItem, LivingEntity placer, Direction facing) {
    super.onPlaced(blockItem, placer, facing);
    if (getLevel() == null || getLevel().isClientSide()) return;
    var data = StackTag.get(blockItem);
    if (data == null || !data.contains("energy")) return;
    double stored = data.getDoubleOr("energy", 0);
    stored = Double.isFinite(stored) ? Math.clamp(stored, 0, energy.getCapacity()) : 0;
    energy.forceAddEnergy(stored - energy.getEnergy());
    setChanged();
  }

  public void onUpgraded() {
    rerender();
  }

  @Override
  public ItemStack adjustDrop(ItemStack original, boolean wrench) {
    var drop = super.adjustDrop(original, wrench);
    if (drop == null || drop.isEmpty()) return drop;
    var owned = drop.copy();
    var data = StackTag.getOrCreate(owned);
    data.remove("energy");
    if (wrench || teBlock.getDefaultDrop() == MachineBlockType.DefaultDrop.Self) {
      double ratio =
          NativeConfigValues.getDouble(
              ProjectConfiguration.get(), "balance/energyRetainedInStorageBlockDrops");
      ratio = Double.isFinite(ratio) ? Math.clamp(ratio, 0, 1) : 0;
      if (ratio > 0 && energy.getEnergy() > 0)
        data.putDouble("energy", Math.min(energy.getCapacity(), energy.getEnergy()) * ratio);
    }
    StackTag.set(owned, data);
    return owned;
  }

  @Override
  public int getOutput() {
    return (int) output;
  }

  @Override
  public double getOutputEnergyUnitsPerTick() {
    return output;
  }

  @Override
  public int getStored() {
    return (int) Math.clamp(energy.getEnergy(), 0, Integer.MAX_VALUE);
  }

  @Override
  public int getCapacity() {
    return (int) Math.clamp(energy.getCapacity(), 0, Integer.MAX_VALUE);
  }

  @Override
  public void setStored(int stored) {
    double target = Math.clamp(stored, 0, energy.getCapacity());
    energy.forceAddEnergy(target - energy.getEnergy());
    setChanged();
  }

  @Override
  public int addEnergy(int requested) {
    int accepted =
        requested >= 0
            ? (int) Math.min(requested, Math.floor(energy.getFreeEnergy()))
            : (int) -Math.min(-(long) requested, Math.floor(energy.getEnergy()));
    if (accepted != 0) {
      energy.addEnergy(accepted);
      setChanged();
    }
    return accepted;
  }

  @Override
  public boolean isTeleporterCompatible(Direction side) {
    return true;
  }

  @Override
  public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag flags) {
    super.addInformation(stack, tooltip, flags);
    tooltip.add(
        LocalizedText.translate("ic2.item.tooltip.Output")
            + " "
            + (long) output
            + " "
            + LocalizedText.translate("ic2.generic.text.EUt")
            + " "
            + LocalizedText.translate("ic2.item.tooltip.Capacity")
            + " "
            + getCapacity()
            + " "
            + LocalizedText.translate("ic2.generic.text.EU"));
    var data = StackTag.get(stack);
    double stored = data == null ? 0 : data.getDoubleOr("energy", 0);
    if (!Double.isFinite(stored) || stored < 0) stored = 0;
    tooltip.add(
        LocalizedText.translate("ic2.item.tooltip.Store")
            + " "
            + (long) stored
            + " "
            + LocalizedText.translate("ic2.generic.text.EU"));
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new BankMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new BankMenu(syncId, inventory, this);
  }
}
