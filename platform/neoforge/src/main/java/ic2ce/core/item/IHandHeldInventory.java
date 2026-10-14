package ic2ce.core.item;

import ic2ce.core.IHasGui;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IHandHeldInventory {
   IHasGui getInventory(Player var1, InteractionHand var2, ItemStack var3);
}
