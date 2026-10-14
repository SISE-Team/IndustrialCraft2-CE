package ic2ce.neoforge.transport;

import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.IUpgradeItem;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.profile.NotClassic;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.transport.menu.DualBankMenu;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Two physical inventory banks and an alternating paid transfer-upgrade turn. */
@NotClassic
public class DualItemBuffer extends InventoryBlockEntity implements IHasGui, IUpgradableBlock {
  public final InventorySlot rightcontentSlot =
      new InventorySlot(
          this, "rightcontent", InventorySlot.Access.IO, 24, InventorySlot.InvSide.SIDE);
  public final InventorySlot leftcontentSlot =
      new InventorySlot(
          this, "leftcontent", InventorySlot.Access.IO, 24, InventorySlot.InvSide.NOTSIDE);
  public final UpgradeInventory upgradeSlot = new UpgradeInventory(this, "upgrade", 2);
  private int next;
  private double workBudget = 40;

  public DualItemBuffer(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.ITEM_BUFFER, position, state);
    comparator.setUpdate(() -> calcRedstoneFromInvSlots(rightcontentSlot, leftcontentSlot));
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    workBudget = 40;
    int selected = -1;
    for (int offset = 0; offset < 2; offset++) {
      int index = (next + offset) & 1;
      var stack = upgradeSlot.get(index);
      if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeItem) {
        selected = index;
        break;
      }
    }
    if (selected < 0) return;
    var stack = upgradeSlot.get(selected);
    next = (selected + 1) & 1;
    if (((IUpgradeItem) stack.getItem()).onTick(stack, this)) super.setChanged();
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return Set.of(UpgradableProperty.ItemProducing);
  }

  @Override
  public double getEnergy() {
    return workBudget;
  }

  @Override
  public boolean useEnergy(double amount) {
    if (!Double.isFinite(amount) || amount < 0 || amount > workBudget) return false;
    workBudget -= amount;
    return true;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return new DualBankMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new DualBankMenu(id, inventory, this);
  }
}
