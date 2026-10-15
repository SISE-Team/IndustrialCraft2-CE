package ic2ce.neoforge.block;

import ic2ce.neoforge.audio.PlaybackHandle;
import ic2ce.neoforge.runtime.SideServices;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Native active state is authoritative; the client owns independent main/sub-loop audio. */
public class ActiveBlockEntity extends InventoryBlockEntity {
  protected PlaybackHandle loopingSound, subLoopingSound, startSound, stopSound, interruptSound;
  private boolean subSoundRequested, audioActive;
  private int audioGeneration;

  public ActiveBlockEntity(
      BlockEntityType<? extends InventoryBlockEntity> type, BlockPos position, BlockState state) {
    super(type, position, state);
  }

  private boolean client() {
    return getLevel() != null && getLevel().isClientSide();
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (client()) {
      initSound();
      synchronizeAudio();
    }
  }

  @Override
  protected void onUnloaded() {
    audioGeneration++;
    audioActive = false;
    if (client()) SideServices.audio().removeAllSound(this);
    clearSound();
    super.onUnloaded();
  }

  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    synchronizeAudio();
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.add("subSoundRequested");
    return fields;
  }

  @Override
  public void onNetworkUpdate(String field) {
    super.onNetworkUpdate(field);
    if (client() && (field.equals("active") || field.equals("subSoundRequested")))
      synchronizeAudio();
  }

  public void setActiveState(boolean active, boolean subLoop) {
    if (active) activate(subLoop);
    else shutdown(false);
  }

  public void activate(boolean subLoop) {
    requestSubLoop(subLoop);
    setActive(true);
    if (client()) synchronizeAudio();
  }

  private void requestSubLoop(boolean value) {
    if (subSoundRequested == value) return;
    subSoundRequested = value;
    if (getLevel() != null && !client())
      SideServices.network(true).updateTileEntityField(this, "subSoundRequested");
  }

  public void shutdown(boolean interrupted) {
    boolean previouslyActive = getActive();
    requestSubLoop(false);
    setActive(false);
    if (client()) {
      audioGeneration++;
      stopStartSound();
      stopLoopingSound();
      audioActive = false;
      if (previouslyActive) {
        if (interrupted) playInterruptSound();
        else playStopSound();
      }
    }
  }

  private void synchronizeAudio() {
    if (!client()) return;
    if (getActive() != audioActive) {
      audioActive = getActive();
      if (audioActive) startPlaySound(subSoundRequested);
      else {
        audioGeneration++;
        stopStartSound();
        stopLoopingSound();
        playStopSound();
      }
    }
    if (!audioActive) return;
    if (loopingSound != null
        && !loopingSound.isPlaying()
        && (startSound == null || !startSound.isPlaying())) loopingSound.play();
    if (subLoopingSound != null) {
      if (subSoundRequested) {
        if (!subLoopingSound.isPlaying()) subLoopingSound.play();
      } else subLoopingSound.stop();
    }
  }

  public boolean startPlaySound(boolean subLoop) {
    if (!client()) return false;
    if (startSound == null) return playLoopingSound(subLoop);
    int generation = ++audioGeneration;
    startSound.onFinish(
        () -> {
          if (generation == audioGeneration && !isRemoved() && getActive())
            playLoopingSound(subSoundRequested);
        });
    startSound.playOnce();
    return true;
  }

  public boolean playLoopingSound(boolean subLoop) {
    if (!client()) return false;
    boolean played = false;
    if (loopingSound != null) {
      loopingSound.play();
      played = true;
    }
    if (subLoopingSound != null) {
      if (subLoop) {
        subLoopingSound.play();
        played = true;
      } else subLoopingSound.stop();
    }
    return played;
  }

  public boolean stopLoopingSound() {
    boolean stopped = false;
    if (loopingSound != null) {
      loopingSound.stop();
      stopped = true;
    }
    if (subLoopingSound != null) {
      subLoopingSound.stop();
      stopped = true;
    }
    return stopped;
  }

  public boolean stopStartSound() {
    if (startSound == null) return false;
    startSound.stop();
    return true;
  }

  public boolean playStopSound() {
    if (!client() || stopSound == null) return false;
    stopSound.playOnce();
    return true;
  }

  public boolean playInterruptSound() {
    if (!client() || interruptSound == null) return false;
    interruptSound.playOnce();
    return true;
  }

  protected boolean hasSound() {
    return loopingSound != null
        || subLoopingSound != null
        || startSound != null
        || stopSound != null
        || interruptSound != null;
  }

  private PlaybackHandle create(SoundEvent event) {
    return !client() || event == null
        ? null
        : SideServices.audio().createSound(
            this, event, SoundSource.BLOCKS, worldPosition, 1, 1);
  }

  protected void initSound() {
    updateStartSound();
    updateLoopingSound();
    updateSubLoopingSound();
    updateInterruptSound();
    updateStopSound();
  }

  protected void updateStartSound() {
    if (startSound == null) startSound = create(getStartSoundEvent());
  }

  protected void updateStopSound() {
    if (stopSound == null) stopSound = create(getStopSoundEvent());
  }

  protected void updateLoopingSound() {
    if (loopingSound == null) loopingSound = create(getLoopingSoundEvent());
  }

  protected void updateSubLoopingSound() {
    if (subLoopingSound == null) subLoopingSound = create(getSubLoopingSoundEvent());
  }

  protected void updateInterruptSound() {
    if (interruptSound == null) interruptSound = create(getInterruptSoundEvent());
  }

  protected void clearSound() {
    loopingSound = subLoopingSound = startSound = stopSound = interruptSound = null;
  }

  protected boolean isLoopingSoundPlaying() {
    return loopingSound != null && loopingSound.isPlaying();
  }

  protected boolean isLoopingSoundIdling() {
    return loopingSound != null && !loopingSound.isPlaying();
  }

  public SoundEvent getStartSoundEvent() {
    return null;
  }

  public SoundEvent getLoopingSoundEvent() {
    return null;
  }

  public SoundEvent getSubLoopingSoundEvent() {
    return null;
  }

  public SoundEvent getStopSoundEvent() {
    return null;
  }

  public SoundEvent getInterruptSoundEvent() {
    return null;
  }
}
