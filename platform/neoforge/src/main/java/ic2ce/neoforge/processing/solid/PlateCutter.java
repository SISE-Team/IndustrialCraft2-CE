package ic2ce.neoforge.processing.solid;

import ic2ce.api.item.IBlockCuttingBlade;
import ic2ce.api.recipe.IRecipeInput;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.network.GuiSynced;
import ic2ce.core.profile.NotClassic;
import ic2ce.neoforge.inventory.ItemRecipeInput;
import ic2ce.neoforge.inventory.TypeFilteredInput;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** A blade capability and recipe hardness jointly determine whether cutting can advance. */
@NotClassic
public class PlateCutter extends PoweredProcessor<IRecipeInput, Collection<ItemStack>, ItemStack> {
  @GuiSynced private boolean bladeTooWeak;
  public final TypeFilteredInput cutterSlot;

  public PlateCutter(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.BLOCK_CUTTER, position, state, 4, 450, 1);
    inputSlot = new ItemRecipeInput(this, "input", 1, Recipes.blockcutter);
    cutterSlot = new TypeFilteredInput(this, "cutterInputSlot", 1, IBlockCuttingBlade.class);
  }

  @Override
  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getRecipeResult() {
    var stack = cutterSlot.get();
    if (stack.isEmpty() || !(stack.getItem() instanceof IBlockCuttingBlade blade)) {
      bladeTooWeak = true;
      return null;
    }
    bladeTooWeak = false;
    var recipe = super.getRecipeResult();
    if (recipe == null || recipe.getRecipe().getMetaData() == null) return null;
    int required = recipe.getRecipe().getMetaData().getIntOr("hardness", 0);
    bladeTooWeak = required < 0 || required > blade.getHardness(stack);
    return bladeTooWeak ? null : recipe;
  }

  @Override
  public boolean getGuiState(String name) {
    return name.equals("isBladeTooWeak") ? bladeTooWeak : super.getGuiState(name);
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

  @Override
  protected boolean directRecipeOutputPreview() {
    return getClass() == PlateCutter.class;
  }
}
