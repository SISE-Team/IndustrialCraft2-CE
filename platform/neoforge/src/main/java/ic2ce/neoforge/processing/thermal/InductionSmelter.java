package ic2ce.neoforge.processing.thermal;

import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.network.GuiSynced;
import ic2ce.engine.machine.InductionCycle;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.inventory.OutputInventory;
import ic2ce.neoforge.inventory.SmeltingInput;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Two independent smelting lanes share one thermal cycle, with heater and processing energy
 * reserved together.
 */
public class InductionSmelter extends PoweredBlockEntity
    implements IHasGui, IUpgradableBlock, ScreenValues {
  public final SmeltingInput inputSlotA, inputSlotB;
  public final OutputInventory outputSlotA, outputSlotB;
  public final UpgradeInventory upgradeSlot;
  protected final SignalInput redstone;
  @GuiSynced public short heat, progress;
  private final InductionCycle cycle = new InductionCycle();

  public InductionSmelter(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.INDUCTION_FURNACE, position, state, 10000, 2);
    inputSlotA = new SmeltingInput(this, "inputA", 1);
    inputSlotB = new SmeltingInput(this, "inputB", 1);
    outputSlotA = new OutputInventory(this, "outputA", 1);
    outputSlotB = new OutputInventory(this, "outputB", 1);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 2);
    redstone = addComponent(new SignalInput(this));
    comparator.setUpdate(() -> heat * 15 / InductionCycle.MAX_HEAT);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    cycle.restore(data.getShortOr("heat", (short) 0), data.getShortOr("progress", (short) 0));
    heat = (short) cycle.heat();
    progress = (short) cycle.progress();
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putShort("heat", heat);
    data.putShort("progress", progress);
  }

  private MachineRecipeResult<ItemStack, ItemStack, ItemStack> ready(
      SmeltingInput input, OutputInventory output) {
    var recipe = input.process();
    return recipe != null && output.canAdd(recipe.getOutput()) ? recipe : null;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    cycle.restore(heat, progress);
    boolean completed = cycle.completionDue();
    if (completed) {
      operate();
      cycle.complete();
    }
    var first = ready(inputSlotA, outputSlotA);
    var second = ready(inputSlotB, outputSlotB);
    int previous = cycle.progress();
    int bill =
        cycle.advance(
            first != null || second != null, redstone.hasRedstoneInput(), energy.getEnergy());
    if (bill > 0 && !energy.useEnergy(bill))
      throw new IllegalStateException("Induction energy budget changed before commit");
    heat = (short) cycle.heat();
    progress = (short) cycle.progress();
    if (cycle.working()) {
      if (previous == 0) ProjectRuntime.network.get(true).initiateTileEntityEvent(this, 0, true);
      activate(false);
    } else {
      boolean interrupted = previous > 0;
      if (interrupted) ProjectRuntime.network.get(true).initiateTileEntityEvent(this, 1, true);
      else if (completed) ProjectRuntime.network.get(true).initiateTileEntityEvent(this, 3, true);
      if (cycle.heated()) activate(false);
      else shutdown(interrupted);
    }
    if (upgradeSlot.tickNoMark() || completed) setChanged();
  }

  public void operate() {
    operate(inputSlotA, outputSlotA);
    operate(inputSlotB, outputSlotB);
  }

  public void operate(SmeltingInput input, OutputInventory output) {
    var recipe = ready(input, output);
    if (recipe == null) return;
    input.consume(recipe);
    if (output.add(recipe.getOutput()) != 0)
      throw new IllegalStateException("Induction output changed before commit");
  }

  public boolean canOperate() {
    return canOperate(inputSlotA, outputSlotA) || canOperate(inputSlotB, outputSlotB);
  }

  public boolean canOperate(SmeltingInput input, OutputInventory output) {
    return ready(input, output) != null;
  }

  public String getHeat() {
    return heat * 100 / InductionCycle.MAX_HEAT + "%";
  }

  public int gaugeProgressScaled(int scale) {
    return (int) ((long) scale * progress / InductionCycle.COMPLETION);
  }

  @Override
  public double getGuiValue(String name) {
    if (name.equals("progress"))
      return Math.clamp(progress / (double) InductionCycle.COMPLETION, 0, 1);
    throw new IllegalArgumentException("Unknown induction gauge: " + name);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return DocumentMenu.create(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(id, inventory, this);
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
    return EnumSet.of(
        UpgradableProperty.RedstoneSensitive,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing);
  }

  @Override
  public SoundEvent getStartSoundEvent() {
    return SoundDefinitions.MACHINE_FURNACE_INDUCTION_START;
  }

  @Override
  public SoundEvent getStopSoundEvent() {
    return SoundDefinitions.MACHINE_FURNACE_INDUCTION_STOP;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_FURNACE_INDUCTION_LOOP;
  }

  @Override
  public SoundEvent getInterruptSoundEvent() {
    return SoundDefinitions.MACHINE_INTERRUPT1;
  }
}
