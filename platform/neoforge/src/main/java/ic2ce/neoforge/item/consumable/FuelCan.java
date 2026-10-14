package ic2ce.neoforge.item.consumable;

import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;

/**
 * Fuel value follows immutable item-component generations; each use leaves one physical empty can.
 */
public final class FuelCan extends Item {
  private record Value(CustomData component, int ticks) {}

  private static final ThreadLocal<Value> lastValue = new ThreadLocal<>();
  private volatile ItemStackTemplate remainder;

  public FuelCan(Properties properties) {
    super(properties);
  }

  @Override
  public int getBurnTime(ItemStack stack, RecipeType<?> recipe, FuelValues values) {
    if (stack == null || stack.isEmpty() || !stack.is(this)) return 0;
    var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    var known = lastValue.get();
    if (known != null && known.component() == data) return known.ticks();
    int ticks = Math.max(0, data.copyTag().getIntOr("value", 0));
    lastValue.set(new Value(data, ticks));
    return ticks;
  }

  @Override
  public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
    if (instance == null || !instance.is(this)) return null;
    var result = remainder;
    if (result == null) {
      synchronized (this) {
        result = remainder;
        if (result == null)
          remainder = result = new ItemStackTemplate(ItemDefinitions.EMPTY_FUEL_CAN);
      }
    }
    return result;
  }
}
