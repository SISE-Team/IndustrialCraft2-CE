package ic2ce.neoforge.utility;

import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.utility.menu.PoleDriveMenu;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Pole boosts consume two shared EU; augmentation extends reach and redstone inhibits motion. */
public class PoleDrive extends PoweredBlockEntity implements IHasGui, IUpgradableBlock {
  private static final Set<UpgradableProperty> UPGRADES =
      Set.of(
          UpgradableProperty.Augmentable,
          UpgradableProperty.RedstoneSensitive,
          UpgradableProperty.Transformer,
          UpgradableProperty.EnergyStorage);
  public static final int defaultMaxEnergy = 100, defaultTier = 1;
  public UpgradeInventory upgradeSlot;
  protected final SignalInput redstone;

  public PoleDrive(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.MAGNETIZER, position, state, 100, 1);
    redstone = addComponent(new SignalInput(this));
    upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (!getLevel().isClientSide()) setOverclockRates();
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (getLevel() != null && !getLevel().isClientSide()) setOverclockRates();
  }

  public void setOverclockRates() {
    upgradeSlot.onChanged();
    int tier = upgradeSlot.getTier(1);
    energy.setSinkTier(tier);
    dischargeSlot.setTier(tier);
    energy.setCapacity(upgradeSlot.getEnergyStorage(100, 0, 0));
  }

  public int getPoleRange() {
    return (int) Math.clamp(20L + upgradeSlot.augmentation, 20, 276);
  }

  public boolean canBoost() {
    return !redstone.hasRedstoneInput() && energy.canUseEnergy(2);
  }

  public boolean tryBoost(double multiplier) {
    if (getLevel() == null
        || getLevel().isClientSide()
        || !Double.isFinite(multiplier)
        || multiplier <= 0
        || !Double.isFinite(multiplier * 2)
        || redstone.hasRedstoneInput()) return false;
    boolean paid = energy.useEnergy(multiplier * 2);
    if (paid && getLevel() != null && !getLevel().isClientSide()) super.setChanged();
    return paid;
  }

  public void boost(double multiplier) {
    tryBoost(multiplier);
  }

  @Override
  public double getEnergy() {
    return energy.getEnergy();
  }

  @Override
  public boolean useEnergy(double amount) {
    return energy.useEnergy(amount);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return UPGRADES;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new PoleDriveMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new PoleDriveMenu(syncId, inventory, this);
  }
}
