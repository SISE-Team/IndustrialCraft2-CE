package ic2ce.neoforge.power;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.IEnergyAcceptor;
import ic2ce.api.energy.tile.IEnergySource;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.engine.energy.UnitConversionLedger;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Demand-driven converters account for indivisible input units across outgoing packets. */
public abstract class DemandDrivenConverter extends InventoryBlockEntity
    implements IHasGui, IEnergySource {
  @GuiSynced private double lastProduction, maxProduction;
  private double production;
  private boolean attached;
  private final UnitConversionLedger prepaid = new UnitConversionLedger();

  protected DemandDrivenConverter(
      BlockEntityType<? extends DemandDrivenConverter> type, BlockPos position, BlockState block) {
    super(type, position, block);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    lastProduction = production;
    production = 0;
    setActive(maxProduction > 0);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (!getLevel().isClientSide() && !attached) {
      EnergyNet.instance.addBlockEntityTile(this);
      attached = true;
    }
  }

  @Override
  protected void onUnloaded() {
    if (attached) {
      EnergyNet.instance.removeTile(this);
      attached = false;
    }
    super.onUnloaded();
  }

  private static String format(double value) {
    double rounded = Math.rint(value * 10) / 10;
    return rounded == Math.rint(rounded) ? Long.toString((long) rounded) : Double.toString(rounded);
  }

  public String getProduction() {
    return format(lastProduction);
  }

  public String getMaxProduction() {
    return format(maxProduction);
  }

  protected abstract int getEnergyAvailable();

  protected abstract void drawEnergyAvailable(int amount);

  protected abstract double getMultiplier();

  @Override
  public double getOfferedEnergy() {
    double factor = getMultiplier();
    maxProduction =
        !Double.isFinite(factor) || factor <= 0
            ? 0
            : Math.max(0, getEnergyAvailable()) * factor + prepaid.credit();
    return maxProduction;
  }

  @Override
  public void drawEnergy(double amount) {
    double factor = getMultiplier();
    int required = prepaid.requiredUnits(amount, factor);
    if (required < 0 || required > Math.max(0, getEnergyAvailable())) return;
    if (required > 0) drawEnergyAvailable(required);
    prepaid.commit(amount, factor, required);
    production += amount;
  }

  @Override
  public int getSourceTier() {
    return Math.max(2, EnergyNet.instance.getTierFromPower(maxProduction));
  }

  @Override
  public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side) {
    return side != getFacing();
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    prepaid.restore(data.getDoubleOr("conversionCredit", 0));
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putDouble("conversionCredit", prepaid.credit());
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return DocumentMenu.create(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(id, inventory, this);
  }
}
