package ic2ce.neoforge.inventory;

import ic2ce.core.block.IInventorySlotHolder;
import java.util.Objects;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TagInput extends ConsumableInventory {
  protected final TagKey<Item> tag;

  public TagInput(IInventorySlotHolder<?> holder, String name, int count, TagKey<Item> tag) {
    this(holder, name, Access.I, count, InvSide.TOP, tag);
  }

  public TagInput(
      IInventorySlotHolder<?> holder,
      String name,
      Access access,
      int count,
      InvSide side,
      TagKey<Item> tag) {
    super(holder, name, access, count, side);
    this.tag = Objects.requireNonNull(tag);
  }

  @Override
  public boolean accepts(ItemStack stack) {
    return stack != null && !stack.isEmpty() && stack.is(tag);
  }
}
