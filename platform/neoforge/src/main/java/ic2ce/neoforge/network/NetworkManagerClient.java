package ic2ce.neoforge.network;

import com.mojang.authlib.GameProfile;
import ic2ce.api.network.INetworkItemEventListener;
import ic2ce.api.network.INetworkTileEntityEventListener;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.handheld.IHandHeldInventory;
import ic2ce.engine.network.FramedCompression;
import ic2ce.neoforge.network.NeoForgeNetworkHandler.Ic2Payload;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ComponentTypes;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.explosion.NativeBlast;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import io.netty.buffer.ByteBuf;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Independent client submissions and owning-thread handling of bounded server values/effects. */
@OnlyIn(Dist.CLIENT)
public final class NetworkManagerClient extends NetworkManager {
  private final FramedCompression largePackets = new FramedCompression(8 << 20, 64 << 20);

  @FunctionalInterface
  private interface Writer {
    void write(GrowingBuffer buffer) throws IOException;
  }

  @Override
  protected boolean isClient() {
    return true;
  }

  public void resetIncomingFrames() {
    largePackets.reset();
  }

  private void submit(SubPacketType type, int capacity, Writer writer) {
    var output = new GrowingBuffer(capacity);
    type.writeTo(output);
    try {
      writer.write(output);
    } catch (IOException error) {
      throw new IllegalStateException("Cannot encode client " + type, error);
    }
    output.flip();
    sendC2SPacket(output);
  }

  @Override
  public void initiateClientItemEvent(ItemStack stack, int event) {
    if (stack == null || stack.isEmpty()) return;
    submit(
        SubPacketType.ItemEvent,
        128,
        output -> {
          DataEncoder.encode(output, stack, false);
          output.writeInt(event);
        });
  }

  @Override
  public void initiateClientTileEntityEvent(BlockEntity tile, int event) {
    submit(
        SubPacketType.TileEntityEvent,
        32,
        output -> {
          DataEncoder.encode(output, tile, false);
          output.writeInt(event);
        });
  }

  @Override
  public void initiateKeyUpdate(int state) {
    if ((state & ~127) != 0) throw new IllegalArgumentException("Invalid client keyboard mask");
    submit(SubPacketType.KeyUpdate, 5, output -> output.writeInt(state));
  }

  @Override
  public void initiateRpc(int id, Class<? extends IRpcProvider<?>> provider, Object[] arguments) {
    if (arguments.length > 32) throw new IllegalArgumentException("Too many RPC arguments");
    submit(
        SubPacketType.Rpc,
        256,
        output -> {
          output.writeInt(id);
          output.writeString(provider.getName());
          DataEncoder.encode(output, arguments, true);
        });
  }

  @Override
  public void requestGUI(IHasGui inventory) {
    var player = Minecraft.getInstance().player;
    if (player == null) return;
    if (!(inventory instanceof BlockEntity)
        && !(player.getMainHandItem().getItem() instanceof IHandHeldInventory)
        && !(player.getOffhandItem().getItem() instanceof IHandHeldInventory)) return;
    submit(
        SubPacketType.RequestGUI,
        32,
        output -> {
          boolean held = !(inventory instanceof BlockEntity);
          output.writeBoolean(held);
          if (!held) DataEncoder.encode(output, inventory, false);
        });
  }

  @Override
  public void onPacket(ByteBuf packet, Player player) {
    try {
      receive(GrowingBuffer.wrap(packet), player);
    } catch (IOException | RuntimeException error) {
      largePackets.reset();
      ProjectRuntime.log.debug(DiagnosticArea.Network, error, "Rejected invalid server message");
    }
  }

  private void receive(GrowingBuffer input, Player player) throws IOException {
    var type = SubPacketType.read(input, false);
    if (type == null) throw new IOException("Invalid server message kind");
    switch (type) {
      case LargePacket -> {
        int flags = input.readUnsignedByte();
        largePackets.frame(flags, System.nanoTime());
        input.writeTo(largePackets);
        if ((flags & 2) != 0) {
          var message = largePackets.finish();
          var body = GrowingBuffer.wrap(message.bytes());
          switch (message.kind()) {
            case 0 -> TeUpdate.receive(body);
            case 1 -> {
              String text = body.readString();
              if (body.hasAvailable()) throw new IOException("Trailing chat bytes");
              ProjectRuntime.sideProxy.requestTick(
                  false,
                  () -> {
                    for (String line : text.split("[\\r\\n]+"))
                      ProjectRuntime.sideProxy.messagePlayer(null, line);
                  });
            }
            case 2 -> {
              String text = body.readString();
              if (body.hasAvailable()) throw new IOException("Trailing console bytes");
              System.out.println(text);
            }
            default -> throw new IOException("Unknown compressed message kind");
          }
        }
      }
      case TileEntityEvent -> {
        var encoded = DataEncoder.decodeDeferred(input, BlockEntity.class);
        int event = input.readInt();
        ProjectRuntime.sideProxy.requestTick(
            false,
            () -> {
              BlockEntity tile = DataEncoder.getValue(encoded, null);
              if (tile instanceof INetworkTileEntityEventListener listener)
                listener.onNetworkEvent(event);
            });
      }
      case ItemEvent -> {
        var profile = DataEncoder.decode(input, GameProfile.class);
        var stack = DataEncoder.decode(input, ItemStack.class);
        int event = input.readInt();
        ProjectRuntime.sideProxy.requestTick(
            false,
            () -> {
              var world = Minecraft.getInstance().level;
              if (world == null
                  || stack.isEmpty()
                  || !(stack.getItem() instanceof INetworkItemEventListener listener)) return;
              var actor = world.getPlayerByUUID(profile.id());
              if (actor != null) listener.onNetworkEvent(stack, actor, event);
            });
      }
      case ExplosionEffect -> {
        var encoded = DataEncoder.decodeDeferred(input, Level.class);
        var position = DataEncoder.decode(input, Vec3.class);
        var effect = DataEncoder.decodeEnum(input, NativeBlast.Type.class);
        if (!Double.isFinite(position.x)
            || !Double.isFinite(position.y)
            || !Double.isFinite(position.z)) throw new IOException("Invalid effect coordinates");
        ProjectRuntime.sideProxy.requestTick(
            false,
            () -> {
              Level world = DataEncoder.getValue(encoded, null);
              if (world != null) explosion(world, position, effect);
            });
      }
      case Rpc -> RpcHandler.processRpcResponse(input);
      case TileEntityBlockComponent -> {
        var dimension = (Identifier) DataEncoder.decode(input, DataEncoder.EncodedType.Identifier);
        var position = DataEncoder.decode(input, BlockPos.class);
        String name = input.readString();
        if (name.length() > 64) throw new IOException("Invalid component name");
        var componentClass = ComponentTypes.getClass(name);
        if (componentClass == null) throw new IOException("Unknown tile component");
        int count = input.readVarInt();
        if (count > 65536 || count > input.available())
          throw new IOException("Invalid component frame size");
        var bytes = input.copyBytes(count);
        ProjectRuntime.sideProxy.requestTick(
            false,
            () -> {
              var world = Minecraft.getInstance().level;
              if (world == null || !world.dimension().identifier().equals(dimension)) return;
              if (world.getBlockEntity(position) instanceof NativeBlockEntity tile) {
                var component = tile.getComponent(componentClass);
                if (component == null) return;
                try (var body = new DataInputStream(new ByteArrayInputStream(bytes))) {
                  component.onNetworkUpdate(body);
                } catch (IOException error) {
                  throw new IllegalStateException("Cannot apply tile component state", error);
                }
              }
            });
      }
      default -> {
        if (player == null) throw new IOException("Client state requires a player");
        ClientStateUpdates.receive(type, input, player);
      }
    }
    if (input.hasAvailable()) throw new IOException("Trailing server message bytes");
  }

  private static void explosion(Level world, Vec3 position, NativeBlast.Type type) {
    SoundEvent sound =
        switch (type) {
          case Normal -> SoundEvents.GENERIC_EXPLODE.value();
          case Electrical -> SoundDefinitions.MACHINE_OVERLOAD;
          case Heat -> SoundEvents.FIRE_EXTINGUISH;
          case Nuclear -> SoundDefinitions.BLOCK_NUKE_EXPLODE;
        };
    boolean large = type == NativeBlast.Type.Normal || type == NativeBlast.Type.Heat;
    float pitch =
        large
            ? (1 + (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.2F) * 0.7F
            : 1;
    world.playLocalSound(
        position.x, position.y, position.z, sound, SoundSource.BLOCKS, large ? 4 : 1, pitch, true);
    world.addParticle(ParticleTypes.EXPLOSION_EMITTER, position.x, position.y, position.z, 0, 0, 0);
  }

  @Override
  protected void sendC2SPacket(GrowingBuffer buffer) {
    if (Minecraft.getInstance().getConnection() == null)
      throw new IllegalStateException("Client is disconnected");
    ClientPacketDistributor.sendToServer(new Ic2Payload(buffer.copyBytes(buffer.available())));
  }
}
