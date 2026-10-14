package ic2ce.neoforge.audio;

/** Platform-neutral playback contract. Server services do not construct audio handles. */
public abstract class PlaybackHandle {
  public abstract void play();

  public abstract void stop();

  public abstract void playOnce();

  public abstract boolean isPlaying();

  public abstract void addOnFinishListener(Runnable listener);

  public abstract void onFinish(Runnable listener);

  public abstract void tick();
}
