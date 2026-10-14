package ic2ce.neoforge;

import ic2ce.core.event.*;
import ic2ce.neoforge.recipe.RecipeBootstrap;
import ic2ce.neoforge.recipe.codec.RecipeGeneration;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/**
 * Native client event lifetimes own synchronized recipes, fog, input/audio ticks and complete
 * disconnect cleanup.
 */
public final class ClientEventHandlerNeoForge {
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void recipes(RecipesReceivedEvent event) {
    RecipeGeneration.setClientRecipes(event.getRecipeMap());
    EventHandler.onInitCommonSetup();
    RecipeBootstrap.registerRecipes();
  }

  @SubscribeEvent
  public void joined(ClientPlayerNetworkEvent.LoggingIn event) {
    EventHandler.onInitCommonSetup();
  }

  @SubscribeEvent
  public void left(ClientPlayerNetworkEvent.LoggingOut event) {
    RecipeGeneration.setClientRecipes(RecipeMap.EMPTY);
    EventHandlerClient.onDisconnect();
    ic2ce.neoforge.crafting.client.WorkbenchScreen.clearReturnSession();
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void screenOpening(ScreenEvent.Opening event) {
    if (!event.isCanceled())
      ic2ce.neoforge.crafting.client.WorkbenchScreen.beforeOpening(
          event.getCurrentScreen(), event.getNewScreen());
  }

  @SubscribeEvent
  public void screenClosing(ScreenEvent.Closing event) {
    ic2ce.neoforge.crafting.client.WorkbenchScreen.onClosing(event.getScreen());
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Pre event) {
    TickHandler.onClientTick();
  }

  @SubscribeEvent
  public void afterTick(ClientTickEvent.Post event) {
    ic2ce.neoforge.crafting.client.WorkbenchScreen.afterClientTick();
  }

  @SubscribeEvent
  public void density(ViewportEvent.RenderFog event) {
    float density = EventHandlerClient.onSetupFogDensity(event.getCamera().getBlockAtCamera());
    if (density >= 0) {
      event.setNearPlaneDistance(-8);
      event.setFarPlaneDistance(Math.max(.01F, density * .5F));
    }
  }

  @SubscribeEvent
  public void color(ViewportEvent.ComputeFogColor event) {
    int color = EventHandlerClient.onRenderFogColor(event.getCamera().getBlockAtCamera());
    if (EventHandlerClient.hasProjectFluid(event.getCamera().getBlockAtCamera())) {
      event.setRed((color >>> 16 & 255) / 255F);
      event.setGreen((color >>> 8 & 255) / 255F);
      event.setBlue((color & 255) / 255F);
    }
  }

  @SubscribeEvent
  public void audio(PlaySoundEvent event) {
    var sound = event.getSound();
    var replacement = EventHandlerClient.onSoundPlayed(sound);
    if (replacement != sound) event.setSound(replacement);
  }
}
