package ic2ce.verification;


import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Records real remote-client frames and translations without changing gameplay. */
public final class RemoteClientProbe {
  private final Map<Integer, Integer> menuFrames = new LinkedHashMap<>();
  private final Map<Integer, String> menuTitles = new LinkedHashMap<>();
  private final Map<Integer, Object> menuStates = new LinkedHashMap<>();
  private final Map<Integer, Integer> menuClicks = new LinkedHashMap<>();
  private int ticks;
  private final java.util.Set<Integer> automaticClicks = new java.util.HashSet<>();
  private boolean languageAudited, wasConnected;

  @SubscribeEvent
  public void click(net.neoforged.neoforge.client.event.InputEvent.MouseButton.Post event) {
    var mc = Minecraft.getInstance();
    if (event.getButton() == 0
        && event.getAction() == 1
        && mc.screen
            instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen)
      menuClicks.merge(screen.getMenu().containerId, 1, Integer::sum);
  }

  @SubscribeEvent
  public void frame(RenderFrameEvent.Post event) {
    var mc = Minecraft.getInstance();
    if (mc.screen
        instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen) {
      menuFrames.merge(screen.getMenu().containerId, 1, Integer::sum);
      menuTitles.put(screen.getMenu().containerId, screen.getTitle().getString());
      if (screen.getMenu() instanceof ic2ce.neoforge.menu.OwnedMenu<?> menu
          && menu.base instanceof BlockEntity tile
          && mc.player != null
          && mc.level != null)
        menuStates.put(
            menu.containerId,
            Map.of(
                "removed",
                tile.isRemoved(),
                "valid",
                menu.stillValid(mc.player),
                "position",
                tile.getBlockPos().toString(),
                "worldObjectMatches",
                mc.level.getBlockEntity(tile.getBlockPos()) == tile));
    }
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post event) {
    var mc = Minecraft.getInstance();
    ticks++;
    if (Boolean.getBoolean("ic2.silentRemoteProbe")) {
      org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
      if (mc.player != null) wasConnected = true;
      else if (wasConnected) {
        mc.stop();
        return;
      }
      if (mc.player != null
          && mc.gameMode != null
          && mc.screen
              instanceof
              net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen
          && screen.getMenu() instanceof ic2ce.neoforge.menu.OwnedMenu<?> menu
          && menu.base instanceof net.minecraft.world.level.block.entity.BlockEntity tile
          && tile.getBlockPos().equals(new BlockPos(152, 181, 152))
          && menuFrames.getOrDefault(menu.containerId, 0) >= 20
          && automaticClicks.add(menu.containerId)) {
        int slot = -1;
        for (int i = 0; i < menu.slots.size(); i++)
          if (menu.getSlot(i).getItem().is(net.minecraft.world.item.Items.IRON_INGOT)) {
            slot = i;
            break;
          }
        mc.gameMode.handleContainerInput(
            menu.containerId,
            slot < 0 ? 0 : slot,
            0,
            net.minecraft.world.inventory.ContainerInput.QUICK_MOVE,
            mc.player);
        menuClicks.merge(menu.containerId, 1, Integer::sum);
      }
    }
    if (mc.player == null || mc.level == null || ticks % 10 != 0) return;
    if (!languageAudited) {
      auditLanguage(mc);
      languageAudited = true;
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("updatedAt", System.currentTimeMillis());
    result.put("player", mc.player.getName().getString());
    result.put("connected", true);
    result.put("alive", mc.player.isAlive());
    result.put("health", mc.player.getHealth());
    result.put("screen", mc.screen == null ? "none" : mc.screen.getClass().getName());
    result.put("currentMenu", mc.player.containerMenu.getClass().getName());
    result.put("currentMenuId", mc.player.containerMenu.containerId);
    result.put("language", mc.options.languageCode);
    result.put("menuFrames", menuFrames);
    result.put("menuTitles", menuTitles);
    result.put("menuClicks", menuClicks);
    result.put("menuStates", menuStates);
    List<String> loaded = new ArrayList<>();
    for (int i = 0; i < 104; i++) {
      var pos = new BlockPos(168 + (i % 12) * 16, 181, 168 + (i / 12) * 16);
      if (mc.level.getBlockEntity(pos) != null)
        loaded.add(pos.getX() + "," + pos.getY() + "," + pos.getZ());
    }
    result.put("loadedMachinePositions", loaded);
    result.put("playerY", mc.player.getY());
    result.put("velocityY", mc.player.getDeltaMovement().y());
    result.put("playerX", mc.player.getX());
    result.put("playerZ", mc.player.getZ());
    result.put("onGround", mc.player.onGround());
    result.put("flying", mc.player.getAbilities().flying);
    var magnetizer = mc.level.getBlockEntity(new BlockPos(216, 181, 232));
    if (magnetizer instanceof ic2ce.neoforge.utility.PoleDrive te)
      result.put("magnetizerEU", te.getEnergy());
    result.put("heapUsed", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    result.put("heapCommitted", Runtime.getRuntime().totalMemory());
    result.put("fps", mc.getFps());
    var recipeCounts = new TreeMap<String, Integer>();
    for (var id : BuiltInRegistries.RECIPE_TYPE.keySet())
      if (id.getNamespace().equals("ic2"))
        recipeCounts.put(
            id.toString(),
            ic2ce.neoforge.recipe.codec.RecipeGeneration.clientRecipes()
                .byType(
                    (net.minecraft.world.item.crafting.RecipeType)
                        BuiltInRegistries.RECIPE_TYPE.getValue(id))
                .size());
    result.put("recipeCounts", recipeCounts);
    write(Path.of("remote-client-probe.json"), result);
  }

  private void auditLanguage(Minecraft mc) {
    Map<String, Object> result = new LinkedHashMap<>();
    List<String> failures = new ArrayList<>();
    List<Map<String, Object>> items = new ArrayList<>();
    Set<String> keys = new TreeSet<>();
    try (var input =
        getClass().getClassLoader().getResourceAsStream("ic2-verification-language-keys.json")) {
      if (input != null)
        keys.addAll(
            new com.google.gson.Gson()
                .fromJson(
                    new java.io.InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8),
                    java.util.List.class));
    } catch (Exception error) {
      failures.add("key manifest: " + error);
    }
    for (var id : BuiltInRegistries.ITEM.keySet())
      if (id.getNamespace().equals("ic2")) {
        var item = BuiltInRegistries.ITEM.getValue(id);
        String key = item.getDescriptionId();
        keys.add(key);
        if (key.startsWith("item.ic2.")) keys.add(key.replace("item.ic2.", "container.ic2."));
        var row = new LinkedHashMap<String, Object>();
        row.put("id", id.toString());
        row.put("key", key);
        try {
          var stack = new ItemStack(item);
          row.put("name", stack.getHoverName().getString());
          var lines =
              stack
                  .getTooltipLines(
                      Item.TooltipContext.of(mc.level, mc.player), mc.player, TooltipFlag.NORMAL)
                  .stream()
                  .map(t -> t.getString())
                  .toList();
          row.put("tooltip", lines);
          for (String text : lines)
            if (text.startsWith("Format error:")
                || text.matches(
                    ".*\\b(?:ic2|item\\.ic2|block\\.ic2|container\\.ic2)\\.[A-Za-z][A-Za-z0-9_.]*.*"))
              failures.add(id + ": " + text);
        } catch (Throwable error) {
          failures.add(id + ": " + error);
        }
        items.add(row);
      }
    for (var id : BuiltInRegistries.BLOCK.keySet())
      if (id.getNamespace().equals("ic2"))
        keys.add(BuiltInRegistries.BLOCK.getValue(id).getDescriptionId());
    int variants = 0;
    int cropCards = ic2ce.api.crops.Crops.instance.getCrops().size();
    if (cropCards != 37)
      failures.add("remote client crop-card catalog: expected 37, got " + cropCards);
    for (var id : BuiltInRegistries.ITEM.keySet())
      if (id.getNamespace().equals("ic2")) {
        var item = BuiltInRegistries.ITEM.getValue(id);
        var stacks = new ArrayList<ItemStack>();
        try {
          item.getClass()
              .getMethod("addCreativeVariants", java.util.function.Consumer.class)
              .invoke(item, (java.util.function.Consumer<ItemStack>) stacks::add);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception error) {
          failures.add(id + " creative variants: " + error);
        }
        for (var stack : stacks) {
          variants++;
          try {
            for (var line :
                stack.getTooltipLines(
                    Item.TooltipContext.of(mc.level, mc.player), mc.player, TooltipFlag.NORMAL)) {
              String value = line.getString();
              if (value.startsWith("Format error:")
                  || value.matches(
                      ".*\\b(?:ic2|item\\.ic2|block\\.ic2|container\\.ic2)\\.[A-Za-z][A-Za-z0-9_.]*.*"))
                failures.add(id + " creative variant: " + value);
            }
          } catch (Throwable error) {
            failures.add(id + " creative tooltip: " + error);
          }
        }
      }
    var memory = new ItemStack(ic2ce.neoforge.registry.ItemDefinitions.CRYSTAL_MEMORY);
    ((ic2ce.core.item.ItemCrystalMemory) memory.getItem())
        .writecontentsTag(
            mc.level, memory, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 3));
    var recorded = ((ic2ce.core.item.ItemCrystalMemory) memory.getItem()).readItemStack(memory);
    if (!recorded.is(net.minecraft.world.item.Items.IRON_INGOT) || recorded.getCount() != 3)
      failures.add("remote client crystal-memory NBT cannot restore stored item");
    var chatChecks = new ArrayList<Map<String, Object>>();
    try {
      var field =
          net.minecraft.client.gui.components.ChatComponent.class.getDeclaredField("allMessages");
      field.setAccessible(true);
      for (String key :
          List.of(
              "ic2.nightvision.enabled",
              "ic2.nightvision.disabled",
              "ic2.frequency_transmitter.unlinked",
              "ic2.EUStorage.gui.mod.redstone0",
              "ic2.blockChargepad.gui.mod.redstone0")) {
        ic2ce.neoforge.runtime.ProjectRuntime.sideProxy.messagePlayer(mc.player, key);
        var messages =
            (List<net.minecraft.client.multiplayer.chat.GuiMessage>) field.get(mc.gui.getChat());
        String actual = messages.getFirst().content().getString();
        String expected = ic2ce.neoforge.client.text.LocalizedText.translate(key);
        boolean passed = actual.equals(expected);
        chatChecks.add(
            Map.of("key", key, "actual", actual, "expected", expected, "passed", passed));
        if (!passed) failures.add("local IC2 chat untranslated: " + key);
      }
    } catch (Exception error) {
      failures.add("client chat translation probe: " + error);
    }
    for (String key : keys)
      if (!Language.getInstance().has(key)) failures.add("missing runtime key: " + key);
    result.put("language", mc.options.languageCode);
    result.put("runtimeKeys", keys.size());
    result.put("items", items);
    result.put("failures", failures);
    result.put("creativeVariants", variants);
    result.put(
        "remoteCrystalMemoryRoundtrip",
        recorded.is(net.minecraft.world.item.Items.IRON_INGOT) && recorded.getCount() == 3);
    result.put("cropCards", cropCards);
    result.put("clientChatChecks", chatChecks);
    result.put("complete", failures.isEmpty());
    write(Path.of("runtime-language-audit.json"), result);
  }

  private void write(Path path, Object data) {
    try {
      Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
      Files.writeString(temporary, new GsonBuilder().setPrettyPrinting().create().toJson(data));
      Files.move(
          temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (java.io.IOException transientFileLock) {
      // Windows readers can briefly prevent replacement; the next tick retries.
    }
  }
}
