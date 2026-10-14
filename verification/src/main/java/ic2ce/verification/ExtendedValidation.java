package ic2ce.verification;

import static ic2ce.verification.AllMachinesVerification.*;

import com.google.gson.GsonBuilder;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.core.IHasGui;
import ic2ce.core.util.LiquidUtil;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.reactor.NuclearCore;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Isolated two-client server validation, followed by a measured real-time soak. */
public final class ExtendedValidation {
  public AllMachinesVerification s;
  public final Map<String, Object> details = new LinkedHashMap<>();
  private final List<String> failures = new ArrayList<>();

  public List<String> failureList() {
    return List.copyOf(failures);
  }

  private final List<Map<String, Object>> samples = new ArrayList<>();
  private final List<Map<String, Object>> outcomes = new ArrayList<>();
  private String phase = "WAITING_FOR_TWO_PLAYERS";
  private int ticks, phaseTicks, checks;
  private UpgradeMatrix upgrades;
  private ReactorMatrix reactors;
  private UpgradeFunctionality functionality;
  private ReactorFunctionality reactorFunctionality;
  private AdvancedFilterFunctionality advancedFilters;
  private ServerPlayer guest;
  private NativeBlockEntity sharedBox;
  private long longStart;
  private long maceratorSupplied,
      maceratorCollected,
      furnaceSupplied,
      furnaceCollected,
      fluidSupplied,
      fluidCollected;
  private final int soakSeconds = Integer.getInteger("ic2.longRunSeconds", 7200);
  private long longTicks;
  private boolean complete;

  @SubscribeEvent
  public void started(ServerStartedEvent event) {
    Map<String, Object> catalog = new LinkedHashMap<>();
    catalog.put(
        "items",
        BuiltInRegistries.ITEM.keySet().stream()
            .filter(id -> id.getNamespace().equals("ic2"))
            .map(
                id ->
                    Map.of(
                        "id",
                        id.toString(),
                        "key",
                        BuiltInRegistries.ITEM.getValue(id).getDescriptionId(),
                        "class",
                        BuiltInRegistries.ITEM.getValue(id).getClass().getName()))
            .toList());
    catalog.put(
        "blocks",
        BuiltInRegistries.BLOCK.keySet().stream()
            .filter(id -> id.getNamespace().equals("ic2"))
            .map(
                id ->
                    Map.of(
                        "id",
                        id.toString(),
                        "key",
                        BuiltInRegistries.BLOCK.getValue(id).getDescriptionId()))
            .toList());
    write(Path.of("registered-localization-catalog.json"), catalog);
  }

  public void check(String description, boolean passed) {
    checks++;
    if (!passed) {
      failures.add(description);
      System.err.println("IC2_EXTENDED FAIL " + description);
    }
  }

  private void change(String next) {
    phase = next;
    phaseTicks = 0;
    save();
    System.out.println("IC2_EXTENDED PHASE " + next);
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    s = AllMachinesVerification.ACTIVE;
    if (s == null || s.level == null) return;
    ticks++;
    phaseTicks++;
    try {
      if (phase.startsWith("WAITING")) {
        if (s.machines.isEmpty()) {
          if (ticks % 100 == 0) save();
          return;
        }
        guest =
            s.level.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> p.getScoreboardName().equals("IC2Guest"))
                .findFirst()
                .orElse(null);
        if (guest != null) {
          if (!guest.isAlive())
            guest =
                s.level
                    .getServer()
                    .getPlayerList()
                    .respawn(guest, false, net.minecraft.world.entity.Entity.RemovalReason.KILLED);
          guest.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
          guest.setInvulnerable(true);
          guest.setHealth(guest.getMaxHealth());
          s.player.setInvulnerable(true);
          s.player.setHealth(s.player.getMaxHealth());
        }
        if (!s.finishedCoreAndGui()) {
          phase = "WAITING_FOR_BASE_MACHINE_SUITE";
          if (ticks % 100 == 0) save();
          return;
        }
        for (String client : List.of("ic2.remoteHostReport", "ic2.remoteGuestReport")) {
          var observation = readClient(client);
          if (((Number) observation.getOrDefault("updatedAt", 0)).longValue()
                  < System.currentTimeMillis() - 5000
              || ((Map<?, ?>) observation.getOrDefault("recipeCounts", Map.of())).size() < 14
              || !Files.isRegularFile(
                  Path.of(System.getProperty(client))
                      .resolveSibling("runtime-language-audit.json"))) {
            phase = "WAITING_FOR_REMOTE_CLIENT_OBSERVATIONS";
            if (ticks % 100 == 0) save();
            return;
          }
        }
        failures.addAll(s.failureList());
        if (!failures.isEmpty()) {
          change("FAILED_BASE_SUITE");
          return;
        }
        check("both network actors are alive", s.player.isAlive() && guest.isAlive());
        check(
            "two distinct actual network players connected",
            guest != null && s.player != guest && !s.player.getUUID().equals(guest.getUUID()));
        details.put("hostUUID", s.player.getUUID().toString());
        details.put("guestUUID", guest.getUUID().toString());
        var expectedRecipes = new TreeMap<String, Integer>();
        for (var id : BuiltInRegistries.RECIPE_TYPE.keySet())
          if (id.getNamespace().equals("ic2"))
            expectedRecipes.put(
                id.toString(),
                s.level
                    .recipeAccess()
                    .recipeMap()
                    .byType(
                        (net.minecraft.world.item.crafting.RecipeType)
                            BuiltInRegistries.RECIPE_TYPE.getValue(id))
                    .size());
        details.put("expectedClientRecipeCounts", expectedRecipes);
        for (String client : List.of("ic2.remoteHostReport", "ic2.remoteGuestReport")) {
          var report = readClient(client);
          var actual = (Map<String, Number>) report.getOrDefault("recipeCounts", Map.of());
          for (var entry : expectedRecipes.entrySet())
            check(
                client + " synced recipes: " + entry.getKey(),
                actual.getOrDefault(entry.getKey(), -1).intValue() == entry.getValue());
          try {
            var audit =
                new com.google.gson.Gson()
                    .fromJson(
                        Files.readString(
                            Path.of(System.getProperty(client))
                                .resolveSibling("runtime-language-audit.json")),
                        Map.class);
            check(
                client + " runtime English/Chinese names, tooltips and variants complete",
                Boolean.TRUE.equals(audit.get("complete")));
            check(
                client + " full client crop catalog initialized",
                ((Number) audit.getOrDefault("cropCards", 0)).intValue() == 37);
          } catch (Exception error) {
            check(client + " runtime language audit readable: " + error, false);
          }
        }
        if (Boolean.getBoolean("ic2.resumeExtendedAfterMatrix")) {
          restorePassedMatrices();
          advancedFilters = new AdvancedFilterFunctionality(this);
          advancedFilters.prepare();
          change("ADVANCED_FILTER_FUNCTIONALITY");
        } else {
          functionality = new UpgradeFunctionality(this);
          functionality.prepare();
          s.level.getServer().tickRateManager().requestGameToSprint(1000);
          change("UPGRADE_FUNCTIONALITY");
        }
      } else if (phase.equals("ADVANCED_FILTER_FUNCTIONALITY")) {
        if (advancedFilters.tick()) {
          if (!failures.isEmpty()) {
            change("FAILED_ADVANCED_FILTER_FUNCTIONALITY");
            return;
          }
          reactorFunctionality = new ReactorFunctionality(this);
          reactorFunctionality.prepare();
          change("REACTOR_FUNCTIONALITY");
        }
      } else if (phase.equals("UPGRADE_FUNCTIONALITY")) {
        if (functionality.tick()) {
          if (!failures.isEmpty()) {
            s.level.getServer().tickRateManager().stopSprinting();
            change("FAILED_UPGRADE_FUNCTIONALITY");
            return;
          }
          upgrades = new UpgradeMatrix(this);
          upgrades.prepare();
          s.level.getServer().tickRateManager().requestGameToSprint(10000);
          change("UPGRADE_MATRIX");
        }
      } else if (phase.equals("UPGRADE_MATRIX")) {
        if (upgrades.tick()) {
          if (!failures.isEmpty()) {
            s.level.getServer().tickRateManager().stopSprinting();
            change("FAILED_UPGRADE_MATRIX");
            return;
          }
          reactors = new ReactorMatrix(this);
          reactors.prepare();
          s.level.getServer().tickRateManager().requestGameToSprint(100000);
          change("REACTOR_MATRIX");
        }
      } else if (phase.equals("REACTOR_MATRIX")) {
        if (reactors.tick()) {
          s.level.getServer().tickRateManager().stopSprinting();
          if (!failures.isEmpty()) {
            change("FAILED_REACTOR_MATRIX");
            return;
          }
          reactorFunctionality = new ReactorFunctionality(this);
          reactorFunctionality.prepare();
          s.level.getServer().tickRateManager().requestGameToSprint(1000);
          change("REACTOR_FUNCTIONALITY");
        }
      } else if (phase.equals("REACTOR_FUNCTIONALITY")) {
        if (reactorFunctionality.tick()) {
          s.level.getServer().tickRateManager().stopSprinting();
          if (!failures.isEmpty()) {
            change("FAILED_REACTOR_FUNCTIONALITY");
            return;
          }
          multiplayerSetup();
          change("MULTIPLAYER_SHARED_INVENTORY");
        }
      } else if (phase.equals("MULTIPLAYER_SHARED_INVENTORY")) multiplayerTick();
      else if (phase.equals("MULTIPLAYER_CHUNK_RESYNC")) resyncTick();
      else if (phase.equals("REAL_TIME_SOAK")) soakTick();
      if (ticks % 100 == 0) save();
    } catch (Throwable error) {
      error.printStackTrace();
      check(phase + ": " + error, false);
      s.level.getServer().tickRateManager().stopSprinting();
      change("FAILED_EXCEPTION");
    }
  }

  private void multiplayerSetup() {
    s.player.closeContainer();
    guest.closeContainer();
    var chest = (ic2ce.neoforge.personal.PrivateChest) s.tile("personal_chest");
    check(
        "private chest accepts owner and denies real second player",
        ic2ce.neoforge.personal.PrivateChest.checkAccess(chest, s.player.getGameProfile())
            && !ic2ce.neoforge.personal.PrivateChest.checkAccess(chest, guest.getGameProfile()));
    for (var actor : List.of(s.player, guest))
      for (int n = 0; n < actor.getInventory().getContainerSize(); n++)
        if (actor.getInventory().getItem(n).is(Items.IRON_INGOT))
          actor.getInventory().setItem(n, ItemStack.EMPTY);
    var pos = new BlockPos(152, 181, 152);
    s.level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
    for (int x = -6; x <= 6; x++)
      for (int z = -6; z <= 6; z++)
        s.level.setBlockAndUpdate(pos.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
    sharedBox = s.helper("iron_storage_box", pos);
    ((InventorySlot) get(sharedBox, "inventory")).put(new ItemStack(Items.IRON_INGOT, 13));
    sharedBox.setChanged();
    for (var actor : List.of(s.player, guest))
      actor.teleportTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 2.5);
    details.put("sharedBoxPosition", List.of(pos.getX(), pos.getY(), pos.getZ()));
  }

  @SuppressWarnings("unchecked")
  private void restorePassedMatrices() throws Exception {
    var source = Path.of(System.getProperty("ic2.previousExtendedReport"));
    var prior = new com.google.gson.Gson().fromJson(Files.readString(source), Map.class);
    var oldDetails = (Map<String, Object>) prior.get("details");
    var deep =
        (List<Map<String, Object>>) oldDetails.getOrDefault("reactorFunctionalCases", List.of());
    Set<String> failedDeep = new HashSet<>();
    for (var row : deep)
      if (Boolean.FALSE.equals(row.get("passed"))) failedDeep.add((String) row.get("test"));
    for (var failure : (List<String>) prior.get("failures"))
      if (!failedDeep.contains(failure))
        throw new IllegalStateException("Previous stage failed: " + failure);
    if (((List<?>) oldDetails.getOrDefault("reactorCases", List.of())).size() != 5220
        || !oldDetails.containsKey("upgradeCases"))
      throw new IllegalStateException("Incomplete preceding matrices");
    int freshChecks = checks;
    details.putAll(oldDetails);
    details.remove("reactorFunctionalCases");
    checks = ((Number) prior.get("checks")).intValue() - deep.size() + freshChecks;
    details.put("passedMatricesSource", source.toString());
    details.put("resumedExistingPassedWorld", true);
  }

  private int iron(ServerPlayer actor) {
    int result = 0;
    for (int n = 0; n < actor.getInventory().getContainerSize(); n++) {
      var item = actor.getInventory().getItem(n);
      if (item.is(Items.IRON_INGOT)) result += item.getCount();
    }
    if (actor.containerMenu.getCarried().is(Items.IRON_INGOT))
      result += actor.containerMenu.getCarried().getCount();
    return result;
  }

  private void multiplayerTick() {
    if (phaseTicks == 60) {
      var gui = (IHasGui) sharedBox;
      check(
          "both remote clients can open shared inventory",
          gui.openManagedBe(s.player, InteractionHand.MAIN_HAND)
              && gui.openManagedBe(guest, InteractionHand.MAIN_HAND));
      details.put("hostSharedMenuId", s.player.containerMenu.containerId);
      details.put("guestSharedMenuId", guest.containerMenu.containerId);
      s.player.sendSystemMessage(
          net.minecraft.network.chat.Component.literal(
              "IC2 multiplayer verification: click the 13 iron ingots in both clients, then close"
                  + " both screens."));
    }
    var hostClicks = readClient("ic2.remoteHostReport");
    var guestClicks = readClient("ic2.remoteGuestReport");
    boolean clickedBoth =
        clicks(hostClicks, ((Number) details.getOrDefault("hostSharedMenuId", -1)).intValue()) > 0
            && clicks(
                    guestClicks,
                    ((Number) details.getOrDefault("guestSharedMenuId", -1)).intValue())
                > 0;
    if (phaseTicks > 60
        && clickedBoth
        && ((InventorySlot) get(sharedBox, "inventory")).isEmpty()
        && iron(s.player) + iron(guest) > 0) {
      check(
          "two-client shared pickup conserves exactly thirteen ingots",
          iron(s.player) + iron(guest) == 13);
      var host = readClient("ic2.remoteHostReport");
      var other = readClient("ic2.remoteGuestReport");
      check(
          "shared inventory rendered on both real remote clients",
          frames(host, ((Number) details.get("hostSharedMenuId")).intValue()) >= 15
              && frames(other, ((Number) details.get("guestSharedMenuId")).intValue()) >= 15);
      details.put("sharedPickupHost", iron(s.player));
      details.put("sharedPickupGuest", iron(guest));
      details.put(
          "hostSharedNativeClicks",
          clicks(hostClicks, ((Number) details.get("hostSharedMenuId")).intValue()));
      details.put(
          "guestSharedNativeClicks",
          clicks(guestClicks, ((Number) details.get("guestSharedMenuId")).intValue()));
      s.player.closeContainer();
      guest.closeContainer();
      guest.teleportTo(3000.5, 182, 3000.5);
      change("MULTIPLAYER_CHUNK_RESYNC");
    }
  }

  private void resyncTick() {
    if (phaseTicks == 100) {
      var report = readClient("ic2.remoteGuestReport");
      check(
          "distant real client unloads original machine chunks",
          ((List<?>) report.getOrDefault("loadedMachinePositions", List.of())).size() < 10);
      guest.teleportTo(232.5, 184, 232.5);
    }
    if (phaseTicks == 240) {
      var report = readClient("ic2.remoteGuestReport");
      check(
          "returning real client reloads machine data",
          ((List<?>) report.getOrDefault("loadedMachinePositions", List.of())).size() > 50);
      check(
          "both players remain connected after chunk transition",
          s.level.getServer().getPlayerList().getPlayerCount() == 2);
      if (!failures.isEmpty()) {
        change("FAILED_MULTIPLAYER");
        return;
      }
      startSoak();
      change("REAL_TIME_SOAK");
    }
  }

  private void startSoak() {
    s.level.getServer().tickRateManager().stopSprinting();
    s.player.closeContainer();
    guest.closeContainer();
    for (String id : List.of("macerator", "electric_furnace")) {
      var machine = (PoweredProcessor<?, ?, ?>) s.tile(id);
      machine.inputSlot.clear();
      machine.outputSlot.clear();
      machine.upgradeSlot.clear();
      machine.upgradeSlot.put(0, stack("overclocker_upgrade", 2));
      machine.setChanged();
    }
    var router =
        (ic2ce.neoforge.transport.PriorityFluidRouter) s.tile("weighted_fluid_distributor");
    // GUI admission tests may leave an intentionally empty route. Prepare the physical workload.
    router.getPriority().clear();
    router.getPriority().addAll(List.of(Direction.EAST, Direction.SOUTH));
    router.updatePriority(true);
    check("soak route admits the actual east receiver", router.getPriority().contains(Direction.EAST));
    var distributor = s.tank("weighted_fluid_distributor", "fluidTank");
    distributor.drainMbUnchecked(Integer.MAX_VALUE, false);
    for (Direction side : List.of(Direction.EAST, Direction.SOUTH))
      LiquidUtil.drainTile(
          s.level.getBlockEntity(s.tile("weighted_fluid_distributor").getBlockPos().relative(side)),
          side.getOpposite(),
          fluid("coolant"),
          Integer.MAX_VALUE,
          false);
    s.level.setBlockAndUpdate(
        s.tile("nuclear_reactor").getBlockPos().south(), Blocks.REDSTONE_BLOCK.defaultBlockState());
    details.put("soakRequestedSeconds", soakSeconds);
    details.put("soakStartedAt", java.time.Instant.now().toString());
    longStart = System.nanoTime();
  }

  private void soakTick() {
    longTicks++;
    if (phaseTicks % 100 == 0) {
      var mac = (PoweredProcessor<?, ?, ?>) s.tile("macerator");
      var furnace = (PoweredProcessor<?, ?, ?>) s.tile("electric_furnace");
      for (var item : mac.outputSlot) {
        if (!item.isEmpty()) {
          check("sustained macerator output is raw iron", item.is(Items.RAW_IRON));
          maceratorCollected += item.getCount();
        }
      }
      mac.outputSlot.clear();
      for (var item : furnace.outputSlot) {
        if (!item.isEmpty()) {
          check("sustained furnace output is iron ingots", item.is(Items.IRON_INGOT));
          furnaceCollected += item.getCount();
        }
      }
      furnace.outputSlot.clear();
      if (mac.inputSlot.isEmpty()) {
        mac.inputSlot.put(new ItemStack(Items.IRON_ORE, 8));
        maceratorSupplied += 8;
        mac.setChanged();
      }
      if (furnace.inputSlot.isEmpty()) {
        furnace.inputSlot.put(new ItemStack(Items.IRON_ORE, 8));
        furnaceSupplied += 8;
        furnace.setChanged();
      }
      check(
          "sustained furnace consumes exactly one ore per ingot",
          furnaceCollected == furnaceSupplied - furnace.inputSlot.get().getCount());
      long consumed = maceratorSupplied - mac.inputSlot.get().getCount();
      check(
          "sustained macerator quantity stays within recipe random range",
          maceratorCollected >= consumed && maceratorCollected <= consumed * 6);
      var regulator = s.tile("weighted_fluid_distributor");
      var receiver = s.level.getBlockEntity(regulator.getBlockPos().east());
      fluidCollected +=
          LiquidUtil.drainTile(receiver, Direction.WEST, fluid("coolant"), 1000, false);
      var tank = s.tank("weighted_fluid_distributor", "fluidTank");
      if (tank.isEmpty()) {
        fluidSupplied += tank.fillMbUnchecked(Ic2FluidStack.create(fluid("coolant"), 1000), false);
        regulator.setChanged();
      }
      int east =
          receiver instanceof NativeBlockEntity tile
              ? tile.getComponent(FluidPorts.class).getAllTanks().iterator().next().getFluidAmount()
              : 0;
      var south = s.level.getBlockEntity(regulator.getBlockPos().south());
      int southMB =
          south instanceof NativeBlockEntity tile
              ? tile.getComponent(FluidPorts.class).getAllTanks().iterator().next().getFluidAmount()
              : 0;
      if (phaseTicks == 200)
        check("prepared soak circuit delivers actual fluid within ten seconds", fluidCollected > 0);
      check(
          "sustained fluid delivery conserves volume",
          fluidSupplied == fluidCollected + tank.getFluidAmount() + east + southMB);
      for (var entry : s.machines.entrySet()) {
        var tile = entry.getValue();
        check(
            "soak machine present: " + entry.getKey(),
            s.level.getBlockEntity(tile.getBlockPos()) == tile);
        if (tile.hasComponent(ElectricalBuffer.class))
          check(
              "soak energy valid: " + entry.getKey(),
              Double.isFinite(tile.getComponent(ElectricalBuffer.class).getEnergy())
                  && tile.getComponent(ElectricalBuffer.class).getEnergy() >= 0);
      }
      check(
          "both real network players stay online",
          s.level.getServer().getPlayerList().getPlayerCount() == 2);
      var reactor = (NuclearCore) s.tile("nuclear_reactor");
      double nominal = reactor.getReactorEUEnergyOutput(), available = reactor.getOfferedEnergy();
      var reactorStats = ic2ce.api.energy.EnergyNet.instance.getNodeStats(reactor);
      double delivered = reactorStats.getEnergyOut();
      check(
          "sustained reactor nominal production and hull remain healthy",
          nominal > 0 && reactor.getHeat() < 1000);
      check(
          "sustained actual reactor delivery plus remaining offer stays within one tick budget",
          delivered > 0 && available >= 0 && delivered + available <= nominal + 1e-7);
    }
    if (phaseTicks % 1200 == 0) {
      double elapsed = (System.nanoTime() - longStart) / 1e9;
      var sample = new LinkedHashMap<String, Object>();
      sample.put("elapsedSeconds", elapsed);
      sample.put("worldTicks", longTicks);
      sample.put("averageTPS", longTicks / elapsed);
      sample.put(
          "serverHeapUsed", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
      sample.put("serverHeapCommitted", Runtime.getRuntime().totalMemory());
      var reactor = (NuclearCore) s.tile("nuclear_reactor");
      sample.put("reactorNominalEU", reactor.getReactorEUEnergyOutput());
      sample.put("reactorRemainingEU", reactor.getOfferedEnergy());
      sample.put(
          "reactorDeliveredEU",
          ic2ce.api.energy.EnergyNet.instance.getNodeStats(reactor).getEnergyOut());
      sample.put("reactorHeat", reactor.getHeat());
      sample.put("players", s.level.getServer().getPlayerList().getPlayerCount());
      sample.put("maceratorOutput", maceratorCollected);
      sample.put("furnaceOutput", furnaceCollected);
      sample.put("deliveredFluidMB", fluidCollected);
      sample.put("hostClient", readClient("ic2.remoteHostReport"));
      sample.put("guestClient", readClient("ic2.remoteGuestReport"));
      samples.add(sample);
      if (samples.size() % 10 == 0)
        for (var entry : s.machines.entrySet()) {
          var tile = entry.getValue();
          var saved = tile.saveWithFullMetadata(s.level.registryAccess());
          var restored =
              net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                  tile.getBlockPos(), tile.getBlockState(), saved, s.level.registryAccess());
          check(
              "periodic soak NBT entity type: " + entry.getKey(),
              restored != null && restored.getType() == tile.getType());
        }
    }
    details.put("soakElapsedSeconds", (System.nanoTime() - longStart) / 1e9);
    details.put("soakWorldTicks", longTicks);
    if ((System.nanoTime() - longStart) / 1e9 >= soakSeconds) {
      check(
          "two-hour processing actually produced items and delivered fluids",
          maceratorCollected > 100 && furnaceCollected > 100 && fluidCollected > 100000);
      check(
          "soak maintained near-normal tick rate",
          longTicks / ((System.nanoTime() - longStart) / 1e9) > 18);
      complete = failures.isEmpty();
      change(complete ? "COMPLETE" : "FAILED_SOAK");
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> readClient(String property) {
    try {
      return new com.google.gson.Gson()
          .fromJson(Files.readString(Path.of(System.getProperty(property))), Map.class);
    } catch (Exception error) {
      return Map.of();
    }
  }

  @SuppressWarnings("unchecked")
  private int frames(Map<String, Object> report, int id) {
    return ((Map<String, Number>) report.getOrDefault("menuFrames", Map.of()))
        .getOrDefault(Integer.toString(id), 0)
        .intValue();
  }

  @SuppressWarnings("unchecked")
  private int clicks(Map<String, Object> report, int id) {
    return ((Map<String, Number>) report.getOrDefault("menuClicks", Map.of()))
        .getOrDefault(Integer.toString(id), 0)
        .intValue();
  }

  private void save() {
    var report = new LinkedHashMap<String, Object>();
    report.put("phase", phase);
    report.put("ticks", ticks);
    report.put("phaseTicks", phaseTicks);
    report.put("checks", checks);
    report.put("failures", failures);
    report.put("complete", complete);
    report.put("details", details);
    report.put("samples", samples);
    write(Path.of("ic2-extended-verification.json"), report);
  }

  private void write(Path path, Object value) {
    try {
      var temporary = path.resolveSibling(path.getFileName() + ".tmp");
      Files.writeString(temporary, new GsonBuilder().setPrettyPrinting().create().toJson(value));
      Files.move(
          temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (java.io.IOException transientFileLock) {
      /* Retry on the next report tick. */
    }
  }
}
