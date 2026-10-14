package ic2ce.neoforge.crafting;

import ic2ce.core.util.StackTag;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Real standard/UU recipe execution, exact last-paid tick and native tool remainder placement. */
public final class AutomaticCraftingContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native automatic crafting: " + label);
  }

  public static Result run(ServerLevel world) {
    return new AutomaticCraftingContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8912, 100, 8912);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, BlockDefinitions.BATCH_CRAFTER.defaultBlockState());
    var tile = (AutomaticCrafter) world.getBlockEntity(position);
    var energy = tile.getComponent(ElectricalBuffer.class);
    tile.craftingGrid[4] = new ItemStack(Items.OAK_LOG);
    tile.ingredientsRow[4].put(new ItemStack(Items.OAK_LOG));
    tile.matrixChange(-1);
    tile.setChanged();
    energy.forceAddEnergy(80);
    for (int tick = 0; tick < 39; tick++) tile.updateEntityServer();
    check(
        tile.progress == 39
            && energy.getEnergy() == 2
            && tile.craftingOutput.isEmpty()
            && tile.ingredientsRow[4].get().getCount() == 1,
        "thirty-nine native processing ticks reserve no output and retain the original ingredient"
            + " until completion");
    tile.updateEntityServer();
    check(
        tile.progress == 0
            && energy.getEnergy() == 0
            && tile.craftingOutput.get().is(Items.OAK_PLANKS)
            && tile.craftingOutput.get().getCount() == 4
            && tile.ingredientsRow[4].isEmpty(),
        "the last two paid EU complete the real offset recipe without requiring another tick of"
            + " energy");
    tile.craftingOutput.put(new ItemStack(Items.BEDROCK, 64));
    tile.ingredientsRow[4].put(new ItemStack(Items.OAK_LOG));
    tile.setChanged();
    energy.forceAddEnergy(80);
    tile.updateEntityServer();
    check(
        tile.progress == 0 && energy.getEnergy() == 80 && !tile.getActive(),
        "blocked native output cannot start charging or consume ingredients");
    tile.craftingOutput.clear();
    for (int tick = 0; tick < 10; tick++) tile.updateEntityServer();
    check(
        tile.progress == 10 && energy.getEnergy() == 60,
        "unblocked recipe resumes its actual paid clock");
    var owned = tile.craftingGrid;
    var advertised = new ItemStack[12];
    advertised[0] = new ItemStack(Items.OAK_LOG, 64);
    tile.craftingGrid = advertised;
    tile.acceptClientField("craftingGrid");
    check(
        tile.craftingGrid == owned
            && owned.length == 9
            && owned[0].getCount() == 1
            && owned[4].isEmpty(),
        "client template replacement preserves the native menu array identity and clamps each ghost"
            + " to one item");
    var malformed = new CompoundTag();
    var list = new ListTag();
    var row = new CompoundTag();
    row.putByte("index", (byte) 255);
    StackTag.save(world.registryAccess(), new ItemStack(Items.DIAMOND), row);
    list.add(row);
    malformed.put("grid", list);
    malformed.putInt("progress", -10);
    tile.load(malformed);
    check(
        tile.progress == 0 && Arrays.stream(tile.craftingGrid).allMatch(ItemStack::isEmpty),
        "malformed saved matrix indices and progress cannot crash or preserve stale ghosts");
    tile.craftingGrid[4] = new ItemStack(ItemDefinitions.FORGE_HAMMER);
    tile.craftingGrid[5] = new ItemStack(Items.IRON_INGOT);
    tile.ingredientsRow[4].put(new ItemStack(ItemDefinitions.FORGE_HAMMER));
    tile.ingredientsRow[5].put(new ItemStack(Items.IRON_INGOT));
    tile.matrixChange(-1);
    tile.setChanged();
    energy.forceAddEnergy(80);
    for (int tick = 0; tick < 40; tick++) tile.updateEntityServer();
    check(
        tile.craftingOutput.get().is(ItemDefinitions.IRON_PLATE)
            && tile.ingredientsRow[5].isEmpty()
            && tile.ingredientsRow[4].get().is(ItemDefinitions.FORGE_HAMMER)
            && tile.ingredientsRow[4].get().getDamageValue() == 1
            && tile.containerOutput.isEmpty(),
        "native compact recipe remainders map a damaged hammer back to its original offset"
            + " ingredient cell");
    world.removeBlock(position, false);
    world.setBlockAndUpdate(position, BlockDefinitions.UU_ASSEMBLY_BENCH.defaultBlockState());
    var assembly = (UuAssembler) world.getBlockEntity(position);
    energy = assembly.getComponent(ElectricalBuffer.class);
    assembly.craftingGrid[1] = new ItemStack(ItemDefinitions.UU_MATTER);
    assembly.ingredientsRow[1].put(new ItemStack(ItemDefinitions.UU_MATTER));
    assembly.matrixChange(-1);
    assembly.setChanged();
    energy.forceAddEnergy(80);
    for (int tick = 0; tick < 40; tick++) assembly.updateEntityServer();
    check(
        assembly.craftingOutput.get().is(Items.OAK_LOG)
            && assembly.craftingOutput.get().getCount() == 8,
        "untrimmed top-center UU layout makes exactly eight logs");
    assembly.craftingOutput.clear();
    Arrays.fill(assembly.craftingGrid, ItemStack.EMPTY);
    assembly.craftingGrid[4] = new ItemStack(ItemDefinitions.UU_MATTER);
    assembly.ingredientsRow[4].put(new ItemStack(ItemDefinitions.UU_MATTER));
    assembly.matrixChange(-1);
    assembly.setChanged();
    energy.forceAddEnergy(80);
    for (int tick = 0; tick < 40; tick++) assembly.updateEntityServer();
    check(
        assembly.craftingOutput.get().is(Items.STONE)
            && assembly.craftingOutput.get().getCount() == 16,
        "untrimmed center UU layout remains distinct and makes exactly sixteen stone");
    world.removeBlock(position, false);
    var cells = new ItemStack[] {new ItemStack(Items.IRON_INGOT, 4), ItemStack.EMPTY};
    var grid = new CraftingGridView.ArrayGrid(cells, 2);
    var view = grid.getItems();
    check(
        grid.removeItem(-1, 1).isEmpty()
            && grid.removeItem(0, -1).isEmpty()
            && cells[0].getCount() == 4,
        "native grid rejects invalid indices and negative extraction");
    var removed = grid.removeItem(0, 2);
    removed.setCount(1);
    check(
        view == grid.getItems() && view.get(0).getCount() == 2 && cells[0].getCount() == 2,
        "stable native item view reflects explicit quantity writes while removed items own their"
            + " counts");
    try {
      new CraftingGridView.ArrayGrid(new ItemStack[5], 3);
      check(false, "nonrectangular grid rejected");
    } catch (IllegalArgumentException expected) {
      check(true, "nonrectangular grid rejected");
    }
    return new Result(checks, List.copyOf(failures));
  }
}
