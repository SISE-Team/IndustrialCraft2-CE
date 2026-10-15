package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.item.tool.ToolItems;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.ItemDisplayContext;

/** Resolves actual baked item geometry, including native conditional models. */
public final class ItemModelStateProbe {
  public static Map<String, Object> run(Minecraft mc) {
    var failures = new ArrayList<String>();
    var rows = new ArrayList<Map<String, Object>>();
    int checks = 0;
    for (var id : BuiltInRegistries.ITEM.keySet())
      if (id.getNamespace().equals("ic2")) {
        checks++;
        try {
          var sprites = sprites(mc, new ItemStack(BuiltInRegistries.ITEM.getValue(id)));
          if (sprites.stream().anyMatch(s -> s.contains("missingno")) || sprites.isEmpty())
            failures.add(id + " missing baked sprite: " + sprites);
          rows.add(Map.of("id", id.toString(), "sprites", sprites));
        } catch (Exception e) {
          failures.add(id + " model: " + e);
        }
      }
    var directionRows = new ArrayList<Map<String, Object>>();
    for (String id :
        List.of(
            "ejector_upgrade",
            "pulling_upgrade",
            "fluid_ejector_upgrade",
            "fluid_pulling_upgrade",
            "advanced_ejector_upgrade",
            "advanced_pulling_upgrade"))
      for (var dir : Direction.values()) {
        checks++;
        var stack =
            new ItemStack(
                BuiltInRegistries.ITEM.getValue(
                    ProjectIds.id(id)));
        StackTag.update(
            stack,
            n -> {
              n.putByte("dir", (byte) (dir.ordinal() + 1));
              return n;
            });
        var sprites = sprites(mc, stack);
        boolean ok = sprites.stream().anyMatch(s -> s.endsWith("_" + dir.getSerializedName()));
        directionRows.add(
            Map.of(
                "id", id, "direction", dir.getSerializedName(), "sprites", sprites, "passed", ok));
        if (!ok) failures.add(id + " missing directional overlay " + dir + ": " + sprites);
      }
    for (var side : Direction.values()) {
      checks++;
      if (ic2ce.neoforge.item.tool.SurfaceImprinter.getRenderInfo(
              net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), side)
          == null) failures.add("obscurator cannot sample solid face " + side);
    }
    for (var fluid : BuiltInRegistries.FLUID)
      if (BuiltInRegistries.FLUID.getKey(fluid).getNamespace().equals("ic2")) {
        checks++;
        var m = mc.getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        if (m.stillMaterial().sprite().contents().name().toString().contains("missingno")
            || m.flowingMaterial().sprite().contents().name().toString().contains("missingno"))
          failures.add("fluid missing native material: " + fluid);
      }
    var saber = new ItemStack(ToolItems.NANO_SABER);
    ic2ce.api.item.ElectricItem.manager.charge(saber, 100000, Integer.MAX_VALUE, true, false);
    StackTag.update(
        saber,
        n -> {
          n.putBoolean("active", true);
          return n;
        });
    checks++;
    var active = sprites(mc, saber);
    if (active.stream().noneMatch(s -> s.contains("/active_")))
      failures.add("active saber remains visually inactive: " + active);
    StackTag.update(
        saber,
        n -> {
          n.putBoolean("active", false);
          return n;
        });
    checks++;
    var inactive = sprites(mc, saber);
    if (inactive.stream().noneMatch(s -> s.endsWith("/inactive")))
      failures.add("inactive saber model: " + inactive);
    var report = new LinkedHashMap<String, Object>();
    report.put("checks", checks);
    report.put("items", rows);
    report.put("directions", directionRows);
    report.put("activeSaberSprites", active);
    report.put("inactiveSaberSprites", inactive);
    report.put("failures", failures);
    report.put("complete", failures.isEmpty());
    try {
      Files.writeString(
          Path.of("ic2-item-model-states.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(report));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return report;
  }

  public static Set<String> sprites(Minecraft mc, ItemStack stack) {
    try {
      var render = new ItemStackRenderState();
      mc.getItemModelResolver()
          .updateForTopItem(render, stack, ItemDisplayContext.GUI, mc.level, mc.player, 0);
      var lf = ItemStackRenderState.class.getDeclaredField("layers");
      lf.setAccessible(true);
      var layers = (Object[]) lf.get(render);
      var af = ItemStackRenderState.class.getDeclaredField("activeLayerCount");
      af.setAccessible(true);
      int count = af.getInt(render);
      var out = new TreeSet<String>();
      for (int i = 0; i < count; i++) {
        var qf = layers[i].getClass().getDeclaredField("quads");
        qf.setAccessible(true);
        for (var q :
            (List<net.minecraft.client.resources.model.geometry.BakedQuad>) qf.get(layers[i]))
          out.add(q.materialInfo().sprite().contents().name().toString());
      }
      var particle = render.pickParticleMaterial(net.minecraft.util.RandomSource.create(1));
      if (particle != null) out.add(particle.sprite().contents().name().toString());
      return out;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
