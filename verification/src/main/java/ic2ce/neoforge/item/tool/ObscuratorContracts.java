package ic2ce.neoforge.item.tool;

import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.item.StackTag;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/**
 * Physical source validation, complete print reservation, owned colors and malformed-payload
 * rejection.
 */
public final class ObscuratorContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native face imprint: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ObscuratorContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var owner =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("c2bf7625-acd8-4c87-b39a-7e5c62541164"), "IC2FaceProbe"));
    owner.getInventory().clearContent();
    owner.getAbilities().instabuild = false;
    owner.setShiftKeyDown(true);
    var pos = new BlockPos(10720, 100, 10720);
    world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
    world.getChunkAt(pos);
    world.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
    owner.setPos(pos.getX() + .5, pos.getY(), pos.getZ() - 1.5);
    var stack = new ItemStack(ToolItems.OBSCURATOR);
    var item = (SurfaceImprinter) stack.getItem();
    ElectricItem.manager.charge(stack, 100000, 2, true, false);
    owner.setItemInHand(InteractionHand.MAIN_HAND, stack);
    int slot = owner.getInventory().getSelectedSlot();
    double before = ElectricItem.manager.getCharge(stack);
    item.onPlayerItemNetworkData(
        owner,
        slot,
        Blocks.STONE,
        "normal",
        Direction.NORTH.ordinal(),
        new int[] {0xFFFFFF},
        pos.east(50));
    check(
        ElectricItem.manager.getCharge(stack) == before
            && SurfaceImprinter.getState(StackTag.get(stack)) == null,
        "out-of-reach client source cannot pay for or save a fabricated material");
    item.onPlayerItemNetworkData(
        owner,
        slot,
        Blocks.GOLD_BLOCK,
        "normal",
        Direction.NORTH.ordinal(),
        new int[] {0xFFFFFF},
        pos);
    check(
        ElectricItem.manager.getCharge(stack) == before,
        "actual server block identity rejects a different client-claimed material without charge");
    var colors = new int[] {0xFFFFFF};
    item.onPlayerItemNetworkData(
        owner, slot, Blocks.STONE, "normal", Direction.NORTH.ordinal(), colors, pos);
    colors[0] = 0;
    check(
        SurfaceImprinter.getState(StackTag.get(stack)).is(Blocks.STONE)
            && ElectricItem.manager.getCharge(stack) == before - 20000
            && SurfaceImprinter.getColorMultipliers(StackTag.get(stack))[0] == 0xFFFFFF,
        "valid actual source pays exact scan bill and owns the supplied color array");
    var read = SurfaceImprinter.getColorMultipliers(StackTag.get(stack));
    read[0] = 0;
    check(
        SurfaceImprinter.getColorMultipliers(StackTag.get(stack))[0] == 0xFFFFFF,
        "retrieving a face color array cannot mutate the saved native component");
    before = ElectricItem.manager.getCharge(stack);
    item.onPlayerItemNetworkData(
        owner, slot, Blocks.STONE, "normal", -1, new int[] {0xFFFFFF}, pos);
    item.onPlayerItemNetworkData(owner, slot, Blocks.STONE, "normal", 0, new int[65], pos);
    check(
        ElectricItem.manager.getCharge(stack) == before,
        "invalid side and excessive material layers cannot spend native scan energy");
    owner.setShiftKeyDown(false);
    var target = pos.east(2);
    world.setBlock(target, Blocks.STONE.defaultBlockState(), 3);
    var context =
        new net.minecraft.world.item.context.UseOnContext(
            owner,
            InteractionHand.MAIN_HAND,
            new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(target), Direction.NORTH, target, false));
    item.onItemUseFirst(stack, context);
    check(
        ElectricItem.manager.getCharge(stack) == before,
        "a native target rejecting face imprint receives no free change and refunds its complete"
            + " reserved print bill");
    owner.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }
}
