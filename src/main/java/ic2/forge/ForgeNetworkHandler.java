package ic2.forge;

import ic2.core.IC2;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Carries the raw IC2 protocol stream ("ic2:m" channel) over the 26.1.2
 * payload system. One payload type per direction, opaque byte array body.
 */
public final class ForgeNetworkHandler {
   public record Ic2Payload(byte[] data) implements CustomPacketPayload {
      public static final CustomPacketPayload.Type<Ic2Payload> TYPE =
         new CustomPacketPayload.Type<>(IC2.getIdentifier("m"));
      public static final StreamCodec<RegistryFriendlyByteBuf, Ic2Payload> CODEC =
         ByteBufCodecs.BYTE_ARRAY.map(Ic2Payload::new, Ic2Payload::data).cast();

      @Override
      public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   private ForgeNetworkHandler() {}

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1");
      // 26.1.2: a payload type maps to one registration; separate playToServer/playToClient
      // calls with the same type throw "already registered". playBidirectional registers
      // the single ic2:m type for both flows: first handler = serverbound (C2S),
      // second = clientbound (S2C).
      registrar.playBidirectional(
         Ic2Payload.TYPE,
         Ic2Payload.CODEC,
         ForgeNetworkHandler::handleC2S,
         ForgeNetworkHandler::handleS2C
      );
   }

   private static void handleC2S(Ic2Payload payload, IPayloadContext context) {
      IC2.network.get(true).onPacket(new net.minecraft.network.FriendlyByteBuf(
         io.netty.buffer.Unpooled.wrappedBuffer(payload.data())), context.player());
   }

   private static void handleS2C(Ic2Payload payload, IPayloadContext context) {
      IC2.network.get(false).onPacket(new net.minecraft.network.FriendlyByteBuf(
         io.netty.buffer.Unpooled.wrappedBuffer(payload.data())), IC2.sideProxy.getPlayerInstance());
   }
}
