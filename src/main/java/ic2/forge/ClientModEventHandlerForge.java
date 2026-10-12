package ic2.forge;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.BlockTintSources;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ItemTintSources;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public final class ClientModEventHandlerForge {
   @SubscribeEvent
   public void onRegisterBlockTintProviders(BlockTintSources event) {
      for (ClientEnvProxyForge.BlockTintRegistration reg : ClientEnvProxyForge.blockTintRegistrations) {
         event.register(reg.provider(), reg.blocks());
      }
   }

   @SubscribeEvent
   public void onRegisterItemTintProviders(ItemTintSources event) {
      for (ItemTintSource provider : ClientEnvProxyForge.itemTintProviders()) {
         event.register(ClientEnvProxyForge.nextTintId(), com.mojang.serialization.MapCodec.unit(provider));
      }
   }

   @SubscribeEvent
   public void onRegisterScreens(RegisterMenuScreensEvent event) {
      for (ClientEnvProxyForge.ScreenRegistration<?> reg : ClientEnvProxyForge.screenRegistrations) {
         registerScreen(reg, event);
      }
   }

   private static <H extends net.minecraft.world.inventory.AbstractContainerMenu> void registerScreen(
      ClientEnvProxyForge.ScreenRegistration<H> reg, RegisterMenuScreensEvent event
   ) {
      event.register(reg.type(), (H menu, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.network.chat.Component title) -> (net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<H>)reg.factory().create(menu, inventory, title));
   }

   @SubscribeEvent
   public void onRegisterRenderers(RegisterRenderers event) {
      for (ClientEnvProxyForge.BerRegistration<?, ?> reg : ClientEnvProxyForge.berRegistrations) {
         registerBer(reg, event);
      }

      for (ClientEnvProxyForge.EntityRendererRegistration<?> reg : ClientEnvProxyForge.entityRendererRegistrations) {
         registerEntityRenderer(reg, event);
      }
   }

   private static <E extends BlockEntity> void registerBer(ClientEnvProxyForge.BerRegistration<E, ?> reg, RegisterRenderers event) {
      event.registerBlockEntityRenderer((BlockEntityType<E>)reg.type(), (BlockEntityRendererProvider)reg.factory());
   }

   private static <E extends Entity> void registerEntityRenderer(ClientEnvProxyForge.EntityRendererRegistration<E> reg, RegisterRenderers event) {
      event.registerEntityRenderer(reg.type(), (EntityRendererProvider)reg.factory());
   }

   @SubscribeEvent
   public void onRegisterKeybindings(RegisterKeyMappingsEvent event) {
      for (KeyMapping keybinding : ClientEnvProxyForge.keyBindingRegistrations) {
         event.register(keybinding);
      }
   }

   @SubscribeEvent
   public void onRegisterFluidModels(net.neoforged.neoforge.client.event.RegisterFluidModelsEvent event) {
      ClientEnvFluidHandlerForge.registerModels(event);
   }

   @SubscribeEvent
   public void onRegisterChargeProperty(net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent event) {
      event.register(ic2.core.IC2.getIdentifier("charge"), ic2.forge.model.ChargeProperty.CODEC);
   }
}
