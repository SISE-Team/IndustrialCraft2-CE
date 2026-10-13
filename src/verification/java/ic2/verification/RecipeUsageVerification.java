package ic2.verification;

import ic2.core.recipe.AdvRecipe;
import ic2.core.recipe.AdvShapelessRecipe;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.block.Blocks;

/** Crafts through the native result slot: real input removal and crafting remainders. */
public final class RecipeUsageVerification {
    private static final BlockPos TABLE=new BlockPos(84,101,80);
    public static void run(ServerPlayer player,AllItemsVerification ledger){
        player.level().setBlock(TABLE,Blocks.CRAFTING_TABLE.defaultBlockState(),3);
        var rows=new ArrayList<Map<String,Object>>();int variants=0;
        for(var holder:player.level().getServer().getRecipeManager().getRecipes()){
            if(!(holder.value() instanceof CraftingRecipe recipe)||!(recipe instanceof AdvRecipe||recipe instanceof AdvShapelessRecipe)||recipe.getType()!=RecipeType.CRAFTING)continue;
            String id=holder.id().identifier().toString();
            try{
                var grid=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));
                if(recipe instanceof AdvRecipe shaped){int i=0;for(var ingredient:shaped.getIngredients()){int x=i%shaped.inputWidth,y=i/shaped.inputWidth;i++;grid.set(x+y*3,ingredient.map(v->v.display().resolveForFirstStack(SlotDisplayContext.fromLevel(player.level()))).orElse(ItemStack.EMPTY));}}
                else{var shapeless=(AdvShapelessRecipe)recipe;for(int i=0;i<shapeless.input.length;i++)grid.set(i,shapeless.input[i].getIngredient().display().resolveForFirstStack(SlotDisplayContext.fromLevel(player.level())));}
                rows.add(craft(player,ledger,id,grid,recipe));
                // Tagged ingredients also exercise every IC2 alternative that lacks a usage entry.
                for(var itemId:BuiltInRegistries.ITEM.keySet()){
                    var item=BuiltInRegistries.ITEM.getValue(itemId);if(!itemId.getNamespace().equals("ic2")||ledger.hasUsage(item))continue;
                    for(int slot=0;slot<9;slot++){if(grid.get(slot).isEmpty())continue;var alternate=new ArrayList<>(grid);alternate.set(slot,new ItemStack(item));if(recipe.matches(CraftingInput.of(3,3,alternate),player.level())){rows.add(craft(player,ledger,id+" ["+itemId+"]",alternate,recipe));variants++;break;}}
                }
            }catch(Throwable e){ledger.check(false,"native crafting "+id+": "+e);rows.add(Map.of("recipe",id,"error",e.toString()));}
        }
        RecipeBookChecks.run(player,ledger);
        ledger.phases.put("crafting",Map.of("cases",rows,"alternativeCases",variants));player.getInventory().clearContent();
    }
    private static Map<String,Object> craft(ServerPlayer player,AllItemsVerification ledger,String id,List<ItemStack> grid,CraftingRecipe recipe){
        player.getInventory().clearContent();var menu=new CraftingMenu(177,player.getInventory(),ContainerLevelAccess.create(player.level(),TABLE));
        for(int i=0;i<9;i++)menu.getSlot(i+1).set(grid.get(i).copyWithCount(grid.get(i).isEmpty()?0:1));
        var before=new ArrayList<ItemStack>();for(int i=1;i<=9;i++)before.add(menu.getSlot(i).getItem().copy());
        var declared=recipe instanceof AdvRecipe a?a.getOutput():((AdvShapelessRecipe)recipe).getOutput();var expected=menu.getResultSlot().getItem().copy();ledger.check(expected.is(declared.getItem())&&expected.getCount()==declared.getCount(),id+" native result matches the requested recipe output");ledger.check(!expected.isEmpty(),id+" has a native result");
        menu.clicked(0,0,ContainerInput.PICKUP,player);var result=menu.getCarried();boolean success=!expected.isEmpty()&&ItemStack.matches(expected,result);ledger.check(success,id+" result slot transfers crafted output");
        var used=new ArrayList<String>();
        for(int i=0;i<9;i++){var input=before.get(i);var after=menu.getSlot(i+1).getItem();if(!input.isEmpty()&&!ItemStack.matches(input,after)&&success){ledger.mark(input.getItem(),"native_crafting_input",id+" -> "+result);used.add(BuiltInRegistries.ITEM.getKey(input.getItem()).toString());}}
        ledger.check(expected.isEmpty()||!used.isEmpty()||before.stream().anyMatch(s->!s.isEmpty()),id+" has input ingredients");
        return Map.of("recipe",id,"output",result.toString(),"usedInputs",used,"success",success);
    }
}
