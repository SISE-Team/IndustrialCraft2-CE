package ic2.core;

import ic2.core.IC2;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.util.Util;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IHasGui extends Container {
   ContainerBase<?> createServerScreenHandler(int var1, Player var2);

   default void writeScreenOpenData(Player player, InteractionHand hand, GrowingBuffer buffer) throws IOException {
   }

   ContainerBase<?> createClientScreenHandler(int var1, Inventory var2, GrowingBuffer var3);

   default void onScreenClosed(Player player) {
   }

   default boolean openManagedBe(Player player, InteractionHand hand) {
      BlockEntity target = (BlockEntity)this;
      if (!player.isAlive() || target.isRemoved() || target.getLevel() != player.level()) return false;
      if (player instanceof net.minecraft.server.level.ServerPlayer actor) {
         // Menus may open immediately after teleporting, before the chunk sender
         // finishes its batch. Deliver the target chunk before the menu payload
         // so the client factory can resolve its block entity deterministically.
         var level = actor.level();
         var chunk = level.getChunkAt(target.getBlockPos());
         var chunkPos = chunk.getPos();
         boolean pending = actor.connection.chunkSender.isPending(chunkPos.pack());
         if (pending) actor.connection.chunkSender.dropChunk(actor, chunkPos);
         actor.connection.send(chunk.getAuxLightManager(chunkPos).sendLightDataTo(
            new net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null)));
         if (pending) {
            level.debugSynchronizers().startTrackingChunk(actor, chunkPos);
            net.neoforged.neoforge.event.EventHooks.fireChunkSent(actor, chunk, level);
         }
      }
      GrowingBuffer buffer = new GrowingBuffer(50);

      try {
         Ic2ScreenHandlers.writeManagedBeData((BlockEntity)this, buffer);
         this.writeScreenOpenData(player, hand, buffer);
      } catch (IOException var5) {
         throw new RuntimeException(var5);
      }

      buffer.flip();
      return IC2.envProxy.openHandledScreen(player, new MenuProvider() {
         public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player playerx) {
            return IHasGui.this.createServerScreenHandler(syncId, playerx);
         }

         public Component getDisplayName() {
            return IHasGui.getBeName((BlockEntity)IHasGui.this);
         }
      }, buffer);
   }

   static Component getBeName(BlockEntity be) {
      Identifier id = Util.getName(be.getBlockState().getBlock());
      return Component.translatable(String.format("container.%s.%s", id.getNamespace(), id.getPath().replace('/', '.')));
   }

   default boolean openManagedItem(Player player, InteractionHand hand, Integer subGuiId) {
      GrowingBuffer buffer = new GrowingBuffer(50);

      try {
         Ic2ScreenHandlers.writeManagedItemData(player, hand, subGuiId, buffer);
         this.writeScreenOpenData(player, hand, buffer);
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      }

      buffer.flip();
      final Item item = player.getItemInHand(hand).getItem();
      return IC2.envProxy.openHandledScreen(player, new MenuProvider() {
         public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player playerx) {
            return IHasGui.this.createServerScreenHandler(syncId, playerx);
         }

         public Component getDisplayName() {
            return IHasGui.getItemName(item, subGuiId);
         }
      }, buffer);
   }

   static Component getItemName(Item item, Integer subGuiId) {
      Identifier id = Util.getName(item);
      return Component.translatable(
         String.format("container.%s.%s%s", id.getNamespace(), id.getPath().replace('/', '.'), subGuiId != null ? String.format(".%d", subGuiId) : "")
      );
   }
}
