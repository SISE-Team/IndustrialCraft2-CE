package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.ClientEnvProxyNeoForge;
import ic2ce.neoforge.item.armor.WearableFluidReservoir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Real-client resources and native item tooltips in a disposable world. */
final class FluidTooltipVerification {
  private static final String[] LOCALES = {"en_us", "zh_cn", "en_us"};
  private static final List<String> failures = new ArrayList<>();
  private static final List<Map<String, Object>> samples = new ArrayList<>();
  private static final Map<String, Component> retained = new LinkedHashMap<>();
  private static CompletableFuture<Void> reload;
  private static int stage, ticks, checks;
  private static boolean finished;

  static void tick(Minecraft minecraft) {
    org.lwjgl.glfw.GLFW.glfwHideWindow(minecraft.getWindow().handle());
    if (finished) return;
    if (++ticks > 1800) {
      failures.add("Timed out while loading real client language resources");
      finish(minecraft);
      return;
    }
    if (minecraft.level == null || minecraft.player == null
        || !Language.getInstance().has("block.ic2.biogas")) return;
    try {
      if (reload == null) {
        minecraft.getLanguageManager().setSelected(LOCALES[stage]);
        minecraft.options.languageCode = LOCALES[stage];
        reload = minecraft.reloadResourcePacks();
        return;
      }
      if (!reload.isDone()) return;
      reload.join();
      verify(LOCALES[stage]);
      reload = null;
      if (++stage == LOCALES.length) finish(minecraft);
    } catch (Throwable error) {
      failures.add(error.toString());
      error.printStackTrace();
      finish(minecraft);
    }
  }

  private static void check(boolean passed, String description) {
    checks++;
    if (!passed) failures.add(description);
  }

  private static void verify(String locale) {
    String empty = locale.equals("zh_cn") ? "空" : "Empty";
    String biogas = locale.equals("zh_cn") ? "沼气" : "Biogas";
    var client = new ClientEnvProxyNeoForge();
    check(Language.getInstance().getOrDefault("block.ic2.biogas").equals(biogas),
        locale + ": client did not load the selected native language");
    for (var item : BuiltInRegistries.ITEM) {
      if (!(item instanceof WearableFluidReservoir reservoir)) continue;
      String id = BuiltInRegistries.ITEM.getKey(item).toString();
      var filled = new ItemStack(item);
      reservoir.filltank(filled);
      var content = Ic2FluidStack.get(filled);
      String name = content.getFluid().getFluidType().getDescription().getString();
      if (id.equals("ic2:jetpack")) check(name.equals(biogas), locale + ": jetpack fuel is not localized");
      for (int amount : new int[] {0, 1, reservoir.getCapacityMb(filled) / 2, reservoir.getCapacityMb(filled)}) {
        var stack = new ItemStack(item);
        if (amount > 0) reservoir.fillMb(stack, content.copyWithAmountMb(amount), false, null);
        String expected = amount == 0 ? empty : "< " + name + ", " + amount + " mB >";
        var minecraft = Minecraft.getInstance();
        List<Component> tooltip = stack.getTooltipLines(
            Item.TooltipContext.of(minecraft.level, minecraft.player), minecraft.player, TooltipFlag.NORMAL);
        check(tooltip.stream().map(Component::getString).anyMatch(expected::equals),
            locale + ": actual item tooltip mismatch for " + id + " at " + amount);
        check(reservoir.getHudInfo(stack, false).equals(List.of(expected))
                && reservoir.getHudInfo(stack, true).equals(List.of(expected)),
            locale + ": HUD mismatch for " + id + " at " + amount);
        check(tooltip.stream().map(Component::getString).noneMatch(text -> text.contains("< ic2:")),
            locale + ": registry identifier leaked into wearable display");
        check(Ic2FluidStack.get(stack).getAmountMb() == amount,
            locale + ": display changed stored fluid amount");
        samples.add(Map.of("locale", locale, "item", id, "amountMb", amount,
            "tooltip", tooltip.stream().map(Component::getString).toList(),
            "hud", reservoir.getHudInfo(stack, false)));
        if (amount == reservoir.getCapacityMb(filled)) {
          var description = tooltip.stream().filter(text -> text.getString().equals(expected)).findFirst().orElseThrow();
          var previous = retained.put(id, description);
          if (previous != null) check(previous.getString().equals(expected),
              locale + ": previously created tooltip did not follow language reload for " + id);
        }
      }
    }
    int fluids = 0;
    for (var fluid : BuiltInRegistries.FLUID) {
      if (!BuiltInRegistries.FLUID.getKey(fluid).getNamespace().equals("ic2")) continue;
      String key = fluid.getFluidType().getDescriptionId();
      String actual = client.getFluidName(Ic2FluidStack.create(fluid, 1000));
      check(Language.getInstance().has(key) && actual.equals(Language.getInstance().getOrDefault(key)),
          locale + ": machine fluid label missing for " + BuiltInRegistries.FLUID.getKey(fluid));
      fluids++;
    }
    check(fluids > 0, "No registered IC2 fluids were checked");
  }

  private static void finish(Minecraft minecraft) {
    finished = true;
    try {
      Files.writeString(Path.of("fluid-tooltip-client.json"), new GsonBuilder().setPrettyPrinting().create().toJson(
          Map.of("complete", failures.isEmpty(), "checks", checks, "failures", failures,
              "locales", List.of(LOCALES), "samples", samples, "disposableWorldOpened", true)));
    } catch (java.io.IOException error) {
      throw new RuntimeException(error);
    }
    minecraft.stop();
  }
}
