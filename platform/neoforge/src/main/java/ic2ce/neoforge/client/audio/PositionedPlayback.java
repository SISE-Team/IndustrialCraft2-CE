package ic2ce.neoforge.client.audio;

import ic2ce.engine.audio.CompletionCallbacks;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Native fixed-position playback owns completion actions independently of the active sound engine.
 */
public class PositionedPlayback extends SimpleSoundInstance implements FinishAwareAudio {
  private final CompletionCallbacks callbacks = new CompletionCallbacks();

  public PositionedPlayback(
      SoundEvent event, SoundSource source, float volume, float pitch, BlockPos position) {
    super(event, source, volume, pitch, ProjectRuntime.random, position);
  }

  public PositionedPlayback(
      SoundEvent event,
      SoundSource source,
      float volume,
      float pitch,
      BlockPos position,
      boolean repeat) {
    this(event, source, volume, pitch, position);
    looping = repeat;
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
  public void clearOnFinishListener() {
    callbacks.clear();
  }

  @Override
  public void finish() {
    callbacks.fire();
  }
}
