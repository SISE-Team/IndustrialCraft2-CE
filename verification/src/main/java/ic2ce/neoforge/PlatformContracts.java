package ic2ce.neoforge;

import ic2ce.neoforge.registry.GameSignals;
import ic2ce.neoforge.registry.RegistryPlan;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.world.WorldBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Physical native registry identity, staged ownership and actual fire/fuel hooks. */
public final class PlatformContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean valid, String label) {
    checks++;
    if (!valid) failures.add("native platform: " + label);
  }

  public static Result run(ServerLevel world) {
    return new PlatformContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    check(
        EnvProxy.get().getRegistryLookup() == world.getServer().registryAccess(),
        "dedicated current registry generation is the actual server registry");
    check(
        BuiltInRegistries.GAME_EVENT
            .getKey(GameSignals.TOOL_USE)
            .equals(ProjectIds.id("tool_use")),
        "game events replay through their native registry phase");
    check(
        EnvProxyNeoForge.BLOCK_ENTITIES.values().size() >= 147
            && EnvProxyNeoForge.BLOCK_ENTITIES.values().size()
                == BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet().stream()
                    .filter(entry -> entry.getKey().identifier().getNamespace().equals("ic2"))
                    .count(),
        "every native machine, storage variant and sign entity type is present in the typed"
            + " capability journal");
    check(
        EnvProxyNeoForge.ITEMS.values().size() == 508,
        "the actual registration journal contains every project item");
    for (var block :
        List.of(
            WorldBlocks.RUBBER_LEAVES,
            WorldBlocks.RUBBER_LOG,
            WorldBlocks.WOODEN_SCAFFOLD,
            WorldBlocks.REINFORCED_WOODEN_SCAFFOLD)) {
      var state = block.defaultBlockState();
      check(
          state.getFlammability(world, BlockPos.ZERO, Direction.UP) > 0
              && state.getFireSpreadSpeed(world, BlockPos.ZERO, Direction.UP) > 0,
          "native fire reads the project block properties: "
              + BuiltInRegistries.BLOCK.getKey(block));
    }
    check(
        EnvProxy.get().getBurnTime(new ItemStack(Items.SUGAR_CANE)) == 50
            && EnvProxy.get().getBurnTime(ItemStack.EMPTY) == 0,
        "registered non-vanilla fuel and empty fuel dispatch");
    check(
        !EnvProxy.get().openHandledScreen(
            ic2ce.neoforge.runtime.AutomationActors.get(world), null, null),
        "rejected native menu providers cannot report an opened menu");
    var plan = new RegistryPlan<Block>(Registries.BLOCK);
    var id = ProjectIds.id("verification_value");
    plan.stage(id, Blocks.STONE);
    var snapshot = plan.values();
    plan.stage(ProjectIds.id("verification_later"), Blocks.DIRT);
    check(
        snapshot.size() == 1 && plan.values().size() == 2,
        "exported registry snapshots own a stable generation");
    boolean immutable = false;
    try {
      snapshot.clear();
    } catch (UnsupportedOperationException expected) {
      immutable = true;
    }
    check(immutable, "external capability consumers cannot alter registry journal entries");
    boolean duplicate = false;
    try {
      plan.stage(id, Blocks.GLASS);
    } catch (IllegalStateException expected) {
      duplicate = true;
    }
    check(
        duplicate && plan.values().getFirst() == Blocks.STONE,
        "duplicate registration cannot replace an admitted physical value");
    boolean foreign = false;
    try {
      plan.stage(Identifier.fromNamespaceAndPath("minecraft", "verification"), Blocks.GLASS);
    } catch (IllegalArgumentException expected) {
      foreign = true;
    }
    check(
        foreign && plan.values().size() == 2,
        "foreign registrations never mutate the project journal");
    var order = new ArrayList<Integer>();
    IndustrialCraft2CE.instance.runAfterRegistryInit(
        () -> {
          order.add(1);
          IndustrialCraft2CE.instance.runAfterRegistryInit(() -> order.add(2));
        });
    check(
        order.equals(List.of(1, 2)),
        "completed registry callbacks support native reentrant work outside the queue lock");
    return new Result(checks, List.copyOf(failures));
  }
}
