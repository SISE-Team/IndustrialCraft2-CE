package ic2ce.neoforge.processing.fluid;

import ic2ce.neoforge.block.ActiveBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.energy.storage.EnergyBank;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.item.container.ContainerItems;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.processing.ProcessingBlockEntities;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Reversible cell storage follows bank hysteresis and transfers only accepted energy. */
public class CellElectrolyzer extends ActiveBlockEntity implements IHasGui {
  public EnergyBank mfe;
  public int ticker;
  public final ConsumableInventory waterSlot, hydrogenSlot;
  @GuiSynced protected final ElectricalBuffer energy;

  public CellElectrolyzer(BlockPos position, BlockState state) {
    super(ProcessingBlockEntities.CLASSIC_ELECTROLYZER, position, state);
    waterSlot =
        new ItemWhitelistInput(
            this,
            "water",
            InventorySlot.Access.IO,
            1,
            InventorySlot.InvSide.TOP,
            ContainerItems.WATER_CELL);
    hydrogenSlot =
        new ItemWhitelistInput(
            this,
            "hydrogen",
            InventorySlot.Access.IO,
            1,
            InventorySlot.InvSide.BOTTOM,
            ContainerItems.ELECTROLYZED_WATER_CELL);
    energy = addComponent(new ElectricalBuffer(this, 20000, Set.of(), Set.of(), 1));
    comparator.setUpdate(energy::getComparatorValue);
    ticker = Math.floorMod(position.hashCode(), 16);
  }

  private boolean liveBank() {
    return mfe != null
        && getLevel() != null
        && !mfe.isRemoved()
        && mfe.getLevel() == getLevel()
        && mfe.getBlockPos().distManhattan(worldPosition) == 1
        && getLevel().getBlockEntity(mfe.getBlockPos()) == mfe;
  }

  public EnergyBank lookForMFE() {
    var world = getLevel();
    if (world == null) return null;
    for (var face : Direction.values()) {
      var position = worldPosition.relative(face);
      if (world.hasChunkAt(position)
          && world.getBlockEntity(position) instanceof EnergyBank bank
          && !bank.isRemoved()) return bank;
    }
    return null;
  }

  private static boolean room(ConsumableInventory slot, Item item) {
    var stack = slot.get();
    return stack.isEmpty()
        || ItemStack.isSameItemSameComponents(stack, new ItemStack(item))
            && stack.getCount() < Math.min(slot.getStackSizeLimit(), stack.getMaxStackSize());
  }

  private static void insert(ConsumableInventory slot, Item item) {
    var stack = slot.get();
    slot.put(stack.isEmpty() ? new ItemStack(item) : stack.copyWithCount(stack.getCount() + 1));
  }

  public boolean shouldDrain() {
    return liveBank() && mfe.energy.getFillRatio() >= 0.7;
  }

  public boolean shouldPower() {
    return liveBank() && mfe.energy.getFillRatio() <= 0.3;
  }

  public boolean canDrain() {
    return waterSlot.get().is(ContainerItems.WATER_CELL)
        && room(hydrogenSlot, ContainerItems.ELECTROLYZED_WATER_CELL);
  }

  public boolean canPower() {
    return hydrogenSlot.get().is(ContainerItems.ELECTROLYZED_WATER_CELL)
        && room(waterSlot, ContainerItems.WATER_CELL);
  }

  public int processRate() {
    return mfe == null
        ? 0
        : switch (mfe.energy.getSinkTier()) {
          case 2 -> 8;
          case 3 -> 32;
          case 4 -> 128;
          default -> 2;
        };
  }

  public boolean drain() {
    if (!liveBank() || !canDrain()) return false;
    double accepted =
        Math.min(processRate(), Math.min(energy.getFreeEnergy(), mfe.energy.getEnergy()));
    if (accepted > 0 && mfe.energy.useEnergy(accepted)) {
      energy.addEnergy(accepted);
      mfe.setChanged();
    }
    if (!energy.useEnergy(20000)) return accepted > 0;
    waterSlot.consume(1);
    insert(hydrogenSlot, ContainerItems.ELECTROLYZED_WATER_CELL);
    return true;
  }

  public boolean power() {
    if (!liveBank()) return false;
    boolean opened = false;
    if (energy.getEnergy() <= 0) {
      if (!canPower()) return false;
      double recovered = Math.min(20000L, 12000L + 2000L * Math.max(0, mfe.energy.getSinkTier()));
      hydrogenSlot.consume(1);
      insert(waterSlot, ContainerItems.WATER_CELL);
      energy.forceAddEnergy(recovered);
      opened = true;
    }
    double offered = Math.min(processRate(), energy.getEnergy());
    double accepted = mfe.energy.addEnergy(offered);
    if (accepted > 0) {
      energy.useEnergy(accepted);
      mfe.setChanged();
      return true;
    }
    return opened;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (++ticker % 16 == 0 || !liveBank()) mfe = lookForMFE();
    boolean changed = shouldDrain() && canDrain() && drain();
    if (shouldPower() && (canPower() || energy.getEnergy() > 0)) changed |= power();
    setActiveState(changed, false);
    if (changed) setChanged();
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_ELECTROLYZER_LOOP;
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
