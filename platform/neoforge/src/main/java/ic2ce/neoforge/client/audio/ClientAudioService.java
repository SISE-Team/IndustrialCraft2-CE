package ic2ce.neoforge.client.audio;

import ic2ce.neoforge.item.IHitSoundOverride;
import ic2ce.neoforge.audio.AudioService;
import ic2ce.neoforge.audio.PlaybackHandle;
import ic2ce.neoforge.runtime.SharedRandom;
import java.lang.ref.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;

/**
 * Weak identity audio owners are drained through a reference queue; reentrant callbacks run outside
 * the ownership lock.
 */
public class ClientAudioService extends AudioService {
  private final ReferenceQueue<Object> collected = new ReferenceQueue<>();
  private final Map<WeakObject, List<NativePlayback>> sounds = new HashMap<>();
  private NativePlayback[] snapshot = new NativePlayback[0];
  private boolean changed = true;

  private synchronized PlaybackHandle register(Object owner, NativePlayback sound) {
    Objects.requireNonNull(owner);
    sounds.computeIfAbsent(new WeakObject(owner, collected), key -> new ArrayList<>()).add(sound);
    changed = true;
    return sound;
  }

  @Override
  public PlaybackHandle createSound(
      Object owner,
      SoundEvent event,
      SoundSource source,
      LivingEntity entity,
      float volume,
      float pitch) {
    return register(owner, new NativePlayback(event, source, entity, volume, pitch));
  }

  @Override
  public PlaybackHandle createSound(
      Object owner,
      SoundEvent event,
      SoundSource source,
      BlockPos position,
      float volume,
      float pitch) {
    return register(owner, new NativePlayback(event, source, position, volume, pitch));
  }

  @Override
  public void playOnce(
      SoundEvent event, SoundSource source, float volume, float pitch, LivingEntity entity) {
    new EntityPlayback(event, source, volume, pitch, entity).playOnce();
  }

  @Override
  public void pauseAll() {
    Minecraft.getInstance().getSoundManager().pauseAllExcept();
  }

  @Override
  public void resumeAll() {
    Minecraft.getInstance().getSoundManager().resume();
  }

  @Override
  public synchronized void stopAll() {
    for (var group : sounds.values()) for (var sound : group) sound.stop();
  }

  @Override
  public synchronized WeakObject stopAll(Object owner) {
    if (owner == null) return null;
    var key = new WeakObject(owner);
    var group = sounds.get(key);
    if (group == null) return null;
    for (var sound : group) sound.stop();
    return key;
  }

  @Override
  public synchronized void removeAllSound(Object owner) {
    if (owner == null) return;
    var group = sounds.remove(new WeakObject(owner));
    if (group == null) return;
    for (var sound : group) sound.stop();
    changed = true;
  }

  @Override
  public synchronized void removeSound(Object owner, PlaybackHandle sound) {
    if (sound == null) return;
    sound.stop();
    if (owner == null) return;
    var key = new WeakObject(owner);
    var group = sounds.get(key);
    if (group != null && group.remove(sound)) {
      if (group.isEmpty()) sounds.remove(key);
      changed = true;
    }
  }

  private synchronized NativePlayback[] currentSounds() {
    Reference<?> key;
    while ((key = collected.poll()) != null) {
      var removed = sounds.remove(key);
      if (removed != null) {
        for (var sound : removed) sound.stop();
        changed = true;
      }
    }
    if (changed) {
      snapshot =
          sounds.values().stream().flatMap(Collection::stream).toArray(NativePlayback[]::new);
      changed = false;
    }
    return snapshot;
  }

  @Override
  public void tick() {
    for (var sound : currentSounds()) sound.tick();
  }

  public synchronized void clear() {
    for (var group : sounds.values()) for (var sound : group) sound.stop();
    sounds.clear();
    snapshot = new NativePlayback[0];
    changed = false;
    while (collected.poll() != null) {}
  }

  public static SoundInstance onSoundPlayed(SoundInstance original) {
    if (original.getSource() != SoundSource.BLOCKS) return original;
    String name = original.getIdentifier().getPath();
    boolean hit = name.endsWith(".hit");
    if (!hit && !name.endsWith(".break")) return original;
    var client = Minecraft.getInstance();
    var actor = client.player;
    if (actor == null
        || !(actor.getMainHandItem().getItem() instanceof IHitSoundOverride policy)
        || !(client.hitResult instanceof net.minecraft.world.phys.BlockHitResult target)
        || target.getType() != HitResult.Type.BLOCK) return original;
    // Native floor semantics preserve negative world coordinates.
    var position = BlockPos.containing(original.getX(), original.getY(), original.getZ());
    if (!position.equals(target.getBlockPos())) return original;
    var replacement =
        hit
            ? policy.getHitSoundForBlock(actor, actor.level(), position, actor.getMainHandItem())
            : policy.getBreakSoundForBlock(actor, actor.level(), position, actor.getMainHandItem());
    if (replacement == null || replacement.location().equals(original.getIdentifier()))
      return original;
    return new net.minecraft.client.resources.sounds.SimpleSoundInstance(
        replacement,
        original.getSource(),
        original.getVolume(),
        original.getPitch(),
        SharedRandom.INSTANCE,
        position);
  }

  public static final class WeakObject extends WeakReference<Object> {
    private final int hash;

    public WeakObject(Object owner) {
      this(owner, null);
    }

    private WeakObject(Object owner, ReferenceQueue<Object> queue) {
      super(Objects.requireNonNull(owner), queue);
      hash = System.identityHashCode(owner);
    }

    @Override
    public int hashCode() {
      return hash;
    }

    @Override
    public boolean equals(Object value) {
      var owner = get();
      return this == value
          || owner != null && value instanceof WeakObject other && owner == other.get();
    }
  }
}
