package ic2ce.neoforge.network;

import ic2ce.api.network.INetworkDataProvider;
import ic2ce.api.network.INetworkManager;
import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.diagnostics.ProjectLog;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.world.WorldData;
import ic2ce.neoforge.network.NeoForgeNetworkHandler.Ic2Payload;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.explosion.NativeBlast;
import ic2ce.neoforge.handheld.HandheldStorage;
import ic2ce.neoforge.menu.OwnedMenu;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Independent side-aware message construction, tile deltas, recipient selection and framed
 * transport.
 */
public class NetworkManager implements INetworkManager {
  public static final Identifier channelId = ProjectIds.id("m");
  private static final int FRAME_BYTES = 32766, FRAME_BODY = FRAME_BYTES - 2;

  @FunctionalInterface
  private interface Writer {
    void write(GrowingBuffer buffer) throws IOException;
  }

  protected boolean isClient() {
    return false;
  }

  private void requireServer() {
    if (isClient()) throw new IllegalStateException("This network operation requires a server");
  }

  private static GrowingBuffer packet(SubPacketType type, int capacity, Writer writer) {
    var output = new GrowingBuffer(capacity);
    type.writeTo(output);
    try {
      writer.write(output);
    } catch (IOException error) {
      throw new IllegalStateException("Cannot encode " + type, error);
    }
    output.flip();
    return output;
  }

  public void onTickEnd(WorldData data) {
    if (isClient()) return;
    try {
      TeUpdate.send(data, this);
    } catch (IOException error) {
      throw new IllegalStateException("Cannot synchronize tile state", error);
    }
  }

  private static TeUpdateDataServer delta(BlockEntity tile) {
    Objects.requireNonNull(tile);
    if (tile.getLevel() == null || tile.getLevel().isClientSide())
      throw new IllegalStateException("Tile synchronization requires a server world");
    return WorldData.get(tile.getLevel())
        .tesToUpdate
        .computeIfAbsent(tile, _ -> new TeUpdateDataServer());
  }

  @Override
  public final void updateTileEntityField(BlockEntity tile, String name) {
    if (!isClient()) delta(tile).addGlobalField(name);
    else if (IncomingActions.modifiable(tile.getClass(), name) != null)
      sendC2SPacket(
          packet(
              SubPacketType.TileEntityData,
              64,
              output -> {
                DataEncoder.encode(output, tile, false);
                writeFieldData(tile, name, output);
              }));
  }

  public final void updateTileEntityFieldTo(BlockEntity tile, String name, ServerPlayer player) {
    requireServer();
    delta(tile).addPlayerField(name, player);
  }

  /** Publishes one server-owned field mask with a single world and delta lookup. */
  public final void updateTileEntityFields(BlockEntity tile, String[] names, int mask) {
    requireServer();
    if (names.length > Integer.SIZE || (names.length < Integer.SIZE && mask >>> names.length != 0))
      throw new IllegalArgumentException("Synchronized field mask exceeds its names");
    if (mask == 0) return;
    var update = delta(tile);
    while (mask != 0) {
      int field = Integer.numberOfTrailingZeros(mask);
      update.addGlobalField(names[field]);
      mask &= mask - 1;
    }
  }

  @Override
  public final void sendInitialData(BlockEntity tile) {
    requireServer();
    if (tile instanceof INetworkDataProvider provider)
      for (String name : provider.getNetworkedFields()) delta(tile).addGlobalField(name);
  }

  public final void sendInitialData(BlockEntity tile, ServerPlayer player) {
    requireServer();
    if (tile instanceof INetworkDataProvider provider)
      for (String name : provider.getNetworkedFields()) delta(tile).addPlayerField(name, player);
  }

  public final void sendPlayerItemData(Player player, int slot, Object... values) {
    if (values.length > 32
        || !(slot >= 0 && slot < 9 || slot == 40)
        || slot >= player.getInventory().getContainerSize())
      throw new IllegalArgumentException("Invalid player item message");
    var output =
        packet(
            SubPacketType.PlayerItemData,
            256,
            buffer -> {
              buffer.writeByte(slot);
              DataEncoder.encode(buffer, player.getInventory().getItem(slot).getItem(), false);
              buffer.writeVarInt(values.length);
              for (Object value : values) DataEncoder.encode(buffer, value, true);
            });
    if (isClient()) sendC2SPacket(output);
    else sendS2CPacket((ServerPlayer) player, output, true);
  }

  public final void sendComponentUpdate(
      NativeBlockEntity tile, String component, ServerPlayer player, GrowingBuffer data) {
    requireServer();
    if (player.level() != tile.getLevel() || component.isEmpty() || component.length() > 64)
      throw new IllegalArgumentException("Invalid component update target");
    sendS2CPacket(
        player,
        packet(
            SubPacketType.TileEntityBlockComponent,
            128,
            buffer -> {
              DataEncoder.encode(buffer, tile, false);
              buffer.writeString(component);
              buffer.writeVarInt(data.available());
              data.writeTo(buffer);
            }),
        true);
  }

  private void broadcast(
      Level world, BlockPos position, GrowingBuffer buffer, boolean limited, double x, double z) {
    var payload = new Ic2Payload(buffer.readableCopy());
    for (var player : getPlayersInRange(world, position, new ArrayList<ServerPlayer>())) {
      double dx = x - player.getX(), dz = z - player.getZ();
      if (!limited || dx * dx + dz * dz <= 400) PacketDistributor.sendToPlayer(player, payload);
    }
  }

  @Override
  public final void initiateTileEntityEvent(BlockEntity tile, int event, boolean limited) {
    requireServer();
    var position = tile.getBlockPos();
    var output =
        packet(
            SubPacketType.TileEntityEvent,
            32,
            buffer -> {
              DataEncoder.encode(buffer, tile, false);
              buffer.writeInt(event);
            });
    broadcast(
        tile.getLevel(), position, output, limited, position.getX() + 0.5, position.getZ() + 0.5);
  }

  @Override
  public final void initiateItemEvent(Player player, ItemStack stack, int event, boolean limited) {
    requireServer();
    if (stack == null || stack.isEmpty())
      throw new IllegalArgumentException("Item events require a nonempty item");
    var output =
        packet(
            SubPacketType.ItemEvent,
            256,
            buffer -> {
              DataEncoder.encode(buffer, player.getGameProfile(), false);
              DataEncoder.encode(buffer, stack, false);
              buffer.writeInt(event);
            });
    broadcast(
        player.level(), player.blockPosition(), output, limited, player.getX(), player.getZ());
  }

  @Override
  public void initiateClientItemEvent(ItemStack stack, int event) {
    throw new UnsupportedOperationException("Client item events require a client");
  }

  @Override
  public void initiateClientTileEntityEvent(BlockEntity tile, int event) {
    throw new UnsupportedOperationException("Client tile events require a client");
  }

  public void initiateRpc(int id, Class<? extends IRpcProvider<?>> provider, Object[] arguments) {
    throw new UnsupportedOperationException("RPC submission requires a client");
  }

  public void requestGUI(IHasGui inventory) {
    throw new UnsupportedOperationException("GUI requests require a client");
  }

  public void initiateKeyUpdate(int state) {
    throw new UnsupportedOperationException("Key submission requires a client");
  }

  public void sendLoginData() {}

  private void sendText(ServerPlayer player, String message, int kind) {
    requireServer();
    var output = new GrowingBuffer(256);
    output.writeString(message);
    output.flip();
    sendLargePacket(player, kind, output);
  }

  public final void sendChat(ServerPlayer player, String message) {
    sendText(player, message, 1);
  }

  public final void sendConsole(ServerPlayer player, String message) {
    sendText(player, message, 2);
  }

  private void sendMenu(OwnedMenu<?> menu, GrowingBuffer output) {
    if (isClient()) sendC2SPacket(output);
    else if (menu.getPlayer() instanceof ServerPlayer player) sendS2CPacket(player, output, true);
  }

  public final void sendContainerFields(OwnedMenu<?> menu, String... names) {
    for (var name : names) sendContainerField(menu, name);
  }

  public final void sendContainerField(OwnedMenu<?> menu, String name) {
    if (isClient() && IncomingActions.modifiable(menu.getClass(), name) == null) return;
    sendMenu(
        menu,
        packet(
            SubPacketType.ContainerData,
            128,
            buffer -> {
              buffer.writeInt(menu.containerId);
              writeFieldData(menu, name, buffer);
            }));
  }

  public final void sendContainerEvent(OwnedMenu<?> menu, String event) {
    if (event.isEmpty() || event.length() > 128)
      throw new IllegalArgumentException("Invalid menu event name");
    sendMenu(
        menu,
        packet(
            SubPacketType.ContainerEvent,
            64,
            buffer -> {
              buffer.writeInt(menu.containerId);
              buffer.writeString(event);
            }));
  }

  public final void sendHandHeldInvField(OwnedMenu<?> menu, String name) {
    if (!(menu.base instanceof HandheldStorage)
        || isClient() && IncomingActions.modifiable(menu.base.getClass(), name) == null) return;
    sendMenu(
        menu,
        packet(
            SubPacketType.HandHeldInvData,
            128,
            buffer -> {
              buffer.writeInt(menu.containerId);
              writeFieldData(menu.base, name, buffer);
            }));
  }

  final void sendLargePacket(ServerPlayer player, int kind, GrowingBuffer data) {
    requireServer();
    if (kind < 0 || kind > 2 || data.available() > 64 << 20)
      throw new IllegalArgumentException("Invalid large message kind/size");
    var compressed = new GrowingBuffer(16384);
    try (var deflater = new DeflaterOutputStream(compressed)) {
      data.writeTo(deflater);
    } catch (IOException error) {
      throw new IllegalStateException("Cannot compress network message", error);
    }
    compressed.flip();
    if (compressed.available() > 8 << 20)
      throw new IllegalArgumentException("Compressed message exceeds its limit");
    boolean first = true;
    while (compressed.hasAvailable()) {
      int count = Math.min(FRAME_BODY, compressed.available());
      boolean last = count == compressed.available();
      var frame = new GrowingBuffer(count + 2);
      SubPacketType.LargePacket.writeTo(frame);
      frame.writeByte((kind << 2) | (first ? 1 : 0) | (last ? 2 : 0));
      compressed.writeTo(frame, count);
      frame.flip();
      sendS2CPacket(player, frame, true);
      first = false;
    }
  }

  public void onPacket(ByteBuf packet, Player player) {
    if (!(player instanceof ServerPlayer actor))
      throw new IllegalArgumentException("Server packet requires a server player");
    try {
      IncomingActions.receive(GrowingBuffer.wrap(packet), actor);
    } catch (IOException | RuntimeException error) {
      ProjectLog.LOG.debug(DiagnosticArea.Network, error, "Rejected invalid client command");
    }
  }

  protected void onCommonPacketData(
      SubPacketType type, boolean server, GrowingBuffer input, Player player) throws IOException {
    if (server) throw new IOException("Client state cannot be applied to a server");
    ClientStateUpdates.receive(type, input, player);
  }

  public final void initiateExplosionEffect(Level world, Vec3 position, NativeBlast.Type type) {
    requireServer();
    var output =
        packet(
            SubPacketType.ExplosionEffect,
            32,
            buffer -> {
              DataEncoder.encode(buffer, world, false);
              DataEncoder.encode(buffer, position, false);
              DataEncoder.encode(buffer, type, false);
            });
    var payload = new Ic2Payload(output.readableCopy());
    for (var candidate : world.players())
      if (candidate instanceof ServerPlayer player && player.distanceToSqr(position) < 128)
        PacketDistributor.sendToPlayer(player, payload);
  }

  protected void sendC2SPacket(GrowingBuffer buffer) {
    throw new UnsupportedOperationException("Client transport requires a client");
  }

  protected final void sendS2CPacket(ServerPlayer player, GrowingBuffer buffer, boolean advance) {
    requireServer();
    byte[] bytes = advance ? buffer.copyBytes(buffer.available()) : buffer.readableCopy();
    PacketDistributor.sendToPlayer(player, new Ic2Payload(bytes));
  }

  static <T extends Collection<ServerPlayer>> T getPlayersInRange(
      Level world, BlockPos position, T result) {
    if (world instanceof ServerLevel server)
      result.addAll(
          server
              .getChunkSource()
              .chunkMap
              .getPlayers(new ChunkPos(position.getX() >> 4, position.getZ() >> 4), false));
    return result;
  }

  static void writeFieldData(Object owner, String specification, GrowingBuffer output)
      throws IOException {
    int separator = specification.indexOf('=');
    String name = separator < 0 ? specification : specification.substring(0, separator);
    if (name.isEmpty() || name.length() > 64)
      throw new IOException("Invalid synchronized field name");
    output.writeString(name);
    if (separator >= 0) DataEncoder.encode(output, specification.substring(separator + 1), true);
    else {
      var field = ReflectionUtil.getFieldRecursive(owner.getClass(), name);
      if (field == null) throw new IOException("Unknown synchronized field " + name);
      DataEncoder.encode(output, ReflectionUtil.getFieldValue(field, owner), true);
    }
  }

  protected static ByteBuf makePacket(GrowingBuffer buffer, boolean advance) {
    return buffer.toByteBuf(advance);
  }
}
