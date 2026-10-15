package ic2ce.neoforge.processing.solid;

import ic2ce.api.recipe.IBasicMachineRecipeManager;
import ic2ce.api.recipe.IRecipeInput;
import ic2ce.api.recipe.MachineRecipe;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.recipe.RecipeOutput;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.ItemRecipeInput;
import ic2ce.neoforge.material.MaterialItems;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.processing.ProcessingBlockEntities;
import ic2ce.neoforge.recipe.processing.ListedMachineInputs;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dynamic allow/deny policy and probabilistic recycling; rejected materials are still consumed
 * without scrap.
 */
public class MaterialRecycler
    extends PoweredProcessor<IRecipeInput, Collection<ItemStack>, ItemStack> {
  public MaterialRecycler(BlockPos position, BlockState state) {
    super(ProcessingBlockEntities.RECYCLER, position, state, 1, 45, 1);
    inputSlot = new ItemRecipeInput(this, "input", 1, _ -> Recipes.recycler);
  }

  public static void init() {
    Recipes.recycler = new RecyclerRecipeManager();
    Recipes.recyclerWhitelist = new ListedMachineInputs();
    Recipes.recyclerBlacklist = new ListedMachineInputs();
  }

  public static void initLate() {
    for (var input :
        NativeConfigValues.asRecipeInputList(
            ProjectConfiguration.get(), "balance/recyclerBlacklist"))
      Recipes.recyclerBlacklist.add(input);
    for (var input :
        NativeConfigValues.asRecipeInputList(
            ProjectConfiguration.get(), "balance/recyclerWhitelist"))
      Recipes.recyclerWhitelist.add(input);
  }

  public static int recycleChance() {
    return 8;
  }

  public static boolean getIsItemBlacklisted(ItemStack input) {
    return Recipes.recyclerWhitelist.isEmpty()
        ? Recipes.recyclerBlacklist.contains(input)
        : !Recipes.recyclerWhitelist.contains(input);
  }

  @Override
  public void operateOnce(
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe,
      Collection<ItemStack> output) {
    boolean produce = getLevel().getRandom().nextInt(recycleChance()) == 0;
    if (produce && !outputSlot.canAdd(output))
      throw new IllegalStateException("Recycling output changed before commit");
    inputSlot.consume(recipe);
    if (produce && outputSlot.add(output) != 0)
      throw new IllegalStateException("Recycling output overflow");
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_RECYCLER_OPERATE;
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

  private static final class RecyclerRecipeManager implements IBasicMachineRecipeManager {
    private record Prototype(ItemStack stack, IRecipeInput input) {}

    private final ConcurrentHashMap<Item, Prototype> prototypes = new ConcurrentHashMap<>();

    private IRecipeInput inputFor(ItemStack stack) {
      var prototype = prototypes.get(stack.getItem());
      if (prototype == null || !ItemStack.isSameItemSameComponents(prototype.stack(), stack)) {
        var normalized = stack.copyWithCount(1);
        prototype = new Prototype(normalized, Recipes.inputFactory.forStack(normalized));
        prototypes.put(stack.getItem(), prototype);
      }
      return prototype.input();
    }

    private static Collection<ItemStack> outputFor(ItemStack stack) {
      return getIsItemBlacklisted(stack)
          ? List.of()
          : List.of(new ItemStack(MaterialItems.SCRAP));
    }

    public boolean addRecipe(
        IRecipeInput input, Collection<ItemStack> output, CompoundTag metadata, boolean replace) {
      return false;
    }

    public boolean addRecipe(
        IRecipeInput input, CompoundTag metadata, boolean replace, ItemStack... output) {
      return false;
    }

    @Override
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(
        ItemStack stack, boolean acceptanceOnly) {
      return stack == null || stack.isEmpty()
          ? null
          : new MachineRecipe<>(inputFor(stack), outputFor(stack))
              .getResult(stack.copyWithCount(stack.getCount() - 1));
    }

    @Override
    public RecipeOutput getOutputFor(ItemStack input, boolean adjust) {
      if (input == null || input.isEmpty()) return null;
      var output = new RecipeOutput(null, new ArrayList<>(outputFor(input)));
      if (adjust) input.shrink(1);
      return output;
    }

    @Override
    public boolean isIterable() {
      return false;
    }

    @Override
    public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
      throw new UnsupportedOperationException("Recycling is a dynamic item policy");
    }
  }

  @Override
  protected boolean directRecipeOutputPreview() {
    return getClass() == MaterialRecycler.class;
  }
}
