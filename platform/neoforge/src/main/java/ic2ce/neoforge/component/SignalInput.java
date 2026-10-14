package ic2ce.neoforge.component;

import ic2ce.neoforge.block.NativeBlockEntity;
import java.util.*;
import net.minecraft.world.level.block.Block;

/**
 * Copy-on-write signal observers and ordered modifiers survive callback edits and detach stale
 * linked inputs.
 */
public class SignalInput extends NativeComponent {
  private int signal;
  private List<IRedstoneChangeHandler> observers = List.of();
  private List<IRedstoneModifier> modifiers = List.of();
  private Link outbound;
  private boolean updating, pending;

  public SignalInput(NativeBlockEntity parent) {
    super(parent);
  }

  @Override
  public void onLoaded() {
    update();
  }

  @Override
  public void onUnloaded() {
    unlinkOutbound();
    unlinkInbound();
  }

  @Override
  public void onNeighborChange(Block block) {
    update();
  }

  public void update() {
    var world = parent.getLevel();
    if (world == null || world.isClientSide()) return;
    if (updating) {
      pending = true;
      return;
    }
    updating = true;
    try {
      int passes = 0;
      do {
        pending = false;
        int next = Math.clamp(world.getBestNeighborSignal(parent.getBlockPos()), 0, 15);
        for (var modifier : modifiers) next = Math.clamp(modifier.getRedstoneInput(next), 0, 15);
        if (next != signal) {
          signal = next;
          for (var observer : observers) observer.onRedstoneChange(next);
        }
      } while (pending && ++passes < 64);
    } finally {
      updating = false;
    }
  }

  public int getRedstoneInput() {
    return signal;
  }

  public boolean hasRedstoneInput() {
    return signal > 0;
  }

  public void subscribe(IRedstoneChangeHandler observer) {
    Objects.requireNonNull(observer);
    if (observers.contains(observer)) return;
    var next = new ArrayList<>(observers);
    next.add(observer);
    observers = List.copyOf(next);
  }

  public void unsubscribe(IRedstoneChangeHandler observer) {
    Objects.requireNonNull(observer);
    if (!observers.contains(observer)) return;
    var next = new ArrayList<>(observers);
    next.remove(observer);
    observers = List.copyOf(next);
  }

  public void addRedstoneModifier(IRedstoneModifier modifier) {
    Objects.requireNonNull(modifier);
    if (modifiers.contains(modifier)) return;
    var next = new ArrayList<>(modifiers);
    next.add(modifier);
    modifiers = List.copyOf(next);
  }

  public void addRedstoneModifiers(Collection<IRedstoneModifier> added) {
    var validated = List.copyOf(added);
    var next = new LinkedHashSet<>(modifiers);
    next.addAll(validated);
    modifiers = List.copyOf(next);
  }

  public void removeRedstoneModifier(IRedstoneModifier modifier) {
    Objects.requireNonNull(modifier);
    if (!modifiers.contains(modifier)) return;
    var next = new ArrayList<>(modifiers);
    next.remove(modifier);
    modifiers = List.copyOf(next);
  }

  public void removeRedstoneModifiers(Collection<IRedstoneModifier> removed) {
    var validated = List.copyOf(removed);
    var next = new ArrayList<>(modifiers);
    next.removeAll(validated);
    modifiers = List.copyOf(next);
  }

  public boolean isLinked() {
    return outbound != null;
  }

  public SignalInput getLinkReceiver() {
    return outbound == null ? null : outbound.receiver;
  }

  public Collection<SignalInput> getLinkedOrigins() {
    var origins = new ArrayList<SignalInput>();
    for (var modifier : modifiers) if (modifier instanceof Link link) origins.add(link.origin);
    return List.copyOf(origins);
  }

  public void linkTo(SignalInput receiver) {
    Objects.requireNonNull(receiver);
    if (outbound != null) {
      if (outbound.receiver == receiver) return;
      throw new IllegalStateException("Signal origin already linked");
    }
    var seen = Collections.newSetFromMap(new IdentityHashMap<SignalInput, Boolean>());
    for (var next = receiver; next != null; next = next.getLinkReceiver())
      if (next == this || !seen.add(next))
        throw new IllegalArgumentException("Cyclic native signal link");
    var link = new Link(this, receiver);
    outbound = link;
    receiver.addRedstoneModifier(link);
    subscribe(link);
    receiver.update();
  }

  public void unlinkOutbound() {
    var link = outbound;
    if (link == null) return;
    outbound = null;
    unsubscribe(link);
    link.receiver.removeRedstoneModifier(link);
    link.receiver.update();
  }

  public void unlinkInbound() {
    for (var origin : getLinkedOrigins()) origin.unlinkOutbound();
  }

  public interface IRedstoneChangeHandler {
    void onRedstoneChange(int level);
  }

  public interface IRedstoneModifier {
    int getRedstoneInput(int level);
  }

  private record Link(SignalInput origin, SignalInput receiver)
      implements IRedstoneChangeHandler, IRedstoneModifier {
    @Override
    public void onRedstoneChange(int level) {
      receiver.update();
    }

    @Override
    public int getRedstoneInput(int level) {
      return Math.max(level, origin.signal);
    }
  }
}
