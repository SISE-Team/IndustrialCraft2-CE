package ic2ce.neoforge.crafting.menu;

import ic2ce.engine.inventory.FillLevels;
import ic2ce.neoforge.crafting.AutomaticCrafter;
import ic2ce.neoforge.crafting.CraftingMenus;
import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.menu.slot.TemplateSlot;
import ic2ce.neoforge.processing.menu.PoweredMenu;
import java.util.List;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;

/** Template-only grid and nine independently paid ingredient queues share no physical stacks. */
public class AutomaticCraftingMenu extends PoweredMenu<AutomaticCrafter> {
  public static final int HEIGHT = 206;

  public AutomaticCraftingMenu(int id, Inventory inventory, AutomaticCrafter machine) {
    super(CraftingMenus.BATCH_CRAFTER, id, inventory, machine, HEIGHT, 8, 62);
    for (int cell = 0; cell < 9; cell++)
      addSlot(
          new TemplateSlot(
              machine.craftingGrid,
              cell,
              30 + (cell % 3) * 18,
              17 + (cell / 3) * 18,
              1,
              index -> {
                if (machine.hasLevel() && !machine.getLevel().isClientSide())
                  machine.matrixChange(index);
              }));
    cell(machine.craftingOutput, 0, 124, 35);
    for (int queue = 0; queue < 9; queue++) {
      cell(machine.ingredientsRow[queue], 0, 8 + queue * 18, 84);
      cell(machine.containerOutput, queue, 8 + queue * 18, 102);
    }
    strip(machine.upgradeSlot, 0, machine.upgradeSlot.size(), 152, 8, 0, 18);
  }

  @Override
  protected ItemStack handlePlayerSlotShiftClick(Player actor, ItemStack offered) {
    if (actor.level().isClientSide()) return offered;
    int[] present = new int[9], capacity = new int[9];
    for (int queue = 0; queue < 9; queue++) {
      var storage = base.ingredientsRow[queue];
      var current = storage.get();
      if (storage.accepts(offered)
          && (current.isEmpty() || ItemStack.isSameItemSameComponents(current, offered))) {
        present[queue] = current.getCount();
        capacity[queue] = Math.min(offered.getMaxStackSize(), storage.getStackSizeLimit());
      }
    }
    var plan = FillLevels.plan(present, capacity, offered.getCount());
    int paid = 0;
    for (int queue = 0; queue < 9; queue++) {
      int amount = plan[queue] - present[queue];
      if (amount > 0) {
        base.ingredientsRow[queue].put(offered.copyWithCount(plan[queue]));
        paid += amount;
      }
    }
    return offered.copyWithCount(offered.getCount() - paid);
  }

  @Override
  public List<String> getNetworkedFields() {
    return MenuFields.extend(super.getNetworkedFields(), "guiProgress", "recipeOutput");
  }
}
