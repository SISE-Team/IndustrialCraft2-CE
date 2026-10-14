package ic2ce.neoforge.reactor;

import ic2ce.api.upgrade.*;
import ic2ce.core.*;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.reactor.part.ChemicalCondensator;
import ic2ce.neoforge.world.DirectionSets;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Condensator service consumes one coolant block and 1000 EU for each actual reset. */
public abstract class CondensatorService extends PoweredBlockEntity
    implements IUpgradableBlock, IHasGui {
  private final Item target;
  public final StackWhitelistInput inputSlot;
  public final UpgradeInventory upgradeSlot;
  private static final Set<UpgradableProperty> PROPERTIES =
      Set.of(UpgradableProperty.ItemConsuming);

  protected CondensatorService(
      BlockEntityType<? extends CondensatorService> type,
      BlockPos position,
      BlockState state,
      ItemStack target,
      ItemStack coolant) {
    super(type, position, state, 48000, 2);
    this.target = target.getItem();
    inputSlot =
        new StackWhitelistInput(
            this, "input", InventorySlot.Access.I, 9, InventorySlot.InvSide.ANY, coolant);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
  }

  private NuclearCore attachedCore() {
    var world = getLevel();
    if (world == null) return null;
    var position = worldPosition.relative(getFacing().getOpposite());
    if (!world.hasChunkAt(position)) return null;
    var entity = world.getBlockEntity(position);
    var core =
        entity instanceof NuclearCore direct
            ? direct
            : entity instanceof ReactorChamber chamber ? chamber.getReactorInstance() : null;
    return core == null || core.isRemoved() || core.getLevel() != world ? null : core;
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (!getLevel().isClientSide()) updateEnergyFacings();
  }

  @Override
  protected void onNeighborChange(Block changed) {
    super.onNeighborChange(changed);
    if (!getLevel().isClientSide()) updateEnergyFacings();
  }

  @Override
  protected void setFacing(Level world, Direction facing) {
    super.setFacing(world, facing);
    if (!world.isClientSide()) updateEnergyFacings();
  }

  public void updateEnergyFacings() {
    var world = getLevel();
    if (world == null) return;
    int mask = 0;
    for (var face : Direction.values()) {
      var entity = world.getBlockEntity(worldPosition.relative(face));
      if (!(entity instanceof NuclearCore) && !(entity instanceof ReactorChamber))
        mask |= 1 << face.ordinal();
    }
    energy.setDirections(DirectionSets.ofMask(mask), Set.of());
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    var core = attachedCore();
    boolean serviced = false;
    if (core != null)
      for (int slot = 0; slot < core.reactorSlot.size(); slot++) {
        if (inputSlot.isEmpty() || energy.getEnergy() < 1000) break;
        var stack = core.reactorSlot.get(slot);
        if (stack.is(target)
            && stack.getItem() instanceof ChemicalCondensator condensator
            && condensator.getUseFraction(stack) > 0.85) {
          var reserved = inputSlot.consume(1, true, false);
          if (reserved == null || reserved.isEmpty() || !energy.useEnergy(1000)) continue;
          var consumed = inputSlot.consume(1);
          if (consumed == null || consumed.isEmpty())
            throw new IllegalStateException(
                "Condensator coolant reservation changed during commit");
          condensator.setUse(stack, 0);
          serviced = true;
        }
      }
    setActive(serviced);
    boolean inventoryChanged = upgradeSlot.tickNoMark();
    if (serviced) core.setChanged();
    if (serviced || inventoryChanged) setChanged();
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return PROPERTIES;
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
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return DocumentMenu.create(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(syncId, inventory, this);
  }
}
