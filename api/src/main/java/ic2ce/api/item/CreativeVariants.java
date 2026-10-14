package ic2ce.api.item;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

/** A project item emits independent physical variants after its native components are bound. */
@FunctionalInterface
public interface CreativeVariants {
  void addCreativeVariants(Consumer<ItemStack> output);
}
