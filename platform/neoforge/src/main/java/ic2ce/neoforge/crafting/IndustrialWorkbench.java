package ic2ce.neoforge.crafting;

import ic2ce.core.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.core.util.StackTag;
import ic2ce.core.util.Util;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.crafting.menu.WorkbenchMenu;
import ic2ce.neoforge.inventory.ConsumableInventory;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.TagInput;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.recipe.codec.RecipeGeneration;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.registry.ItemGroups;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

/** Manual nine-cell work and two independent tool stations, with quantity-preserving cleanup. */
@NotClassic
public class IndustrialWorkbench extends InventoryBlockEntity implements IHasGui {
  public final InventorySlot craftingGrid =
      new InventorySlot(this, "crafting", InventorySlot.Access.NONE, 9);
  public final InventorySlot craftingStorage =
      new InventorySlot(this, "craftingStorage", InventorySlot.Access.I, 18);
  public final ToolStation leftCrafting = new ToolStation(this, "left", ItemGroups.FORGE_HAMMERS);
  public final ToolStation rightCrafting = new ToolStation(this, "right", ItemGroups.WIRE_CUTTERS);

  public IndustrialWorkbench(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.INDUSTRIAL_WORKBENCH, position, state);
  }

  @Override
  public void onPlaced(ItemStack item, LivingEntity placer, Direction facing) {
    super.onPlaced(item, placer, facing);
    var data = StackTag.get(item);
    if (data == null || !data.getBooleanOr("PLACED", false)) {
      leftCrafting.tool.put(new ItemStack(ItemDefinitions.FORGE_HAMMER));
      rightCrafting.tool.put(new ItemStack(ItemDefinitions.CUTTER));
    }
  }

  @Override
  public ItemStack adjustDrop(ItemStack original, boolean wrench) {
    var drop = super.adjustDrop(original, wrench);
    if (drop == null || drop.isEmpty()) return drop;
    var owned = drop.copy();
    StackTag.update(
        owned,
        data -> {
          data.putBoolean("PLACED", true);
          return data;
        });
    return owned;
  }

  public void rebalance() {
    if (craftingGrid.isEmpty()) return;
    boolean changed = false;
    int[] existing = new int[9], capacity = new int[9];
    for (int storage = 0; storage < craftingStorage.size(); storage++) {
      var extra = craftingStorage.get(storage);
      if (extra.isEmpty()) continue;
      java.util.Arrays.fill(existing, 0);
      java.util.Arrays.fill(capacity, 0);
      for (int cell = 0; cell < 9; cell++) {
        var current = craftingGrid.get(cell);
        if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, extra)) {
          existing[cell] = current.getCount();
          capacity[cell] = Math.min(craftingGrid.getStackSizeLimit(), extra.getMaxStackSize());
        }
      }
      var plan = ic2ce.engine.inventory.FillLevels.plan(existing, capacity, extra.getCount());
      int accepted = 0;
      for (int cell = 0; cell < 9; cell++)
        if (plan[cell] > existing[cell]) {
          craftingGrid.putInTransaction(cell, extra.copyWithCount(plan[cell]));
          accepted += plan[cell] - existing[cell];
        }
      if (accepted > 0) {
        craftingStorage.putInTransaction(storage, extra.copyWithCount(extra.getCount() - accepted));
        changed = true;
      }
    }
    if (changed) setChanged();
  }

  private static ItemStack store(InventorySlot destination, ItemStack offered) {
    var remainder = offered.copy();
    for (int pass = 0; pass < 2 && !remainder.isEmpty(); pass++)
      for (int index = 0; index < destination.size() && !remainder.isEmpty(); index++) {
        var current = destination.get(index);
        if (current.isEmpty() != (pass == 1)) continue;
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, remainder)) continue;
        int capacity =
            Math.min(destination.getStackSizeLimit(), remainder.getMaxStackSize())
                - current.getCount();
        int accepted = Math.min(remainder.getCount(), Math.max(0, capacity));
        if (accepted > 0) {
          destination.putInTransaction(
              index, remainder.copyWithCount(current.getCount() + accepted));
          remainder = remainder.copyWithCount(remainder.getCount() - accepted);
        }
      }
    return remainder;
  }

  private static ItemStack storePlayer(Player player, ItemStack offered) {
    var remainder = offered.copy();
    var main = Util.playerMainItems(player);
    for (int pass = 0; pass < 2 && !remainder.isEmpty(); pass++)
      for (int index = 0; index < main.size() && !remainder.isEmpty(); index++) {
        var current = main.get(index);
        if (current.isEmpty() != (pass == 1)
            || !current.isEmpty() && !ItemStack.isSameItemSameComponents(current, remainder))
          continue;
        int accepted =
            Math.min(
                remainder.getCount(),
                Math.max(0, remainder.getMaxStackSize() - current.getCount()));
        if (accepted > 0) {
          main.set(index, remainder.copyWithCount(current.getCount() + accepted));
          remainder = remainder.copyWithCount(remainder.getCount() - accepted);
        }
      }
    player.getInventory().setChanged();
    return remainder;
  }

  public void clear(Player player) {
    var world = getLevel();
    if (player == null || world == null || world.isClientSide() || player.level() != world) return;
    boolean changed = false;
    for (int cell = 0; cell < 9; cell++) {
      var original = craftingGrid.get(cell);
      if (original.isEmpty()) continue;
      var remainder = store(craftingStorage, original);
      if (!remainder.isEmpty()) remainder = storePlayer(player, remainder);
      if (!ItemStack.matches(original, remainder)) {
        craftingGrid.putInTransaction(cell, remainder);
        changed = true;
      }
    }
    if (changed) setChanged();
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return new WorkbenchMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new WorkbenchMenu(id, inventory, this);
  }

  public static class ToolStation {
    private final InventoryBlockEntity owner;
    protected CraftingRecipe recipe;
    private RecipeMap generation;
    public final ConsumableInventory input;
    public final TagInput tool;
    public final ResultContainer resultInv = new ResultContainer();
    public final CraftingContainer crafting =
        new CraftingGridView(2, 1) {
          private InventorySlot group(int index) {
            return index == 0 ? tool : input;
          }

          @Override
          protected ItemStack get(int index) {
            return group(index).get();
          }

          @Override
          protected void set(int index, ItemStack stack) {
            group(index).put(stack);
          }
        };

    public ToolStation(InventoryBlockEntity owner, String name, TagKey<Item> tag) {
      this.owner = owner;
      input =
          new ConsumableInventory(
              owner, name + "Input", InventorySlot.Access.I, 1, InventorySlot.InvSide.ANY) {
            @Override
            public boolean accepts(ItemStack stack) {
              if (stack == null || stack.isEmpty() || tool == null || tool.isEmpty()) return false;
              var probe =
                  new CraftingGridView.ArrayGrid(
                      new ItemStack[] {tool.get().copy(), stack.copy()}, 2);
              return resolve(probe) != null;
            }

            @Override
            public void onChanged() {
              refresh();
            }
          };
      tool =
          new TagInput(
              owner, name + "Tool", InventorySlot.Access.I, 1, InventorySlot.InvSide.ANY, tag) {
            @Override
            public void onChanged() {
              refresh();
            }
          };
    }

    private RecipeMap recipes() {
      var world = owner.getLevel();
      return world == null
          ? null
          : world.getServer() == null
              ? RecipeGeneration.clientRecipes()
              : world.getServer().getRecipeManager().recipeMap();
    }

    private CraftingRecipe resolve(CraftingContainer grid) {
      var map = recipes();
      return map == null
          ? null
          : map.getRecipesFor(RecipeType.CRAFTING, grid.asCraftInput(), owner.getLevel())
              .map(holder -> holder.value())
              .findFirst()
              .orElse(null);
    }

    protected boolean canProcess() {
      if (tool == null
          || input == null
          || tool.isEmpty()
          || input.isEmpty()
          || owner.getLevel() == null) {
        recipe = null;
        return false;
      }
      var current = recipes();
      if (generation != current
          || recipe == null
          || !recipe.matches(crafting.asCraftInput(), owner.getLevel())) {
        generation = current;
        recipe = resolve(crafting);
      }
      return recipe != null;
    }

    private void refresh() {
      var world = owner.getLevel();
      if (world != null && !world.isClientSide()) resultInv.setItem(0, getOutputStack());
    }

    public ItemStack getOutputStack() {
      return canProcess() ? recipe.assemble(crafting.asCraftInput()).copy() : ItemStack.EMPTY;
    }
  }
}
