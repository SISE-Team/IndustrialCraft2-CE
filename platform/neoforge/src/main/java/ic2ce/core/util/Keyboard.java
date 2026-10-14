package ic2ce.core.util;

import ic2ce.api.util.IKeyboard;
import java.util.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.*;

/**
 * Seven bounded input bits per weak player identity avoid per-packet EnumSet allocation and
 * cross-thread map races.
 */
public class Keyboard implements IKeyboard {
  protected volatile List<IKeyWatcher> watchers = List.of();
  private final Map<Player, Integer> states = new WeakHashMap<>();
  private static final int MASK = (1 << 7) - 1;

  private synchronized boolean get(Player player, Key key) {
    return player != null && (states.getOrDefault(player, 0) & (1 << key.ordinal())) != 0;
  }

  @Override
  public boolean isAltKeyDown(Player player) {
    return get(player, Key.alt);
  }

  @Override
  public boolean isBoostKeyDown(Player player) {
    return get(player, Key.boost);
  }

  @Override
  public boolean isForwardKeyDown(Player player) {
    return get(player, Key.forward);
  }

  @Override
  public boolean isJumpKeyDown(Player player) {
    return get(player, Key.jump);
  }

  @Override
  public boolean isModeSwitchKeyDown(Player player) {
    return get(player, Key.modeSwitch);
  }

  @Override
  public boolean isSideinventoryKeyDown(Player player) {
    return get(player, Key.sideInventory);
  }

  @Override
  public boolean isHudModeKeyDown(Player player) {
    return get(player, Key.hubMode);
  }

  @Override
  public boolean isSneakKeyDown(Player player) {
    return player != null && player.isShiftKeyDown();
  }

  public void sendKeyUpdate() {}

  public synchronized void processKeyUpdate(Player player, int bits) {
    if (player == null) return;
    int state = bits & MASK;
    if (state == 0) states.remove(player);
    else states.put(player, state);
  }

  public synchronized void removePlayerReferences(Player player) {
    if (player != null) states.remove(player);
  }

  public synchronized void addKeyWatcher(IKeyWatcher watcher) {
    Objects.requireNonNull(watcher);
    if (watchers.contains(watcher)) return;
    var next = new ArrayList<>(watchers);
    next.add(watcher);
    watchers = List.copyOf(next);
  }

  public boolean isKeyDown(Player player, IKeyWatcher watcher) {
    return get(player, Objects.requireNonNull(watcher).getRepresentation());
  }

  public interface IKeyWatcher {
    @OnlyIn(Dist.CLIENT)
    void checkForKey(Set<Key> keys);

    Key getRepresentation();
  }

  protected enum Key {
    alt,
    boost,
    forward,
    modeSwitch,
    jump,
    sideInventory,
    hubMode;
    public static final Key[] keys = values();

    public static int toInt(Iterable<Key> pressed) {
      int state = 0;
      for (var key : pressed) state |= 1 << Objects.requireNonNull(key).ordinal();
      return state;
    }

    public static Set<Key> fromInt(int bits) {
      var pressed = EnumSet.noneOf(Key.class);
      for (var key : keys) if ((bits & (1 << key.ordinal())) != 0) pressed.add(key);
      return pressed;
    }
  }
}
