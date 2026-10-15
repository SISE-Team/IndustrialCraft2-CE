package ic2ce.neoforge.runtime;

import ic2ce.neoforge.registry.ItemDefinitions;
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
            case GENERAL -> ItemDefinitions.MACHINE;
            case GENERATORS_AND_WIRING -> ItemDefinitions.GENERATOR;
            case REACTOR -> ItemDefinitions.NUCLEAR_REACTOR;
            case MACHINES -> ItemDefinitions.MACERATOR;
            case TOOLS_AND_UTILITIES -> ItemDefinitions.WRENCH;
            case COMBAT -> ItemDefinitions.NANO_SABER;
            case MATERIALS -> ItemDefinitions.RUBBER;
            case FARMING -> ItemDefinitions.CROP_SEED_BACK;
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
          nano = new ItemStack(ItemDefinitions.NANO_CHESTPLATE);
        }
        return (frame < 3000 ? zombie : frame < 4500 ? skeleton : nano).copy();
      }
    }
    return normal.copy();
  }
}
