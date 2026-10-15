package ic2ce.neoforge.crafting;

import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.processing.ProcessingItems;
import ic2ce.neoforge.recipe.crafting.PositionedCrafting;
import ic2ce.neoforge.registry.RecipeKinds;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.state.BlockState;

/**
 * UU assembly resolves the complete untrimmed nine-cell layout, independent of ordinary crafting
 * placement.
 */
public class UuAssembler extends AutomaticCrafter {
  public static final List<CraftingRecipe> RECIPES = new CopyOnWriteArrayList<>();

  public UuAssembler(BlockPos position, BlockState state) {
    super(CraftingBlockEntities.UU_ASSEMBLY_BENCH, position, state);
  }

  @Override
  protected CraftingRecipe resolve(CraftingContainer grid) {
    var map = recipes();
    if (map == null || getLevel() == null) return null;
    for (var holder : map.byType(RecipeKinds.UU_ASSEMBLY))
      if (holder.value().matchesAssembly(grid.getItems(), getLevel())) return holder.value();
    for (var recipe : RECIPES) if (matchesRecipe(recipe, grid)) return recipe;
    return null;
  }

  @Override
  protected boolean matchesRecipe(CraftingRecipe recipe, CraftingContainer grid) {
    return recipe instanceof PositionedCrafting assembly
        ? assembly.matchesAssembly(grid.getItems(), getLevel())
        : super.matchesRecipe(recipe, grid);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return Set.of(
        UpgradableProperty.Transformer,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing);
  }

  @Override
  public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag flags) {
    tooltip.add(
        LocalizedText.translate(
            "ic2.tooltip.preferred_machine",
            LocalizedText.translate(ProcessingItems.REPLICATOR.getDescriptionId())));
  }
}
