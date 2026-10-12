package ic2.forge;

import ic2.core.event.EventHandlerClient;
import ic2.core.event.TickHandler;
import ic2.core.proxy.SideProxyClient;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeFogColor;
import net.neoforged.neoforge.client.event.ViewportEvent.RenderFog;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class ClientEventHandlerForge {
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void recipesReceived(net.neoforged.neoforge.client.event.RecipesReceivedEvent event) {
      ic2.core.recipe.v2.RecipeManagerGetter.setClientRecipes(event.getRecipeMap());
      // Dedicated clients do not fire ServerAboutToStartEvent. Registry-backed
      // crop cards, fuel/fluid managers, and handheld data must be initialized here.
      ic2.core.event.EventHandler.onInitCommonSetup();
      ic2.core.init.Rezepte.registerRecipes();
   }

   @SubscribeEvent
   public void disconnectRecipes(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
      ic2.core.recipe.v2.RecipeManagerGetter.setClientRecipes(net.minecraft.world.item.crafting.RecipeMap.EMPTY);
   }

   @SubscribeEvent
   public void onClientTick(ClientTickEvent.Pre event) {
      TickHandler.onClientTick();
   }

   @SubscribeEvent
   public void livingEntityPreRender(RenderLivingEvent.Pre<LivingEntity, ? extends net.minecraft.client.renderer.entity.state.LivingEntityRenderState, EntityModel<?>> event) {
      
   }

   @SubscribeEvent
   public void livingEntityPostRender(RenderLivingEvent.Post<LivingEntity, ? extends net.minecraft.client.renderer.entity.state.LivingEntityRenderState, EntityModel<?>> event) {
      
   }

   @SubscribeEvent
   public void onSetupFogDensity(RenderFog event) {
      float newDensity = EventHandlerClient.onSetupFogDensity(event.getCamera().getBlockAtCamera());
      if (newDensity >= 0.0F) {
         event.setNearPlaneDistance(-8.0F);
         event.setFarPlaneDistance(newDensity * 0.5F);
      }
   }

   @SubscribeEvent
   public void onRenderFogColor(ComputeFogColor event) {
      int color = EventHandlerClient.onRenderFogColor(event.getCamera().getBlockAtCamera());
      if (color >= 0) {
         event.setRed((color >>> 16 & 0xFF) / 255.0F);
         event.setGreen((color >>> 8 & 0xFF) / 255.0F);
         event.setBlue((color & 0xFF) / 255.0F);
      }
   }

   // 26.1.2: RenderHighlightEvent.Block was replaced by ExtractBlockOutlineRenderStateEvent;
   // the IC2 highlight hooks need a custom BlockOutlineRenderer rewrite (see PORTING.md).


   @SubscribeEvent
   public void onGuiCreate(Post event) {
      EventHandlerClient.onGuiCreate(event.getScreen(), event.getListenersList(), event::addListener);
   }

   @SubscribeEvent
   public void onDrawTooltip(ItemTooltipEvent event) {
      EventHandlerClient.onDrawTooltip(event.getItemStack(), event.getToolTip());
   }

   @SubscribeEvent
   public void onRenderHotBar(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post event) {
      if (VanillaGuiLayers.HOTBAR.equals(event.getName())) {
         EventHandlerClient.onRenderHotBar();
      }
   }

   @SubscribeEvent
   public void onSoundPlayed(PlaySoundEvent event) {
      SoundInstance sound = event.getSound();
      SoundInstance newSound = EventHandlerClient.onSoundPlayed(sound);
      if (newSound != sound) {
         event.setSound(newSound);
      }
   }

   @SubscribeEvent
   public void onDisconnect(PlayerLoggedOutEvent event) {
      EventHandlerClient.onDisconnect();
   }

   @SubscribeEvent
   public void onClientLoggingIn(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) {
      // Remote-server fallback: ServerAboutToStartEvent never fires in this JVM when
      // joining a remote server; holder components bind during registry sync instead
      // (RegistryDataCollector). First caller wins — see the guard in
      // EventHandler.onInitCommonSetup.
      ic2.core.event.EventHandler.onInitCommonSetup();
   }
}
