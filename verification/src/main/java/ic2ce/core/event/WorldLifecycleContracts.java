package ic2ce.core.event;

import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.NativeComponent;
import ic2ce.neoforge.processing.solid.OreGrinder;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Callback phase ordering, component ownership and authoritative native activation contracts. */
public final class WorldLifecycleContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native world lifecycle: " + label);
  }

  private static final class Inventory extends InventoryBlockEntity {
    Inventory() {
      super(
          BlockEntityDefinitions.MACERATOR,
          BlockPos.ZERO,
          BlockDefinitions.MACERATOR.defaultBlockState());
    }

    <T extends NativeComponent> T attach(T component) {
      return addComponent(component);
    }
  }

  private static final class Marker extends NativeComponent {
    Marker(Inventory owner) {
      super(owner);
    }
  }

  public static Result run(ServerLevel world) {
    return new WorldLifecycleContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var data = WorldData.get(world);
    var phases = new ArrayList<Integer>();
    TickHandler.requestSingleWorldTick(
        world,
        observed -> {
          phases.add(1);
          TickHandler.requestSingleWorldTick(observed, next -> phases.add(2));
        });
    TickHandler.dispatchCallbacks(world, data);
    check(
        phases.equals(List.of(1)), "reentrant one-shot work is deferred beyond the current phase");
    TickHandler.dispatchCallbacks(world, data);
    check(
        phases.equals(List.of(1, 2)),
        "deferred work executes on the next callback phase exactly once");
    var calls = new ArrayList<String>();
    IWorldTickCallback[] callbacks = new IWorldTickCallback[3];
    callbacks[2] = observed -> calls.add("third");
    callbacks[1] = observed -> calls.add("second");
    callbacks[0] =
        observed -> {
          calls.add("first");
          TickHandler.removeContinuousWorldTick(observed, callbacks[1]);
          TickHandler.requestContinuousWorldTick(observed, callbacks[2]);
        };
    TickHandler.requestContinuousWorldTick(world, callbacks[0]);
    TickHandler.requestContinuousWorldTick(world, callbacks[1]);
    TickHandler.dispatchCallbacks(world, data);
    check(
        calls.equals(List.of("first")),
        "removed continuous work does not mutate the world later in the same phase");
    TickHandler.dispatchCallbacks(world, data);
    check(
        calls.equals(List.of("first", "first", "third")),
        "new continuous subscriptions start on the next phase without duplicates");
    for (var callback : callbacks) TickHandler.removeContinuousWorldTick(world, callback);
    var owner = new Inventory();
    var original = owner.attach(new Marker(owner));
    boolean rejected = false;
    try {
      owner.attach(new Marker(owner));
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(
        rejected && owner.getComponent(Marker.class) == original,
        "duplicate component rejection preserves the first owned instance");
    rejected = false;
    try {
      owner.attach(new Marker(new Inventory()));
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(
        rejected && owner.getComponent(Marker.class) == original,
        "a foreign-parent component cannot be attached");
    var machine = new OreGrinder(BlockPos.ZERO, BlockDefinitions.MACERATOR.defaultBlockState());
    machine.setLevel(world);
    machine.setActiveState(true, true);
    check(machine.getActive(), "common activation changes the authoritative native running state");
    machine.shutdown(false);
    check(!machine.getActive(), "common shutdown clears the authoritative running state");
    return new Result(checks, List.copyOf(failures));
  }
}
