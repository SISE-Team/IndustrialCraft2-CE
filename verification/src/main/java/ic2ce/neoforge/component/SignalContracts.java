package ic2ce.neoforge.component;

import ic2ce.neoforge.crafting.CraftingBlocks;
import ic2ce.neoforge.crafting.IndustrialWorkbench;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * Actual redstone nodes cover signal clamping, callback edits, linked source detach and cycle
 * rejection.
 */
public final class SignalContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native signal: " + label);
  }

  private IndustrialWorkbench owner(ServerLevel world, BlockPos pos) {
    var result =
        new IndustrialWorkbench(pos, CraftingBlocks.INDUSTRIAL_WORKBENCH.defaultBlockState());
    result.setLevel(world);
    return result;
  }

  public static Result run(ServerLevel world) {
    return new SignalContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var pos = new BlockPos(1660, 110, 1330);
    var origin = new SignalInput(owner(world, pos));
    var receiver = new SignalInput(owner(world, pos.east(3)));
    var tail = new SignalInput(owner(world, pos.east(6)));
    world.setBlockAndUpdate(pos.west(), Blocks.REDSTONE_BLOCK.defaultBlockState());
    var calls = new AtomicInteger();
    SignalInput.IRedstoneChangeHandler[] callback = new SignalInput.IRedstoneChangeHandler[1];
    callback[0] =
        value -> {
          calls.incrementAndGet();
          origin.unsubscribe(callback[0]);
        };
    origin.subscribe(callback[0]);
    origin.update();
    check(
        origin.getRedstoneInput() == 15 && calls.get() == 1,
        "actual native neighbor signal safely permits an observer to remove itself");
    origin.update();
    check(calls.get() == 1, "unchanged native signal causes no repeated observer side effect");
    origin.linkTo(receiver);
    receiver.linkTo(tail);
    check(
        receiver.getRedstoneInput() == 15 && tail.getRedstoneInput() == 15,
        "actual linked circuit forwards the origin to two dependent native nodes");
    origin.linkTo(receiver);
    check(receiver.getLinkedOrigins().size() == 1, "identical signal links attach exactly once");
    boolean cycle = false;
    try {
      tail.linkTo(origin);
    } catch (IllegalArgumentException expected) {
      cycle = true;
    }
    check(
        cycle && !tail.isLinked(), "cyclic signal link rejected before attaching either callback");
    origin.unlinkOutbound();
    check(
        receiver.getRedstoneInput() == 0
            && tail.getRedstoneInput() == 0
            && receiver.getLinkedOrigins().isEmpty(),
        "unlink immediately clears stale downstream signal at both native receivers");
    SignalInput.IRedstoneModifier high = value -> Integer.MAX_VALUE;
    receiver.addRedstoneModifier(high);
    receiver.update();
    check(
        receiver.getRedstoneInput() == 15,
        "addon high signal is clamped to the physical native range");
    receiver.removeRedstoneModifier(high);
    SignalInput.IRedstoneModifier low = value -> -1;
    receiver.addRedstoneModifier(low);
    receiver.update();
    check(
        receiver.getRedstoneInput() == 0,
        "negative addon signal cannot become a negative circuit value");
    receiver.removeRedstoneModifier(low);
    receiver.unlinkOutbound();
    world.removeBlock(pos.west(), false);
    int[] transitions = {0};
    var output =
        new SignalOutput(owner(world, pos)) {
          @Override
          public void onChange() {
            transitions[0]++;
          }
        };
    output.setLevel(100);
    output.setLevel(1000);
    output.setLevel(-2);
    check(
        output.getLevel() == 0 && transitions[0] == 2,
        "direct output clamps once and notifies only observable transitions");
    output.setUpdate(null);
    output.onWorldTick();
    check(
        output.getLevel() == 0,
        "detached signal supplier is safe to tick during lifecycle replacement");
    new DirectSignalOutput(
            new IndustrialWorkbench(pos, CraftingBlocks.INDUSTRIAL_WORKBENCH.defaultBlockState()))
        .setLevel(15);
    new AnalogSignalOutput(
            new IndustrialWorkbench(pos, CraftingBlocks.INDUSTRIAL_WORKBENCH.defaultBlockState()))
        .setLevel(15);
    check(
        true,
        "direct and analog output callbacks tolerate construction before native world attachment");
    return new Result(checks, List.copyOf(failures));
  }
}
