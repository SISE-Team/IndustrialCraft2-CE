package ic2ce.neoforge.reactor;

import ic2ce.api.reactor.IReactorChamber;
import ic2ce.api.upgrade.*;
import ic2ce.core.*;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.profile.NotClassic;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Shell-only coolant capability; detached ports expose no stale reactor tanks. */
@NotClassic
public class CoolantPort extends InventoryBlockEntity
    implements IHasGui, IUpgradableBlock, IReactorChamber {
  public final UpgradeInventory upgradeSlot = new UpgradeInventory(this, "upgrade", 1);
  private final FluidCoreLookup lookup = addComponent(new FluidCoreLookup(this));
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));
  private NuclearCore tankOwner;
  private List<FluidPorts.InternalFluidTank> tanks = List.of();
  private static final Set<UpgradableProperty> PROPERTIES =
      Set.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);

  public CoolantPort(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.REACTOR_FLUID_PORT, position, state);
    fluids.addUnmanagedTankHook(
        () -> {
          var core = getReactorInstance();
          if (core != tankOwner) {
            tankOwner = core;
            tanks = core == null ? List.of() : List.of(core.inputTank, core.outputTank);
          }
          return tanks;
        });
  }

  @Override
  protected void onUnloaded() {
    tankOwner = null;
    tanks = List.of();
    super.onUnloaded();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (getReactorInstance() != null) upgradeSlot.tick();
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

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return PROPERTIES;
  }

  @Override
  public double getEnergy() {
    return getReactorInstance() == null ? 0 : 40;
  }

  @Override
  public boolean useEnergy(double amount) {
    return Double.isFinite(amount) && amount >= 0 && amount <= getEnergy();
  }

  public NuclearCore getReactorInstance() {
    return lookup.getReactor();
  }

  @Override
  public boolean isWall() {
    return true;
  }
}
