package ic2ce.neoforge;

import java.util.*;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Blocks;

/**
 * Native binding records own caller arrays and preserve stable tint identifiers across event
 * replay.
 */
public final class ClientBindingContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run() {
    var failures = new ArrayList<String>();
    int checks = 0;
    var caller = new net.minecraft.world.level.block.Block[] {Blocks.STONE};
    BlockTintSource color = state -> 0xffffff;
    var binding = new ClientEnvProxyNeoForge.BlockTintRegistration(List.of(color), caller);
    caller[0] = Blocks.DIRT;
    binding.blocks()[0] = Blocks.DIRT;
    checks++;
    if (binding.blocks()[0] != Blocks.STONE)
      failures.add(
          "native client binding: caller array or exported block array edits the registered tint"
              + " target");
    var ids =
        ClientEnvProxyNeoForge.itemTintRegistrations.stream()
            .map(ClientEnvProxyNeoForge.ItemTintRegistration::id)
            .toList();
    checks++;
    if (ids.size() != new HashSet<>(ids).size()
        || !ids.equals(
            ClientEnvProxyNeoForge.itemTintRegistrations.stream()
                .map(ClientEnvProxyNeoForge.ItemTintRegistration::id)
                .toList()))
      failures.add("native client binding: replay changes retained native item tint identifiers");
    var client = new ClientEnvProxyNeoForge();
    var quad =
        ic2ce.neoforge.model.ModelUtil.getQuads(
                Blocks.STONE.defaultBlockState(),
                net.minecraft.core.Direction.UP,
                net.minecraft.util.RandomSource.create(25))
            .getFirst();
    var snapshot = client.getQuadData(quad);
    float before = snapshot.positions()[0];
    snapshot.positions()[0] = before + 100;
    checks++;
    if (snapshot.positions()[0] != before || snapshot.uvs().length != 8)
      failures.add(
          "native client binding: borrowed quad arrays mutate another native projection read");
    checks++;
    if (client.getRegistryLookup()
        != net.minecraft.client.Minecraft.getInstance().level.registryAccess())
      failures.add("native client binding: registry lookup belongs to a retired client world");
    return new Result(checks, List.copyOf(failures));
  }
}
