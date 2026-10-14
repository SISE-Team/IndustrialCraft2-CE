package ic2ce.api.reactor;

import net.minecraft.world.item.ItemStack;

public interface IBaseReactorComponent {
   boolean canBePlacedIn(ItemStack var1, IReactor var2);
}
