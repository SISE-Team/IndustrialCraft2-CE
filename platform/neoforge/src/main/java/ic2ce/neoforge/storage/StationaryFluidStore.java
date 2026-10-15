package ic2ce.neoforge.storage;

import ic2ce.api.upgrade.*;
import ic2ce.core.*;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.network.*;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Native 24-bucket storage shares the component-sensitive buffer and integer comparator contract.
 */
@NotClassic
public class StationaryFluidStore extends InventoryBlockEntity
    implements IUpgradableBlock, IHasGui {
  public final UpgradeInventory upgradeSlot;
  @GuiSynced protected final Ic2FluidTank fluidTank;
  protected final FluidPorts fluids;
  private static final Set<UpgradableProperty> PROPERTIES =
      Set.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);

  public StationaryFluidStore(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.TANK, position, state);
    fluids = addComponent(new FluidPorts(this));
    fluidTank = fluids.addTank("fluid", 24000);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
    comparator.setUpdate(
        () ->
            fluidTank.isEmpty()
                ? 0
                : (int)
                    Math.clamp(
                        1 + 14L * fluidTank.getFluidAmount() / fluidTank.getCapacity(), 1, 15));
  }

  public Ic2FluidTank getFluidTank() {
    return fluidTank;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (upgradeSlot.tickNoMark()) setChanged();
  }

  @Override
  public double getEnergy() {
    return 0;
  }

  @Override
  public boolean useEnergy(double amount) {
    return amount == 0;
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return PROPERTIES;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return DocumentMenu.create(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(syncId, inventory, this);
  }
}
