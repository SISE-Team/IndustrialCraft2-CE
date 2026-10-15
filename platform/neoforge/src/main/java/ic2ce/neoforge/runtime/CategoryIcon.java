package ic2ce.neoforge.runtime;

import ic2ce.neoforge.agriculture.AgricultureItems;
import ic2ce.neoforge.item.armor.ArmorItems;
import ic2ce.neoforge.item.tool.ToolItems;
import ic2ce.neoforge.material.MaterialItems;
import ic2ce.neoforge.power.PowerItems;
import ic2ce.neoforge.processing.ProcessingItems;
import ic2ce.neoforge.reactor.ReactorItems;
import ic2ce.neoforge.world.WorldItems;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

/**
 * Cached icon prototypes preserve separate creative categories; every borrowed result is an owned
 * stack.
 */
public final class CategoryIcon implements Supplier<ItemStack> {
  private final CreativeCategory category;
  private ItemStack normal, zombie, skeleton, nano;
  private int frame;

  public CategoryIcon(CreativeCategory category) {
    this.category = Objects.requireNonNull(category);
  }

  @Override
  public ItemStack get() {
    if (normal == null) {
      var item =
          switch (category) {
            case GENERAL -> WorldItems.MACHINE;
            case GENERATORS_AND_WIRING -> PowerItems.GENERATOR;
            case REACTOR -> ReactorItems.NUCLEAR_REACTOR;
            case MACHINES -> ProcessingItems.MACERATOR;
            case TOOLS_AND_UTILITIES -> ToolItems.WRENCH;
            case COMBAT -> ToolItems.NANO_SABER;
            case MATERIALS -> MaterialItems.RUBBER;
            case FARMING -> AgricultureItems.CROP_SEED_BACK;
          };
      normal = new ItemStack(item);
      if (category == CreativeCategory.COMBAT)
        normal.set(DataComponents.CUSTOM_NAME, Component.literal("ic2:tab_icon"));
    }
    if (category == CreativeCategory.GENERAL) {
      frame = (frame + 1) % 5000;
      if (frame >= 2500) {
        if (zombie == null) {
          zombie = new ItemStack(Items.ZOMBIE_HEAD);
          skeleton = new ItemStack(Items.SKELETON_SKULL);
          nano = new ItemStack(ArmorItems.NANO_CHESTPLATE);
        }
        return (frame < 3000 ? zombie : frame < 4500 ? skeleton : nano).copy();
      }
    }
    return normal.copy();
  }
}
