package ic2ce.neoforge.client.audio;

import ic2ce.engine.audio.CompletionCallbacks;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * Attached native playback follows one entity; one-shot playback is an actual non-looping client
 * instance.
 */
public class EntityPlayback extends EntityBoundSoundInstance implements FinishAwareAudio {
  protected final Entity entity;
  protected final SoundEvent soundEvent;
  protected final net.minecraft.client.sounds.SoundManager vanillaManager;
  private final CompletionCallbacks callbacks = new CompletionCallbacks();

  public EntityPlayback(
      SoundEvent event, SoundSource source, float volume, float pitch, Entity entity) {
    super(event, source, volume, pitch, entity, ProjectRuntime.random.nextLong());
    this.entity = entity;
    soundEvent = event;
    vanillaManager = Minecraft.getInstance().getSoundManager();
    looping = true;
  }

  public void setLooping(boolean enabled) {
    looping = enabled;
  }

  public void playOnce() {
    looping = false;
    vanillaManager.play(this);
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
