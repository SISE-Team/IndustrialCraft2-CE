package ic2ce.api.fluid;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Fluid quantity contract; all capability and serialization access is injected. */
public interface Ic2FluidStack {
  int BUCKET_MB = 1000;
  Ic2FluidStack EMPTY = new Empty();

  static Ic2FluidStack create(Fluid fluid, int amount) {
    Objects.requireNonNull(fluid);
    if (amount < 0) throw new IllegalArgumentException("Negative fluid quantity");
    return FluidStackAccess.platform().createFluidStackMb(fluid, amount, null);
  }

  static Ic2FluidStack get(ItemStack stack) {
    Objects.requireNonNull(stack);
    return stack.getItem() instanceof Ic2FluidItem item
        ? item.getFluidStack(stack)
        : FluidStackAccess.platform().getFluidStack(stack);
  }

  static Ic2FluidStack[] getAll(ItemStack stack) {
    Objects.requireNonNull(stack);
    if (stack.getItem() instanceof Ic2FluidItem item) {
      Ic2FluidStack content = item.getFluidStack(stack);
      return content == null || content.isEmpty() ? null : new Ic2FluidStack[] {content};
    }
    return FluidStackAccess.platform().getFluidStacks(stack);
  }

  static Ic2FluidStack read(CompoundTag data) {
    return FluidStackAccess.platform().readFluidStack(Objects.requireNonNull(data));
  }

  Ic2FluidStack copy();

  Fluid getFluid();

  boolean hasExactFluid(Fluid fluid);

  boolean hasExactFluid(Ic2FluidStack stack);

  int getAmountMb();

  void setAmountMb(int amount);

  void toNbt(CompoundTag data);

  default boolean isEmpty() {
    return getAmountMb() == 0;
  }

  default Ic2FluidStack copyWithAmountMb(int amount) {
    Ic2FluidStack copy = copy();
    copy.setAmountMb(amount);
    return copy;
  }

  default void increaseMb(int amount) {
    if (amount < 0) throw new IllegalArgumentException("Negative increase");
    setAmountMb(Math.addExact(getAmountMb(), amount));
  }

  default void decreaseMb(int amount) {
    if (amount < 0) throw new IllegalArgumentException("Negative decrease");
    setAmountMb(Math.subtractExact(getAmountMb(), amount));
  }

  final class Empty implements Ic2FluidStack {
    private Empty() {}

    public Ic2FluidStack copy() {
      return Ic2FluidStack.create(Fluids.EMPTY, 0);
    }

    public Fluid getFluid() {
      return Fluids.EMPTY;
    }

    public boolean hasExactFluid(Fluid fluid) {
      return fluid == Fluids.EMPTY;
    }

    public boolean hasExactFluid(Ic2FluidStack stack) {
      return stack != null && stack.hasExactFluid(Fluids.EMPTY);
    }

    public int getAmountMb() {
      return 0;
    }

    public void setAmountMb(int amount) {
      if (amount != 0) throw new IllegalArgumentException("Global empty fluid is immutable");
    }

    public void toNbt(CompoundTag data) {
      data.putString("FluidName", "minecraft:empty");
      data.putInt("Amount", 0);
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof Ic2FluidStack stack
          && stack.getAmountMb() == 0
          && stack.hasExactFluid(Fluids.EMPTY);
    }

    @Override
    public int hashCode() {
      return Fluids.EMPTY.hashCode();
    }
  }
}
