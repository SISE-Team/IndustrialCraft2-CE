package ic2ce.neoforge.agriculture;

import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.core.util.StackUtil;
import ic2ce.neoforge.agriculture.menu.HarvesterMenu;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Half-second raster harvests approve the complete stochastic drop bill before changing crop age.
 */
public class AutoHarvester extends PoweredBlockEntity implements IHasGui, IUpgradableBlock {
  public final InventorySlot contentSlot =
      new InventorySlot(this, "content", InventorySlot.Access.IO, 15);
  public final UpgradeInventory upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
  public int scanX = -4, scanY = -1, scanZ = -4;

  public AutoHarvester(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.CROP_HARVESTER, position, state, 10000, 1, false);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    scanX = Math.clamp(data.getIntOr("scanX", -4), -4, 4);
    scanY = Math.clamp(data.getIntOr("scanY", -1), -1, 1);
    scanZ = Math.clamp(data.getIntOr("scanZ", -4), -4, 4);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("scanX", scanX);
    data.putInt("scanY", scanY);
    data.putInt("scanZ", scanZ);
  }

  private void upgrades() {
    upgradeSlot.onChanged();
    energy.setSinkTier(upgradeSlot.getTier(1));
    energy.setCapacity(upgradeSlot.getEnergyStorage(10000, 0, 0));
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    upgrades();
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (getLevel() != null && !getLevel().isClientSide()) upgrades();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    upgradeSlot.tick();
    var world = getLevel();
    if (world != null && world.getGameTime() % 10 == 0 && energy.getEnergy() >= 21) scan();
  }

  private void advance() {
    int cell =
        Math.floorMod(
            (Math.clamp(scanY, -1, 1) + 1) * 81
                + (Math.clamp(scanZ, -4, 4) + 4) * 9
                + Math.clamp(scanX, -4, 4)
                + 5,
            243);
    scanX = cell % 9 - 4;
    scanZ = cell / 9 % 9 - 4;
    scanY = cell / 81 - 1;
  }

  public void scan() {
    var world = getLevel();
    if (world == null || world.isClientSide() || energy.getEnergy() < 1) return;
    advance();
    var position = worldPosition.offset(scanX, scanY, scanZ);
    if (!world.isLoaded(position) || world.isOutsideBuildHeight(position) || !energy.useEnergy(1))
      return;
    if (!(world.getBlockEntity(position) instanceof CropPlot crop)
        || crop.getCrop() == null
        || full()) return;
    var species = crop.getCrop();
    if (crop.getCurrentAge() != species.getOptimalHarvestAge(crop)
        && crop.getCurrentAge() != species.getMaxAge()) return;
    var drops = crop.performHarvest(count -> energy.useEnergy(count * 20.0));
    if (drops == null) return;
    for (var drop : drops) {
      var remaining = drop.copy();
      for (int pass = 0; pass < 2 && !remaining.isEmpty(); pass++)
        for (int slot = 0; slot < contentSlot.size() && !remaining.isEmpty(); slot++) {
          var current = contentSlot.get(slot);
          if (current.isEmpty() != (pass == 1)
              || !current.isEmpty() && !ItemStack.isSameItemSameComponents(current, remaining))
            continue;
          int inserted =
              Math.min(
                  remaining.getCount(),
                  Math.max(
                      0,
                      Math.min(contentSlot.getStackSizeLimit(), remaining.getMaxStackSize())
                          - current.getCount()));
          if (inserted > 0) {
            contentSlot.putInTransaction(
                slot, remaining.copyWithCount(current.getCount() + inserted));
            remaining = remaining.copyWithCount(remaining.getCount() - inserted);
          }
        }
      if (!remaining.isEmpty()) StackUtil.dropAsEntity(world, worldPosition, remaining);
    }
    super.setChanged();
  }

  private boolean full() {
    for (var stack : contentSlot)
      if (stack.isEmpty()
          || stack.getCount() < Math.min(stack.getMaxStackSize(), contentSlot.getStackSizeLimit()))
        return false;
    return true;
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
    return Set.of(
        UpgradableProperty.Transformer,
        UpgradableProperty.EnergyStorage,
        UpgradableProperty.ItemProducing);
  }

  @Override
  public OwnedMenu<AutoHarvester> createServerScreenHandler(int id, Player player) {
    return new HarvesterMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new HarvesterMenu(id, inventory, this);
  }
}
