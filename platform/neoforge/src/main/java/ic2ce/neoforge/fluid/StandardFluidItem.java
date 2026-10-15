package ic2ce.neoforge.fluid;

import ic2ce.api.fluid.*;
import ic2ce.neoforge.item.StackTag;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.Mutable;

/**
 * Component-backed one-item reservoir reserves bounded real fluid bills; observations and
 * simulations never write tags.
 */
public interface StandardFluidItem extends Ic2FluidItem {
  default boolean canDrain(ItemStack stack) {
    return true;
  }

  default boolean canFill(ItemStack stack, Ic2FluidStack incoming) {
    return true;
  }

  private static void physical(ItemStack stack) {
    if (stack == null || stack.isEmpty() || stack.getCount() != 1)
      throw new IllegalArgumentException("Fluid operations require one physical item");
  }

  @Override
  default Ic2FluidStack getFluidStack(ItemStack stack) {
    var fluid = getFs(stack);
    return fluid == null || fluid.isEmpty() ? Ic2FluidStack.EMPTY : fluid;
  }

  @Override
  default Ic2FluidStack drainMb(
      ItemStack stack, int requested, boolean simulate, Mutable<ItemStack> replacement) {
    if (requested < 0) throw new IllegalArgumentException("Negative fluid drain quantity");
    if (replacement != null) replacement.setValue(stack);
    if (requested == 0) return Ic2FluidStack.EMPTY;
    physical(stack);
    if (!canDrain(stack)) return Ic2FluidStack.EMPTY;
    var stored = getFluidStack(stack);
    if (stored.isEmpty()) return Ic2FluidStack.EMPTY;
    int paid = Math.min(requested, stored.getAmountMb());
    if (paid <= 0) return Ic2FluidStack.EMPTY;
    if (!simulate) {
      int remaining = stored.getAmountMb() - paid;
      setFs(stack, remaining == 0 ? null : stored.copyWithAmountMb(remaining));
      updateDamage(stack, remaining);
    }
    return stored.copyWithAmountMb(paid);
  }

  @Override
  default int drainMb(
      ItemStack stack, Ic2FluidStack requested, boolean simulate, Mutable<ItemStack> replacement) {
    Objects.requireNonNull(requested);
    if (replacement != null) replacement.setValue(stack);
    if (requested.isEmpty()) return 0;
    physical(stack);
    var stored = getFluidStack(stack);
    if (stored.isEmpty() || !stored.hasExactFluid(requested)) return 0;
    return drainMb(stack, requested.getAmountMb(), simulate, replacement).getAmountMb();
  }

  @Override
  default int fillMb(
      ItemStack stack, Ic2FluidStack incoming, boolean simulate, Mutable<ItemStack> replacement) {
    Objects.requireNonNull(incoming);
    if (replacement != null) replacement.setValue(stack);
    if (incoming.isEmpty()) return 0;
    physical(stack);
    if (!canFill(stack, incoming)) return 0;
    int capacity = Math.max(0, getCapacityMb(stack));
    if (capacity == 0) return 0;
    var stored = getFluidStack(stack);
    if (!stored.isEmpty() && !stored.hasExactFluid(incoming)) return 0;
    int present = stored.isEmpty() ? 0 : Math.max(0, stored.getAmountMb());
    int paid = (int) Math.min(incoming.getAmountMb(), Math.max(0, (long) capacity - present));
    if (paid <= 0) return 0;
    if (!simulate) {
      int total = present + paid;
      setFs(stack, (stored.isEmpty() ? incoming : stored).copyWithAmountMb(total));
      updateDamage(stack, total);
    }
    return paid;
  }

  static Ic2FluidStack getFs(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return null;
    var data = StackTag.get(stack);
    return data == null || !data.contains("Fluid")
        ? null
        : Ic2FluidStack.read(data.getCompoundOrEmpty("Fluid"));
  }

  static void setFs(ItemStack stack, Ic2FluidStack fluid) {
    if (stack == null || stack.isEmpty()) throw new IllegalArgumentException("Missing fluid item");
    var previous = StackTag.get(stack);
    if ((fluid == null || fluid.isEmpty()) && (previous == null || !previous.contains("Fluid")))
      return;
    StackTag.update(
        stack,
        data -> {
          if (fluid == null || fluid.isEmpty()) data.remove("Fluid");
          else {
            var encoded = new CompoundTag();
            fluid.toNbt(encoded);
            data.put("Fluid", encoded);
          }
          return data;
        });
  }

  default void updateDamage(ItemStack stack, int amount) {
    int maximum = stack.getMaxDamage(), capacity = getCapacityMb(stack);
    if (stack.isDamageableItem() && maximum > 2 && capacity > 0) {
      long bounded = Math.clamp(amount, 0, capacity);
      stack.setDamageValue(maximum - 1 - (int) (bounded * (maximum - 2) / capacity));
    }
  }
}
