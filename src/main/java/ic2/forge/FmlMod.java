package ic2.forge;

import ic2.core.event.EventHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod("ic2")
public final class FmlMod {
   private List<Runnable> toRunAfterRegistryInit = new ArrayList<>();
   public static FmlMod instance;
   private static final AtomicInteger loadState = new AtomicInteger();

   public FmlMod(IEventBus modEventBus, net.neoforged.fml.ModContainer container) {
      instance = this;
      modEventBus.register(this);
      EnvProxyForge.blockEntityRegistry.register(modEventBus);
      EnvProxyForge.entityRegistry.register(modEventBus);
      EnvProxyForge.screenHandlerRegistry.register(modEventBus);
      EnvProxyForge.statusEffectRegistry.register(modEventBus);
      EnvProxyForge.foliagePlacerRegistry.register(modEventBus);
      EnvProxyForge.recipeTypeRegistry.register(modEventBus);
      EnvProxyForge.recipeSerializerRegistry.register(modEventBus);
      EnvProxyForge.ticketTypeRegistry.register(modEventBus);
      EnvProxyForge.creativeTabRegistry.register(modEventBus);
      modEventBus.addListener(ic2.forge.ForgeNetworkHandler::register);
      modEventBus.addListener(ic2.forge.Ic2Capabilities::registerCapabilities);
      if (FMLEnvironment.getDist().isClient()) {
         modEventBus.register(new ClientModEventHandlerForge());
      }
   }

   @SubscribeEvent
   public void load(FMLCommonSetupEvent event) {
      NeoForge.EVENT_BUS.register(new EventHandlerForge());
      if (Boolean.getBoolean("ic2.verifyMigration")) {
         try {
            String verification = Boolean.getBoolean("ic2.verifyCables") ? "ic2.verification.CableVerification" : Boolean.getBoolean("ic2.verifyAllMachines")
               ? "ic2.verification.AllMachinesVerification" : "ic2.verification.MigrationVerification";
            NeoForge.EVENT_BUS.register(Class.forName(verification).getConstructor().newInstance());
            if (Boolean.getBoolean("ic2.verifyExtended")) {
               NeoForge.EVENT_BUS.register(Class.forName("ic2.verification.ExtendedValidation").getConstructor().newInstance());
            }
            if (FMLEnvironment.getDist().isClient()) {
               String clientVerification = Boolean.getBoolean("ic2.verifyCables")
                  ? "ic2.verification.CableVerification$Client" : "ic2.verification.ClientUiVerification";
               NeoForge.EVENT_BUS.register(Class.forName(clientVerification).getConstructor().newInstance());
               if (Boolean.getBoolean("ic2.verifyDedicated")) {
                  NeoForge.EVENT_BUS.register(Class.forName("ic2.verification.RemoteClientProbe").getConstructor().newInstance());
               }
            }
         } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Use the IC2 verification artifact when ic2.verifyMigration is enabled", e);
         }
      }
      if (FMLEnvironment.getDist().isClient()) {
         NeoForge.EVENT_BUS.register(new ClientEventHandlerForge());
      }

      if (!loadState.compareAndSet(1, 2)) {
         throw new IllegalStateException();
      } else {
         EventHandler.onInit();
      }
   }

   @SubscribeEvent
   public void init(FMLLoadCompleteEvent event) {
      if (FMLEnvironment.getDist().isClient()) {
         ic2.core.proxy.SideProxyClient.refreshMc(); // clinit may predate Minecraft
      }
      if (!loadState.compareAndSet(2, 3)) {
         throw new IllegalStateException();
      } else {
         EventHandler.onInitLate();
      }
   }

   // 26.1.2: recipe/data init (EventHandler.onInitCommonSetup) is NOT run here.
   // Holder components bind only when a world's datapacks load (WorldLoader ->
   // ReloadableServerResources#updateComponentsAndStaticRegistryTags), i.e. long
   // after mod loading. It runs from EventHandlerForge on ServerAboutToStartEvent.

   @SubscribeEvent
   public void registerEntries(RegisterEvent event) {
      // 26.1.2: sound_event is the first registry event NeoForge fires; running IC2's
      // full static init here means every DeferredRegister/staging map is populated
      // before its own RegisterEvent fires (mob_effect/fluid fire before block).
      if (event.getRegistryKey() == Registries.SOUND_EVENT) {
         if (!loadState.compareAndSet(0, 1)) {
            throw new IllegalStateException();
         } else {
            EventHandler.onInitEarly();
            EventHandler.onInitGameEvents();
         }

         EnvProxyForge.replaySoundEvents(event);
      } else if (event.getRegistryKey() == Registries.FLUID) {
         ic2.core.ref.Ic2Fluids.init(); // force class load so pending fluids are staged
         for (java.util.Map.Entry<net.minecraft.resources.Identifier, net.minecraft.world.level.material.Fluid> entry
            : EnvFluidHandlerForge.pendingFluids.entrySet()) {
            event.register(Registries.FLUID, entry.getKey(), () -> entry.getValue());
         }
      } else if (event.getRegistryKey() == net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES) {
         for (java.util.Map.Entry<net.minecraft.resources.Identifier, net.neoforged.neoforge.fluids.FluidType> entry
            : EnvFluidHandlerForge.pendingFluidTypes.entrySet()) {
            event.register(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES, entry.getKey(), () -> entry.getValue());
         }
      } else if (event.getRegistryKey() == Registries.BLOCK) {
         EnvProxyForge.replayBlocks(event);
      } else if (event.getRegistryKey() == Registries.ITEM) {
         EnvProxyForge.replayItems(event);
      } else if (event.getRegistryKey() == net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.INGREDIENT_TYPES) {
         event.register(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.INGREDIENT_TYPES,
            ic2.core.IC2.getIdentifier("fluid_container"), () -> ic2.core.recipe.input.FluidContainerIngredient.TYPE);
      }
   }

   @SubscribeEvent
   public void registerLate(RegisterEvent event) {
      if (event.getRegistryKey() == net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.HOLDER_SET_TYPES) {
         for (Runnable runnable : this.toRunAfterRegistryInit) {
            runnable.run();
         }

         this.toRunAfterRegistryInit = null;
      }
   }

   void runAfterRegistryInit(Runnable runnable) {
      if (this.toRunAfterRegistryInit != null) {
         this.toRunAfterRegistryInit.add(runnable);
      } else {
         // The registry-init phase has passed (e.g. called from common setup); run now.
         runnable.run();
      }
   }

   @SubscribeEvent
   public void registerFeatures(RegisterEvent event) {
      // 26.1.2: worldgen registrations go through their own registries.
      if (event.getRegistryKey() == net.minecraft.core.registries.Registries.CONFIGURED_FEATURE) {
         for (EnvProxyForge.ConfiguredFeatureRegistration<?, ?> reg : EnvProxyForge.configuredFeatureRegistrations) {
            registerConfiguredFeature(event, reg);
         }
      }

      if (event.getRegistryKey() == net.minecraft.core.registries.Registries.PLACEMENT_MODIFIER_TYPE) {
         for (EnvProxyForge.PlacementModifierTypeRegistration reg : EnvProxyForge.placementModifierTypeRegistrations) {
            event.register(net.minecraft.core.registries.Registries.PLACEMENT_MODIFIER_TYPE, reg.id(), () -> reg.type());
         }
      }
   }

   private static <FC extends net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration, F extends net.minecraft.world.level.levelgen.feature.Feature<FC>> void registerConfiguredFeature(
      RegisterEvent event, EnvProxyForge.ConfiguredFeatureRegistration<FC, F> reg
   ) {
      event.register(
         net.minecraft.core.registries.Registries.CONFIGURED_FEATURE,
         reg.id(),
         () -> new net.minecraft.world.level.levelgen.feature.ConfiguredFeature<>(reg.feature(), reg.config())
      );
      // 26.1.2: configured features are data-driven per-world; BuiltInRegistries.CONFIGURED_FEATURE
      // no longer exists, so the staged Holder cannot be resolved here. Tree placement stays
      // non-functional (see PORTING.md) until the worldgen rewrite.
   }
}
