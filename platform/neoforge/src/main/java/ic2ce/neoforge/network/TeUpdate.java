package ic2ce.neoforge.network;

import ic2ce.api.network.INetworkUpdateListener;
import ic2ce.core.event.WorldData;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.runtime.ProjectRuntime;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;

/** Per-chunk recipient caching and bounded, world-thread application of complete tile deltas. */
final class TeUpdate {
  static final boolean debug = Boolean.getBoolean("ic2.network.debug.teupdate");
  private static final int MAX_FIELDS = 1024, MAX_TILES = 16384;

  private TeUpdate() {}

  private static final class Batch {
    GrowingBuffer buffer;
    int tiles;

    Batch(ServerPlayer player) throws IOException {
      reset(player);
    }

    void reset(ServerPlayer player) throws IOException {
      buffer = new GrowingBuffer(512);
      tiles = 0;
      DataEncoder.encode(buffer, player.level().dimension().identifier(), false);
    }

    void flush(ServerPlayer player, NetworkManager network) {
      if (tiles != 0) {
        buffer.flip();
        network.sendLargePacket(player, 0, buffer);
      }
    }
  }

  public static void send(WorldData data, NetworkManager network) throws IOException {
    if (data.tesToUpdate.isEmpty()) return;
    var recipients = new Long2ObjectOpenHashMap<List<ServerPlayer>>();
    Map<ServerPlayer, Batch> outputs = new IdentityHashMap<>();
    var global = new GrowingBuffer(256);
    for (var entry : data.tesToUpdate.entrySet()) {
      var tile = entry.getKey();
      if (tile.isRemoved() || !(tile.getLevel() instanceof ServerLevel world)) continue;
      long chunk = ChunkPos.pack(tile.getBlockPos());
      var players = recipients.get(chunk);
      if (players == null) {
        players =
            world
                .getChunkSource()
                .chunkMap
                .getPlayers(
                    new ChunkPos(tile.getBlockPos().getX() >> 4, tile.getBlockPos().getZ() >> 4),
                    false);
        recipients.put(chunk, players);
      }
      if (players.isEmpty()) continue;
      var delta = entry.getValue();
      var fields = delta.getGlobalFields();
      global.clear();
      for (String field : fields) NetworkManager.writeFieldData(tile, field, global);
      global.flip();
      for (var player : players) {
        if (player.level() != world) continue;
        var privateFields = delta.getPlayerFields(player);
        int count = fields.size() + privateFields.size();
        if (count == 0) continue;
        if (count > MAX_FIELDS) throw new IOException("Too many synchronized tile fields");
        var batch = outputs.get(player);
        if (batch == null) {
          batch = new Batch(player);
          outputs.put(player, batch);
        }
        if (batch.tiles == MAX_TILES) {
          batch.flush(player, network);
          batch.reset(player);
        }
        var output = batch.buffer;
        batch.tiles++;
        DataEncoder.encode(output, tile.getBlockPos(), false);
        output.writeShort(count);
        global.rewind();
        global.writeTo(output);
        for (String field : privateFields) NetworkManager.writeFieldData(tile, field, output);
      }
    }
    data.tesToUpdate.clear();
    for (var entry : outputs.entrySet()) entry.getValue().flush(entry.getKey(), network);
  }

  static void receive(GrowingBuffer input) throws IOException {
    var dimension = (Identifier) DataEncoder.decode(input, DataEncoder.EncodedType.Identifier);
    var batch = new TeUpdateDataClient();
    while (input.hasAvailable()) {
      if (batch.getTes().size() >= MAX_TILES)
        throw new IOException("Tile update quantity limit exceeded");
      var position = DataEncoder.decode(input, BlockPos.class);
      int count = input.readUnsignedShort();
      if (count > MAX_FIELDS) throw new IOException("Tile field quantity limit exceeded");
      var update = batch.addTe(position, count);
      for (int index = 0; index < count; index++) {
        String name = input.readString();
        if (name.isEmpty() || name.length() > 64)
          throw new IOException("Invalid synchronized field name");
        var value = DataEncoder.decode(input);
        if (name.equals("teBlk")) {
          if (!(value instanceof String id)) throw new IOException("Invalid tile block identifier");
          var identifier = Identifier.tryParse(id);
          if (identifier == null) throw new IOException("Invalid tile block identifier");
          var block = BuiltInRegistries.BLOCK.getValue(identifier);
          if (!(block instanceof MachineBlockType type))
            throw new IOException("Unknown synchronized IC2 tile type");
          update.teType = type;
        } else update.addField(name, value);
      }
    }
    ProjectRuntime.sideProxy.requestTick(
        false,
        () -> {
          var world = ProjectRuntime.sideProxy.getPlayerWorld();
          if (world == null || !world.dimension().identifier().equals(dimension)) return;
          for (var update : batch.getTes()) {
            try {
              apply(update, world);
            } catch (RuntimeException error) {
              ProjectRuntime.log.warn(
                  DiagnosticArea.Network, error, "Tile synchronization failed at %s", update.pos);
            }
          }
        });
  }

  private static void apply(TeUpdateDataClient.TeData update, Level world) {
    if (!world.hasChunkAt(update.pos)) return;
    var state = world.getBlockState(update.pos);
    if (update.hasClass() && state.getBlock() != update.teType) return;
    var tile = world.getBlockEntity(update.pos);
    if (update.hasClass()
        && (tile == null
            || tile.getClass() != update.teType.getTeClass()
            || tile.isRemoved()
            || tile.getLevel() != world)) {
      if (!(state.getBlock() instanceof EntityBlock provider)) return;
      tile = provider.newBlockEntity(update.pos, state);
      if (tile == null) return;
      tile.setLevel(world);
      world.setBlockEntity(tile);
    }
    if (tile == null || tile.isRemoved() || tile.getLevel() != world) return;
    for (var field : update.getFields()) {
      Object value = DataEncoder.getValue(field.value(), null);
      if (ReflectionUtil.setValueRecursive(tile, field.name(), value)
          && tile instanceof INetworkUpdateListener listener)
        listener.onNetworkUpdate(field.name());
    }
  }
}
