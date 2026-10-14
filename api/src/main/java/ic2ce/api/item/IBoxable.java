package ic2ce.api.item;

import net.minecraft.world.item.ItemStack;

public interface IBoxable {
   boolean canBeStoredInToolbox(ItemStack var1);
}
