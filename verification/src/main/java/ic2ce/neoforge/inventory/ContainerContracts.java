package ic2ce.neoforge.inventory;

import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Whole native-container quantity semantics and dynamic group boundaries. */
public final class ContainerContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native containers: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ContainerContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var block = (MachineBlockType) BlockDefinitions.IRON_STORAGE_BOX;
    var box =
        (InventoryBlockEntity)
            block.createBlockEntity(new BlockPos(8350, 100, 8350), block.defaultBlockState());
    box.setLevel(world);
    box.setItem(0, new ItemStack(Items.IRON_INGOT, 64));
    check(
        box.removeItem(0, -10).isEmpty() && box.getItem(0).getCount() == 64,
        "negative removal cannot reverse insertion");
    check(
        box.removeItem(0, 0).isEmpty() && box.getItem(0).getCount() == 64, "zero removal is inert");
    var taken = box.removeItem(0, 10);
    check(
        taken.getCount() == 10 && box.getItem(0).getCount() == 54,
        "partial extraction conserves quantity");
    taken.setCount(1);
    check(
        box.getItem(0).getCount() == 54, "returned extraction does not alias the remaining stack");
    check(
        box.removeItem(-1, 1).isEmpty() && box.getItem(-1).isEmpty(),
        "invalid external indices are safe empty reads");
    check(
        box.removeItemNoUpdate(0).getCount() == 54 && box.getItem(0).isEmpty(),
        "whole extraction returns exactly the old contents");
    int previous = box.getContainerSize();
    var group = new InventorySlot(box, "contract-group", InventorySlot.Access.IO, 2);
    check(
        box.getContainerSize() == previous + 2
            && box.getBaseIndex(group) == previous
            && box.getInventorySlot("contract-group") == group,
        "late native groups extend indices without changing earlier groups");
    group.put(new ItemStack(Items.COAL, 20));
    check(box.getItem(previous).getCount() == 20, "flattened lookup resolves the appended group");
    boolean rejected = false;
    try {
      new InventorySlot(box, "contract-group", InventorySlot.Access.IO, 1);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(
        rejected && box.getContainerSize() == previous + 2,
        "duplicate group names cannot silently replace another inventory");
    return new Result(checks, List.copyOf(failures));
  }
}
