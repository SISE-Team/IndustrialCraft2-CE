package ic2ce.verification;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Focused actual native result-slot and vanilla book placement, independent of slow block placement
 * coverage.
 */
public final class RecipeBookVerification {
  private static volatile boolean finished;
  private ServerPlayer player;
  private int age;

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (player == null && event.getEntity() instanceof ServerPlayer actor) {
      player = actor;
      player.setGameMode(GameType.SURVIVAL);
      player.setInvulnerable(true);
    }
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (player == null || finished || ++age < 40) return;
    var ledger = new AllItemsVerification();
    try {
      RecipeUsageVerification.run(player, ledger);
    } catch (Throwable failure) {
      ledger.check(false, "native crafting/book fixture exception: " + failure);
    }
    try {
      Files.writeString(
          Path.of("ic2-recipe-book-verification.json"),
          new GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  Map.of(
                      "scope",
                      "actual native crafting result slot and vanilla recipe-book auto-placement;"
                          + " separate from full item coverage",
                      "checks",
                      ledger.checkCount(),
                      "failures",
                      ledger.failureList(),
                      "phases",
                      ledger.phases,
                      "complete",
                      ledger.failureList().isEmpty())));
    } catch (Exception error) {
      throw new RuntimeException(error);
    }
    finished = true;
  }

  public static final class Client {
    private boolean hidden;

    @SubscribeEvent
    public void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      var minecraft = net.minecraft.client.Minecraft.getInstance();
      if (!hidden) {
        org.lwjgl.glfw.GLFW.glfwHideWindow(minecraft.getWindow().handle());
        hidden = true;
      }
      if (finished) minecraft.stop();
    }
  }
}
