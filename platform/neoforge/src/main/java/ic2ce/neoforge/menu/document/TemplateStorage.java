package ic2ce.neoforge.menu.document;

import net.minecraft.world.item.ItemStack;

/** Named template storage. Menus copy displayed stacks and charge physical inputs separately. */
@FunctionalInterface
public interface TemplateStorage {
  ItemStack[] getStacksForName(String group);
}
