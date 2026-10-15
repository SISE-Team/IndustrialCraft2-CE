package ic2ce.neoforge.processing.solid;

import ic2ce.api.recipe.IRecipeInput;
import ic2ce.api.recipe.MachineRecipe;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.neoforge.inventory.ItemRecipeInput;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.processing.ProcessingBlockEntities;
import ic2ce.neoforge.processing.fluid.FluidCollector;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/** Item compression and adjacent-pump snow production have separate, quantity-checked commits. */
public class PressureCompressor
    extends PoweredProcessor<IRecipeInput, Collection<ItemStack>, ItemStack> {
  protected boolean usingPumpRecipe;
  protected final List<FluidCollector> pumps = new ArrayList<>(6);
  private final int[] waterPlan = new int[6];
  private final MachineRecipe<IRecipeInput, Collection<ItemStack>> snowRecipe =
      new MachineRecipe<>(null, List.of(new ItemStack(Items.SNOWBALL)));

  public PressureCompressor(BlockPos position, BlockState state) {
    super(ProcessingBlockEntities.COMPRESSOR, position, state, 2, 300, 1);
    inputSlot = new ItemRecipeInput(this, "input", 1, Recipes.compressor);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    findPumps();
  }

  @Override
  protected void onNeighborChange(Block block) {
    super.onNeighborChange(block);
    findPumps();
  }

  protected void findPumps() {
    pumps.clear();
    var world = getLevel();
    if (world == null) return;
    for (var side : Direction.values())
      if (world.getBlockEntity(worldPosition.relative(side)) instanceof FluidCollector pump)
        pumps.add(pump);
  }

  private boolean planWater() {
    Arrays.fill(waterPlan, 0);
    int remaining = 1000;
    for (int index = 0; index < pumps.size(); index++) {
      var pump = pumps.get(index);
      if (pump.isRemoved()) continue;
      int available = LiquidUtil.drainTile(pump, Direction.UP, Fluids.WATER, remaining, true);
      if (available < 0 || available > remaining)
        throw new IllegalStateException("Pump violated compressor water budget");
      waterPlan[index] = available;
      remaining -= available;
      if (remaining == 0) return true;
    }
    return false;
  }

  @Override
  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getRecipeResult() {
    usingPumpRecipe = false;
    var item = super.getRecipeResult();
    if (item != null) return item;
    if (!inputSlot.isEmpty()
        || pumps.isEmpty()
        || !outputSlot.canAdd(snowRecipe.getOutput())
        || !planWater()) return null;
    usingPumpRecipe = true;
    processResult = getOutput(snowRecipe.getOutput());
    return snowRecipe.getResult(null);
  }

  @Override
  public void operateOnce(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe,
      Collection<ItemStack> output) {
    if (recipe.getRecipe() != snowRecipe) {
      super.operateOnce(recipe, output);
      return;
    }
    if (!outputSlot.canAdd(output) || !planWater())
      throw new IllegalStateException("Compressor snow inputs changed before commit");
    for (int index = 0; index < pumps.size(); index++)
      if (waterPlan[index] != 0)
        if (LiquidUtil.drainTile(
                pumps.get(index), Direction.UP, Fluids.WATER, waterPlan[index], false)
            != waterPlan[index])
          throw new IllegalStateException("Pump water changed before compressor commit");
    if (outputSlot.add(output) != 0)
      throw new IllegalStateException("Compressor snow output changed before commit");
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_COMPRESSOR_OPERATE;
  }

  @Override
  public SoundEvent getInterruptSoundEvent() {
    return SoundDefinitions.MACHINE_INTERRUPT1;
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.Processing,
        UpgradableProperty.Transformer,
        UpgradableProperty.EnergyStorage,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing);
  }
}
