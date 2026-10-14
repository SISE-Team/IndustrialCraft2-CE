package ic2ce.neoforge.client.audio;

import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

/**
 * Real initialized native sound instances execute owned finish callbacks and manager identity
 * controls.
 */
public final class AudioContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run() {
    var failures = new ArrayList<String>();
    int checks = 0;
    var events = new ArrayList<Integer>();
    var sound =
        new PositionedPlayback(
            SoundDefinitions.ITEM_TREETAP_USE, SoundSource.BLOCKS, 1, 1, BlockPos.ZERO);
    sound.addOnFinishListener(() -> events.add(1));
    sound.addOnFinishListener(() -> events.add(2));
    sound.finish();
    checks++;
    if (!events.equals(List.of(1, 2)))
      failures.add(
          "audio: actual positioned instance completion recursively captures itself or loses a"
              + " callback");
    sound.onFinish(() -> events.add(3));
    sound.finish();
    checks++;
    if (!events.equals(List.of(1, 2, 3)))
      failures.add("audio: replacement callback is discarded or retains a stale prior callback");
    sound.onFinish(
        () -> {
          events.add(4);
          sound.clearOnFinishListener();
          sound.addOnFinishListener(() -> events.add(6));
        });
    sound.addOnFinishListener(() -> events.add(5));
    sound.finish();
    sound.finish();
    checks++;
    if (!events.equals(List.of(1, 2, 3, 4, 5, 6)))
      failures.add(
          "audio: reentrant listener replacement corrupts current native completion ordering");
    var actor = Minecraft.getInstance().player;
    var entity =
        new EntityPlayback(SoundDefinitions.ITEM_TREETAP_USE, SoundSource.PLAYERS, 1, 1, actor);
    entity.onFinish(() -> events.add(7));
    entity.finish();
    checks++;
    if (events.getLast() != 7)
      failures.add("audio: actual entity-bound completion listener cannot run");
    var manager = new ClientAudioService();
    var owner = new Object();
    var first =
        manager.createSound(
            owner, SoundDefinitions.ITEM_TREETAP_USE, SoundSource.BLOCKS, BlockPos.ZERO, 1, 1);
    var second =
        manager.createSound(
            owner, SoundDefinitions.ITEM_DRILL_IDLE, SoundSource.PLAYERS, actor, 1, 1);
    first.stop();
    second.stop();
    manager.tick();
    checks++;
    if (first.isPlaying() || second.isPlaying() || manager.stopAll(owner) == null)
      failures.add(
          "audio: actual native stopped handles lose their registered owner or report playing");
    manager.removeSound(owner, first);
    manager.removeAllSound(owner);
    checks++;
    if (manager.stopAll(owner) != null)
      failures.add("audio: explicit owner removal leaves stale playback sessions");
    var a = new ClientAudioService.WeakObject(owner);
    var b = new ClientAudioService.WeakObject(owner);
    var different = new ClientAudioService.WeakObject(new Object());
    checks++;
    if (!a.equals(b) || a.hashCode() != b.hashCode() || a.equals(different))
      failures.add("audio: distinct equal-sounding objects cannot share identity audio ownership");
    a.clear();
    b.clear();
    checks++;
    if (a.equals(b) || !a.equals(a))
      failures.add(
          "audio: collected weak owners accidentally become equal to another removed owner");
    try {
      var repeating = NativePlayback.class.getDeclaredField("repeatInstance");
      var once = NativePlayback.class.getDeclaredField("onceInstance");
      repeating.setAccessible(true);
      once.setAccessible(true);
      for (var playback :
          List.of(
              new NativePlayback(
                  SoundDefinitions.ITEM_DRILL_IDLE, SoundSource.BLOCKS, BlockPos.ZERO, 1, 1),
              new NativePlayback(
                  SoundDefinitions.ITEM_DRILL_IDLE, SoundSource.PLAYERS, actor, 1, 1))) {
        checks++;
        if (!((net.minecraft.client.resources.sounds.SoundInstance) repeating.get(playback))
                .isLooping()
            || ((net.minecraft.client.resources.sounds.SoundInstance) once.get(playback))
                .isLooping())
          failures.add(
              "audio: actual native loop and one-shot factory states are indistinguishable");
      }
    } catch (ReflectiveOperationException failure) {
      failures.add("audio: native factory configuration could not be observed: " + failure);
    }
    manager.clear();
    return new Result(checks, List.copyOf(failures));
  }
}
