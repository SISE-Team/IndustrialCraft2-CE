package ic2ce.neoforge.crafting.menu;


import ic2ce.api.block.container.Ic2CraftingResultSlot;
import ic2ce.engine.inventory.FillLevels;
import ic2ce.neoforge.crafting.CraftingGridView;
import ic2ce.neoforge.crafting.CraftingMenus;
import ic2ce.neoforge.crafting.IndustrialWorkbench;
import ic2ce.neoforge.menu.PhysicalInventoryMenu;
import ic2ce.neoforge.menu.slot.MachineSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Three actual result stations pay a single native recipe bill; further shifts use native menu
 * repetition.
 */
public class WorkbenchMenu extends PhysicalInventoryMenu<IndustrialWorkbench> {
  public static final int WIDTH = 194, HEIGHT = 228;
  public final Player player;
  public final int indexOutput,
      indexGridStart,
      indexGridEnd,
      indexBufferStart,
      indexBufferEnd,
      indexOutputHammer,
      indexOutputCutter;
  protected final CraftingContainer craftMatrix;
  protected final Container craftResult = new ResultContainer();
  protected final Slot[] outputs = new Slot[3];
  private boolean taking;

  public WorkbenchMenu(int id, Inventory inventory, IndustrialWorkbench machine) {
    super(CraftingMenus.INDUSTRIAL_WORKBENCH, id, inventory, machine, HEIGHT);
    player = inventory.player;
    craftMatrix =
        new CraftingGridView.InventoryGrid(machine.craftingGrid, 3) {
          @Override
          protected void set(int index, ItemStack stack) {
            super.set(index, stack);
            slotsChanged(this);
          }

          @Override
          public ItemStack removeItem(int index, int count) {
            var removed = super.removeItem(index, count);
            slotsChanged(this);
            return removed;
          }
        };
    indexOutput = slots.size();
    outputs[0] = addSlot(new Ic2CraftingResultSlot(player, craftMatrix, craftResult, 0, 124, 61));
    indexGridStart = slots.size();
    for (int cell = 0; cell < 9; cell++)
      addSlot(
          new MachineSlot(machine.craftingGrid, cell, 30 + (cell % 3) * 18, 43 + (cell / 3) * 18) {
            @Override
            public void setChanged() {
              super.setChanged();
              WorkbenchMenu.this.slotsChanged(craftMatrix);
            }
          });
    indexGridEnd = slots.size();
    indexBufferStart = slots.size();
    for (int cell = 0; cell < machine.craftingStorage.size(); cell++)
      cell(machine.craftingStorage, cell, 8 + (cell % 9) * 18, 106 + (cell / 9) * 18);
    indexBufferEnd = slots.size();
    cell(machine.leftCrafting.tool, 0, 7, 17);
    cell(machine.leftCrafting.input, 0, 25, 17);
    indexOutputHammer = slots.size();
    outputs[1] =
        addSlot(
            new Ic2CraftingResultSlot(
                player, machine.leftCrafting.crafting, machine.leftCrafting.resultInv, 0, 69, 17));
    cell(machine.rightCrafting.tool, 0, 91, 17);
    cell(machine.rightCrafting.input, 0, 109, 17);
    indexOutputCutter = slots.size();
    outputs[2] =
        addSlot(
            new Ic2CraftingResultSlot(
                player,
                machine.rightCrafting.crafting,
                machine.rightCrafting.resultInv,
                0,
                153,
                17));
    slotsChanged(craftMatrix);
  }

  @Override
  public void onContainerEvent(String event) {
    if (player.level().isClientSide() || taking) return;
    if ("craft".equals(event)) base.rebalance();
    else if ("clear".equals(event)) base.clear(player);
    else return;
    slotsChanged(craftMatrix);
    broadcastChanges();
  }

  @Override
  public void slotsChanged(Container inventory) {
    var world = base.getLevel();
    if (world == null || world.getServer() == null) return;
    var input = craftMatrix.asCraftInput();
    var recipe =
        world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, world);
    craftResult.setItem(
        0, recipe.map(holder -> holder.value().assemble(input).copy()).orElse(ItemStack.EMPTY));
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack stack, Slot candidate) {
    for (var output : outputs) if (candidate.container == output.container) return false;
    return super.canTakeItemForPickAll(stack, candidate);
  }

  @Override
  protected ItemStack handlePlayerSlotShiftClick(Player actor, ItemStack offered) {
    if (actor.level().isClientSide()) return offered;
    var remainder = offered.copy();
    int[] present = new int[9], capacity = new int[9];
    for (int cell = 0; cell < 9; cell++) {
      var current = base.craftingGrid.get(cell);
      if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, remainder)) {
        present[cell] = current.getCount();
        capacity[cell] =
            Math.min(remainder.getMaxStackSize(), base.craftingGrid.getStackSizeLimit());
      }
    }
    var plan = FillLevels.plan(present, capacity, remainder.getCount());
    int accepted = 0;
    for (int cell = 0; cell < 9; cell++) {
      int amount = plan[cell] - present[cell];
      if (amount > 0) {
        base.craftingGrid.put(cell, remainder.copyWithCount(plan[cell]));
        accepted += amount;
      }
    }
    if (accepted > 0) {
      remainder.shrink(accepted);
      slotsChanged(craftMatrix);
    }
    return super.handlePlayerSlotShiftClick(actor, remainder);
  }

  @Override
  protected ItemStack handleGUISlotShiftClick(Player actor, ItemStack offered) {
    if (actor.level().isClientSide() || taking) return offered;
    Slot result = null;
    for (var output : outputs)
      if (output.getItem() == offered) {
        result = output;
        break;
      }
    boolean buffer = false;
    for (int cell = indexBufferStart; cell < indexBufferEnd; cell++)
      if (slots.get(cell).getItem() == offered) {
        buffer = true;
        break;
      }
    var original = offered.copy();
    var remainder = offered.copy();
    for (int pass = 0; pass < 2 && !remainder.isEmpty(); pass++)
      for (int cell = slots.size() - 1; cell >= 0 && !remainder.isEmpty(); cell--) {
        var target = slots.get(cell);
        if ((target.container == actor.getInventory()
                || !buffer && cell >= indexBufferStart && cell < indexBufferEnd)
            && isValidTargetSlot(target, remainder, pass == 1, false))
          remainder = transfer(remainder, target);
      }
    if (result != null && remainder.getCount() < original.getCount()) {
      taking = true;
      try {
        result.set(ItemStack.EMPTY);
        result.onQuickCraft(remainder, original);
        result.onTake(actor, original);
        if (!remainder.isEmpty()) {
          var leftover = remainder.copy();
          if (!actor.getInventory().add(leftover)) actor.drop(leftover, false);
        }
      } finally {
        taking = false;
      }
      base.rebalance();
      slotsChanged(craftMatrix);
    }
    return remainder;
  }
}
