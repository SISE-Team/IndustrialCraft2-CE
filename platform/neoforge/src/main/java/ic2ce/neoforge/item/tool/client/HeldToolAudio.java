package ic2ce.neoforge.item.tool.client;

import ic2ce.neoforge.audio.PlaybackHandle;
import ic2ce.neoforge.item.tool.PoweredTool;
import ic2ce.neoforge.runtime.SideServices;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Main-thread audio sessions belong to actual visible players, rather than globally shared item
 * singletons.
 */
@EventBusSubscriber(modid = "ic2", value = Dist.CLIENT)
public final class HeldToolAudio {
  private record Session(
      PoweredTool tool, SoundEvent idle, SoundEvent start, SoundEvent stop, PlaybackHandle loop) {}

  private static final IdentityHashMap<Player, Session> SESSIONS = new IdentityHashMap<>();
  private static final Set<Player> SEEN =
      java.util.Collections.newSetFromMap(new IdentityHashMap<>());

  private HeldToolAudio() {}

  private static void release(Player owner, Session session, boolean stop) {
    if (session.loop != null) SideServices.audio().removeSound(owner, session.loop);
    if (stop && session.stop != null && !owner.isRemoved())
      SideServices.audio().playOnce(session.stop, SoundSource.PLAYERS, 1, 1, owner);
  }

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    var world = Minecraft.getInstance().level;
    SEEN.clear();
    if (world != null)
      for (var owner : world.players()) {
        if (owner.isRemoved()) continue;
        var held = owner.getMainHandItem();
        if (!(held.getItem() instanceof PoweredTool tool) || !tool.canUse(held)) continue;
        if (tool instanceof ic2ce.neoforge.item.tool.NanoBladeBase) {
          var data = ic2ce.neoforge.item.StackTag.get(held);
          if (data == null || !data.getBooleanOr("active", false)) continue;
        }
        var idle = tool.heldIdleSound(owner, held);
        var start = tool.heldStartSound(owner, held);
        var stop = tool.heldStopSound(owner, held);
        if (idle == null && start == null && stop == null) continue;
        SEEN.add(owner);
        var existing = SESSIONS.get(owner);
        if (existing != null
            && existing.tool == tool
            && existing.idle == idle
            && existing.start == start
            && existing.stop == stop) {
          if (existing.loop != null && !existing.loop.isPlaying()) existing.loop.play();
          continue;
        }
        if (existing != null) release(owner, existing, true);
        var loop =
            idle == null
                ? null
                : SideServices.audio().createSound(
                    owner, idle, SoundSource.PLAYERS, owner, 1, 1);
        if (loop != null) loop.play();
        if (start != null)
          SideServices.audio().playOnce(start, SoundSource.PLAYERS, 1, 1, owner);
        SESSIONS.put(owner, new Session(tool, idle, start, stop, loop));
      }
    var entries = SESSIONS.entrySet().iterator();
    while (entries.hasNext()) {
      var entry = entries.next();
      if (!SEEN.contains(entry.getKey())) {
        release(entry.getKey(), entry.getValue(), world != null && entry.getKey().level() == world);
        entries.remove();
      }
    }
    SEEN.clear();
  }
}
