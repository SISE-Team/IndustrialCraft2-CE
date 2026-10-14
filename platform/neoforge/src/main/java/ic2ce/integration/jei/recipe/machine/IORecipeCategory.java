package ic2ce.integration.jei.recipe.machine;

import ic2ce.integration.jeirei.SlotPosition;
import ic2ce.neoforge.block.MachineBlockType;
import java.util.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Recipe alternatives occupy input slots; distinct physical outputs occupy distinct output slots.
 */
public abstract class IORecipeCategory implements IRecipeCategory<IORecipeWrapper> {
  protected final MachineBlockType block;

  protected IORecipeCategory(MachineBlockType block) {
    this.block = Objects.requireNonNull(block);
  }

  @Override
  public Component getTitle() {
    return getBlockStack().getHoverName();
  }

  @Override
  public abstract int getWidth();

  @Override
  public abstract int getHeight();

  protected abstract List<SlotPosition> getInputSlotPos();

  protected abstract List<SlotPosition> getOutputSlotPos();

  protected void addRecipeSlots(
      IRecipeLayoutBuilder builder, IORecipeWrapper recipe, IFocusGroup focuses, int dx, int dy) {
    var inputs = recipe.getInputs();
    var locations = getInputSlotPos();
    for (int index = 0; index < locations.size(); index++) {
      var position = locations.get(index);
      var slot =
          builder.addSlot(RecipeIngredientRole.INPUT, position.getX() + dx, position.getY() + dy);
      if (index < inputs.size()) slot.addItemStacks(inputs.get(index));
    }
    var outputs = recipe.getOutputs();
    locations = getOutputSlotPos();
    for (int index = 0; index < locations.size(); index++) {
      var position = locations.get(index);
      var slot =
          builder.addSlot(RecipeIngredientRole.OUTPUT, position.getX() + dx, position.getY() + dy);
      if (index < outputs.size()) slot.addItemStacks(List.of(outputs.get(index)));
    }
  }

  public ItemStack getBlockStack() {
    return new ItemStack(block.asItem());
  }

  @Override
  public IDrawable getIcon() {
    return null;
  }
}
