package ic2ce.neoforge;

import com.mojang.serialization.MapCodec;
import ic2ce.neoforge.fluid.ClientEnvFluidHandlerNeoForge;
import ic2ce.neoforge.model.*;
import ic2ce.neoforge.world.WorldBlocks;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;

/**
 * Immutable registration records are replayed against the current native model/screen/render
 * service generation.
 */
public final class ClientModEventHandlerNeoForge {
  @SubscribeEvent
  public void blockTints(RegisterColorHandlersEvent.BlockTintSources event) {
    for (var binding : ClientEnvProxyNeoForge.blockTintRegistrations)
      event.register(binding.provider(), binding.blocks());
  }

  @SubscribeEvent
  public void itemTints(RegisterColorHandlersEvent.ItemTintSources event) {
    for (var binding : ClientEnvProxyNeoForge.itemTintRegistrations)
      event.register(binding.id(), MapCodec.unit(binding.provider()));
  }

  @SubscribeEvent
  public void screens(RegisterMenuScreensEvent event) {
    for (var binding : ClientEnvProxyNeoForge.screenRegistrations) screen(event, binding);
  }

  private static <M extends AbstractContainerMenu> void screen(
      RegisterMenuScreensEvent event, ClientEnvProxyNeoForge.ScreenRegistration<M> binding) {
    event.register(binding.type(), binding.factory()::create);
  }

  @SubscribeEvent
  public void renderers(EntityRenderersEvent.RegisterRenderers event) {
    for (var binding : ClientEnvProxyNeoForge.berRegistrations) blockRenderer(event, binding);
    for (var binding : ClientEnvProxyNeoForge.entityRendererRegistrations)
      entityRenderer(event, binding);
  }

  private static <
          B extends BlockEntity,
          S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState>
      void blockRenderer(
          EntityRenderersEvent.RegisterRenderers event,
          ClientEnvProxyNeoForge.BerRegistration<B, S> binding) {
    event.registerBlockEntityRenderer(binding.type(), binding.factory());
  }

  private static <E extends Entity> void entityRenderer(
      EntityRenderersEvent.RegisterRenderers event,
      ClientEnvProxyNeoForge.EntityRendererRegistration<E> binding) {
    event.registerEntityRenderer(binding.type(), binding.factory());
  }

  @SubscribeEvent
  public void keys(RegisterKeyMappingsEvent event) {
    for (var mapping : ClientEnvProxyNeoForge.keyBindingRegistrations) event.register(mapping);
  }

  @SubscribeEvent
  public void fluidModels(RegisterFluidModelsEvent event) {
    ClientEnvFluidHandlerNeoForge.registerModels(event);
  }

  @SubscribeEvent
  public void wallModels(ModelEvent.ModifyBakingResult event) {
    var models = event.getBakingResult().blockStateModels();
    var source = Map.copyOf(models);
    for (var state : WorldBlocks.OBSCURED_WALL.getStateDefinition().getPossibleStates()) {
      var fallback = models.get(state);
      if (fallback != null) models.put(state, new ObscuredWallModel(fallback, source));
    }
  }

  @SubscribeEvent
  public void itemProperties(RegisterRangeSelectItemModelPropertyEvent event) {
    event.register(
        ProjectIds.id("charge"), ChargeProperty.CODEC);
    event.register(
        ProjectIds.id("state"), StateProperty.CODEC);
  }
}
