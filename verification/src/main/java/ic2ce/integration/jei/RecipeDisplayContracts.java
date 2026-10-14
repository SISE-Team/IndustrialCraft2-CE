package ic2ce.integration.jei;

import ic2ce.integration.jei.recipe.machine.*;
import ic2ce.neoforge.recipe.input.ExactStackInput;
import java.util.*;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;

/**
 * Real JEI runtime constructs every registered machine category and verifies simultaneous output
 * slots and input bill ownership.
 */
public final class RecipeDisplayContracts {
  public record Result(int checks, List<String> failures) {}

  public static boolean ready() {
    return Ic2JeiPlugin.runtimeSnapshot() != null;
  }

  public static Result run() {
    int checks = 0;
    var failures = new ArrayList<String>();
    var runtime = Objects.requireNonNull(Ic2JeiPlugin.runtimeSnapshot());
    var manager = runtime.getRecipeManager();
    var focus = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();
    var output = new ItemStack(Items.IRON_INGOT, 2);
    var wrapper =
        new IORecipeWrapper(new ExactStackInput(new ItemStack(Items.RAW_IRON, 3)), List.of(output));
    output.setCount(64);
    checks++;
    if (wrapper.getInputs().getFirst().getFirst().getCount() != 3
        || wrapper.getOutputs().getFirst().getCount() != 2)
      failures.add(
          "JEI: declared item bill or borrowed output prototype changed native presentation"
              + " quantity");
    wrapper.getOutputs().getFirst().setCount(17);
    checks++;
    if (wrapper.getOutputs().getFirst().getCount() != 2)
      failures.add("JEI: exported item mutation edits the owned recipe result");
    var categories =
        manager
            .createRecipeCategoryLookup()
            .includeHidden()
            .get()
            .filter(category -> category.getRecipeType().getUid().getNamespace().equals("ic2"))
            .toList();
    checks++;
    if (categories.size() != 10)
      failures.add("JEI: actual registered machine category count=" + categories.size());
    for (var category : categories) {
      checks++;
      if (category.getWidth() <= 0
          || category.getHeight() <= 0
          || category.getWidth() > 4096
          || category.getHeight() > 4096)
        failures.add("JEI: invalid native category geometry " + category.getRecipeType().getUid());
    }
    var type =
        manager
            .getRecipeType(
                Identifier.fromNamespaceAndPath("ic2", "ore_washer"), IORecipeWrapper.class)
            .orElseThrow();
    var category = manager.getRecipeCategory(type);
    var multiple =
        new IORecipeWrapper(
            new ExactStackInput(new ItemStack(Items.RAW_IRON, 3)),
            List.of(
                new ItemStack(Items.IRON_INGOT, 2),
                new ItemStack(Items.COPPER_INGOT),
                new ItemStack(Items.GOLD_NUGGET, 3)));
    var layout = manager.createRecipeLayoutDrawable(category, multiple, focus).orElseThrow();
    var slots = layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT);
    checks++;
    if (slots.size() != 3)
      failures.add("JEI: actual ore washer layout output slot count=" + slots.size());
    var expected = List.of(Items.IRON_INGOT, Items.COPPER_INGOT, Items.GOLD_NUGGET);
    for (int index = 0; index < Math.min(3, slots.size()); index++) {
      var values = slots.get(index).getItemStacks().toList();
      checks++;
      if (values.size() != 1 || !values.getFirst().is(expected.get(index)))
        failures.add(
            "JEI: simultaneous native output occupies the wrong physical recipe slot " + index);
    }
    checks++;
    if (!ic2ce.neoforge.client.widget.RecipeOverlay.canUse())
      failures.add("JEI: available native runtime did not bind actual machine recipe buttons");

    var client = net.minecraft.client.Minecraft.getInstance();
    var previousScreen = client.screen;
    var previousMenu = client.player.containerMenu;
    try {
      var open =
          ic2ce.neoforge.client.widget.RecipeOverlay.jeiRecipeListOpener.apply(
              new String[] {"ore_washing_plant"});
      open.onClick(ic2ce.neoforge.client.widget.PointerButton.left);
      checks++;
      if (client.screen == previousScreen
          || !ic2ce.neoforge.crafting.client.WorkbenchScreen.jeiScreenRecipesGuiCheck.test(
              client.screen))
        failures.add("JEI: actual production machine button did not open the native recipe screen");
      checks++;
      if (client.player.containerMenu != previousMenu)
        failures.add("JEI: recipe presentation changed the live physical machine menu");
    } finally {
      client.setScreen(previousScreen);
    }
    return new Result(checks, List.copyOf(failures));
  }
}
