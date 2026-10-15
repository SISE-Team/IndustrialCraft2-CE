package ic2ce.neoforge.processing.thermal;

import ic2ce.api.recipe.*;
import ic2ce.api.upgrade.*;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.neoforge.thermal.HeatSupply;
import java.util.Collection;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hot blast processing reserves compressed air for every advancing tick and owns recipe progress.
 */
@NotClassic
public class AlloyFurnace extends InventoryBlockEntity
    implements IUpgradableBlock, IHasGui, ScreenValues {
  public int heat;
  public static int maxHeat = 50000;
  @GuiSynced public float guiHeat;
  protected final SignalInput redstone;
  protected final FluidPorts fluids;
  protected int progress, progressNeeded = 300;
  @GuiSynced protected float guiProgress;
  public final ItemRecipeInput inputSlot;
  public final OutputInventory outputSlot, tankOutputSlot;
  public final FluidWhitelistInput tankInputSlot;
  public final UpgradeInventory upgradeSlot;
  @GuiSynced public final Ic2FluidTank fluidTank;
  private MachineRecipe<IRecipeInput, Collection<ItemStack>> activeRecipe;
  private static final Set<UpgradableProperty> PROPERTIES =
      Set.of(
          UpgradableProperty.RedstoneSensitive,
          UpgradableProperty.ItemConsuming,
          UpgradableProperty.ItemProducing,
          UpgradableProperty.FluidConsuming);

  public AlloyFurnace(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.BLAST_FURNACE, position, state);
    redstone = addComponent(new SignalInput(this));
    fluids = addComponent(new FluidPorts(this));
    fluidTank =
        fluids.addTankInsert("fluid", 8000, FluidPorts.fluidPredicate(FluidDefinitions.AIR.still));
    inputSlot = new ItemRecipeInput(this, "input", 1, Recipes.blastfurnace);
    outputSlot =
        new OutputInventory(this, "output", 2) {
          @Override
          public void onPickupFromSlot(Player player, ItemStack stack) {
            if (player != null && stack.is(ItemDefinitions.STEEL_INGOT))
              ProjectRuntime.achievements.issueAchievement(player, "acquireRefinedIron");
          }
        };
    tankInputSlot = new FluidWhitelistInput(this, "cellInput", 1, FluidDefinitions.AIR.still);
    tankOutputSlot = new OutputInventory(this, "cellOutput", 1);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 2);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    heat = Math.clamp(data.getIntOr("heat", 0), 0, Math.max(1, maxHeat));
    progress = Math.max(0, data.getIntOr("progress", 0));
    activeRecipe = null;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("heat", heat);
    data.putInt("progress", progress);
  }

  private void heatChamber() {
    int capacity = Math.max(1, maxHeat);
    heat = Math.clamp(heat - 1, 0, capacity);
    if (!inputSlot.isEmpty() || progress > 0 || redstone.hasRedstoneInput())
      heat += HeatSupply.fromSide(getLevel(), worldPosition, getFacing(), capacity - heat);
  }

  @Override
  public void updateEntityServer() {
    super.updateEntityServer();
    int before = progress, previousHeat = heat;
    gainFluid();
    heatChamber();
    var candidate = inputSlot.isEmpty() ? null : inputSlot.process();
    var selected = candidate == null ? null : candidate.getRecipe();
    if (selected != activeRecipe) {
      if (activeRecipe != null || selected == null) progress = 0;
      activeRecipe = selected;
    }
    boolean worked = false;
    if (candidate != null && selected.getMetaData() != null) {
      var metadata = selected.getMetaData();
      int duration = metadata.getIntOr("duration", 0), air = metadata.getIntOr("fluid", 0);
      progressNeeded = Math.max(1, duration);
      if (duration > 0
          && air >= 0
          && isHot()
          && outputSlot.canAdd(candidate.getOutput())
          && (air == 0
              || fluidTank.hasExactFluid(FluidDefinitions.AIR.still)
                  && fluidTank.getFluidAmount() >= air)) {
        if (air > 0) fluidTank.drainMbUnchecked(air, false);
        progress = Math.min(progress, duration - 1) + 1;
        worked = true;
        if (progress == duration) {
          operateOnce(candidate, candidate.getOutput());
          progress = 0;
          activeRecipe = null;
        }
      }
    }
    setActive(worked);
    guiProgress = (float) progress / progressNeeded;
    guiHeat = (float) heat / Math.max(1, maxHeat);
    boolean upgraded = upgradeSlot.tickNoMark();
    if (worked || upgraded || progress != before || heat != previousHeat) setChanged();
  }

  public void operateOnce(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result,
      Collection<ItemStack> outputs) {
    if (!outputSlot.canAdd(outputs))
      throw new IllegalStateException("Blast furnace output reservation changed during commit");
    inputSlot.consume(result);
    outputSlot.add(outputs);
  }

  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
    var candidate = inputSlot.isEmpty() ? null : inputSlot.process();
    return candidate != null
            && candidate.getRecipe().getMetaData() != null
            && outputSlot.canAdd(candidate.getOutput())
        ? candidate
        : null;
  }

  public boolean gainFluid() {
    return tankInputSlot.processIntoTank(fluidTank, tankOutputSlot);
  }

  public boolean isHot() {
    return heat >= Math.max(1, maxHeat);
  }

  @Override
  public double getGuiValue(String key) {
    return switch (key) {
      case "progress" -> guiProgress;
      case "heat" -> guiHeat;
      default -> throw new IllegalArgumentException("Unknown blast furnace gauge: " + key);
    };
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
