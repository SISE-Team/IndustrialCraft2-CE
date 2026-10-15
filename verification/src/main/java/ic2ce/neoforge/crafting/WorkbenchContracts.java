package ic2ce.neoforge.crafting;

import com.mojang.authlib.GameProfile;
import ic2ce.core.util.Util;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Real full player/storage overflow preserves each original grid cell; previews never alter
 * tool/input state.
 */
public final class WorkbenchContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native workbench: " + label);
  }

  public static Result run(ServerLevel world) {
    return new WorkbenchContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8944, 100, 8944);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, BlockDefinitions.INDUSTRIAL_WORKBENCH.defaultBlockState());
    var bench = (IndustrialWorkbench) world.getBlockEntity(position);
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new GameProfile(
                UUID.fromString("48f9df25-2ae8-480b-97da-b0188e09e3a1"), "IC2BenchProbe"));
    player.setPos(position.getX() + .5, position.getY(), position.getZ() + .5);
    player.getAbilities().instabuild = true;
    var main = Util.playerMainItems(player);
    for (int index = 0; index < main.size(); index++)
      main.set(index, new ItemStack(Items.BEDROCK, 64));
    for (int index = 0; index < bench.craftingStorage.size(); index++)
      bench.craftingStorage.put(index, new ItemStack(Items.BEDROCK, 64));
    bench.craftingStorage.put(0, new ItemStack(Items.GOLD_INGOT, 63));
    bench.craftingGrid.put(0, new ItemStack(Items.COAL, 7));
    bench.craftingGrid.put(8, new ItemStack(Items.GOLD_INGOT, 10));
    bench.clear(player);
    check(
        bench.craftingStorage.get(0).getCount() == 64
            && bench.craftingGrid.get(8).is(Items.GOLD_INGOT)
            && bench.craftingGrid.get(8).getCount() == 9
            && bench.craftingGrid.get(0).is(Items.COAL)
            && bench.craftingGrid.get(0).getCount() == 7,
        "partial storage acceptance preserves the original last-cell remainder and never overwrites"
            + " cell zero even with a full creative inventory");
    bench.clear(player);
    check(
        bench.craftingGrid.get(8).getCount() == 9 && bench.craftingGrid.get(0).getCount() == 7,
        "repeated blocked cleanup cannot duplicate or discard either grid item");
    main.set(0, ItemStack.EMPTY);
    main.set(1, ItemStack.EMPTY);
    bench.clear(player);
    check(
        bench.craftingGrid.isEmpty()
            && main.get(0).is(Items.COAL)
            && main.get(0).getCount() == 7
            && main.get(1).is(Items.GOLD_INGOT)
            && main.get(1).getCount() == 9,
        "available real player slots receive every remaining source item exactly once");
    bench.onPlaced(
        new ItemStack(BlockDefinitions.INDUSTRIAL_WORKBENCH.asItem()), player, Direction.NORTH);
    bench.leftCrafting.input.put(new ItemStack(Items.IRON_INGOT, 5));
    var before = bench.leftCrafting.resultInv.getItem(0).copy();
    check(
        before.is(ItemDefinitions.IRON_PLATE)
            && !bench.leftCrafting.input.accepts(new ItemStack(Items.DIAMOND))
            && bench.leftCrafting.input.get().is(Items.IRON_INGOT)
            && bench.leftCrafting.input.get().getCount() == 5
            && ItemStack.matches(before, bench.leftCrafting.resultInv.getItem(0)),
        "rejected native station preview changes neither live input nor its displayed recipe"
            + " output");
    check(
        bench.rightCrafting.input.accepts(new ItemStack(ItemDefinitions.COPPER_PLATE))
            && bench.rightCrafting.input.isEmpty(),
        "accepted cutter preview resolves an independent recipe without inserting its probe stack");
    var offeredDrop = new ItemStack(BlockDefinitions.INDUSTRIAL_WORKBENCH.asItem());
    var marked = bench.adjustDrop(offeredDrop, true);
    check(
        !ic2ce.neoforge.item.StackTag.has(offeredDrop)
            && ic2ce.neoforge.item.StackTag.get(marked).getBooleanOr("PLACED", false),
        "drop tool policy writes an owned placed marker without mutating its incoming item");
    for (int index = 0; index < main.size(); index++) main.set(index, ItemStack.EMPTY);
    bench.craftingStorage.clear();
    bench.leftCrafting.input.clear();
    bench.leftCrafting.tool.clear();
    bench.rightCrafting.tool.clear();
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
