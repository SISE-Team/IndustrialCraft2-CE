package ic2.verification;

import java.util.*;
import ic2.core.recipe.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

/** Vanilla recipe-book entries and actual native auto-placement, including 2x2 recipes. */
public final class RecipeBookChecks {
 public static void run(ServerPlayer player,AllItemsVerification ledger){var rows=new ArrayList<Map<String,Object>>();var missing=new ArrayList<String>();var context=SlotDisplayContext.fromLevel(player.level());
  for(var holder:player.level().getServer().getRecipeManager().getRecipes()){
   if(holder.value().getType()!=RecipeType.CRAFTING||!(holder.value() instanceof AdvRecipe||holder.value() instanceof AdvShapelessRecipe))continue;var recipe=(CraftingRecipe)holder.value();var ingredients=recipe instanceof AdvRecipe shaped?shaped.getIngredients().stream().flatMap(Optional::stream).toList():Arrays.stream(((AdvShapelessRecipe)recipe).input).map(ic2.api.recipe.IRecipeInput::getIngredient).toList();
   var offered=ingredients.stream().map(i->i.display().resolveForFirstStack(context)).toList();if(offered.stream().anyMatch(ItemStack::isEmpty)){missing.add(holder.id().identifier().toString());continue;}
   var displays=new ArrayList<net.minecraft.world.item.crafting.display.RecipeDisplayEntry>();player.level().getServer().getRecipeManager().listDisplaysForRecipe(holder.id(),displays::add);
   ledger.check(!displays.isEmpty()||recipe.display().isEmpty(),holder.id()+" has a native recipe-book entry");ledger.check(!recipe.placementInfo().isImpossibleToPlace(),holder.id()+" native placement is available");
   player.getInventory().clearContent();for(int i=0;i<offered.size();i++)player.getInventory().setItem(i,offered.get(i).copyWithCount(1));var menu=new CraftingMenu(187,player.getInventory(),ContainerLevelAccess.create(player.level(),new net.minecraft.core.BlockPos(84,101,80)));var action=menu.handlePlacement(false,false,holder,player.level(),player.getInventory());var actual=menu.getResultSlot().getItem();var expected=recipe instanceof AdvRecipe a?a.getOutput():((AdvShapelessRecipe)recipe).getOutput();boolean passed=actual.is(expected.getItem())&&actual.getCount()==expected.getCount();ledger.check(passed,holder.id()+" vanilla 3x3 recipe-book auto-fill produces declared result");
   rows.add(Map.of("recipe",holder.id().identifier().toString(),"displayCount",displays.size(),"passed",passed,"output",actual.toString(),"postPlaceAction",action.toString()));
   if(recipe instanceof AdvRecipe shape&&shape.inputWidth<=2&&shape.inputHeight<=2){player.getInventory().clearContent();for(int i=0;i<offered.size();i++)player.getInventory().setItem(i,offered.get(i).copyWithCount(1));var inventoryMenu=player.inventoryMenu;for(int i=1;i<=4;i++)inventoryMenu.getSlot(i).set(ItemStack.EMPTY);inventoryMenu.handlePlacement(false,false,holder,player.level(),player.getInventory());var result=inventoryMenu.getSlot(0).getItem();ledger.check(result.is(expected.getItem())&&result.getCount()==expected.getCount(),holder.id()+" vanilla 2x2 auto-fill layout");}
  }ledger.phases.put("vanillaRecipeBook",Map.of("cases",rows,"missingOptionalProviderIngredients",missing));player.getInventory().clearContent();
 }
}
