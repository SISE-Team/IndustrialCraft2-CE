package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.network.DataEncoder;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.SubPacketType;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Actual placed-machine tests. Setup supplies inputs; normal world ticks do all work. */
public final class AllMachinesVerification {
  public static volatile AllMachinesVerification ACTIVE;

  public AllMachinesVerification() {
    ACTIVE = this;
    if (Boolean.getBoolean("ic2.verifyRecipeBookOnly")) {
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new RecipeBookVerification());
      if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(
            new RecipeBookVerification.Client());
    }
    if (Boolean.getBoolean("ic2.verifyChunkLeaseRestart"))
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new ChunkLeaseRestartProbe());
    if (Boolean.getBoolean("ic2.verifyLifecycle"))
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new LifecycleVerification());
    if (Boolean.getBoolean("ic2.verifyHotpaths"))
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new HotpathRegression());
    if (Boolean.getBoolean("ic2.verifyCableReplacement"))
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new CableReplacementProbe());
    if (Boolean.getBoolean("ic2.verifyPerformance"))
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new PerformanceVerification());
    if (Boolean.getBoolean("ic2.verifyWorkflow")) {
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new CoupledWorkflowVerification());
      if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(
            new CoupledWorkflowVerification.Client());
    }
    if (Boolean.getBoolean("ic2.verifyProcessingRecipes"))
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new ProcessingRecipeVerification());
    if (Boolean.getBoolean("ic2.verifyItemFunctions")) {
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new AllItemFunctions());
      if (System.getProperty("ic2.itemFunctionSubset", "all").equals("extra")) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new AdditionalItemUsage());
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new CanningRecipeVerification());
      }
      if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new AllItemFunctions.Client());
    }
    if (Boolean.getBoolean("ic2.verifyAllItems")) {
      var items = new AllItemsVerification();
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(items);
      if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(
            new AllItemsVerification.Client());
    }
    if (Boolean.getBoolean("ic2.inventoryProbeOnly")) {
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new ReviewInventoryProbe());
      if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(
            new ReviewInventoryProbe.Client());
    }
  }

  public boolean baseFinished() {
    return coreFinished && guiFinished;
  }

  public boolean finishedCoreAndGui() {
    return baseFinished()
        && (!Boolean.getBoolean("ic2.verifyProcessingRecipes")
            || ProcessingRecipeVerification.finished)
        && (!Boolean.getBoolean("ic2.verifyLifecycle") || LifecycleVerification.finished);
  }

  public List<String> failureList() {
    return List.copyOf(failures);
  }

  public Map<String, Object> remoteHost = new LinkedHashMap<>();
  public static volatile String CLIENT_GUI_CASE;
  public static volatile int CLIENT_GUI_CONTAINER_ID = -1;
  public static volatile BlockPos CLIENT_GUI_POS;
  public static volatile boolean CLIENT_GUI_READY;
  public static volatile boolean CLIENT_SCANNER_EXPECTED, CLIENT_SCANNER_DONE, CLIENT_SCANNER_PASS;
  public static volatile boolean CLIENT_SMELTING_HINTS_DONE, CLIENT_SMELTING_HINTS_PASS;
  public static final Map<String, Integer> GUI_TICKS = new ConcurrentHashMap<>();
  public static volatile BlockPos ACTOR_ENERGY_BLOCK_POS;
  public static final Map<String, Object> ACTOR_CLIENT_STATE = new ConcurrentHashMap<>();
  public final Map<String, NativeBlockEntity> machines = new LinkedHashMap<>();
  public final Map<String, Map<String, Object>> rows = new LinkedHashMap<>();
  public final Map<Integer, List<Runnable>> jobs = new TreeMap<>();
  public ServerLevel level;
  public ServerPlayer player;
  public int ticks;
  private int checks;
  private int guiIndex;
  private int guiTicks;
  private List<String> guiNames;
  private boolean started, coreFinished, guiFinished;
  private final List<String> failures = new ArrayList<>();
  private final Map<String, String> exclusions = new TreeMap<>();
  private final AllMachineBehaviors behavior = new AllMachineBehaviors(this);
  private final int duration = Integer.getInteger("ic2.allMachineTicks", 12000);

  @SubscribeEvent
  public void serverStarted(ServerStartedEvent event) {
    level = event.getServer().overworld();
  }

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (Boolean.getBoolean("ic2.verifyRecipeBookOnly")
        || Boolean.getBoolean("ic2.verifyWorkflow")
        || Boolean.getBoolean("ic2.verifyItemFunctions")
        || Boolean.getBoolean("ic2.inventoryProbeOnly")
        || (Boolean.getBoolean("ic2.verifyAllItems") && !AllItemsVerification.machinePhase)) return;
    if (started || !(event.getEntity() instanceof ServerPlayer actual)) return;
    if (Boolean.getBoolean("ic2.verifyDedicated")) {
      if (level.getServer().getPlayerList().getPlayers().size() < 2) return;
      actual =
          level.getServer().getPlayerList().getPlayers().stream()
              .filter(p -> p.getScoreboardName().equals("IC2Test"))
              .findFirst()
              .orElseThrow();
    }
    started = true;
    player = actual;
    if (Boolean.getBoolean("ic2.processingOnly")) {
      coreFinished = guiFinished = true;
      return;
    }
    if (Boolean.getBoolean("ic2.resumeExtendedAfterMatrix")) {
      restorePassedWorld();
      return;
    }
    var server = level.getServer();
    server
        .getCommands()
        .performPrefixedCommand(
            server.createCommandSourceStack(), "gamemode creative " + player.getScoreboardName());
    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set noon");
    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "weather clear");
    ProjectRuntime.random.setSeed(20261012L);
    createFixtures();
    guard("suite", "setup behaviors", behavior::setup);
    at(
        40,
        () -> {
          for (var entry : machines.entrySet()) {
            var tile = entry.getValue();
            if (tile.hasComponent(ElectricalBuffer.class)
                && !tile.getComponent(ElectricalBuffer.class).getSinkDirs().isEmpty()
                && !rows.get(entry.getKey()).containsKey("powerFixture"))
              supplyPower(entry.getKey());
          }
        });
    at(
        80,
        () -> {
          commonChecks("initial");
          level.getServer().tickRateManager().requestGameToSprint(duration - ticks);
        });
    saveReport();
  }

  @SuppressWarnings("unchecked")
  private void restorePassedWorld() {
    try {
      var prior =
          new com.google.gson.Gson()
              .fromJson(
                  Files.readString(Path.of(System.getProperty("ic2.previousBaseReport"))),
                  Map.class);
      if (!Boolean.TRUE.equals(prior.get("complete"))
          || !((List<?>) prior.get("failures")).isEmpty())
        throw new IllegalStateException("Previous machine suite must be fully passed");
      var previousRows = (Map<String, Map<String, Object>>) prior.get("machines");
      for (var entry : previousRows.entrySet()) {
        var xyz = (List<Number>) entry.getValue().get("position");
        var pos = new BlockPos(xyz.get(0).intValue(), xyz.get(1).intValue(), xyz.get(2).intValue());
        for (int x = -1; x <= 1; x++)
          for (int z = -1; z <= 1; z++)
            level.getChunk((pos.getX() >> 4) + x, (pos.getZ() >> 4) + z);
        var tile = level.getBlockEntity(pos);
        if (!(tile instanceof NativeBlockEntity machine)
            || machine.getType()
                != BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("ic2", entry.getKey())))
          throw new IllegalStateException("Passed world missing machine " + entry.getKey());
        machines.put(entry.getKey(), machine);
        rows.put(entry.getKey(), entry.getValue());
      }
      if (machines.size() != 104)
        throw new IllegalStateException("Expected all 104 saved machine types");
      checks = ((Number) prior.get("checks")).intValue();
      ticks = duration;
      coreFinished = guiFinished = true;
      saveReport();
    } catch (Exception error) {
      throw new IllegalStateException("Cannot resume the existing passed world", error);
    }
  }

  private void createFixtures() {
    int index = 0;
    var ids =
        BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet().stream()
            .filter(id -> id.getNamespace().equals("ic2"))
            .sorted()
            .toList();
    for (var id : ids) {
      String name = id.getPath();
      if (name.endsWith("_crop")
          || Set.of("crop_stick", "sign", "wall", "itnt", "nuke", "classic_nuke").contains(name)) {
        exclusions.put(
            name,
            name.endsWith("_crop") || name.equals("crop_stick")
                ? "crop (tested as crop-machine input)"
                : "non-machine block");
        continue;
      }
      BlockPos pos = new BlockPos(168 + (index % 12) * 16, 181, 168 + (index / 12) * 16);
      index++;
      Map<String, Object> row = new LinkedHashMap<>();
      rows.put(name, row);
      row.put("position", List.of(pos.getX(), pos.getY(), pos.getZ()));
      row.put("checks", new LinkedHashMap<String, Boolean>());
      row.put("behavior", new LinkedHashMap<String, Boolean>());
      guard(
          name,
          "place",
          () -> {
            for (int dx = -1; dx <= 1; dx++)
              for (int dz = -1; dz <= 1; dz++)
                level.setChunkForced((pos.getX() >> 4) + dx, (pos.getZ() >> 4) + dz, true);
            for (int x = -6; x <= 6; x++)
              for (int z = -6; z <= 6; z++) {
                level.setBlockAndUpdate(
                    pos.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                for (int y = 0; y <= 3; y++)
                  level.setBlockAndUpdate(pos.offset(x, y, z), Blocks.AIR.defaultBlockState());
              }
            if (name.equals("reactor_chamber")) helper("nuclear_reactor", pos.west());
            if (Set.of("reactor_access_hatch", "reactor_fluid_port", "reactor_redstone_port")
                .contains(name)) {
              BlockPos core = pos.west(2);
              for (int x = -2; x <= 2; x++)
                for (int y = -2; y <= 2; y++)
                  for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2)
                      level.setBlockAndUpdate(
                          core.offset(x, y, z), block("reactor_vessel").defaultBlockState());
                  }
              helper("nuclear_reactor", core);
              for (Direction direction : Direction.values())
                helper("reactor_chamber", core.relative(direction));
            }
            NativeBlockEntity tile = helper(name, pos);
            machines.put(name, tile);
            tile.onPlaced(new ItemStack(block(name)), player, Direction.NORTH);
            row.put("class", tile.getClass().getName());
            row.put("hasGui", tile instanceof IHasGui);
            row.put("hasEnergy", tile.hasComponent(ElectricalBuffer.class));
            row.put("hasFluid", tile.hasComponent(FluidPorts.class));
            check(
                name,
                "registered block creates expected entity",
                tile.getType() == BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(id));
            if (tile.hasComponent(ElectricalBuffer.class)
                && !tile.getComponent(ElectricalBuffer.class).getSinkDirs().isEmpty())
              supplyPower(name);
          });
    }
    check(
        "suite",
        "all 104 machine/equipment registrations have fixtures",
        rows.size() == 104 && machines.size() == 104);
  }

  public NativeBlockEntity helper(String id, BlockPos pos) {
    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    level.setBlockAndUpdate(pos, block(id).defaultBlockState());
    var tile = level.getBlockEntity(pos);
    if (!(tile instanceof NativeBlockEntity result))
      throw new IllegalStateException("No IC2 tile for " + id + " at " + pos);
    facing(result, Direction.NORTH);
    return result;
  }

  public void supplyPower(String id) {
    var tile = tile(id);
    var energy = tile.getComponent(ElectricalBuffer.class);
    Direction side =
        energy.getSinkDirs().contains(Direction.WEST)
            ? Direction.WEST
            : energy.getSinkDirs().iterator().next();
    helper("creative_generator", tile.getBlockPos().relative(side));
    rows.get(id).put("powerFixture", side.getName());
  }

  public void facing(NativeBlockEntity tile, Direction direction) {
    if (tile.getFacing() == direction
        || tile.getBlockState().getProperties().stream()
            .noneMatch(p -> p.getPossibleValues().contains(direction))) return;
    try {
      for (Class<?> type = tile.getClass(); type != null; type = type.getSuperclass()) {
        try {
          var method =
              type.getDeclaredMethod(
                  "setFacing", net.minecraft.world.level.Level.class, Direction.class);
          method.setAccessible(true);
          method.invoke(tile, level, direction);
          return;
        } catch (NoSuchMethodException ignored) {
        }
      }
    } catch (ReflectiveOperationException error) {
      throw new RuntimeException(error);
    }
  }

  public NativeBlockEntity heatSupply(String id) {
    var owner = tile(id);
    BlockPos pos = owner.getBlockPos().relative(owner.getFacing());
    var source = helper("electric_heat_generator", pos);
    facing(source, owner.getFacing().getOpposite());
    var coils = (InventorySlot) get(source, "coilSlot");
    for (int i = 0; i < coils.size(); i++) coils.put(i, stack("coil", 1));
    helper("creative_generator", pos.above());
    source.setChanged();
    return source;
  }

  public NativeBlockEntity tile(String id) {
    return Objects.requireNonNull(machines.get(id), id);
  }

  public static Block block(String id) {
    return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("ic2", id));
  }

  public static ItemStack stack(String id, int count) {
    Identifier key =
        id.contains(":") ? Identifier.parse(id) : Identifier.fromNamespaceAndPath("ic2", id);
    var item =
        BuiltInRegistries.ITEM
            .getOptional(key)
            .orElseThrow(() -> new IllegalArgumentException("Missing item " + key));
    return new ItemStack(item, count);
  }

  public static Fluid fluid(String id) {
    return BuiltInRegistries.FLUID.getValue(
        id.contains(":") ? Identifier.parse(id) : Identifier.fromNamespaceAndPath("ic2", id));
  }

  public static Object get(Object owner, String field) {
    try {
      for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
        try {
          Field value = type.getDeclaredField(field);
          value.setAccessible(true);
          return value.get(owner);
        } catch (NoSuchFieldException ignored) {
        }
      }
    } catch (ReflectiveOperationException error) {
      throw new RuntimeException(error);
    }
    throw new IllegalArgumentException("Missing setup field " + field + " in " + owner.getClass());
  }

  public InventorySlot slot(String id, String name) {
    return ((InventoryBlockEntity) tile(id)).getInventorySlot(name);
  }

  public Ic2FluidTank tank(String id, String name) {
    return Objects.requireNonNull(
        tile(id).getComponent(FluidPorts.class).getFluidTank(name),
        "Missing tank " + name + " in " + id);
  }

  public void fill(String id, String name, String fluid, int amount) {
    int filled = tank(id, name).fillMbUnchecked(Ic2FluidStack.create(fluid(fluid), amount), false);
    check(id, "fixture accepts " + fluid + " into " + name, filled == amount);
    tile(id).setChanged();
  }

  public void event(String id, int event) {
    try {
      GrowingBuffer data = new GrowingBuffer(64);
      SubPacketType.TileEntityEvent.writeTo(data);
      DataEncoder.encode(data, tile(id), false);
      data.writeInt(event);
      data.flip();
      var packet = data.toByteBuf(false);
      var previous = player.position();
      var target = tile(id).getBlockPos();
      try {
        player.setPos(target.getX() + 0.5, target.getY() + 1, target.getZ() + 2.5);
        ProjectRuntime.network.get(true).onPacket(packet, player);
      } finally {
        player.setPos(previous.x, previous.y, previous.z);
        packet.release();
      }
    } catch (Exception error) {
      throw new RuntimeException(error);
    }
  }

  public void at(int tick, Runnable action) {
    jobs.computeIfAbsent(tick, ignored -> new ArrayList<>()).add(action);
  }

  public void guard(String id, String task, Runnable action) {
    try {
      action.run();
    } catch (Throwable error) {
      check(id, task + ": " + error, false);
      error.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public void check(String id, String detail, boolean passed) {
    checks++;
    if (rows.containsKey(id))
      ((Map<String, Boolean>) rows.get(id).get("checks")).put(detail, passed);
    if (!passed) {
      failures.add(id + ": " + detail);
      System.err.println("IC2_ALL FAIL " + id + ": " + detail);
    }
  }

  @SuppressWarnings("unchecked")
  public void behave(String id, String detail, boolean passed) {
    ((Map<String, Boolean>) rows.get(id).get("behavior")).put(detail, passed);
    check(id, detail, passed);
  }

  public void observe(String id, String key, Object value) {
    rows.get(id).put(key, value);
  }

  private void commonChecks(String stage) {
    for (String id : machines.keySet())
      guard(
          id,
          stage + " common",
          () -> {
            var tile = tile(id);
            check(
                id,
                stage + " machine remains placed",
                level.getBlockEntity(tile.getBlockPos()) == tile);
            var saved = tile.saveWithFullMetadata(level.registryAccess());
            var restored =
                BlockEntity.loadStatic(
                    tile.getBlockPos(), tile.getBlockState(), saved, level.registryAccess());
            check(
                id,
                stage + " saves/loads correct entity type",
                restored != null && restored.getType() == tile.getType());
            if (restored instanceof NativeBlockEntity copy) {
              copy.setLevel(level);
              check(
                  id,
                  stage + " IC2 state survives NBT roundtrip",
                  saved
                      .getCompoundOrEmpty("ic2")
                      .equals(
                          copy.saveWithFullMetadata(level.registryAccess())
                              .getCompoundOrEmpty("ic2")));
            }
            if (tile instanceof IHasGui gui) {
              var menu = gui.createServerScreenHandler(1, player);
              check(id, stage + " server menu is constructible", menu != null);
              if (menu != null) {
                var fields = menu.getNetworkedFields();
                check(
                    id,
                    stage + " native menu synchronization fields are enumerable and unique",
                    fields != null && fields.stream().distinct().count() == fields.size());
              }
            }
          });
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (!started) return;
    if (Boolean.getBoolean("ic2.verifyDedicated")) {
      if (level.getServer().getPlayerList().getPlayers().size() < 2) return;
      player =
          level.getServer().getPlayerList().getPlayers().stream()
              .filter(p -> p.getScoreboardName().equals("IC2Test"))
              .findFirst()
              .orElse(null);
      if (player == null) return;
      readRemoteHost();
    }
    if (coreFinished) {
      if (!guiFinished) guiCycle();
      return;
    }
    // Keep the real world ticking while waiting for an actual client jump.
    // No timeout can turn a missing human/UI action into a passing assertion.
    if (ticks == 8950 && Boolean.TRUE.equals(rows.get("magnetizer").get("jumpWindow"))) {
      if (Boolean.getBoolean("ic2.verifyDedicated")) {
        double clientY = ((Number) remoteHost.getOrDefault("playerY", 0)).doubleValue();
        boolean flying = Boolean.TRUE.equals(remoteHost.get("flying"));
        if (Boolean.TRUE.equals(remoteHost.get("onGround"))
            && !flying
            && Math.abs(clientY - 181) < 0.2) observe("magnetizer", "jumpArmed", true);
        if (!Boolean.TRUE.equals(rows.get("magnetizer").get("jumpArmed")) || flying) {
          saveReport();
          return;
        }
        double peak =
            Math.max(
                ((Number) rows.get("magnetizer").getOrDefault("peakPolePlayerY", 0)).doubleValue(),
                clientY);
        observe("magnetizer", "peakPolePlayerY", peak);
        observe("magnetizer", "clientState", new LinkedHashMap<>(remoteHost));
        if (peak <= 183) {
          saveReport();
          return;
        }
        observe("magnetizer", "actualPlayerRiseObserved", true);
      } else {
        double peak =
            ((Number) rows.get("magnetizer").getOrDefault("peakPolePlayerY", 0)).doubleValue();
        peak = Math.max(peak, player.getY());
        observe("magnetizer", "peakPolePlayerY", peak);
        observe("magnetizer", "clientState", new LinkedHashMap<>(ACTOR_CLIENT_STATE));
        if (peak <= tile("magnetizer").getBlockPos().getY() + 2) {
          saveReport();
          return;
        }
        observe("magnetizer", "actualPlayerRiseObserved", true);
      }
    }
    ticks++;
    if (jobs.containsKey(ticks))
      for (Runnable action : List.copyOf(jobs.get(ticks))) guard("suite", "tick " + ticks, action);
    if (ticks % 100 == 0) {
      for (var entry : machines.entrySet()) {
        var row = rows.get(entry.getKey());
        var tile = entry.getValue();
        if (tile.getActive()) row.put("everActive", true);
        if (tile.hasComponent(ElectricalBuffer.class)) {
          double energy = tile.getComponent(ElectricalBuffer.class).getEnergy();
          double peak = ((Number) row.getOrDefault("peakEnergy", 0)).doubleValue();
          row.put("peakEnergy", Math.max(peak, energy));
        }
      }
      saveReport();
    }
    if (ticks >= duration) {
      guard("suite", "final behavior checks", behavior::finish);
      commonChecks("final");
      coreFinished = true;
      level.getServer().tickRateManager().stopSprinting();
      saveReport();
    }
  }

  @SuppressWarnings("unchecked")
  private void readRemoteHost() {
    try {
      var path = Path.of(System.getProperty("ic2.remoteHostReport"));
      if (!Files.exists(path)) return;
      remoteHost = new com.google.gson.Gson().fromJson(Files.readString(path), Map.class);
      if (((Number) remoteHost.getOrDefault("updatedAt", 0)).longValue()
          < System.currentTimeMillis() - 5000) return;
      var loaded = (List<String>) remoteHost.getOrDefault("loadedMachinePositions", List.of());
      if (CLIENT_GUI_POS != null)
        CLIENT_GUI_READY =
            loaded.contains(
                CLIENT_GUI_POS.getX() + "," + CLIENT_GUI_POS.getY() + "," + CLIENT_GUI_POS.getZ());
      var frames = (Map<String, Number>) remoteHost.getOrDefault("menuFrames", Map.of());
      if (CLIENT_GUI_CASE != null && CLIENT_GUI_CONTAINER_ID >= 0)
        GUI_TICKS.put(
            CLIENT_GUI_CASE,
            frames.getOrDefault(Integer.toString(CLIENT_GUI_CONTAINER_ID), 0).intValue());
      ACTOR_CLIENT_STATE.putAll(remoteHost);
    } catch (java.io.IOException | com.google.gson.JsonParseException ignored) {
    }
  }

  private void guiCycle() {
    if (guiNames == null)
      guiNames =
          machines.keySet().stream()
              .filter(
                  id ->
                      tile(id) instanceof IHasGui
                          || tile(id) instanceof ic2ce.neoforge.reactor.ReactorChamber
                          || tile(id) instanceof ic2ce.neoforge.reactor.VesselHatch)
              .filter(id -> !Boolean.getBoolean("ic2.verifyWorkbenchOnly") || id.equals("industrial_workbench")).toList();
    if (guiIndex >= guiNames.size()) {
      guiFinished = true;
      CLIENT_GUI_CASE = null;
      saveReport();
      return;
    }
    String id = guiNames.get(guiIndex);
    if (!id.equals(CLIENT_GUI_CASE)) {
      player.closeContainer();
      CLIENT_GUI_CASE = id;
      CLIENT_GUI_CONTAINER_ID = -1;
      CLIENT_GUI_POS = tile(id).getBlockPos();
      CLIENT_GUI_READY = false;
      saveReport();
      guard(
          id,
          "real client GUI",
          () -> {
            var pos = tile(id).getBlockPos();
            var server = level.getServer();
            server
                .getCommands()
                .performPrefixedCommand(
                    server.createCommandSourceStack(),
                    "tp "
                        + player.getScoreboardName()
                        + " "
                        + (pos.getX() + 0.5)
                        + " "
                        + (pos.getY() + 1)
                        + " "
                        + (pos.getZ() + 2.5)
                        + " 180 25");
          });
    }
    guiTicks++;
    boolean settled =
        !Boolean.getBoolean("ic2.verifyDedicated")
            || (Math.abs(
                        ((Number) remoteHost.getOrDefault("playerX", 0)).doubleValue()
                            - tile(id).getBlockPos().getX()
                            - 0.5)
                    < 1
                && Math.abs(
                        ((Number) remoteHost.getOrDefault("playerZ", 0)).doubleValue()
                            - tile(id).getBlockPos().getZ()
                            - 2.5)
                    < 1);
    if (CLIENT_GUI_CONTAINER_ID < 0 && CLIENT_GUI_READY && settled && guiTicks >= 10) {
      guard(
          id,
          "real client GUI after chunk arrives",
          () -> {
            var machine = tile(id);
            if (machine instanceof ic2ce.neoforge.processing.fabrication.PatternScanner scanner
                && !Boolean.getBoolean("ic2.verifyDedicated"))
              CLIENT_SCANNER_EXPECTED =
                  scanner.inputSlot.accepts(
                      new ItemStack(net.minecraft.world.item.Items.IRON_INGOT));
            boolean opened;
            if (machine instanceof IHasGui gui)
              opened = gui.openManagedBe(player, InteractionHand.MAIN_HAND);
            else {
              int previous = player.containerMenu.containerId;
              machine
                  .getBlockState()
                  .useWithoutItem(
                      level,
                      player,
                      new net.minecraft.world.phys.BlockHitResult(
                          net.minecraft.world.phys.Vec3.atCenterOf(machine.getBlockPos()),
                          Direction.NORTH,
                          machine.getBlockPos(),
                          false));
              opened = player.containerMenu.containerId != previous;
              rows.get(id).put("delegatedCoreGui", true);
            }
            check(id, "opens actual client GUI", opened);
            CLIENT_GUI_CONTAINER_ID = player.containerMenu.containerId;
          });
      guiTicks = 0;
    }
    if (Boolean.getBoolean("ic2.reviewShutdown")
        && !Boolean.getBoolean("ic2.verifyDedicated")
        && tile(id) instanceof ic2ce.neoforge.crafting.IndustrialWorkbench bench
        && CLIENT_GUI_CONTAINER_ID >= 0
        && GUI_TICKS.getOrDefault(id, 0) >= 15) {
      if (!WorkbenchNavigationProbe.serverTick(player, bench)) return;
      CLIENT_GUI_CONTAINER_ID = player.containerMenu.containerId;
      for (var failure : WorkbenchNavigationProbe.failures())
        check(id, "native workbench return workflow: " + failure, false);
    }
    boolean scannerQueryReady =
        !(tile(id) instanceof ic2ce.neoforge.processing.fabrication.PatternScanner)
            || Boolean.getBoolean("ic2.verifyDedicated")
            || CLIENT_SCANNER_DONE;
    if (CLIENT_GUI_CONTAINER_ID >= 0
            && guiTicks >= 40
            && GUI_TICKS.getOrDefault(id, 0) >= 15
            && scannerQueryReady
        || guiTicks >= 600) {
      check(
          id,
          "actual menu survives at least 15 client frames",
          GUI_TICKS.getOrDefault(id, 0) >= 15);
      if (tile(id) instanceof ic2ce.neoforge.processing.fabrication.PatternScanner
          && !Boolean.getBoolean("ic2.verifyDedicated"))
        check(
            id,
            "asynchronous native scanner RPC and client acceptance",
            CLIENT_SCANNER_DONE && CLIENT_SCANNER_PASS);
      if (tile(id) instanceof ic2ce.neoforge.processing.thermal.ElectricSmelter
          && !Boolean.getBoolean("ic2.verifyDedicated"))
        check(
            id,
            "client uses synchronized vanilla smelting ingredient hints",
            CLIENT_SMELTING_HINTS_DONE && CLIENT_SMELTING_HINTS_PASS);
      rows.get(id).put("clientGuiTicks", GUI_TICKS.getOrDefault(id, 0));
      guiIndex++;
      guiTicks = 0;
      saveReport();
    }
  }

  @SuppressWarnings("unchecked")
  private void saveReport() {
    List<String> unverified =
        rows.entrySet().stream()
            .filter(e -> ((Map<?, ?>) e.getValue().get("behavior")).isEmpty())
            .map(Map.Entry::getKey)
            .toList();
    Map<String, Object> report = new LinkedHashMap<>();
    report.put(
        "startedAt",
        ProcessHandle.current().info().startInstant().map(Object::toString).orElse("unknown"));
    report.put("registeredMachineTypes", rows.size());
    report.put("excludedNonMachines", exclusions);
    report.put("ticks", ticks);
    report.put("checks", checks);
    report.put("failures", failures);
    report.put("coreFinished", coreFinished);
    report.put("clientGuiCycleComplete", guiFinished);
    report.put("guiInProgress", CLIENT_GUI_CASE);
    report.put("unverifiedBehavior", unverified);
    report.put(
        "complete", coreFinished && guiFinished && failures.isEmpty() && unverified.isEmpty());
    report.put("machines", rows);
    try {
      Files.writeString(
          Path.of("ic2-all-machines-verification.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(report));
    } catch (Exception error) {
      throw new RuntimeException(error);
    }
  }
}
