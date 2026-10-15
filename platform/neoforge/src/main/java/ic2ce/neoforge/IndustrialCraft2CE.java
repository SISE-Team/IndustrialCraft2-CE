package ic2ce.neoforge;

import ic2ce.neoforge.runtime.EventHandler;
import ic2ce.neoforge.fluid.EnvFluidHandlerNeoForge;
import ic2ce.neoforge.network.NeoForgeNetworkHandler;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

/** The sole mod entry owns ordered native registration and platform lifecycle. */
@Mod("ic2")
public final class IndustrialCraft2CE {
  public static IndustrialCraft2CE instance;

  private enum Phase {
    CONSTRUCTED,
    REGISTERED,
    COMMON,
    COMPLETE
  }

  private final AtomicReference<Phase> phase = new AtomicReference<>(Phase.CONSTRUCTED);
  private final ArrayDeque<Runnable> registryTasks = new ArrayDeque<>();
  private boolean registriesReady, sideServicesInstalled;

  public IndustrialCraft2CE(IEventBus bus, net.neoforged.fml.ModContainer container) {
    if (instance != null)
      throw new IllegalStateException("Repeated IndustrialCraft2-CE construction");
    instance = this;
    bus.addListener(this::register);
    bus.addListener(this::common);
    bus.addListener(this::complete);
    bus.addListener(NeoForgeNetworkHandler::register);
    bus.addListener(Ic2Capabilities::registerCapabilities);
    if (FMLEnvironment.getDist().isClient()) ClientBindings.attach(bus);
  }

  private static final class ClientBindings {
    static void attach(IEventBus bus) {
      bus.register(new ClientModEventHandlerNeoForge());
    }

    static void install() {
      ic2ce.neoforge.runtime.SideServices.installClient(
          new ic2ce.neoforge.client.SideProxyClient(),
          new ic2ce.neoforge.network.NetworkManagerClient());
    }

    static void common() {
      NeoForge.EVENT_BUS.register(new ClientEventHandlerNeoForge());
    }

    static void complete() {
      ic2ce.neoforge.client.SideProxyClient.refreshMc();
    }
  }

  private void transition(Phase expected, Phase next) {
    if (!phase.compareAndSet(expected, next))
      throw new IllegalStateException(
          "IndustrialCraft2-CE lifecycle expected " + expected + ", observed " + phase.get());
  }

  private void register(RegisterEvent event) {
    var key = event.getRegistryKey();
    if (!sideServicesInstalled) {
      // Side services bind on the first registry callback, before any content stages.
      sideServicesInstalled = true;
      if (FMLEnvironment.getDist().isClient()) ClientBindings.install();
    }
    // Tabs must be staged before the creative tab plan replays, whichever registry comes first.
    ic2ce.neoforge.registry.CreativeTabs.init();
    if (key.equals(Registries.GAME_EVENT)) ic2ce.neoforge.registry.GameSignals.init();
    if (key.equals(Registries.LOOT_NBT_PROVIDER_TYPE))
      ic2ce.neoforge.world.loot.Ic2LootNbtProviderTypes.registerAll();
    if (key.equals(Registries.SOUND_EVENT)) {
      transition(Phase.CONSTRUCTED, Phase.REGISTERED);
      EventHandler.onInitEarly();
      EventHandler.onInitGameEvents();
    }
    EnvProxyNeoForge.replay(event);
    if (key.equals(Registries.FLUID))
      event.register(
          Registries.FLUID,
          helper -> EnvFluidHandlerNeoForge.pendingFluids.forEach(helper::register));
    if (key.equals(NeoForgeRegistries.Keys.FLUID_TYPES))
      event.register(
          NeoForgeRegistries.Keys.FLUID_TYPES,
          helper -> EnvFluidHandlerNeoForge.pendingFluidTypes.forEach(helper::register));
    if (key.equals(NeoForgeRegistries.Keys.INGREDIENT_TYPES))
      event.register(
          NeoForgeRegistries.Keys.INGREDIENT_TYPES,
          helper -> {
            helper.register(
                ProjectIds.id("fluid_container"),
                ic2ce.neoforge.recipe.input.FluidContentsIngredient.TYPE);
            helper.register(
                ProjectIds.id("material_tag"),
                ic2ce.neoforge.recipe.input.MaterialTagIngredient.TYPE);
          });
    if (key.equals(NeoForgeRegistries.Keys.HOLDER_SET_TYPES)) completeRegistries();
  }

  private void common(FMLCommonSetupEvent event) {
    transition(Phase.REGISTERED, Phase.COMMON);
    NeoForge.EVENT_BUS.register(new EventHandlerNeoForge());
    if (FMLEnvironment.getDist().isClient()) ClientBindings.common();
    attachVerification();
    event.enqueueWork(EventHandler::onInit);
  }

  private void complete(FMLLoadCompleteEvent event) {
    transition(Phase.COMMON, Phase.COMPLETE);
    if (FMLEnvironment.getDist().isClient()) ClientBindings.complete();
    event.enqueueWork(EventHandler::onInitLate);
  }

  void runAfterRegistryInit(Runnable task) {
    synchronized (registryTasks) {
      if (!registriesReady) {
        registryTasks.addLast(task);
        return;
      }
    }
    task.run();
  }

  private void completeRegistries() {
    final List<Runnable> ready;
    synchronized (registryTasks) {
      if (registriesReady) return;
      registriesReady = true;
      ready = List.copyOf(registryTasks);
      registryTasks.clear();
    }
    for (var task : ready) task.run();
  }

  /** Verification implementations exist only in the separate fixture artifact. */
  private static void attachVerification() {
    if (!Boolean.getBoolean("ic2.verifyMigration")) return;
    String kind =
        Boolean.getBoolean("ic2.verifyTools")
            ? "ToolInteractionVerification"
            : Boolean.getBoolean("ic2.verifyPainter")
                ? "PainterVerification"
                : Boolean.getBoolean("ic2.verifyCables")
                    ? "CableVerification"
                    : Boolean.getBoolean("ic2.verifyAllMachines")
                        ? "AllMachinesVerification"
                        : "MigrationVerification";
    attachFixture(kind);
    if (Boolean.getBoolean("ic2.verifyExtended")) attachFixture("ExtendedValidation");
    if (FMLEnvironment.getDist().isClient()) {
      attachFixture(
          switch (kind) {
            case "ToolInteractionVerification", "PainterVerification", "CableVerification" ->
                kind + "$Client";
            default -> "ClientUiVerification";
          });
      if (Boolean.getBoolean("ic2.verifyDedicated")) attachFixture("RemoteClientProbe");
    }
  }

  private static void attachFixture(String name) {
    try {
      NeoForge.EVENT_BUS.register(
          Class.forName("ic2ce.verification." + name).getConstructor().newInstance());
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException(
          "Use the IndustrialCraft2-CE verification artifact when verification is enabled",
          failure);
    }
  }
}
