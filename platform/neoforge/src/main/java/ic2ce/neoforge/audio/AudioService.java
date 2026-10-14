package ic2ce.neoforge.audio;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;

/**
 * Dedicated servers use this explicit inert service; client playback is supplied by the actual
 * native audio implementation.
 */
public class AudioService {
  public PlaybackHandle createSound(
      Object owner,
      SoundEvent event,
      SoundSource source,
      LivingEntity entity,
      float volume,
      float pitch) {
    return null;
  }

  public PlaybackHandle createSound(
      Object owner,
      SoundEvent event,
      SoundSource source,
      BlockPos position,
      float volume,
      float pitch) {
    return null;
  }

  public void playOnce(
      SoundEvent event, SoundSource source, float volume, float pitch, LivingEntity entity) {}

  public void pauseAll() {}

  public void resumeAll() {}

  public void stopAll() {}

  public Object stopAll(Object owner) {
    return null;
  }

  public void removeAllSound(Object owner) {}

  public void removeSound(Object owner, PlaybackHandle sound) {
    if (sound != null) sound.stop();
  }

  public void tick() {}
}
