package ic2ce.verification;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.menu.IHasGui;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Headless server coverage, deliberately separate from real-client GUI verification. */
public final class DedicatedServerVerification {
  private final Map<String, Map<String, Object>> machines = new LinkedHashMap<>();
  private final List<String> failures = new ArrayList<>();
  private ServerLevel level;
  private int ticks, checks;
  private final boolean reload = Boolean.getBoolean("ic2.serverReload");

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) { failures.add(label); System.err.println("IC2_SERVER_VERIFY FAIL " + label); }
  }

  @FunctionalInterface
  private interface Probe { void run() throws Exception; }

  private void guard(String name, Probe action) {
    try { action.run(); } catch (Throwable error) {
      check(false, name + ": " + error); error.printStackTrace();
    }
  }

  private BlockPos position(int index) {
    return new BlockPos(256 + index % 12 * 16, 181, 256 + index / 12 * 16);
  }

  private void place(String name, BlockPos pos) {
    var block = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("ic2", name));
    check(block != Blocks.AIR, name + " registered block");
    level.setBlockAndUpdate(pos, block.defaultBlockState());
  }

  @SubscribeEvent
  public void started(ServerStartedEvent event) {
    level = event.getServer().overworld();
    // A headless world needs a simulation/keep-active ticket, not only a loading
    // ticket. Use the same registered ticket as the real IC2 chunk loader.
    var simulationTicket = ic2ce.neoforge.EnvProxyNeoForge.CHUNK_LOADER_TICKET_TYPE.get();
    level.getChunkSource().addTicketWithRadius(simulationTicket, new net.minecraft.world.level.ChunkPos(2, 2), 2);
    var idsFile = Path.of(System.getProperty("ic2.serverMachineIds"));
    guard("machine catalog", () -> {
      String[] ids = new Gson().fromJson(Files.readString(idsFile), String[].class);
      check(ids.length == 104, "current catalog contains all 104 runtime machines");
      int index = 0;
      for (String id : ids) {
        BlockPos pos = position(index++);
        var row = new LinkedHashMap<String, Object>();
        row.put("position", List.of(pos.getX(), pos.getY(), pos.getZ()));
        machines.put(id, row);
        guard(id + " placement", () -> {
          for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
              level.setChunkForced((pos.getX() >> 4) + dx, (pos.getZ() >> 4) + dz, true);
          level.getChunkSource().addTicketWithRadius(simulationTicket, new net.minecraft.world.level.ChunkPos(pos.getX() >> 4, pos.getZ() >> 4), 2);
          if (!reload) {
            level.setBlockAndUpdate(pos.below(), Blocks.SMOOTH_STONE.defaultBlockState());
            if (id.equals("reactor_chamber")) place("nuclear_reactor", pos.west());
            if (Set.of("reactor_access_hatch", "reactor_fluid_port", "reactor_redstone_port").contains(id)) {
              BlockPos core = pos.west(2);
              for (int x = -2; x <= 2; x++) for (int y = -2; y <= 2; y++) for (int z = -2; z <= 2; z++)
                if (Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2)
                  place("reactor_vessel", core.offset(x,y,z));
              place("nuclear_reactor", core);
              for (Direction direction : Direction.values()) place("reactor_chamber", core.relative(direction));
            }
            place(id, pos);
          }
          var tile = level.getBlockEntity(pos);
          check(tile instanceof NativeBlockEntity, id + " native entity " + (reload ? "restored" : "placed"));
          if (tile instanceof NativeBlockEntity nativeTile) {
            if (!reload) nativeTile.onPlaced(new ItemStack(nativeTile.getBlockState().getBlock()), FakePlayerFactory.getMinecraft(level), Direction.NORTH);
            row.put("class", tile.getClass().getName());
            row.put("entityType", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(tile.getType()).toString());
          }
        });
      }
    });
    event.getServer().getCommands().performPrefixedCommand(event.getServer().createCommandSourceStack(), "tick sprint " + (reload ? 240 : 1200));
    save(false);
  }

  private void inspect(String stage) {
    int index = 0;
    for (var entry : machines.entrySet()) {
      String id = entry.getKey(); BlockPos pos = position(index++);
      guard(id + " " + stage, () -> {
        var tile = level.getBlockEntity(pos);
        check(tile instanceof NativeBlockEntity, id + " " + stage + " remains placed");
        if (!(tile instanceof NativeBlockEntity nativeTile)) return;
        for (String fieldName : List.of("loadState", "enableWorldTick")) {
          var field = NativeBlockEntity.class.getDeclaredField(fieldName);
          field.setAccessible(true);
          entry.getValue().put(stage + "_" + fieldName, field.get(nativeTile));
        }
        entry.getValue().put(stage + "_chunkLoaded", level.isLoaded(pos));
        check(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(tile.getType()).getPath().equals(id), id + " " + stage + " correct registered type");
        var saved = tile.saveWithFullMetadata(level.registryAccess());
        var copy = BlockEntity.loadStatic(pos, tile.getBlockState(), saved, level.registryAccess());
        check(copy != null && copy.getType() == tile.getType(), id + " " + stage + " NBT restores type");
        if (copy instanceof NativeBlockEntity restored) {
          restored.setLevel(level);
          check(saved.getCompoundOrEmpty("ic2").equals(restored.saveWithFullMetadata(level.registryAccess()).getCompoundOrEmpty("ic2")), id + " " + stage + " NBT preserves machine state");
        }
        if (nativeTile instanceof IHasGui gui) {
          var menu = gui.createServerScreenHandler(1, FakePlayerFactory.getMinecraft(level));
          check(menu != null, id + " " + stage + " server menu constructs");
          if (menu != null) {
            var fields = menu.getNetworkedFields();
            check(fields != null && fields.stream().distinct().count() == fields.size(), id + " " + stage + " synchronization fields unique");
            menu.removed(FakePlayerFactory.getMinecraft(level));
            entry.getValue().put("serverMenu", true);
          }
        }
        entry.getValue().put(stage, true);
      });
    }
  }

  private void save(boolean finished) {
    guard("write report", () -> {
      var report = new LinkedHashMap<String,Object>();
      report.put("scope", "dedicated server placement, ticking, server menus and NBT; no real-client rendering or multiplayer claim");
      report.put("reload", reload); report.put("finished", finished); report.put("ticks", ticks);
      report.put("checks", checks); report.put("failures", failures); report.put("machines", machines);
      Files.writeString(Path.of(System.getProperty("ic2.serverOnlyReport")), new GsonBuilder().setPrettyPrinting().create().toJson(report));
    });
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (level == null) return;
    ticks++;
    if (ticks == 40) inspect("initial");
    if (ticks == (reload ? 200 : 1100)) {
      inspect("final"); save(true);
      System.out.println("IC2_SERVER_VERIFY FINISHED " + checks + " checks, " + failures.size() + " failures");
      level = null;
      event.getServer().getCommands().performPrefixedCommand(event.getServer().createCommandSourceStack(), "stop");
    }
  }
}
