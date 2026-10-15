package ic2ce.neoforge.client;

import ic2ce.core.proxy.SideProxyClient;
import ic2ce.core.util.Keyboard;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.*;

/**
 * Live client bindings publish only input transitions and explicitly reset local state on
 * world/player replacement.
 */
@OnlyIn(Dist.CLIENT)
public class KeyboardClient extends Keyboard {
  private static final KeyMapping.Category CATEGORY =
      new KeyMapping.Category(ProjectRuntime.getIdentifier("ic2"));
  private static final KeyMapping ALT = new KeyMapping("ALT Key", 342, CATEGORY),
      BOOST = new KeyMapping("Boost Key", 341, CATEGORY),
      MODE = new KeyMapping("Mode Switch Key", 77, CATEGORY),
      SIDE = new KeyMapping("Side Inventory Key", 67, CATEGORY),
      HUD = new KeyMapping("Hub Expand Key", 88, CATEGORY);
  private static boolean registered;
  private int last = -1;
  private Player previous;

  public KeyboardClient() {
    synchronized (KeyboardClient.class) {
      if (!registered) {
        for (var binding : List.of(ALT, BOOST, MODE, SIDE, HUD))
          SideProxyClient.envProxy.registerKeyBinding(binding);
        registered = true;
      }
    }
  }

  @Override
  public void sendKeyUpdate() {
    var client = Minecraft.getInstance();
    if (client == null) return;
    var player = client.player;
    if (player != previous) {
      removePlayerReferences(previous);
      previous = player;
      last = -1;
    }
    if (player == null || client.getConnection() == null) return;
    int bits = 0;
    if (client.screen == null) {
      if (ALT.isDown()) bits |= 1 << Key.alt.ordinal();
      if (BOOST.isDown()) bits |= 1 << Key.boost.ordinal();
      if (client.options.keyUp.isDown()) bits |= 1 << Key.forward.ordinal();
      if (MODE.isDown()) bits |= 1 << Key.modeSwitch.ordinal();
      if (client.options.keyJump.isDown()) bits |= 1 << Key.jump.ordinal();
      if (SIDE.isDown()) bits |= 1 << Key.sideInventory.ordinal();
      if (HUD.isDown()) bits |= 1 << Key.hubMode.ordinal();
      var observers = watchers;
      if (!observers.isEmpty()) {
        var state = Key.fromInt(bits);
        for (var observer : observers) observer.checkForKey(state);
        bits = Key.toInt(state);
      }
    }
    if (bits != last) {
      ProjectRuntime.network.get(false).initiateKeyUpdate(bits);
      processKeyUpdate(player, bits);
      last = bits;
    }
  }
}
