package ic2ce.neoforge.crafting;

import com.mojang.authlib.GameProfile;
import ic2ce.neoforge.crafting.menu.*;
import ic2ce.neoforge.menu.MenuFields;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Actual native three-station recipe payment, component boundaries and nine queue admission. */
public final class CraftingMenuContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native crafting menu: " + label);
  }

  private static int count(Player owner, Item item) {
    int result = 0;
    for (int cell = 0; cell < owner.getInventory().getContainerSize(); cell++) {
      var stack = owner.getInventory().getItem(cell);
      if (stack.is(item)) result += stack.getCount();
    }
    return result;
  }

  private static int stored(IndustrialWorkbench bench, Item item) {
    int result = 0;
    for (int cell = 0; cell < bench.craftingStorage.size(); cell++) {
      var stack = bench.craftingStorage.get(cell);
      if (stack.is(item)) result += stack.getCount();
    }
    return result;
  }

  public static Result run(ServerLevel world) {
    return new CraftingMenuContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var owner =
        FakePlayerFactory.get(
            world,
            new GameProfile(
                UUID.fromString("2c34c405-c01e-4393-aaf9-edc587c083d9"), "CraftMenuProbe"));
    owner.getInventory().clearContent();
    owner.getAbilities().instabuild = false;
    var pos = new BlockPos(1580, 110, 1320);
    world.setBlockAndUpdate(pos, BlockDefinitions.INDUSTRIAL_WORKBENCH.defaultBlockState());
    var bench = (IndustrialWorkbench) world.getBlockEntity(pos);
    var menu = new WorkbenchMenu(196, owner.getInventory(), bench);
    bench.craftingGrid.put(0, new ItemStack(Items.OAK_LOG, 3));
    menu.slotsChanged(bench);
    check(
        menu.slots.get(menu.indexOutput).getItem().is(Items.OAK_PLANKS)
            && menu.slots.get(menu.indexOutput).getItem().getCount() == 4,
        "real recipe map produces four planks before transfer");
    var first = menu.quickMoveStack(owner, menu.indexOutput);
    check(
        first.is(Items.OAK_PLANKS)
            && first.getCount() == 4
            && (count(owner, Items.OAK_PLANKS) + stored(bench, Items.OAK_PLANKS)) == 4
            && bench.craftingGrid.get(0).getCount() == 2,
        "single native shift pays one log and returns one original result: returned="
            + first
            + ", player="
            + (count(owner, Items.OAK_PLANKS) + stored(bench, Items.OAK_PLANKS))
            + ", input="
            + bench.craftingGrid.get(0));
    check(
        menu.slots.get(menu.indexOutput).getItem().getCount() == 4,
        "next legitimate recipe result survives the transfer bookkeeping: "
            + menu.slots.get(menu.indexOutput).getItem());
    menu.quickMoveStack(owner, menu.indexOutput);
    menu.quickMoveStack(owner, menu.indexOutput);
    check(
        (count(owner, Items.OAK_PLANKS) + stored(bench, Items.OAK_PLANKS)) == 12
            && bench.craftingGrid.isEmpty()
            && menu.slots.get(menu.indexOutput).getItem().isEmpty(),
        "repeated real shifts conserve all twelve paid products without stale output");
    owner.getInventory().clearContent();
    bench.leftCrafting.tool.put(new ItemStack(ItemDefinitions.FORGE_HAMMER));
    bench.leftCrafting.input.put(new ItemStack(Items.IRON_INGOT, 2));
    var product = menu.slots.get(menu.indexOutputHammer).getItem().copy();
    check(
        !product.isEmpty(), "actual two-cell hammer station resolves its native component recipe");
    var hammer = menu.quickMoveStack(owner, menu.indexOutputHammer);
    check(
        !hammer.isEmpty()
            && bench.leftCrafting.input.get().getCount() == 1
            && bench.leftCrafting.tool.get().getDamageValue() == 1
            && (count(owner, product.getItem()) + stored(bench, product.getItem()))
                == product.getCount(),
        "hammer shift pays one actual ingot and one durability bill: returned="
            + hammer
            + ", input="
            + bench.leftCrafting.input.get()
            + ", damage="
            + bench.leftCrafting.tool.get().getDamageValue()
            + ", player="
            + (count(owner, product.getItem()) + stored(bench, product.getItem())));
    owner.getInventory().clearContent();
    bench.craftingGrid.clear();
    bench.craftingStorage.clear();
    bench.craftingGrid.put(0, new ItemStack(Items.IRON_INGOT, 1));
    bench.craftingGrid.put(1, new ItemStack(Items.IRON_INGOT, 10));
    owner.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 3));
    menu.quickMoveStack(owner, 27);
    check(
        bench.craftingGrid.get(0).getCount() == 4 && bench.craftingGrid.get(1).getCount() == 10,
        "shift fills the lower existing grid cell without moving or shrinking the higher cell");
    var named = new ItemStack(Items.IRON_INGOT, 5);
    named.set(DataComponents.CUSTOM_NAME, Component.literal("owned component boundary"));
    owner.getInventory().setItem(0, named);
    menu.quickMoveStack(owner, 27);
    check(
        bench.craftingGrid.get(0).getCount() == 4
            && bench.craftingGrid.get(1).getCount() == 10
            && bench.craftingStorage.get(0).has(DataComponents.CUSTOM_NAME),
        "different actual components bypass occupied grid cells and remain intact in physical"
            + " storage");
    owner.getInventory().clearContent();
    var automaticPos = pos.east(3);
    world.setBlockAndUpdate(automaticPos, BlockDefinitions.BATCH_CRAFTER.defaultBlockState());
    var automatic = (AutomaticCrafter) world.getBlockEntity(automaticPos);
    automatic.craftingGrid[0] = new ItemStack(Items.COAL);
    automatic.craftingGrid[3] = new ItemStack(Items.STICK);
    automatic.matrixChange(0);
    automatic.matrixChange(3);
    automatic.ingredientsRow[0].put(new ItemStack(Items.COAL, 1));
    automatic.ingredientsRow[3].put(new ItemStack(Items.STICK, 10));
    var automaticMenu = new AutomaticCraftingMenu(197, owner.getInventory(), automatic);
    owner.getInventory().setItem(0, new ItemStack(Items.COAL, 3));
    automaticMenu.quickMoveStack(owner, 27);
    check(
        automatic.ingredientsRow[0].get().getCount() == 4
            && automatic.ingredientsRow[3].get().getCount() == 10
            && owner.getInventory().getItem(0).isEmpty(),
        "nine actual queue admission adds exactly three accepted inputs without removing existing"
            + " quantities: queue0="
            + automatic.ingredientsRow[0].get()
            + ", queue3="
            + automatic.ingredientsRow[3].get()
            + ", remainder="
            + owner.getInventory().getItem(0)
            + ", recipe="
            + automatic.hasRecipe());
    check(
        automatic.craftingGrid[0].getCount() == 1 && automatic.craftingGrid[3].getCount() == 1,
        "physical queue insertion never pays or changes a template quantity");
    owner.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 5));
    check(
        automaticMenu.quickMoveStack(owner, 27).isEmpty()
            && owner.getInventory().getItem(0).getCount() == 5,
        "unaccepted queue item cannot disappear into a physical or template slot");
    var fields = automaticMenu.getNetworkedFields();
    boolean immutable = false;
    try {
      fields.add("injected");
    } catch (UnsupportedOperationException expected) {
      immutable = true;
    }
    check(
        immutable && fields.equals(List.of("guiProgress", "recipeOutput")),
        "actual automatic menu field plan owns immutable ordered names");
    check(
        MenuFields.extend(List.of("a", "b"), "b", "c").equals(List.of("a", "b", "c")),
        "field composition never duplicates synchronization work");
    owner.getInventory().clearContent();
    world.removeBlock(pos, false);
    world.removeBlock(automaticPos, false);
    return new Result(checks, List.copyOf(failures));
  }
}
