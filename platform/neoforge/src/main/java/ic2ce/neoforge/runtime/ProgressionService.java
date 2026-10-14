package ic2ce.neoforge.runtime;

import ic2ce.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Project progression grants the actual registered datapack advancement if that progression entry
 * exists.
 */
public final class ProgressionService {
  public void issueAchievement(Player player, String name) {
    if (!(player instanceof ServerPlayer actor) || name == null || name.isBlank()) return;
    String path =
        name.indexOf(':') >= 0
            ? name
            : "ic2:"
                + name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT);
    var id = Identifier.tryParse(path);
    if (id == null) return;
    var advancement = actor.level().getServer().getAdvancements().get(id);
    if (advancement == null) return;
    var progress = actor.getAdvancements().getOrStartProgress(advancement);
    if (progress.isDone()) return;
    var remaining = new java.util.ArrayList<String>();
    progress.getRemainingCriteria().forEach(remaining::add);
    for (var criterion : remaining) actor.getAdvancements().award(advancement, criterion);
  }
}
