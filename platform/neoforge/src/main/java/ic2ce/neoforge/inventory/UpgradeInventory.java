package ic2ce.neoforge.inventory;

import ic2ce.api.upgrade.*;
import ic2ce.core.block.IInventorySlotHolder;
import ic2ce.engine.machine.UpgradeMath;
import ic2ce.neoforge.component.SignalInput;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Upgrade callback adapter. Rate arithmetic is implemented in the standalone engine. */
public class UpgradeInventory extends InventorySlot {
  public int augmentation, extraProcessTime, extraEnergyDemand, extraEnergyStorage, extraTier;
  public double processTimeMultiplier, energyDemandMultiplier, energyStorageMultiplier;
  private List<SignalInput.IRedstoneModifier> redstoneModifiers = List.of();

  public static UpgradeInventory createUnchecked(
      IInventorySlotHolder<?> owner, String name, int count) {
    return new UpgradeInventory(owner, name, count);
  }

  public UpgradeInventory(IInventorySlotHolder<?> owner, String name, int count) {
    super(owner, name, Access.NONE, count);
    reset();
  }

  @Override
  public boolean accepts(ItemStack stack) {
    return stack.getItem() instanceof IUpgradeItem item
        && item.isSuitableFor(stack, ((IUpgradableBlock) base).getUpgradableProperties());
  }

  private void reset() {
    augmentation = extraProcessTime = extraEnergyDemand = extraEnergyStorage = extraTier = 0;
    processTimeMultiplier = energyDemandMultiplier = energyStorageMultiplier = 1;
  }

  @Override
  public void onChanged() {
    reset();
    var block = (IUpgradableBlock) base;
    var modifiers = new ArrayList<SignalInput.IRedstoneModifier>();
    for (int slot = 0; slot < size(); slot++) {
      ItemStack stack = get(slot);
      if (stack.isEmpty() || !accepts(stack)) continue;
      IUpgradeItem item = (IUpgradeItem) stack.getItem();
      int count = stack.getCount();
      if (item instanceof IAugmentationUpgrade upgrade)
        augmentation += upgrade.getAugmentation(stack, block) * count;
      if (item instanceof IProcessingUpgrade upgrade) {
        extraProcessTime += upgrade.getExtraProcessTime(stack, block) * count;
        processTimeMultiplier *= Math.pow(upgrade.getProcessTimeMultiplier(stack, block), count);
        extraEnergyDemand += upgrade.getExtraEnergyDemand(stack, block) * count;
        energyDemandMultiplier *= Math.pow(upgrade.getEnergyDemandMultiplier(stack, block), count);
      }
      if (item instanceof IEnergyStorageUpgrade upgrade) {
        extraEnergyStorage += upgrade.getExtraEnergyStorage(stack, block) * count;
        energyStorageMultiplier *=
            Math.pow(upgrade.getEnergyStorageMultiplier(stack, block), count);
      }
      if (item instanceof ITransformerUpgrade upgrade)
        extraTier += upgrade.getExtraTier(stack, block) * count;
      if (item instanceof IRedstoneSensitiveUpgrade upgrade
          && upgrade.modifiesRedstoneInput(stack, block))
        modifiers.add(new RedstoneModifier(upgrade, stack.copy(), block));
    }
    for (var component : base.getParent().getComponents()) {
      if (component instanceof SignalInput redstone) {
        redstone.removeRedstoneModifiers(redstoneModifiers);
        redstone.addRedstoneModifiers(modifiers);
        redstone.update();
      }
    }
    redstoneModifiers = modifiers.isEmpty() ? List.of() : modifiers;
  }

  public int getOperationsPerTick(int length) {
    return UpgradeMath.operations(length, extraProcessTime, processTimeMultiplier);
  }

  public int getOperationLength(int length) {
    return UpgradeMath.ticks(length, extraProcessTime, processTimeMultiplier);
  }

  public int getEnergyDemand(int demand) {
    return UpgradeMath.modified(demand, extraEnergyDemand, energyDemandMultiplier);
  }

  public int getEnergyStorage(int storage, int length, int demand) {
    return UpgradeMath.modified(
        storage,
        (long) extraEnergyStorage + (long) getOperationLength(length) * getEnergyDemand(demand),
        energyStorageMultiplier);
  }

  public int getTier(int tier) {
    return UpgradeMath.modified(tier, extraTier, 1);
  }

  public int getRemoteRange(int range) {
    for (ItemStack stack : this)
      if (!stack.isEmpty()
          && accepts(stack)
          && stack.getItem() instanceof IRemoteAccessUpgrade upgrade)
        range = upgrade.getRangeAmplification(stack, (IUpgradableBlock) base, range);
    return range;
  }

  public boolean tickNoMark() {
    var block = (IUpgradableBlock) base;
    boolean changed = false;
    for (int slot = 0; slot < size(); slot++) {
      ItemStack stack = get(slot);
      if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeItem upgrade)
        changed |= upgrade.onTick(stack, block);
    }
    return changed;
  }

  public void tick() {
    if (tickNoMark()) base.getParent().setChanged();
  }

  private record RedstoneModifier(
      IRedstoneSensitiveUpgrade upgrade, ItemStack stack, IUpgradableBlock block)
      implements SignalInput.IRedstoneModifier {
    @Override
    public int getRedstoneInput(int input) {
      return upgrade.getRedstoneInput(stack, block, input);
    }
  }
}
