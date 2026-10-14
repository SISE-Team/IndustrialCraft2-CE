package ic2ce.neoforge.client.audio;

import ic2ce.engine.audio.CompletionCallbacks;
import ic2ce.neoforge.audio.PlaybackHandle;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * One native playback session owns distinct loop/one-shot instances and completes once per started
 * session.
 */
public class NativePlayback extends PlaybackHandle {
  private final SoundInstance repeatInstance, onceInstance;
  private final CompletionCallbacks callbacks = new CompletionCallbacks();
  private SoundInstance active;
  private boolean started;
  public final net.minecraft.client.sounds.SoundManager vanillaManager =
      Minecraft.getInstance().getSoundManager();

  public NativePlayback(
      SoundEvent event, SoundSource source, BlockPos position, float volume, float pitch) {
    repeatInstance =
        new PositionedPlayback(
            event, source, volume, pitch, Objects.requireNonNull(position), true);
    onceInstance = new PositionedPlayback(event, source, volume, pitch, position);
  }

  public NativePlayback(
      SoundEvent event, SoundSource source, LivingEntity entity, float volume, float pitch) {
    var repeat = new EntityPlayback(event, source, volume, pitch, Objects.requireNonNull(entity));
    repeat.setLooping(true);
    repeatInstance = repeat;
    var once = new EntityPlayback(event, source, volume, pitch, entity);
    once.setLooping(false);
    onceInstance = once;
  }

  @Override
  public void play() {
    start(repeatInstance);
  }

  @Override
  public void playOnce() {
    start(onceInstance);
  }

  private void start(SoundInstance next) {
    if (started && active == next && vanillaManager.isActive(next)) return;
    stop();
    active = next;
    started = true;
    vanillaManager.play(next);
  }

  @Override
  public void stop() {
    if (active != null) vanillaManager.stop(active);
    active = null;
    started = false;
  }

  @Override
  public boolean isPlaying() {
    return started && active != null && vanillaManager.isActive(active);
  }

  @Override
  public void addOnFinishListener(Runnable action) {
    callbacks.add(action);
  }

  @Override
  public void onFinish(Runnable action) {
    callbacks.replace(action);
  }

  @Override
  public void tick() {
    if (started && active != null && !vanillaManager.isActive(active)) {
      active = null;
      started = false;
      callbacks.fire();
    }
  }
}
