package ic2ce.neoforge.agriculture;

import ic2ce.core.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.agriculture.menu.CellCareMenu;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.inventory.ConsumableInventory;
import ic2ce.neoforge.inventory.StackWhitelistInput;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Cell-fed eleven-square care scans every native tick and pays each distinct successful dose. */
public class CellCropCaretaker extends PoweredBlockEntity implements IHasGui {
  public int scanX = -5, scanY = -1, scanZ = -5;
  public final ConsumableInventory fertilizerSlot =
      new StackWhitelistInput(this, "fertilizer", 3, new ItemStack(ItemDefinitions.FERTILIZER));
  public final ConsumableInventory hydrationSlot =
      new StackWhitelistInput(this, "hydration", 3, new ItemStack(ItemDefinitions.HYDRATION_CELL));
  public final ConsumableInventory weedExSlot =
      new StackWhitelistInput(this, "weedEx", 3, new ItemStack(ItemDefinitions.WEED_EX_CELL));

  public CellCropCaretaker(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.CLASSIC_CROPMATRON, position, state, 1000, 1);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    scanX = Math.clamp(data.getIntOr("scanX", -5), -5, 5);
    scanY = Math.clamp(data.getIntOr("scanY", -1), -1, 1);
    scanZ = Math.clamp(data.getIntOr("scanZ", -5), -5, 5);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("scanX", scanX);
    data.putInt("scanY", scanY);
    data.putInt("scanZ", scanZ);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    fertilizerSlot.organize();
    hydrationSlot.organize();
    weedExSlot.organize();
    if (energy.getEnergy() >= 31) scan();
  }

  private void advance() {
    int cell =
        Math.floorMod(
            (Math.clamp(scanY, -1, 1) + 1) * 121
                + (Math.clamp(scanZ, -5, 5) + 5) * 11
                + Math.clamp(scanX, -5, 5)
                + 6,
            363);
    scanX = cell % 11 - 5;
    scanZ = cell / 11 % 11 - 5;
    scanY = cell / 121 - 1;
  }

  public void scan() {
    var world = getLevel();
    if (world == null || world.isClientSide() || energy.getEnergy() < 1) return;
    advance();
    var position = worldPosition.offset(scanX, scanY, scanZ);
    if (!world.isLoaded(position)
        || world.isOutsideBuildHeight(position)
        || !energy.useEnergy(1)
        || !(world.getBlockEntity(position) instanceof CropPlot crop)) return;
    if (crop.getStorageNutrients() < 100
        && fertilizerSlot.consume(1, true, false) != null
        && energy.useEnergy(10)) {
      if (crop.applyFertilizer(false)) fertilizerSlot.consume(1);
      else energy.forceAddEnergy(10);
    }
    if (!hydrationSlot.isEmpty() && energy.useEnergy(10)) {
      var owned = hydrationSlot.get().copy();
      if (ItemDefinitions.HYDRATION_CELL.useOnCrop(owned, crop, false)) hydrationSlot.put(owned);
      else energy.forceAddEnergy(10);
    }
    if (!weedExSlot.isEmpty() && energy.useEnergy(10)) {
      var owned = weedExSlot.get().copy();
      if (ItemDefinitions.WEED_EX_CELL.useOnCrop(owned, crop, false)) weedExSlot.put(owned);
      else energy.forceAddEnergy(10);
    }
    super.setChanged();
  }

  @Override
  public OwnedMenu<CellCropCaretaker> createServerScreenHandler(int id, Player player) {
    return new CellCareMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new CellCareMenu(id, inventory, this);
  }
}
