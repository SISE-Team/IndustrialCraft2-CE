package ic2ce.neoforge.crafting;

import com.google.common.base.Predicate;
import ic2ce.api.network.ClientModifiable;
import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.IUpgradeItem;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.profile.NotClassic;
import ic2ce.core.util.StackTag;
import ic2ce.engine.fluid.FairAllocation;
import ic2ce.engine.value.ValueTuples;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.crafting.menu.AutomaticCraftingMenu;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.OutputInventory;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.network.ClientFieldValidator;
import ic2ce.neoforge.recipe.codec.RecipeGeneration;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ghost templates and actual ingredients are distinct; each completed craft owns its
 * output/remainder plan.
 */
@NotClassic
public class AutomaticCrafter extends PoweredBlockEntity
    implements IHasGui,
        IUpgradableBlock,
        ScreenValues,
        INetworkClientTileEntityEventListener,
        ClientFieldValidator {
  private static final Set<UpgradableProperty> UPGRADES =
      Set.of(
          UpgradableProperty.Processing,
          UpgradableProperty.Transformer,
          UpgradableProperty.EnergyStorage,
          UpgradableProperty.ItemConsuming,
          UpgradableProperty.ItemProducing);
  public static final int defaultTier = 1,
      defaultEnergyConsume = 2,
      defaultOperationLength = 40,
      defaultEnergyStorage = 20000;
  private final ItemStack[] ownedGrid = new ItemStack[9];
  @ClientModifiable public ItemStack[] craftingGrid = ownedGrid;
  public final InventorySlot[] ingredientsRow = new InventorySlot[9];
  public final OutputInventory craftingOutput =
      new OutputInventory(this, "output", 1, InventorySlot.InvSide.SIDE);
  public final OutputInventory containerOutput =
      new OutputInventory(this, "containersOut", 9, InventorySlot.InvSide.NOTSIDE);
  public final UpgradeInventory upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
  protected final CraftingContainer crafting = new CraftingGridView.ArrayGrid(ownedGrid, 3);
  public final CraftingContainer ingredients =
      new CraftingGridView(3, 3) {
        @Override
        protected ItemStack get(int index) {
          return ingredientsRow[index].get();
        }

        @Override
        protected void set(int index, ItemStack stack) {
          ingredientsRow[index].put(stack);
        }
      };
  public final Predicate<ValueTuples.T2<ItemStack, Integer>> acceptPredicate =
      request ->
          request != null
              && request.b != null
              && request.b >= 0
              && request.b < 9
              && acceptsIngredient(request.b, request.a);
  protected CraftingRecipe recipe;
  protected boolean canCraft, newChange = true, attemptToBalance;
  public ItemStack recipeOutput = ItemStack.EMPTY;
  public int energyConsume = 2, operationLength = 40, operationsPerTick = 1;
  protected int progress;
  protected float guiProgress;
  private RecipeMap generation;

  public AutomaticCrafter(BlockPos position, BlockState state) {
    this(BlockEntityDefinitions.BATCH_CRAFTER, position, state);
  }

  protected AutomaticCrafter(
      BlockEntityType<? extends AutomaticCrafter> type, BlockPos position, BlockState state) {
    super(type, position, state, defaultEnergyStorage, defaultTier);
    Arrays.fill(ownedGrid, ItemStack.EMPTY);
    for (int index = 0; index < 9; index++) {
      final int slot = index;
      ingredientsRow[index] =
          new InventorySlot(this, "ingredient[" + index + "]", InventorySlot.Access.I, 1) {
            @Override
            public boolean accepts(ItemStack stack) {
              return acceptsIngredient(slot, stack);
            }

            @Override
            public void onChanged() {
              ingredientChange(slot);
            }
          };
    }
    comparator.setUpdate(
        () ->
            operationLength <= 0
                ? 0
                : (int) Math.clamp((long) progress * 15 / operationLength, 0, 15));
  }

  protected RecipeMap recipes() {
    var world = getLevel();
    return world == null
        ? null
        : world.getServer() == null
            ? RecipeGeneration.clientRecipes()
            : world.getServer().getRecipeManager().recipeMap();
  }

  protected CraftingRecipe resolve(CraftingContainer grid) {
    var map = recipes();
    return map == null
        ? null
        : map.getRecipesFor(RecipeType.CRAFTING, grid.asCraftInput(), getLevel())
            .map(holder -> holder.value())
            .findFirst()
            .orElse(null);
  }

  protected boolean matchesRecipe(CraftingRecipe selected, CraftingContainer grid) {
    return selected != null && selected.matches(grid.asCraftInput(), getLevel());
  }

  protected CraftingRecipe findRecipe() {
    return resolve(crafting);
  }

  protected CraftingRecipe findCraftingRecipe() {
    return resolve(ingredients);
  }

  private void sanitizeTemplate() {
    var incoming = craftingGrid;
    if (incoming != ownedGrid) {
      Arrays.fill(ownedGrid, ItemStack.EMPTY);
      if (incoming != null)
        for (int index = 0; index < Math.min(9, incoming.length); index++)
          ownedGrid[index] = incoming[index];
      craftingGrid = ownedGrid;
    }
    for (int index = 0; index < 9; index++) {
      var stack = ownedGrid[index];
      ownedGrid[index] =
          stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
    }
  }

  private boolean acceptsIngredient(int slot, ItemStack stack) {
    if (stack == null || stack.isEmpty() || getLevel() == null || ownedGrid[slot].isEmpty())
      return false;
    var selected = getLevel().isClientSide() ? findRecipe() : recipe;
    if (selected == null) return false;
    var candidate = ownedGrid.clone();
    candidate[slot] = stack.copyWithCount(1);
    return matchesRecipe(selected, new CraftingGridView.ArrayGrid(candidate, 3));
  }

  public void matrixChange(int slot) {
    sanitizeTemplate();
    var previous = recipe;
    recipe = findRecipe();
    generation = recipes();
    recipeOutput =
        recipe == null ? ItemStack.EMPTY : recipe.assemble(crafting.asCraftInput()).copy();
    if (previous != recipe) progress = 0;
    newChange = true;
    attemptToBalance = true;
  }

  public void ingredientChange(int slot) {
    newChange = true;
  }

  @Override
  public void acceptClientField(String name) {
    if (name.equals("craftingGrid")) {
      matrixChange(-1);
      super.setChanged();
    }
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    Arrays.fill(ownedGrid, ItemStack.EMPTY);
    craftingGrid = ownedGrid;
    progress = Math.max(0, data.getIntOr("progress", 0));
    var saved = data.getListOrEmpty("grid");
    for (int row = 0; row < Math.min(9, saved.size()); row++) {
      var cell = saved.getCompound(row).orElse(null);
      if (cell == null) continue;
      int index = cell.getByteOr("index", (byte) -1) & 255;
      if (index >= 9) continue;
      var stack = StackTag.parseHolder(cell);
      if (!stack.isEmpty()) ownedGrid[index] = stack.copyWithCount(1);
    }
    newChange = attemptToBalance = true;
    recipe = null;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("progress", progress);
    var cells = new ListTag();
    for (int index = 0; index < 9; index++)
      if (!ownedGrid[index].isEmpty()) {
        var row = new CompoundTag();
        row.putByte("index", (byte) index);
        StackTag.save(getLevel().registryAccess(), ownedGrid[index], row);
        cells.add(row);
      }
    data.put("grid", cells);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (getLevel() != null && !getLevel().isClientSide()) {
      setOverclockRates();
      int saved = progress;
      matrixChange(-1);
      progress = Math.min(saved, Math.max(0, operationLength - 1));
    }
  }

  @Override
  public void setChanged() {
    super.setChanged();
    var world = getLevel();
    if (world != null && !world.isClientSide()) {
      setOverclockRates();
      newChange = attemptToBalance = true;
    }
  }

  protected void setOverclockRates() {
    upgradeSlot.onChanged();
    int previous = Math.max(1, operationLength);
    operationsPerTick = upgradeSlot.getOperationsPerTick(40);
    operationLength = upgradeSlot.getOperationLength(40);
    energyConsume = upgradeSlot.getEnergyDemand(2);
    progress =
        (int)
            Math.clamp(
                Math.floor((double) progress / previous * operationLength),
                0,
                Math.max(0, operationLength - 1));
    int tier = upgradeSlot.getTier(1);
    energy.setSinkTier(tier);
    dischargeSlot.setTier(tier);
    energy.setCapacity(upgradeSlot.getEnergyStorage(20000, 40, 2));
  }

  private boolean balance() {
    boolean changed = false;
    var visited = new boolean[9];
    var demands = new int[9];
    var counts = new int[9];
    for (int first = 0; first < 9; first++) {
      var kind = ingredientsRow[first].get();
      if (visited[first] || kind.isEmpty()) continue;
      long total = 0, capacity = 0;
      for (int index = 0; index < 9; index++) {
        var current = ingredientsRow[index].get();
        if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, kind)) {
          total += current.getCount();
          visited[index] = true;
        }
      }
      Arrays.fill(demands, 0);
      for (int index = 0; index < 9; index++) {
        var current = ingredientsRow[index].get();
        if ((current.isEmpty() || ItemStack.isSameItemSameComponents(current, kind))
            && acceptsIngredient(index, kind)) {
          demands[index] =
              Math.min(ingredientsRow[index].getStackSizeLimit(), kind.getMaxStackSize());
          capacity += demands[index];
        }
      }
      if (total > capacity || total > Integer.MAX_VALUE) continue;
      FairAllocation.plan((int) total, demands, counts, 0);
      for (int index = 0; index < 9; index++) {
        var current = ingredientsRow[index].get();
        if (demands[index] == 0
            && (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, kind))) continue;
        if (current.getCount() != counts[index] || current.isEmpty() && counts[index] > 0) {
          ingredientsRow[index].putInTransaction(
              0, counts[index] == 0 ? ItemStack.EMPTY : kind.copyWithCount(counts[index]));
          changed = true;
        }
      }
    }
    if (changed) newChange = true;
    return changed;
  }

  public boolean hasRecipe() {
    return recipe != null;
  }

  public boolean hasCraftingRecipe() {
    return matchesRecipe(recipe, ingredients);
  }

  public boolean canCraft() {
    if (recipe == null || !matchesRecipe(recipe, ingredients)) return false;
    for (int index = 0; index < 9; index++)
      if (!ownedGrid[index].isEmpty() && !acceptsIngredient(index, ingredientsRow[index].get()))
        return false;
    return true;
  }

  public ItemStack getCraftingRecipeOutput() {
    var actual = findCraftingRecipe();
    return actual == null ? ItemStack.EMPTY : actual.assemble(ingredients.asCraftInput()).copy();
  }

  private record CraftPlan(ItemStack[] after, List<ItemStack> outputs, List<ItemStack> excess) {}

  private CraftPlan plan() {
    if (!canCraft()) return null;
    var before = new ItemStack[9];
    var after = new ItemStack[9];
    for (int index = 0; index < 9; index++) {
      before[index] = ingredientsRow[index].get().copy();
      after[index] =
          before[index].copyWithCount(
              before[index].getCount() - (ownedGrid[index].isEmpty() ? 0 : 1));
    }
    var positioned = ingredients.asPositionedCraftInput();
    var assembled = recipe.assemble(positioned.input());
    if (assembled.isEmpty()) return null;
    var outputs = new ArrayList<ItemStack>();
    outputs.add(assembled.copy());
    for (var stack : upgradeSlot)
      if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeItem upgrade)
        upgrade.onProcessEnd(stack, this, outputs);
    outputs.replaceAll(stack -> stack == null ? ItemStack.EMPTY : stack.copy());
    var excess = new ArrayList<ItemStack>();
    var remainder = recipe.getRemainingItems(positioned.input());
    if (remainder.size() > positioned.input().size())
      throw new IllegalStateException("Recipe remainder exceeded its native input grid");
    for (int index = 0; index < remainder.size(); index++) {
      var item = remainder.get(index);
      if (item.isEmpty()) continue;
      int slot =
          (index / positioned.input().width() + positioned.top()) * 3
              + index % positioned.input().width()
              + positioned.left();
      if (slot < 0 || slot >= 9)
        throw new IllegalStateException("Invalid native positioned recipe remainder");
      var current = after[slot];
      int limit = Math.min(ingredientsRow[slot].getStackSizeLimit(), item.getMaxStackSize());
      if (acceptsIngredient(slot, item)
          && (current.isEmpty() || ItemStack.isSameItemSameComponents(current, item))
          && (long) current.getCount() + item.getCount() <= limit)
        after[slot] = item.copyWithCount(current.getCount() + item.getCount());
      else excess.add(item.copy());
    }
    if (!craftingOutput.canAdd(outputs) || !containerOutput.canAdd(excess)) return null;
    for (int index = 0; index < 9; index++)
      if (!ItemStack.matches(before[index], ingredientsRow[index].get())) return null;
    return new CraftPlan(after, outputs, excess);
  }

  private void commit(CraftPlan plan) {
    for (int index = 0; index < 9; index++)
      ingredientsRow[index].putInTransaction(0, plan.after[index]);
    if (craftingOutput.add(plan.outputs) != 0 || containerOutput.add(plan.excess) != 0)
      throw new IllegalStateException("Native craft outputs changed during reserved commit");
    newChange = true;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (generation != recipes()) matrixChange(-1);
    boolean changed = false;
    if (attemptToBalance) {
      if (recipe != null && !ingredients.isEmpty()) changed |= balance();
      attemptToBalance = false;
    }
    canCraft = canCraft();
    newChange = false;
    CraftPlan finalPlan = canCraft && progress + 1L >= operationLength ? plan() : null;
    boolean permitted =
        canCraft
            && (progress + 1L < operationLength
                ? craftingOutput.canAdd(recipeOutput)
                : finalPlan != null);
    boolean paid = permitted && energy.useEnergy(energyConsume);
    setActive(paid);
    if (paid && ++progress >= operationLength) {
      progress = 0;
      commit(finalPlan);
      changed = true;
      for (int operation = 1; operation < Math.min(operationsPerTick, 4096); operation++) {
        var extra = plan();
        if (extra == null) break;
        commit(extra);
      }
    } else if (recipe == null) progress = 0;
    changed |= upgradeSlot.tickNoMark();
    guiProgress = operationLength <= 0 ? 0 : (float) progress / operationLength;
    if (changed) super.setChanged();
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event == 0) {
      matrixChange(-1);
      super.setChanged();
    }
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return UPGRADES;
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
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return new AutomaticCraftingMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new AutomaticCraftingMenu(id, inventory, this);
  }

  @Override
  public double getGuiValue(String name) {
    if (!name.equals("progress"))
      throw new IllegalArgumentException("Unknown automatic crafting gauge: " + name);
    return guiProgress;
  }
}
