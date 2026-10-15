package ic2ce.neoforge.client;

import ic2ce.api.tile.IRotorProvider;
import ic2ce.neoforge.kinetic.client.KineticGeneratorRenderer;
import ic2ce.neoforge.explosion.client.ExplosiveBlockRenderer;
import ic2ce.neoforge.runtime.Keyboard;
import ic2ce.neoforge.agriculture.client.CellCareScreen;
import ic2ce.neoforge.agriculture.client.FluidCareScreen;
import ic2ce.neoforge.agriculture.client.HarvesterScreen;
import ic2ce.neoforge.audio.AudioService;
import ic2ce.neoforge.client.audio.ClientAudioService;
import ic2ce.neoforge.client.render.MiningBeamRenderer;
import ic2ce.neoforge.crafting.client.AutomaticCraftingScreen;
import ic2ce.neoforge.crafting.client.WorkbenchScreen;
import ic2ce.neoforge.energy.client.BankScreen;
import ic2ce.neoforge.energy.client.ChargingPadScreen;
import ic2ce.neoforge.energy.client.TransformerScreen;
import ic2ce.neoforge.handheld.client.EnergyMeterScreen;
import ic2ce.neoforge.handheld.client.ScannerScreen;
import ic2ce.neoforge.handheld.client.ToolboxScreen;
import ic2ce.neoforge.item.upgrade.AdvancedFilterScreenFactory;
import ic2ce.neoforge.item.upgrade.MaterialTagEditor;
import ic2ce.neoforge.item.upgrade.NumericFilterEditor;
import ic2ce.neoforge.kinetic.client.MotorScreen;
import ic2ce.neoforge.kinetic.client.SteamTurbineScreen;
import ic2ce.neoforge.kinetic.client.StirlingEngineScreen;
import ic2ce.neoforge.kinetic.client.WaterTurbineScreen;
import ic2ce.neoforge.kinetic.client.WindTurbineScreen;
import ic2ce.neoforge.menu.document.DocumentScreen;
import ic2ce.neoforge.mining.client.AreaMinerScreen;
import ic2ce.neoforge.mining.client.PipeMinerScreen;
import ic2ce.neoforge.personal.client.OwnerEnergyTradeScreen;
import ic2ce.neoforge.personal.client.OwnerItemTradeScreen;
import ic2ce.neoforge.personal.client.PublicEnergyTradeScreen;
import ic2ce.neoforge.personal.client.PublicItemTradeScreen;
import ic2ce.neoforge.power.client.SolarCollectorScreen;
import ic2ce.neoforge.processing.client.BoilerScreen;
import ic2ce.neoforge.processing.client.CanningScreen;
import ic2ce.neoforge.processing.client.CondensationScreen;
import ic2ce.neoforge.processing.client.DistillationScreen;
import ic2ce.neoforge.processing.client.ElectrolysisScreen;
import ic2ce.neoforge.processing.client.FermentationScreen;
import ic2ce.neoforge.processing.client.FluidPackagingScreen;
import ic2ce.neoforge.processing.client.HeatExchangeScreen;
import ic2ce.neoforge.processing.client.LegacyCanningScreen;
import ic2ce.neoforge.processing.client.MatterScreen;
import ic2ce.neoforge.processing.client.MetalFormingScreen;
import ic2ce.neoforge.processing.client.PatternLibraryScreen;
import ic2ce.neoforge.processing.client.ReplicationScreen;
import ic2ce.neoforge.processing.client.ScanningScreen;
import ic2ce.neoforge.reactor.client.ReactorScreen;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.EntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.neoforge.runtime.SideProxy;
import ic2ce.neoforge.runtime.SideProxyServer;
import ic2ce.neoforge.thermal.client.ElectricHeaterScreen;
import ic2ce.neoforge.thermal.client.FluidHeaterScreen;
import ic2ce.neoforge.thermal.client.IsotopeHeaterScreen;
import ic2ce.neoforge.transport.client.BatchItemScreen;
import ic2ce.neoforge.transport.client.DualBankScreen;
import ic2ce.neoforge.transport.client.FairFluidScreen;
import ic2ce.neoforge.transport.client.MeteredFluidScreen;
import ic2ce.neoforge.transport.client.PriorityFluidScreen;
import ic2ce.neoforge.transport.client.PriorityItemScreen;
import ic2ce.neoforge.utility.client.ChunkAnchorScreen;
import ic2ce.neoforge.utility.client.PoleDriveScreen;
import ic2ce.neoforge.vehicle.client.HullRenderer;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Native client services own current-world lookup, deferred server work and typed screen/renderer
 * registration.
 */
public final class SideProxyClient implements SideProxy {
  public static final ClientEnvProxy envProxy = new ic2ce.neoforge.ClientEnvProxyNeoForge();
  public static volatile Minecraft mc = Minecraft.getInstance();
  private final AudioService audio = new ClientAudioService();
  private final Keyboard input = new KeyboardClient();
  private final java.util.ArrayDeque<Runnable> awaitingServer = new java.util.ArrayDeque<>();

  public static void refreshMc() {
    mc = Minecraft.getInstance();
  }

  private static Minecraft client() {
    refreshMc();
    return java.util.Objects.requireNonNull(mc, "Native client unavailable");
  }

  private static <M extends net.minecraft.world.inventory.AbstractContainerMenu> void screen(
      net.minecraft.world.inventory.MenuType<M> type, ClientEnvProxy.ScreenFactory<M> factory) {
    envProxy.registerScreen(type, factory);
  }

  @Override
  public void preInit() {
    screen(MenuDefinitions.DYNAMIC_BE, DocumentScreen::create);
    screen(MenuDefinitions.DYNAMIC_ITEM, DocumentScreen::create);
    screen(MenuDefinitions.ELECTRIC_HEAT_GENERATOR, ElectricHeaterScreen::new);
    screen(MenuDefinitions.FLUID_HEAT_GENERATOR, FluidHeaterScreen::new);
    screen(MenuDefinitions.RT_HEAT_GENERATOR, IsotopeHeaterScreen::new);
    screen(MenuDefinitions.ELECTRIC_KINETIC_GENERATOR, MotorScreen::new);
    screen(MenuDefinitions.STEAM_KINETIC_GENERATOR, SteamTurbineScreen::new);
    screen(MenuDefinitions.STIRLING_KINETIC_GENERATOR, StirlingEngineScreen::new);
    screen(MenuDefinitions.WATER_KINETIC_GENERATOR, WaterTurbineScreen::new);
    screen(MenuDefinitions.WIND_KINETIC_GENERATOR, WindTurbineScreen::new);
    screen(MenuDefinitions.NUCLEAR_REACTOR, ReactorScreen::new);
    screen(MenuDefinitions.CONDENSER, CondensationScreen::new);
    screen(MenuDefinitions.FLUID_BOTTLER, FluidPackagingScreen::new);
    screen(MenuDefinitions.FLUID_DISTRIBUTOR, FairFluidScreen::new);
    screen(MenuDefinitions.FLUID_REGULATOR, MeteredFluidScreen::new);
    screen(MenuDefinitions.LIQUID_HEAT_EXCHANGER, HeatExchangeScreen::new);
    screen(MenuDefinitions.SOLAR_DISTILLER, DistillationScreen::new);
    screen(MenuDefinitions.STEAM_GENERATOR, BoilerScreen::new);
    screen(MenuDefinitions.ITEM_BUFFER, DualBankScreen::new);
    screen(MenuDefinitions.MAGNETIZER, PoleDriveScreen::new);
    screen(MenuDefinitions.SORTING_MACHINE, BatchItemScreen::new);
    screen(MenuDefinitions.CANNER, CanningScreen::new);
    screen(MenuDefinitions.CLASSIC_CANNER, LegacyCanningScreen::new);
    screen(MenuDefinitions.FERMENTER, FermentationScreen::new);
    screen(MenuDefinitions.METAL_FORMER, MetalFormingScreen::new);
    screen(MenuDefinitions.ADVANCED_MINER, AreaMinerScreen::new);
    screen(MenuDefinitions.CROP_HARVESTER, HarvesterScreen::new);
    screen(MenuDefinitions.CROPMATRON, FluidCareScreen::new);
    screen(MenuDefinitions.CLASSIC_CROPMATRON, CellCareScreen::new);
    screen(MenuDefinitions.MINER, PipeMinerScreen::new);
    screen(MenuDefinitions.MATTER_GENERATOR, MatterScreen::new);
    screen(MenuDefinitions.PATTERN_STORAGE, PatternLibraryScreen::new);
    screen(MenuDefinitions.REPLICATOR, ReplicationScreen::new);
    screen(MenuDefinitions.UU_SCANNER, ScanningScreen::new);
    screen(MenuDefinitions.ENERGY_O_MAT_CLOSED, PublicEnergyTradeScreen::new);
    screen(MenuDefinitions.ENERGY_O_MAT_OPEN, OwnerEnergyTradeScreen::new);
    screen(MenuDefinitions.TRADE_O_MAT_CLOSED, PublicItemTradeScreen::new);
    screen(MenuDefinitions.TRADE_O_MAT_OPEN, OwnerItemTradeScreen::new);
    screen(MenuDefinitions.CHARGEPAD, ChargingPadScreen::new);
    screen(MenuDefinitions.ENERGY_STORAGE, BankScreen::new);
    screen(MenuDefinitions.ELECTROLYZER, ElectrolysisScreen::new);
    screen(MenuDefinitions.TRANSFORMER, TransformerScreen::new);
    screen(MenuDefinitions.CHUNK_LOADER, ChunkAnchorScreen::new);
    screen(MenuDefinitions.WEIGHTED_FLUID_DISTRIBUTOR, PriorityFluidScreen::new);
    screen(MenuDefinitions.WEIGHTED_ITEM_DISTRIBUTOR, PriorityItemScreen::new);
    screen(MenuDefinitions.INDUSTRIAL_WORKBENCH, WorkbenchScreen::new);
    screen(MenuDefinitions.BATCH_CRAFTER, AutomaticCraftingScreen::new);
    screen(MenuDefinitions.ADVANCED_UPGRADE, new AdvancedFilterScreenFactory());
    screen(MenuDefinitions.ADVANCED_UPGRADE_EDIT_ORE, MaterialTagEditor.GuiEditOre::new);
    screen(MenuDefinitions.ADVANCED_UPGRADE_VALUE_CONFIG, NumericFilterEditor.GuiValueConfig::new);
    screen(MenuDefinitions.SCANNER, ScannerScreen::new);
    screen(MenuDefinitions.TOOL_BOX, ToolboxScreen::new);
    screen(MenuDefinitions.METER, EnergyMeterScreen::new);
    screen(MenuDefinitions.SOLAR_GENERATOR, SolarCollectorScreen::new);
    envProxy.registerColorProvider(state -> 6723908, BlockDefinitions.RUBBER_LEAVES);
    envProxy.registerColorProvider(
        new ItemTintSource() {
          public int calculate(
              net.minecraft.world.item.ItemStack stack,
              net.minecraft.client.multiplayer.ClientLevel world,
              net.minecraft.world.entity.LivingEntity owner) {
            return 6723908;
          }

          public com.mojang.serialization.MapCodec<? extends ItemTintSource> type() {
            return com.mojang.serialization.MapCodec.unit(this);
          }
        },
        ItemDefinitions.RUBBER_LEAVES);
    registerRotorProvider(BlockEntityDefinitions.WIND_KINETIC_GENERATOR);
    registerRotorProvider(BlockEntityDefinitions.WATER_KINETIC_GENERATOR);
    registerRotorProvider(BlockEntityDefinitions.WIND_GENERATOR);
    registerRotorProvider(BlockEntityDefinitions.WATER_GENERATOR);
    envProxy.registerEntityRenderer(EntityDefinitions.ITNT, ExplosiveBlockRenderer::new);
    envProxy.registerEntityRenderer(EntityDefinitions.NUKE, ExplosiveBlockRenderer::new);
    envProxy.registerEntityRenderer(EntityDefinitions.LASER_BULLET, MiningBeamRenderer::new);
    envProxy.registerEntityRenderer(
        EntityDefinitions.RUBBER_BOAT,
        context ->
            new HullRenderer(
                context,
                ProjectRuntime.getIdentifier("textures/entity/boat/boat_rubber.png"),
                false));
    envProxy.registerEntityRenderer(
        EntityDefinitions.ELECTRIC_BOAT,
        context ->
            new HullRenderer(
                context,
                ProjectRuntime.getIdentifier("textures/entity/boat/boat_electric.png"),
                false));
    envProxy.registerEntityRenderer(
        EntityDefinitions.CARBON_BOAT,
        context ->
            new HullRenderer(
                context,
                ProjectRuntime.getIdentifier("textures/entity/boat/boat_carbon.png"),
                false));
    envProxy.registerBlockEntityRenderer(
        BlockEntityDefinitions.SIGN,
        net.minecraft.client.renderer.blockentity.StandingSignRenderer::new);
  }

  @Override
  public void onPostInit() {}

  @Override
  public boolean isRendering() {
    return client().isSameThread();
  }

  @Override
  public boolean isSimulating() {
    return !isRendering();
  }

  @Override
  public AudioService getSoundManager() {
    return audio;
  }

  @Override
  public Keyboard getKeyboard() {
    return input;
  }

  @Override
  public void requestTick(boolean simulating, Runnable action) {
    java.util.Objects.requireNonNull(action);
    var client = client();
    if (!simulating) {
      client.execute(action);
      return;
    }
    synchronized (awaitingServer) {
      var server = client.getSingleplayerServer();
      if (server == null) awaitingServer.add(action);
      else server.execute(action);
    }
  }

  @Override
  public void onServerAvailable(MinecraftServer server) {
    java.util.Objects.requireNonNull(server);
    synchronized (awaitingServer) {
      Runnable action;
      while ((action = awaitingServer.poll()) != null) server.execute(action);
    }
  }

  @Override
  public void displayError(String format, Object... arguments) {
    SideProxyServer.displayError0(format, arguments);
  }

  @Override
  public void displayError(Exception failure, String format, Object... arguments) {
    throw new IllegalStateException(
        "IndustrialCraft2-CE: " + String.format(java.util.Locale.ROOT, format, arguments), failure);
  }

  @Override
  public Player getPlayerInstance() {
    return client().player;
  }

  @Override
  public Level getPlayerWorld() {
    return client().level;
  }

  @Override
  public Level getWorld(MinecraftServer server, Identifier dimension) {
    if (server != null)
      return server.getLevel(
          net.minecraft.resources.ResourceKey.create(
              net.minecraft.core.registries.Registries.DIMENSION, dimension));
    var world = client().level;
    return world != null && world.dimension().identifier().equals(dimension) ? world : null;
  }

  @Override
  public RecipeManager getRecipeManager() {
    var server = client().getSingleplayerServer();
    return server == null ? null : server.getRecipeManager();
  }

  @Override
  public File getMinecraftDir() {
    return envProxy.getMinecraftDir();
  }

  @Override
  public void playSoundSp(SoundEvent event, SoundSource source, float volume, float pitch) {
    var owner = getPlayerInstance();
    if (owner != null) audio.playOnce(event, source, volume, pitch, owner);
  }

  @Override
  public void playSoundOnce(Entity entity, SoundEvent event, float volume, float pitch) {
    if (entity != null) entity.playSound(event, volume, pitch);
  }

  @Override
  public void messagePlayer(Player player, String key, Object... arguments) {
    var recipient = player == null ? getPlayerInstance() : player;
    if (recipient == null) return;
    var message =
        net.minecraft.locale.Language.getInstance().has(key)
            ? Component.translatable(
                key, (Object[]) SideProxyServer.getMessageComponents(arguments))
            : Component.literal(
                arguments.length == 0 ? key : String.format(java.util.Locale.ROOT, key, arguments));
    recipient.sendSystemMessage(message);
  }

  @Override
  public <T extends BlockEntity & IRotorProvider> void registerRotorProvider(
      BlockEntityType<T> type) {
    envProxy.registerBer(type, KineticGeneratorRenderer::new);
  }
}
