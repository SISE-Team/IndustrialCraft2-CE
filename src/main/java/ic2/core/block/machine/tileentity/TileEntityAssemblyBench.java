package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.IHasGui;
import ic2.core.init.Localization;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityAssemblyBench extends TileEntityBatchCrafter implements IHasGui, IUpgradableBlock {
   public static final List<CraftingRecipe> RECIPES = new ArrayList<>();

   public TileEntityAssemblyBench(BlockPos pos, BlockState state) {
      super(Ic2BlockEntities.UU_ASSEMBLY_BENCH, pos, state);
   }

   @Override
   protected CraftingRecipe findRecipe() {
      return findAssemblyRecipe(this.crafting);
   }

   @Override
   protected CraftingRecipe findCraftingRecipe() {
      return findAssemblyRecipe(this.ingredients);
   }

   private CraftingRecipe findAssemblyRecipe(net.minecraft.world.inventory.CraftingContainer grid) {
      var server = this.getLevel().getServer();
      var recipes = server == null ? ic2.core.recipe.v2.RecipeManagerGetter.clientRecipes() : server.getRecipeManager().recipeMap();
      {
         for (var holder : recipes.byType(ic2.core.ref.Ic2RecipeTypes.UU_ASSEMBLY)) {
            if (holder.value().matchesAssembly(grid.getItems(), this.getLevel())) return holder.value();
         }
      }
      for (CraftingRecipe recipe : RECIPES) {
         if (recipe.matches(grid.asCraftInput(), this.getLevel())) {
            return recipe;
         }
      }

      return null;
   }

   @Override
   public void matrixChange(int slot) {
      this.recipe = null;
      super.matrixChange(slot);
   }

   @Override
   public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(Localization.translate("ic2.tooltip.preferred_machine", Localization.translate(Ic2Items.REPLICATOR.getDescriptionId())));
   }

   @Override
   public Set<UpgradableProperty> getUpgradableProperties() {
      return EnumSet.of(UpgradableProperty.Transformer, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
   }
}
