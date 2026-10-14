package ic2ce.neoforge.processing;

import ic2ce.neoforge.block.NativeBlockEntity;
import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.util.Collection;
import net.minecraft.world.item.ItemStack;

/**
 * Verification-only native access. Production visibility stays narrow after device domains move.
 */
public final class MachineFixtureAccess {
  private MachineFixtureAccess() {}

  private static final ClassValue<MethodHandle> TICKS =
      new ClassValue<>() {
        @Override
        protected MethodHandle computeValue(Class<?> type) {
          for (Class<?> owner = type; owner != null; owner = owner.getSuperclass())
            try {
              var method = owner.getDeclaredMethod("updateEntityServer");
              method.setAccessible(true);
              return MethodHandles.lookup().unreflect(method);
            } catch (NoSuchMethodException missing) {
            } catch (IllegalAccessException denied) {
              throw new AssertionError(denied);
            }
          throw new AssertionError("No native server tick for " + type);
        }
      };

  public static void tick(NativeBlockEntity machine) {
    try {
      TICKS.get(machine.getClass()).invoke(machine);
    } catch (RuntimeException | Error failure) {
      throw failure;
    } catch (Throwable failure) {
      throw new AssertionError(failure);
    }
  }

  public static Number number(Object owner, String name) {
    for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass())
      try {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return (Number) field.get(owner);
      } catch (NoSuchFieldException missing) {
      } catch (IllegalAccessException denied) {
        throw new AssertionError(denied);
      }
    throw new AssertionError("Missing actual native field: " + name);
  }

  public static <RI, RO, I> Collection<ItemStack> output(
      PoweredProcessor<RI, RO, I> machine, RO value) {
    return machine.getOutput(value);
  }
}
