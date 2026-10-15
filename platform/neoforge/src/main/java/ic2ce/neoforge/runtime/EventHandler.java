package ic2ce.neoforge.runtime;

import ic2ce.api.block.BreakableBlock;
import ic2ce.api.energy.EnergyNet;
import ic2ce.api.item.*;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.sound.item.ISwingSoundItem;
import ic2ce.neoforge.diagnostics.ProjectLog;
import ic2ce.neoforge.world.ChunkLoadAwareBlockHandler;
import ic2ce.neoforge.energy.grid.EnergyNetGlobal;
import ic2ce.neoforge.agriculture.CropCatalog;
import ic2ce.neoforge.component.ComponentTypes;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.item.armor.*;
import ic2ce.neoforge.item.energy.*;
import ic2ce.neoforge.power.BiogasGenerator;
import ic2ce.neoforge.processing.fabrication.LiquidMatterFabricator;
import ic2ce.neoforge.processing.fluid.*;
import ic2ce.neoforge.processing.solid.MaterialRecycler;
import ic2ce.neoforge.recipe.RecipeBootstrap;
import ic2ce.neoforge.registry.BlockGroups;
import ic2ce.neoforge.registry.BoatDefinitions;
import ic2ce.neoforge.registry.ContentBootstrap;
import ic2ce.neoforge.registry.EntityDefinitions;
import ic2ce.neoforge.registry.EntityGroups;
import ic2ce.neoforge.registry.GameSignals;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.registry.ItemGroups;
import ic2ce.neoforge.registry.RecipeCodecs;
import ic2ce.neoforge.registry.RecipeKinds;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.thermal.*;
import ic2ce.neoforge.world.ChunkLeases;
import ic2ce.neoforge.world.WorldData;
import ic2ce.neoforge.world.generation.WorldFeatures;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

/**
 * Ordered bootstrap and native gameplay dispatch remain owned by the actual registry/world
 * generation.
 */
public final class EventHandler {
  private EventHandler() {}

  private enum DataPhase {
    NEW,
    LOADING,
    READY,
    FAILED
  }

  private static DataPhase dataPhase = DataPhase.NEW;
  private static RuntimeException dataFailure;

  public static void onInitGameEvents() {
    SoundDefinitions.init();
    GameSignals.init();
  }

  public static void onInitEarly() {
    long start = System.nanoTime();
    ProjectConfiguration.load();
    CoreAccessImpl.init();
    ic2ce.neoforge.fluid.FluidHandler.initialize();
    Recipes.inputFactory = new ic2ce.neoforge.recipe.input.InputFactory();
    EnergyNet.instance = EnergyNetGlobal.create();
    ElectricItem.manager = new ElectricManagerRouter();
    ElectricItem.rawManager = new ComponentChargeLedger();
    ComponentTypes.init();
    ContentBootstrap.init();
    BlockGroups.init();
    ItemGroups.init();
    EntityGroups.init();
    BoatDefinitions.init();
    EntityDefinitions.init();
    ic2ce.neoforge.world.loot.Ic2LootNbtProviderTypes.registerAll();
    RecipeKinds.init();
    RecipeCodecs.init();
    WorldFeatures.init();
    SideServices.proxy().preInit();
    ProjectLog.LOG.debug(
        DiagnosticArea.General,
        "IndustrialCraft2-CE registry bootstrap completed in %d ms",
        (System.nanoTime() - start) / 1_000_000L);
  }

  /**
   * Recipes create actual component-bound item stacks only after the world's registry data has
   * loaded.
   */
  public static synchronized void onInitCommonSetup() {
    switch (dataPhase) {
      case READY -> {
        return;
      }
      case FAILED -> throw dataFailure;
      case LOADING ->
          throw new IllegalStateException("Reentrant IndustrialCraft2-CE data bootstrap");
      case NEW -> dataPhase = DataPhase.LOADING;
    }
    try {
      MaterialRecycler.init();
      ic2ce.neoforge.recipe.processing.ScrapboxDrops.setup();
      ic2ce.neoforge.recipe.processing.ScrapboxDrops.load();
      ic2ce.neoforge.terrain.TerrainBlueprint.init();
      LiquidMatterFabricator.init();
      BiogasGenerator.init();
      FluidBurner.init();
      FluidHeatExchanger.init();
      BiomassFermenter.init();
      FluidElectrolyzer.init();
      RecipeBootstrap.registerRecipes();
      CropCatalog.init();
      ic2ce.neoforge.item.upgrade.MachineUpgrade.registerPending();
      dataPhase = DataPhase.READY;
    } catch (RuntimeException failure) {
      dataFailure = failure;
      dataPhase = DataPhase.FAILED;
      throw failure;
    }
  }

  public static void onInit() {}

  public static void onInitLate() {
    SideServices.proxy().onPostInit();
    SideServices.proxy().requestTick(
        !EnvProxy.get().isClientEnv(), ChunkLoadAwareBlockHandler::init);
    ProjectLog.LOG.info(
        DiagnosticArea.General, "IndustrialCraft2-CE native runtime initialized");
  }

  public static void onServerStart(MinecraftServer server) {
    SideServices.proxy().onServerAvailable(server);
  }

  public static void onPlayerLogout(Player player) {
    SideServices.proxy().getKeyboard().removePlayerReferences(player);
  }

  public static void onWorldLoad(Level world) {
    if (world instanceof ServerLevel server) ChunkLeases.onWorldLoad(server);
  }

  public static void onWorldUnload(Level world) {
    WorldData.onWorldUnload(world);
  }

  public static void onChunkDataLoad(LevelChunk chunk) {}

  public static void onChunkSave(LevelChunk chunk) {}

  public static void onChunkLoad(LevelChunk chunk) {
    ChunkLoadAwareBlockHandler.onChunkLoad(chunk);
  }

  public static void onChunkUnload(LevelChunk chunk) {
    ChunkLoadAwareBlockHandler.onChunkUnload(chunk);
    if (!chunk.getLevel().isClientSide()) ChunkLeases.onChunkUnload(chunk);
  }

  public static InteractionResult onBlockStartBreak(
      Player actor, Level world, InteractionHand hand, BlockPos position, Direction face) {
    if (actor.getItemInHand(hand).getItem() instanceof BlockBreakableItem tool) {
      var result = tool.onBlockStartBreak(actor, world, hand, position, face);
      if (result != InteractionResult.PASS) return result;
    }
    var state = world.getBlockState(position);
    return state.getBlock() instanceof BreakableBlock block
        ? block.startBreak(actor, world, hand, position, state, face)
        : InteractionResult.PASS;
  }

  public static boolean beforeBlockBreak(
      Level world, Player actor, BlockPos position, BlockState state, @Nullable BlockEntity tile) {
    return !(actor.getMainHandItem().getItem() instanceof BlockBreakableItem tool)
        || tool.beforeBlockBreak(world, actor, position, state, tile);
  }

  public static void afterBlockBreak(
      Level world, Player actor, BlockPos position, BlockState state, @Nullable BlockEntity tile) {
    if (actor.getMainHandItem().getItem() instanceof BlockBreakableItem tool)
      tool.afterBlockBreak(world, actor, position, state, tile);
  }

  public static void onPlayerTick(Player player) {}

  public static boolean onLivingFall(LivingEntity entity, float distance) {
    if (entity.level().isClientSide() || !Float.isFinite(distance) || distance < 0) return false;
    var boots = entity.getItemBySlot(EquipmentSlot.FEET);
    return switch (boots.getItem()) {
      case SealedGarment garment when boots.is(ItemDefinitions.RUBBER_BOOTS) ->
          garment.absorbFall(boots, entity, distance);
      case NanoArmor nano -> nano.absorbFall(boots, entity, distance);
      case QuantumArmor quantum -> quantum.absorbFall(boots, entity, distance);
      default -> false;
    };
  }

  public static boolean onEntitySwingHand(LivingEntity entity, InteractionHand hand) {
    if (entity.getItemInHand(hand).getItem() instanceof ISwingSoundItem item) {
      var sound = item.getSwingSound(entity, hand);
      if (sound != null) entity.playSound(sound, 1, 1);
    }
    return false;
  }

  public static boolean onEntityInteract(Player actor, InteractionHand hand, Entity target) {
    return false;
  }

  public static boolean onAttackEntity(Player actor, Entity target) {
    return !(actor.getMainHandItem().getItem() instanceof IEntityAttackableItem item)
        || item.onAttackEntity(actor, target);
  }

  /** Attack admission is inert; the native completed damage sequence owns the armor bill. */
  public static boolean onEntityAttacked(LivingEntity victim, DamageSource source, float amount) {
    return true;
  }

  public static void onDamageApplied(LivingEntity victim, DamageSource source, float armorDamage) {
    if (victim instanceof Player actor) ChargedArmor.damageArmor(actor, source, armorDamage);
  }
}
