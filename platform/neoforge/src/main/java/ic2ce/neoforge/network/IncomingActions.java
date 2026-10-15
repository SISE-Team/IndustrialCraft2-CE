package ic2ce.neoforge.network;


import ic2ce.api.network.ClientModifiable;
import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.network.INetworkItemEventListener;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.handheld.IHandHeldInventory;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.handheld.HandheldStorage;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Client commands parse to bounded values and act only on the player's authoritative
 * world/inventory/menu.
 */
final class IncomingActions {
  private IncomingActions() {}

  private static final InteractionHand[] HANDS = InteractionHand.values();
  private static final EquipmentSlot[] ARMOR = {
    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };
  private static final ClassValue<Map<String, Field>> writable =
      new ClassValue<>() {
        @Override
        protected Map<String, Field> computeValue(Class<?> type) {
          var fields = new HashMap<String, Field>();
          for (var current = type; current != null; current = current.getSuperclass()) {
            for (var field : current.getDeclaredFields())
              if (!Modifier.isStatic(field.getModifiers())
                  && field.isAnnotationPresent(ClientModifiable.class)
                  && field.trySetAccessible()) fields.putIfAbsent(field.getName(), field);
          }
          return Map.copyOf(fields);
        }
      };

  static Field modifiable(Class<?> type, String name) {
    return name != null && name.length() <= 64 ? writable.get(type).get(name) : null;
  }

  static boolean reachable(ServerPlayer player, BlockEntity tile) {
    return tile != null
        && !tile.isRemoved()
        && tile.getLevel() == player.level()
        && player.distanceToSqr(
                tile.getBlockPos().getX() + 0.5,
                tile.getBlockPos().getY() + 0.5,
                tile.getBlockPos().getZ() + 0.5)
            <= 64;
  }

  static ItemStack ownedItem(ServerPlayer player, Item item) {
    for (var hand : HANDS) {
      var stack = player.getItemInHand(hand);
      if (stack.is(item)) return stack;
    }
    for (var slot : ARMOR) {
      var stack = player.getItemBySlot(slot);
      if (stack.is(item)) return stack;
    }
    var inventory = player.getInventory();
    for (int index = 0; index < inventory.getContainerSize(); index++) {
      var stack = inventory.getItem(index);
      if (stack.is(item)) return stack;
    }
    return ItemStack.EMPTY;
  }

  private static OwnedMenu<?> menu(ServerPlayer player, int id) {
    return player.containerMenu instanceof OwnedMenu<?> container
            && container.containerId == id
            && container.stillValid(player)
        ? container
        : null;
  }

  private static boolean hasMenu(ServerPlayer player, BlockEntity tile) {
    return player.containerMenu instanceof OwnedMenu<?> container
        && container.base == tile
        && container.stillValid(player);
  }

  private static String fieldName(GrowingBuffer input) throws IOException {
    String name = input.readString();
    if (name.isEmpty() || name.length() > 64) throw new IOException("Invalid client field name");
    return name;
  }

  private static void update(Object target, String name, Object value, ServerPlayer player) {
    var field = modifiable(target.getClass(), name);
    if (field == null) return;
    var resolved = DataEncoder.getValue(value, player.level().getServer());
    if (field.getType().isEnum() && resolved instanceof Integer ordinal) {
      var constants = field.getType().getEnumConstants();
      if (ordinal < 0 || ordinal >= constants.length) return;
      resolved = constants[ordinal];
    }
    try {
      ReflectionUtil.setValue(target, field, resolved);
      if (target instanceof ic2ce.neoforge.network.ClientFieldValidator validator)
        validator.acceptClientField(name);
    } catch (RuntimeException error) {
      ProjectRuntime.log.debug(
          DiagnosticArea.Network, error, "Rejected incompatible client field value");
    }
  }

  static void receive(GrowingBuffer input, ServerPlayer player) throws IOException {
    var type = SubPacketType.read(input, true);
    if (type == null) throw new IOException("Invalid client command kind");
    Runnable action =
        switch (type) {
          case ItemEvent -> {
            var advertised = DataEncoder.decode(input, ItemStack.class);
            int event = input.readInt();
            yield () -> {
              var stack = ownedItem(player, advertised.getItem());
              if (!stack.isEmpty() && stack.getItem() instanceof INetworkItemEventListener listener)
                listener.onNetworkEvent(stack, player, event);
            };
          }
          case KeyUpdate -> {
            int state = input.readInt();
            if ((state & ~127) != 0) throw new IOException("Invalid keyboard state mask");
            yield () -> ProjectRuntime.keyboard.processKeyUpdate(player, state);
          }
          case TileEntityEvent -> {
            var encoded = DataEncoder.decodeDeferred(input, BlockEntity.class);
            int event = input.readInt();
            yield () -> {
              BlockEntity tile = DataEncoder.getValue(encoded, player.level().getServer());
              if (reachable(player, tile)
                  && tile instanceof INetworkClientTileEntityEventListener listener) {
                listener.onNetworkEvent(player, event);
                tile.setChanged();
              }
            };
          }
          case TileEntityData -> {
            var encoded = DataEncoder.decodeDeferred(input, BlockEntity.class);
            String name = fieldName(input);
            var value = DataEncoder.decode(input);
            yield () -> {
              BlockEntity tile = DataEncoder.getValue(encoded, player.level().getServer());
              if (reachable(player, tile)
                  && hasMenu(player, tile)
                  && modifiable(tile.getClass(), name) != null) {
                update(tile, name, value, player);
                tile.setChanged();
              }
            };
          }
          case RequestGUI -> {
            boolean hand = input.readBoolean();
            var encoded = hand ? null : DataEncoder.decodeDeferred(input, BlockEntity.class);
            yield () -> {
              if (hand) {
                for (var side : HANDS) {
                  var stack = player.getItemInHand(side);
                  if (!stack.isEmpty() && stack.getItem() instanceof IHandHeldInventory held) {
                    held.getInventory(player, side, stack).openManagedItem(player, side, null);
                    break;
                  }
                }
              } else {
                BlockEntity tile = DataEncoder.getValue(encoded, player.level().getServer());
                if (reachable(player, tile) && tile instanceof IHasGui gui)
                  gui.openManagedBe(player, InteractionHand.MAIN_HAND);
              }
            };
          }
          case Rpc -> {
            var body = input.copyBytes(input.available());
            yield () -> {
              try {
                RpcHandler.processRpcRequest(GrowingBuffer.wrap(body), player);
              } catch (IOException error) {
                ProjectRuntime.log.debug(
                    DiagnosticArea.Network, error, "Rejected malformed RPC request");
              }
            };
          }
          case PlayerItemData -> {
            int slot = input.readByte();
            var item = DataEncoder.decode(input, Item.class);
            int count = input.readVarInt();
            if (count > 32) throw new IOException("Too many player item values");
            var values = new Object[count];
            for (int index = 0; index < count; index++) values[index] = DataEncoder.decode(input);
            yield () -> {
              if (!(slot >= 0 && slot < 9 || slot == 40)
                  || slot >= player.getInventory().getContainerSize()) return;
              var stack = player.getInventory().getItem(slot);
              if (stack.isEmpty()
                  || stack.getItem() != item
                  || !(item instanceof IPlayerItemDataListener listener)) return;
              for (int index = 0; index < values.length; index++)
                values[index] = DataEncoder.getValue(values[index], player.level().getServer());
              listener.onPlayerItemNetworkData(player, slot, values);
            };
          }
          case ContainerData, HandHeldInvData -> {
            int id = input.readInt();
            String name = fieldName(input);
            var value = DataEncoder.decode(input);
            yield () -> {
              var container = menu(player, id);
              if (container == null) return;
              if (type == SubPacketType.ContainerData) update(container, name, value, player);
              else if (container.base instanceof HandheldStorage)
                update(container.base, name, value, player);
              if (container.base instanceof BlockEntity tile && reachable(player, tile))
                tile.setChanged();
            };
          }
          case ContainerEvent -> {
            int id = input.readInt();
            String event = input.readString();
            if (event.length() > 128) throw new IOException("Invalid container command name");
            yield () -> {
              var container = menu(player, id);
              if (container == null) return;
              container.onContainerEvent(event);
              if (container.base instanceof BlockEntity tile) tile.setChanged();
            };
          }
          default -> throw new IOException("Command kind is not client-writable");
        };
    if (input.hasAvailable()) throw new IOException("Trailing client command bytes");
    Runnable dispatch =
        () -> {
          if (!player.isRemoved()
              && (type == SubPacketType.KeyUpdate || player.isAlive() && !player.isSpectator()))
            action.run();
        };
    var server = player.level().getServer();
    if (server.isSameThread()) dispatch.run();
    else server.execute(dispatch);
  }
}
