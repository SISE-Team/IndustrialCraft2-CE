package ic2ce.core.network;

import ic2ce.core.util.ReflectionUtil;
import ic2ce.neoforge.handheld.HandheldStorage;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.io.IOException;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/** Bounded authoritative server state, applied to the current client menu and inventory. */
final class ClientStateUpdates {
  private ClientStateUpdates() {}

  private static String name(GrowingBuffer input, int maximum) throws IOException {
    var name = input.readString();
    if (name.isEmpty() || name.length() > maximum)
      throw new IOException("Invalid state field/event name");
    return name;
  }

  static void receive(SubPacketType type, GrowingBuffer input, Player player) throws IOException {
    Runnable apply =
        switch (type) {
          case PlayerItemData -> {
            int slot = input.readByte();
            Item item = DataEncoder.decode(input, Item.class);
            int count = input.readVarInt();
            if (count > 32) throw new IOException("Player item state quantity limit exceeded");
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
                values[index] = DataEncoder.getValue(values[index], null);
              listener.onPlayerItemNetworkData(player, slot, values);
            };
          }
          case ContainerData, HandHeldInvData -> {
            int id = input.readInt();
            String field = name(input, 64);
            Object value = DataEncoder.decode(input);
            yield () -> {
              if (!(player.containerMenu instanceof OwnedMenu<?> menu) || menu.containerId != id)
                return;
              Object target = type == SubPacketType.ContainerData ? menu : menu.base;
              if (type == SubPacketType.HandHeldInvData && !(target instanceof HandheldStorage))
                return;
              ReflectionUtil.setValueRecursive(target, field, DataEncoder.getValue(value, null));
            };
          }
          case ContainerEvent -> {
            int id = input.readInt();
            String event = name(input, 128);
            yield () -> {
              if (player.containerMenu instanceof OwnedMenu<?> menu && menu.containerId == id)
                menu.onContainerEvent(event);
            };
          }
          default -> throw new IOException("Unsupported shared client state message: " + type);
        };
    if (input.hasAvailable()) throw new IOException("Trailing client state bytes");
    ProjectRuntime.sideProxy.requestTick(false, apply);
  }
}
