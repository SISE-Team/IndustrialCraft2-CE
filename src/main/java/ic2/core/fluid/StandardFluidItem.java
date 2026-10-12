package ic2.core.fluid;

import ic2.core.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.Mutable;

public interface StandardFluidItem extends Ic2FluidItem {
   default boolean canDrain(ItemStack stack) {
      return true;
   }

   default boolean canFill(ItemStack stack, Ic2FluidStack fs) {
      return true;
   }

   @Override
   default Ic2FluidStack getFluidStack(ItemStack stack) {
      Ic2FluidStack fs = getFs(stack);
      return fs != null && !fs.isEmpty() ? fs : Ic2FluidStack.EMPTY;
   }

   @Override
   default Ic2FluidStack drainMb(ItemStack stack, int amount, boolean simulate, Mutable<ItemStack> newStack) {
      if (newStack != null) {
         newStack.setValue(stack);
      }

      if (amount < 0) {
         throw new IllegalArgumentException("negative amount");
      } else if (amount == 0) {
         return Ic2FluidStack.EMPTY;
      } else if (stack.getCount() != 1) {
         throw new IllegalArgumentException("invalid stack size");
      } else if (!this.canDrain(stack)) {
         return Ic2FluidStack.EMPTY;
      } else {
         Ic2FluidStack fs = getFs(stack);
         if (fs != null && !fs.isEmpty()) {
            if (fs.getAmountMb() <= amount) {
               amount = fs.getAmountMb();
               if (!simulate) {
                  setFs(stack, null);
                  this.updateDamage(stack, 0);
               }
            } else {
               if (!simulate) {
                  fs.decreaseMb(amount);
                  setFs(stack, fs);
                  this.updateDamage(stack, fs.getAmountMb());
               }

               fs.setAmountMb(amount);
            }

            if (newStack != null) {
               newStack.setValue(stack);
            }

            return fs;
         } else {
            return Ic2FluidStack.EMPTY;
         }
      }
   }

   @Override
   default int drainMb(ItemStack stack, Ic2FluidStack drainFs, boolean simulate, Mutable<ItemStack> newStack) {
      if (newStack != null) {
         newStack.setValue(stack);
      }

      if (drainFs == null) {
         throw new IllegalArgumentException("invalid drain medium");
      } else if (drainFs.isEmpty()) {
         return 0;
      } else if (stack.getCount() != 1) {
         throw new IllegalArgumentException("invalid stack size");
      } else if (!this.canDrain(stack)) {
         return 0;
      } else {
         Ic2FluidStack fs = getFs(stack);
         if (fs != null && !fs.isEmpty() && fs.hasExactFluid(drainFs)) {
            int amount = drainFs.getAmountMb();
            if (fs.getAmountMb() <= amount) {
               amount = fs.getAmountMb();
               if (!simulate) {
                  setFs(stack, null);
                  this.updateDamage(stack, 0);
               }
            } else if (!simulate) {
               fs.decreaseMb(amount);
               setFs(stack, fs);
               this.updateDamage(stack, fs.getAmountMb());
            }

            if (newStack != null) {
               newStack.setValue(stack);
            }

            return amount;
         } else {
            return 0;
         }
      }
   }

   @Override
   default int fillMb(ItemStack stack, Ic2FluidStack fillFs, boolean simulate, Mutable<ItemStack> newStack) {
      if (newStack != null) {
         newStack.setValue(stack);
      }

      if (fillFs == null) {
         throw new IllegalArgumentException("invalid fill medium");
      } else if (fillFs.isEmpty()) {
         return 0;
      } else if (stack.getCount() != 1) {
         throw new IllegalArgumentException("invalid stack size");
      } else if (!this.canFill(stack, fillFs)) {
         return 0;
      } else {
         int capacity = this.getCapacityMb(stack);
         if (capacity <= 0) {
            return 0;
         } else {
            int amount = fillFs.getAmountMb();
            Ic2FluidStack fs = getFs(stack);
            if (fs == null || fs.isEmpty() && !fs.hasExactFluid(fillFs)) {
               amount = Math.min(amount, capacity);
               if (!simulate) {
                  fs = fillFs.copyWithAmountMb(amount);
                  setFs(stack, fs);
                  this.updateDamage(stack, amount);
               }
            } else {
               if (!fs.hasExactFluid(fillFs)) {
                  return 0;
               }

               amount = Math.min(amount, capacity - fs.getAmountMb());
               if (!simulate) {
                  fs.increaseMb(amount);
                  setFs(stack, fs);
                  this.updateDamage(stack, fs.getAmountMb());
               }
            }

            if (newStack != null) {
               newStack.setValue(stack);
            }

            return amount;
         }
      }
   }

   static Ic2FluidStack getFs(ItemStack stack) {
      CompoundTag nbt = ic2.core.util.StackTag.get(stack);
      if (nbt == null) {
         return null;
      } else {
         return !nbt.contains("Fluid") ? null : Ic2FluidStack.read(nbt.getCompoundOrEmpty("Fluid"));
      }
   }

   static void setFs(ItemStack stack, Ic2FluidStack fs) {
      CompoundTag nbt = ic2.core.util.StackTag.get(stack);
      if (nbt == null) {
         nbt = new CompoundTag();
         ic2.core.util.StackTag.set(stack, nbt);
      }

      if (fs != null && !fs.isEmpty()) {
         CompoundTag fsNbt = new CompoundTag();
         fs.toNbt(fsNbt);
         nbt.put("Fluid", fsNbt);
         ic2.core.util.StackTag.set(stack, nbt);
      } else {
         nbt.remove("Fluid");
         ic2.core.util.StackTag.set(stack, nbt);
      }
   }

   default void updateDamage(ItemStack stack, int amount) {
      if (stack.isDamageableItem()) {
         int maxDmg = stack.getMaxDamage();
         if (maxDmg > 2) {
            stack.setDamageValue(maxDmg - 1 - (int)Util.map(amount, this.getCapacityMb(stack), maxDmg - 2));
         }
      }
   }
}
