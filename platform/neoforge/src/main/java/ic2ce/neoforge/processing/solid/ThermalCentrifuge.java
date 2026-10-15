package ic2ce.neoforge.processing.solid;

import ic2ce.api.recipe.IRecipeInput;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.engine.machine.ThermalRamp;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.inventory.ItemRecipeInput;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Thermal eligibility and the one-EU heater have their own control path, separate from recipe
 * progress.
 */
@NotClassic
public class ThermalCentrifuge
    extends PoweredProcessor<IRecipeInput, Collection<ItemStack>, ItemStack> {
  protected final SignalInput redstone;
  public static final short maxHeat = 5000;
  @GuiSynced public short heat;
  @GuiSynced public short workheat = maxHeat;

  public ThermalCentrifuge(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.CENTRIFUGE, position, state, 48, 500, 3, 2);
    inputSlot = new ItemRecipeInput(this, "input", 1, Recipes.centrifuge);
    redstone = addComponent(new SignalInput(this));
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    heat = (short) ThermalRamp.normalize(data.getShortOr("heat", (short) 0), maxHeat);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putShort("heat", heat);
  }

  public double getHeatRatio() {
    return ThermalRamp.ratio(heat, workheat);
  }

  private static int requiredHeat(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe) {
    var metadata = recipe.getRecipe().getMetaData();
    return metadata == null ? -1 : metadata.getIntOr("minHeat", 0);
  }

  @Override
  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getRecipeResult() {
    var recipe = super.getRecipeResult();
    if (recipe == null) return null;
    int required = requiredHeat(recipe);
    return required >= 0 && required <= heat ? recipe : null;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    int target = -1;
    if (redstone.hasRedstoneInput()) target = maxHeat;
    else {
      var recipe = super.getRecipeResult();
      if (recipe != null) target = requiredHeat(recipe);
    }
    if (target >= 0) workheat = (short) ThermalRamp.normalize(target, maxHeat);
    boolean powered = target >= 0 && energy.useEnergy(1);
    heat = (short) ThermalRamp.advance(heat, target, maxHeat, 1, powered);
  }

  @Override
  public double getGuiValue(String name) {
    return name.equals("heat") ? getHeatRatio() : super.getGuiValue(name);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.Processing,
        UpgradableProperty.RedstoneSensitive,
        UpgradableProperty.Transformer,
        UpgradableProperty.EnergyStorage,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing);
  }

  @Override
  protected boolean directRecipeOutputPreview() {
    return getClass() == ThermalCentrifuge.class;
  }
}
