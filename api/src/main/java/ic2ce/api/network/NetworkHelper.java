package ic2ce.api.network;

import ic2ce.api.util.CoreAccessRef;
import java.util.Objects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Public network routing resolves the current installed service and rejects unattached or
 * wrong-side native targets.
 */
public final class NetworkHelper {
  private NetworkHelper() {}

  private static boolean client(BlockEntity tile) {
    Objects.requireNonNull(tile);
    var world = tile.getLevel();
    if (world == null) throw new IllegalStateException("Network target has no native world");
    return world.isClientSide();
  }

  private static String field(String name) {
    if (name == null || name.isBlank() || name.length() > 256)
      throw new IllegalArgumentException("Invalid network field name");
    return name;
  }

  public static void updateTileEntityField(BlockEntity tile, String name) {
    getNetworkManager(client(tile)).updateTileEntityField(tile, field(name));
  }

  public static void initiateTileEntityEvent(BlockEntity tile, int event, boolean limited) {
    getNetworkManager(client(tile)).initiateTileEntityEvent(tile, event, limited);
  }

  public static void initiateItemEvent(Player actor, ItemStack stack, int event, boolean limited) {
    Objects.requireNonNull(actor);
    Objects.requireNonNull(stack);
    getNetworkManager(actor.level().isClientSide()).initiateItemEvent(actor, stack, event, limited);
  }

  public static void sendInitialData(BlockEntity tile) {
    getNetworkManager(client(tile)).sendInitialData(tile);
  }

  public static void initiateClientTileEntityEvent(BlockEntity tile, int event) {
    if (!client(tile)) throw new IllegalStateException("Client event requires a client world");
    getNetworkManager(true).initiateClientTileEntityEvent(tile, event);
  }

  public static void initiateClientItemEvent(ItemStack stack, int event) {
    getNetworkManager(true).initiateClientItemEvent(Objects.requireNonNull(stack), event);
  }

  public static INetworkManager getNetworkManager(boolean forClient) {
    var services = CoreAccessRef.get();
    return Objects.requireNonNull(
        forClient ? services.getClientNetworkManager() : services.getServerNetworkManager(),
        "Native network service unavailable");
  }
}
