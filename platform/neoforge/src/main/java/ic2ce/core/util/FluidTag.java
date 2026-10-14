package ic2ce.core.util;

import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;

/** Fluid custom data is always an owned copy and empty updates remove the inert component. */
public final class FluidTag {
  private FluidTag() {}

  public static boolean has(FluidStack stack) {
    return stack != null && stack.has(DataComponents.CUSTOM_DATA);
  }

  public static CompoundTag get(FluidStack stack) {
    if (stack == null) return new CompoundTag();
    var value = stack.get(DataComponents.CUSTOM_DATA);
    return value == null ? new CompoundTag() : value.copyTag();
  }

  public static CompoundTag getOrCreate(FluidStack stack) {
    return get(stack);
  }

  public static void set(FluidStack stack, CompoundTag data) {
    Objects.requireNonNull(stack);
    if (data == null || data.isEmpty()) {
      stack.remove(DataComponents.CUSTOM_DATA);
      return;
    }
    var previous = stack.get(DataComponents.CUSTOM_DATA);
    if (previous != null && previous.copyTag().equals(data)) return;
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data.copy()));
  }

  public static void update(FluidStack stack, UnaryOperator<CompoundTag> operation) {
    set(stack, Objects.requireNonNull(operation).apply(get(stack)));
  }
}
