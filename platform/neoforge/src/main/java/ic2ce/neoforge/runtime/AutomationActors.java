package ic2ce.neoforge.runtime;

import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Automation actor identity is stable per dimension and its profile is built once without retaining
 * native worlds.
 */
public final class AutomationActors {
  private static final Map<Identifier, GameProfile> IDENTITIES = new ConcurrentHashMap<>();

  private AutomationActors() {}

  public static Player get(Level level) {
    if (!(level instanceof ServerLevel world)) return null;
    var identity =
        IDENTITIES.computeIfAbsent(
            world.dimension().identifier(),
            dimension -> {
              String name = "[IC2 " + dimension + "]";
              return new GameProfile(
                  UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name);
            });
    return EnvProxy.get().createFakePlayer(world, identity);
  }
}
