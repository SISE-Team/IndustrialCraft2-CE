package ic2.forge;

import ic2.core.fluid.Ic2FluidStack;
import java.util.Objects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

final class Ic2FluidStackImpl implements Ic2FluidStack {
   final FluidStack parent;

   public Ic2FluidStackImpl(FluidStack parent) {
      if (parent == null) {
         throw new NullPointerException();
      } else {
         this.parent = parent;
      }
   }

   @Override
   public Ic2FluidStack copy() {
      return ic2.core.util.FluidTag.has(this.parent)
         ? new Ic2FluidStackImpl(this.parent.copy())
         : Ic2FluidStack.create(this.parent.getFluid(), this.parent.getAmount());
   }

   @Override
   public Fluid getFluid() {
      return this.parent.getFluid();
   }

   @Override
   public boolean hasExactFluid(Fluid fluid) {
      return !ic2.core.util.FluidTag.has(this.parent) && fluid == this.parent.getFluid();
   }

   @Override
   public boolean hasExactFluid(Ic2FluidStack fs) {
      return fs instanceof Ic2FluidStackImpl impl
         ? FluidStack.isSameFluidSameComponents(this.parent, impl.parent)
         : this.hasExactFluid(fs.getFluid());
   }

   @Override
   public int getAmountMb() {
      return this.parent.getAmount();
   }

   @Override
   public void setAmountMb(int amount) {
      if (amount < 0) {
         throw new IllegalArgumentException();
      } else {
         this.parent.setAmount(amount);
      }
   }

   @Override
   public void toNbt(CompoundTag nbt) {
      Fluid fluid = this.parent.getFluid();
      if (fluid != null) {
         nbt.putString("FluidName", BuiltInRegistries.FLUID.getKey(fluid).toString());
         nbt.putInt("Amount", this.parent.getAmount());
         CompoundTag tag = ic2.core.util.FluidTag.get(this.parent);
         if (!tag.isEmpty()) {
            nbt.put("Tag", tag);
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof Ic2FluidStack o)) {
         return false;
      } else if (this.parent.getAmount() != o.getAmountMb() || this.parent.getFluid() != o.getFluid()) {
         return false;
      } else {
         return o instanceof Ic2FluidStackImpl impl
            ? Objects.equals(ic2.core.util.FluidTag.get(this.parent), ic2.core.util.FluidTag.get(impl.parent))
            : !ic2.core.util.FluidTag.has(this.parent);
      }
   }

   @Override
   public int hashCode() {
      return this.parent.getFluid().hashCode() ^ this.parent.getAmount();
   }

   @Override
   public String toString() {
      Fluid fluid = this.parent.getFluid();
      return String.format(
         "%dx%s@%s",
         this.parent.getAmount(),
         fluid != null ? BuiltInRegistries.FLUID.getKey(fluid) : "(null)",
         ic2.core.util.FluidTag.has(this.parent) ? ic2.core.util.FluidTag.get(this.parent).toString() : "(-)"
      );
   }
}
