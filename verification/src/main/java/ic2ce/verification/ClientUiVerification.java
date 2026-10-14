package ic2ce.verification;

import ic2ce.neoforge.client.screen.MachineScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Confirms actual client screens survive multiple render frames, not just menu construction. */
public final class ClientUiVerification {
  private ic2ce.core.network.Rpc<Boolean> scannerResponse;
  private int scannerFrames;
  private int clientTicks;
  private boolean widgetContractsDone,
      audioContractsDone,
      displayContractsDone,
      recipeDisplayContractsDone,
      clientBindingContractsDone;
  private boolean chunkMapContractsDone, outletContractsDone;
  private long scannerMaxCallNanos;
  private boolean scannerCallsPass = true;

  private void verifyScanner(net.minecraft.client.Minecraft minecraft) {
    if (AllMachinesVerification.CLIENT_SCANNER_DONE
        || AllMachinesVerification.CLIENT_GUI_POS == null
        || minecraft.level == null
        || AllMachinesVerification.CLIENT_GUI_CONTAINER_ID < 0
        || minecraft.player == null
        || minecraft.player.containerMenu.containerId
            != AllMachinesVerification.CLIENT_GUI_CONTAINER_ID
        || !(minecraft.level.getBlockEntity(AllMachinesVerification.CLIENT_GUI_POS)
            instanceof ic2ce.neoforge.processing.fabrication.PatternScanner scanner)) return;
    var item = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
    long started = System.nanoTime();
    boolean accepted = scanner.inputSlot.accepts(item);
    long duration = System.nanoTime() - started;
    scannerMaxCallNanos = Math.max(scannerMaxCallNanos, duration);
    scannerCallsPass &= duration < 250_000_000L;
    if (scannerResponse == null) {
      scannerCallsPass &= !accepted;
      scannerResponse =
          ic2ce.core.network.RpcHandler.run(
              ic2ce.neoforge.inventory.ScannerInput.ServerScannableCheck.class, item);
    }
    scannerFrames++;
    if (scannerFrames < 3
        || !scannerResponse.isDone()
        || accepted != AllMachinesVerification.CLIENT_SCANNER_EXPECTED) {
      if (scannerFrames < 200) return;
      scannerCallsPass = false;
    }
    boolean responseMatches;
    try {
      responseMatches =
          scannerResponse.isDone()
              && scannerResponse.completion().getNow(false)
                  == AllMachinesVerification.CLIENT_SCANNER_EXPECTED;
    } catch (RuntimeException error) {
      responseMatches = false;
    }
    AllMachinesVerification.CLIENT_SCANNER_PASS = scannerCallsPass && responseMatches;
    AllMachinesVerification.CLIENT_SCANNER_DONE = true;
    try {
      java.nio.file.Files.writeString(
          java.nio.file.Path.of("ic2-scanner-client-rpc.json"),
          new com.google.gson.GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  java.util.Map.of(
                      "complete",
                      AllMachinesVerification.CLIENT_SCANNER_PASS,
                      "expectedAcceptance",
                      AllMachinesVerification.CLIENT_SCANNER_EXPECTED,
                      "actualAcceptance",
                      accepted,
                      "responseMatches",
                      responseMatches,
                      "frames",
                      scannerFrames,
                      "maxAcceptanceCallMs",
                      scannerMaxCallNanos / 1e6)));
    } catch (java.io.IOException error) {
      throw new RuntimeException(error);
    }
  }

  private static void writeViewReport(String file, int checks, java.util.List<String> failures) {
    try {
      java.nio.file.Files.writeString(
          java.nio.file.Path.of(file),
          new com.google.gson.GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  java.util.Map.of(
                      "complete", failures.isEmpty(), "checks", checks, "failures", failures)));
    } catch (java.io.IOException error) {
      throw new RuntimeException(error);
    }
  }

  @SubscribeEvent
  public void clientTick(ClientTickEvent.Post event) {
    var minecraft = Minecraft.getInstance();
    if (Boolean.getBoolean("ic2.reviewShutdown")) WorkbenchNavigationProbe.Client.tick();
    if (Boolean.getBoolean("ic2.reviewShutdown")
        && !widgetContractsDone
        && minecraft.player != null
        && minecraft.screen instanceof MachineScreen<?> ownedScreen) {
      widgetContractsDone = true;
      var result = ic2ce.neoforge.client.widget.WidgetContracts.run(ownedScreen);
      try {
        java.nio.file.Files.writeString(
            java.nio.file.Path.of("ic2-widget-contracts.json"),
            new com.google.gson.GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(
                    java.util.Map.of(
                        "complete",
                        result.failures().isEmpty(),
                        "checks",
                        result.checks(),
                        "failures",
                        result.failures())));
      } catch (java.io.IOException error) {
        throw new RuntimeException(error);
      }
    }

    if (Boolean.getBoolean("ic2.reviewShutdown")
        && !clientBindingContractsDone
        && minecraft.player != null
        && minecraft.screen instanceof MachineScreen<?>) {
      clientBindingContractsDone = true;
      var result = ic2ce.neoforge.ClientBindingContracts.run();
      writeViewReport("ic2-client-binding-contracts.json", result.checks(), result.failures());
    }
    if (Boolean.getBoolean("ic2.reviewShutdown")
        && !recipeDisplayContractsDone
        && minecraft.player != null
        && minecraft.screen instanceof MachineScreen<?>
        && ic2ce.integration.jei.RecipeDisplayContracts.ready()) {
      recipeDisplayContractsDone = true;
      var result = ic2ce.integration.jei.RecipeDisplayContracts.run();
      writeViewReport("ic2-recipe-display-contracts.json", result.checks(), result.failures());
    }
    if (Boolean.getBoolean("ic2.reviewShutdown")
        && !displayContractsDone
        && minecraft.player != null
        && minecraft.screen instanceof MachineScreen<?>) {
      displayContractsDone = true;
      var result = ic2ce.neoforge.model.DisplayContracts.run();
      writeViewReport("ic2-display-contracts.json", result.checks(), result.failures());
    }
    if (Boolean.getBoolean("ic2.reviewShutdown")
        && !audioContractsDone
        && minecraft.player != null
        && minecraft.screen instanceof MachineScreen<?>) {
      audioContractsDone = true;
      var result = ic2ce.neoforge.client.audio.AudioContracts.run();
      writeViewReport("ic2-audio-contracts.json", result.checks(), result.failures());
    }
    if (Boolean.getBoolean("ic2.reviewShutdown")
        && !chunkMapContractsDone
        && minecraft.screen instanceof ic2ce.neoforge.utility.client.ChunkAnchorScreen screen) {
      chunkMapContractsDone = true;
      var result = ic2ce.neoforge.utility.client.ChunkMapContracts.run(screen);
      writeViewReport("ic2-chunk-map-contracts.json", result.checks(), result.failures());
    }
    if (Boolean.getBoolean("ic2.reviewShutdown")
        && !outletContractsDone
        && minecraft.screen
            instanceof ic2ce.neoforge.transport.client.OutletOrderScreen<?> screen) {
      outletContractsDone = true;
      var result = ic2ce.neoforge.transport.client.OutletScreenContracts.run(screen);
      writeViewReport("ic2-outlet-screen-contracts.json", result.checks(), result.failures());
    }
    if (Boolean.getBoolean("ic2.reviewShutdown")
        && java.nio.file.Files.exists(java.nio.file.Path.of("ic2-verification-stop.request"))) {
      minecraft.stop();
      return;
    }
    if (Boolean.getBoolean("ic2.reviewShutdown")) minecraft.options.pauseOnLostFocus = false;
    if (Boolean.getBoolean("ic2.reviewShutdown") && ++clientTicks % 100 == 0) {
      var state = new java.util.LinkedHashMap<String, Object>();
      state.put("updatedAt", System.currentTimeMillis());
      state.put("clientTicks", clientTicks);
      state.put("paused", minecraft.isPaused());
      state.put(
          "screen", minecraft.screen == null ? "none" : minecraft.screen.getClass().getName());
      state.put("machine", AllMachinesVerification.CLIENT_GUI_CASE);
      state.put("ready", AllMachinesVerification.CLIENT_GUI_READY);
      state.put("serverContainerId", AllMachinesVerification.CLIENT_GUI_CONTAINER_ID);
      state.put(
          "clientContainerId",
          minecraft.player == null ? -1 : minecraft.player.containerMenu.containerId);
      state.put("renderFrames", new java.util.TreeMap<>(AllMachinesVerification.GUI_TICKS));
      try {
        java.nio.file.Files.writeString(
            java.nio.file.Path.of("ic2-client-ui-heartbeat.json"),
            new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(state));
      } catch (java.io.IOException error) {
        throw new RuntimeException(error);
      }
    }
    if (Boolean.getBoolean("ic2.verifyAllMachines")) verifyScanner(minecraft);
    if (Boolean.getBoolean("ic2.verifyAllMachines")
        && !AllMachinesVerification.CLIENT_SMELTING_HINTS_DONE
        && AllMachinesVerification.CLIENT_GUI_POS != null
        && minecraft.level != null
        && minecraft.player != null
        && minecraft.player.containerMenu.containerId
            == AllMachinesVerification.CLIENT_GUI_CONTAINER_ID
        && minecraft.level.getBlockEntity(AllMachinesVerification.CLIENT_GUI_POS)
            instanceof ic2ce.neoforge.processing.thermal.ElectricSmelter furnace) {
      AllMachinesVerification.CLIENT_SMELTING_HINTS_PASS =
          furnace.inputSlot.accepts(
                  new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.RAW_IRON))
              && !furnace.inputSlot.accepts(
                  new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND));
      AllMachinesVerification.CLIENT_SMELTING_HINTS_DONE = true;
    }
    if (Boolean.getBoolean("ic2.reviewShutdown")) {
      org.lwjgl.glfw.GLFW.glfwHideWindow(minecraft.getWindow().handle());
      if (AllMachinesVerification.ACTIVE != null
          && AllMachinesVerification.ACTIVE.finishedCoreAndGui()
          && (!Boolean.getBoolean("ic2.verifyAllItems") || AllItemsVerification.completed)) {
        minecraft.stop();
        return;
      }
    }
    if (Boolean.getBoolean("ic2.verifyAllMachines")
        && AllMachinesVerification.ACTOR_ENERGY_BLOCK_POS != null
        && minecraft.player != null
        && minecraft.level != null) {
      var tile = minecraft.level.getBlockEntity(AllMachinesVerification.ACTOR_ENERGY_BLOCK_POS);
      var data = AllMachinesVerification.ACTOR_CLIENT_STATE;
      data.put("playerY", minecraft.player.getY());
      data.put("velocityY", minecraft.player.getDeltaMovement().y());
      data.put("onGround", minecraft.player.onGround());
      data.put("flying", minecraft.player.getAbilities().flying);
      if (tile instanceof ic2ce.neoforge.utility.PoleDrive machine)
        data.put("magnetizerEU", machine.getEnergy());
    }
    if (Boolean.getBoolean("ic2.verifyAllMachines")
        && AllMachinesVerification.CLIENT_GUI_POS != null
        && Minecraft.getInstance().level != null
        && Minecraft.getInstance().level.getBlockEntity(AllMachinesVerification.CLIENT_GUI_POS)
            != null) {
      AllMachinesVerification.CLIENT_GUI_READY = true;
    }
    if (Boolean.getBoolean("ic2.verifyAllMachines")
        && AllMachinesVerification.CLIENT_GUI_CASE != null
        && Minecraft.getInstance().screen
            instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen
        && screen.getMenu().containerId == AllMachinesVerification.CLIENT_GUI_CONTAINER_ID) {
      AllMachinesVerification.GUI_TICKS.merge(
          AllMachinesVerification.CLIENT_GUI_CASE, 1, Integer::sum);
    }
    if (Minecraft.getInstance().screen instanceof MachineScreen<?> gui
        && gui.getContainer().base instanceof BlockEntity tile) {
      String id = BuiltInRegistries.BLOCK.getKey(tile.getBlockState().getBlock()).getPath();
      MigrationVerification.CLIENT_GUI_TICKS.merge(id, 1, Integer::sum);
    }
  }
}
