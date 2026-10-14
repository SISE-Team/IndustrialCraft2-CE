package ic2ce.neoforge.inventory;

import ic2ce.neoforge.block.InventoryBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class BlockInput extends ConsumableInventory {
  public BlockInput(InventoryBlockEntity holder, String name, int count) {
    this(holder, name, Access.I, count, InvSide.TOP);
  }

  public BlockInput(
      InventoryBlockEntity holder, String name, Access access, int count, InvSide side) {
    super(holder, name, access, count, side);
  }

  @Override
  public boolean accepts(ItemStack stack) {
    return stack != null && !stack.isEmpty() && stack.getItem() instanceof BlockItem;
  }
}
