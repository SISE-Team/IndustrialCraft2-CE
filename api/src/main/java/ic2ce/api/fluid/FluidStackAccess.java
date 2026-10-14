package ic2ce.api.fluid;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/** Loader supplies fluid capability access without an API-to-core dependency. */
public final class FluidStackAccess {
  public interface Access {
    Ic2FluidStack createFluidStackMb(Fluid fluid, int amount, CompoundTag data);

    Ic2FluidStack getFluidStack(ItemStack stack);

    Ic2FluidStack[] getFluidStacks(ItemStack stack);

    Ic2FluidStack readFluidStack(CompoundTag data);
  }

  private static volatile Access platform;

  private FluidStackAccess() {}

  public static synchronized void install(Access implementation) {
    Objects.requireNonNull(implementation);
    if (platform != null && platform != implementation)
      throw new IllegalStateException("Fluid platform already installed");
    platform = implementation;
  }

  static Access platform() {
    Access current = platform;
    if (current == null)
      throw new IllegalStateException("IndustrialCraft2-CE fluid platform is not initialized");
    return current;
  }
}
