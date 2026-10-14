package ic2ce.core.proxy;

import ic2ce.api.tile.IRotorProvider;
import ic2ce.core.util.Keyboard;
import ic2ce.neoforge.audio.AudioService;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.io.*;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;

/** Dedicated-server services own deferred tasks until a native server becomes available. */
public final class SideProxyServer implements SideProxy {
  private final AudioService audio = new AudioService();
  private final Keyboard input = new Keyboard();
  private final Queue<Runnable> waiting = new ArrayDeque<>();

  @Override
  public void preInit() {}

  @Override
  public void onPostInit() {}

  @Override
  public AudioService getSoundManager() {
    return audio;
  }

  @Override
  public Keyboard getKeyboard() {
    return input;
  }

  @Override
  public boolean isSimulating() {
    return true;
  }

  @Override
  public boolean isRendering() {
    return false;
  }

  @Override
  public void requestTick(boolean simulating, Runnable action) {
    if (!simulating)
      throw new IllegalArgumentException("Client tick requested from a dedicated server");
    Objects.requireNonNull(action);
    synchronized (waiting) {
      var server = ProjectRuntime.envProxy.getServer();
      if (server == null) waiting.add(action);
      else server.execute(action);
    }
  }

  @Override
  public void onServerAvailable(MinecraftServer server) {
    Objects.requireNonNull(server);
    synchronized (waiting) {
      Runnable action;
      while ((action = waiting.poll()) != null) server.execute(action);
    }
  }

  @Override
  public void displayError(String format, Object... arguments) {
    displayError0(format, arguments);
  }

  static void displayError0(String format, Object... arguments) {
    throw new IllegalStateException("IndustrialCraft2-CE: " + message(format, arguments));
  }

  @Override
  public void displayError(Exception error, String format, Object... arguments) {
    throw new IllegalStateException("IndustrialCraft2-CE: " + message(format, arguments), error);
  }

  static void displayError(SideProxy proxy, Exception error, String format, Object... arguments) {
    var trace = new StringWriter();
    error.printStackTrace(new PrintWriter(trace));
    proxy.displayError("%s\n%s", message(format, arguments), trace.toString());
  }

  private static String message(String format, Object... arguments) {
    return arguments == null || arguments.length == 0
        ? format
        : String.format(Locale.ROOT, format, arguments);
  }

  @Override
  public void playSoundSp(SoundEvent event, SoundSource source, float volume, float pitch) {}

  @Override
  public void playSoundOnce(Entity entity, SoundEvent event, float volume, float pitch) {
    if (entity != null && !entity.level().isClientSide())
      entity
          .level()
          .playSound(null, entity.blockPosition(), event, SoundSource.PLAYERS, volume, pitch);
  }

  @Override
  public Player getPlayerInstance() {
    return null;
  }

  @Override
  public Level getPlayerWorld() {
    return null;
  }

  @Override
  public Level getWorld(MinecraftServer server, Identifier dimension) {
    return server == null
        ? null
        : server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
  }

  @Override
  public RecipeManager getRecipeManager() {
    var server = ProjectRuntime.envProxy.getServer();
    if (server == null) throw new IllegalStateException("No server recipe generation is available");
    return server.getRecipeManager();
  }

  @Override
  public File getMinecraftDir() {
    return new File(".");
  }

  @Override
  public void messagePlayer(Player actor, String key, Object... arguments) {
    if (actor instanceof ServerPlayer)
      actor.sendSystemMessage(
          Component.translatable(key, (Object[]) getMessageComponents(arguments)));
  }

  static Component[] getMessageComponents(Object... arguments) {
    if (arguments == null) return new Component[0];
    var result = new Component[arguments.length];
    for (int index = 0; index < result.length; index++)
      result[index] =
          switch (arguments[index]) {
            case Component component -> component.copy();
            case String text when text.startsWith("ic2.") -> Component.translatable(text);
            case null -> Component.literal("null");
            default -> Component.literal(String.valueOf(arguments[index]));
          };
    return result;
  }

  @Override
  public <T extends BlockEntity & IRotorProvider> void registerRotorProvider(
      BlockEntityType<T> type) {}
}
