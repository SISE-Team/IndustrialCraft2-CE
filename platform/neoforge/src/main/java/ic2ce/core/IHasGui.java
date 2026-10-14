package ic2ce.core;

import ic2ce.core.network.GrowingBuffer;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.personal.OwnedDevice;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.io.*;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Native menu host keeps block and handheld identity payloads distinct and validates current world
 * ownership before opening.
 */
public interface IHasGui extends Container {
  OwnedMenu<?> createServerScreenHandler(int id, Player actor);

  OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer openingData);

  default void writeScreenOpenData(Player actor, InteractionHand hand, GrowingBuffer output)
      throws IOException {}

  default void onScreenClosed(Player actor) {}

  default boolean openManagedBe(Player actor, InteractionHand hand) {
    if (!(actor instanceof ServerPlayer player)
        || !(this instanceof BlockEntity target)
        || !player.isAlive()
        || target.isRemoved()
        || target.getLevel() != player.level()
        || player.level().getBlockEntity(target.getBlockPos()) != target) return false;
    if (this instanceof OwnedDevice secured && !secured.permitsAccess(player.getGameProfile()))
      return false;
    var payload = new GrowingBuffer(64);
    try {
      MenuDefinitions.writeManagedBeData(target, payload);
      writeScreenOpenData(player, Objects.requireNonNull(hand), payload);
    } catch (IOException error) {
      throw new UncheckedIOException("Cannot encode native block menu", error);
    }
    payload.flip();
    // The actual target chunk must reach the native client before its block-entity menu
    // constructor.
    var world = player.level();
    var chunk = world.getChunkAt(target.getBlockPos());
    var position = chunk.getPos();
    boolean pending = player.connection.chunkSender.isPending(position.pack());
    if (pending) player.connection.chunkSender.dropChunk(player, position);
    player.connection.send(
        chunk
            .getAuxLightManager(position)
            .sendLightDataTo(
                new ClientboundLevelChunkWithLightPacket(
                    chunk, world.getLightEngine(), null, null)));
    if (pending) {
      world.debugSynchronizers().startTrackingChunk(player, position);
      net.neoforged.neoforge.event.EventHooks.fireChunkSent(player, chunk, world);
    }
    return ProjectRuntime.envProxy.openHandledScreen(player, provider(getBeName(target)), payload);
  }

  default boolean openManagedItem(Player actor, InteractionHand hand, Integer subGuiId) {
    if (!(actor instanceof ServerPlayer player)
        || !player.isAlive()
        || hand == null
        || player.getItemInHand(hand).isEmpty()) return false;
    if (this instanceof ic2ce.neoforge.handheld.HandheldStorage storage
        && !storage.isThisContainer(player.getItemInHand(hand))) return false;
    var held = player.getItemInHand(hand).getItem();
    var payload = new GrowingBuffer(64);
    try {
      MenuDefinitions.writeManagedItemData(player, hand, subGuiId, payload);
      writeScreenOpenData(player, hand, payload);
    } catch (IOException error) {
      throw new UncheckedIOException("Cannot encode native handheld menu", error);
    }
    payload.flip();
    return ProjectRuntime.envProxy.openHandledScreen(
        player, provider(getItemName(held, subGuiId)), payload);
  }

  private MenuProvider provider(Component title) {
    return new MenuProvider() {
      public AbstractContainerMenu createMenu(int id, Inventory inventory, Player actor) {
        return createServerScreenHandler(id, actor);
      }

      public Component getDisplayName() {
        return title.copy();
      }
    };
  }

  static Component getBeName(BlockEntity target) {
    var id = BuiltInRegistries.BLOCK.getKey(target.getBlockState().getBlock());
    return Component.translatable(
        "container." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
  }

  static Component getItemName(Item item, Integer subGuiId) {
    var id = BuiltInRegistries.ITEM.getKey(item);
    return Component.translatable(
        "container."
            + id.getNamespace()
            + "."
            + id.getPath().replace('/', '.')
            + (subGuiId == null ? "" : "." + subGuiId));
  }
}
