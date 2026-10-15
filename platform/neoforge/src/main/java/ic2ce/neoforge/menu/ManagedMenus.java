package ic2ce.neoforge.menu;

import ic2ce.neoforge.handheld.IHandHeldInventory;
import ic2ce.neoforge.handheld.IHandHeldSubInventory;
import ic2ce.neoforge.network.DataEncoder;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.runtime.ProjectRuntime;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Block and held-item menu packets have distinct admission and provider resolution protocols. */
public final class ManagedMenus {
  private ManagedMenus() {}

  @SuppressWarnings("unchecked")
  public static <T extends AbstractContainerMenu> MenuType<T> block(String name) {
    return ProjectRuntime.envProxy.registerExtendedScreenHandler(
        ProjectRuntime.getIdentifier(name),
        (id, inventory, data) -> (T) readBlock(id, inventory, data));
  }

  @SuppressWarnings("unchecked")
  public static <T extends AbstractContainerMenu> MenuType<T> item(String name) {
    return ProjectRuntime.envProxy.registerExtendedScreenHandler(
        ProjectRuntime.getIdentifier(name),
        (id, inventory, data) -> (T) readItem(id, inventory, data));
  }

  static OwnedMenu<?> readBlock(int id, Inventory inventory, ByteBuf data) {
    var buffer = GrowingBuffer.wrap(data);
    try {
      Object value =
          DataEncoder.getValue(
              DataEncoder.decode(buffer, DataEncoder.EncodedType.TileEntity), null);
      if (!(value instanceof BlockEntity entity)
          || entity.isRemoved()
          || entity.getLevel() != inventory.player.level()
          || entity.getLevel().getBlockEntity(entity.getBlockPos()) != entity
          || !(entity instanceof IHasGui provider))
        throw invalid("missing or stale block provider");
      return requireMenu(provider.createClientScreenHandler(id, inventory, buffer));
    } catch (IOException failure) {
      throw new IllegalStateException("Cannot decode IndustrialCraft2-CE block menu", failure);
    }
  }

  static OwnedMenu<?> readItem(int id, Inventory inventory, ByteBuf data) {
    var buffer = GrowingBuffer.wrap(data);
    int location = buffer.readInt();
    boolean nested = buffer.readBoolean();
    int nestedId = nested ? buffer.readVarInt() : 0;
    Player player = inventory.player;
    InteractionHand hand;
    if (location == -InteractionHand.OFF_HAND.ordinal()) hand = InteractionHand.OFF_HAND;
    else if (location >= 0 && location < 9 && location == player.getInventory().getSelectedSlot())
      hand = InteractionHand.MAIN_HAND;
    else throw invalid("held item location changed");
    var stack = player.getItemInHand(hand);
    if (stack.isEmpty() || !(stack.getItem() instanceof IHandHeldInventory item))
      throw invalid("held item has no menu");
    IHasGui provider;
    if (nested) {
      if (nestedId < 0 || nestedId > 4095 || !(item instanceof IHandHeldSubInventory sub))
        throw invalid("invalid nested item menu");
      provider = sub.getSubInventory(player, hand, stack, nestedId);
    } else provider = item.getInventory(player, hand, stack);
    if (provider == null) throw invalid("item provider rejected the requested menu");
    return requireMenu(provider.createClientScreenHandler(id, inventory, buffer));
  }

  private static OwnedMenu<?> requireMenu(OwnedMenu<?> menu) {
    if (menu == null) throw invalid("provider created no native menu");
    return menu;
  }

  private static IllegalStateException invalid(String reason) {
    return new IllegalStateException("Invalid IndustrialCraft2-CE menu packet: " + reason);
  }

  public static void writeBlock(BlockEntity entity, GrowingBuffer buffer) throws IOException {
    Objects.requireNonNull(entity);
    if (entity.isRemoved()
        || entity.getLevel() == null
        || entity.getLevel().getBlockEntity(entity.getBlockPos()) != entity)
      throw invalid("detached server block provider");
    DataEncoder.encode(buffer, entity, false);
  }

  public static void writeItem(
      Player player, InteractionHand hand, Integer nestedId, GrowingBuffer buffer) {
    Objects.requireNonNull(player);
    Objects.requireNonNull(hand);
    var stack = player.getItemInHand(hand);
    if (stack.isEmpty() || !(stack.getItem() instanceof IHandHeldInventory))
      throw invalid("server held item has no menu");
    if (nestedId != null
        && (nestedId < 0 || nestedId > 4095 || !(stack.getItem() instanceof IHandHeldSubInventory)))
      throw invalid("server nested item menu is invalid");
    buffer.writeInt(
        hand == InteractionHand.MAIN_HAND
            ? player.getInventory().getSelectedSlot()
            : -hand.ordinal());
    buffer.writeBoolean(nestedId != null);
    if (nestedId != null) buffer.writeVarInt(nestedId);
  }
}
