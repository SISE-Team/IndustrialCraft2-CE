package ic2ce.neoforge.network;

import ic2ce.neoforge.runtime.ProjectRuntime;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Protocol 2 transports owned payload bytes and releases the temporary Netty view after decoding.
 */
public final class NeoForgeNetworkHandler {
  private NeoForgeNetworkHandler() {}

  public record Ic2Payload(byte[] data) implements CustomPacketPayload {
    public static final Type<Ic2Payload> TYPE = new Type<>(ProjectRuntime.getIdentifier("m"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Ic2Payload> CODEC =
        ByteBufCodecs.BYTE_ARRAY.map(Ic2Payload::new, Ic2Payload::data).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("2")
        .playBidirectional(
            Ic2Payload.TYPE,
            Ic2Payload.CODEC,
            (payload, context) -> receive(payload, context, true),
            (payload, context) -> receive(payload, context, false));
  }

  private static void receive(Ic2Payload payload, IPayloadContext context, boolean server) {
    var view = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()));
    try {
      ProjectRuntime.network.get(server).onPacket(view, context.player());
    } finally {
      view.release();
    }
  }
}
