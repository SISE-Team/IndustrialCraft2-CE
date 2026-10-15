package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.core.util.*;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.kinetic.WindTurbine;
import ic2ce.neoforge.processing.fluid.LegacyCellCanner;
import ic2ce.neoforge.processing.solid.MaterialRecycler;
import ic2ce.neoforge.processing.solid.PlateCutter;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.neoforge.terrain.TerrainBlueprint;
import ic2ce.neoforge.terrain.TerrainController;
import ic2ce.neoforge.world.BiomeUtil;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Actual machine ticks for remaining equipment and materials; direct terrain API checks are
 * labelled separately.
 */
public final class AdditionalItemUsage {
  public static volatile boolean finished;
  private final List<Map<String, Object>> cases = new ArrayList<>();
  private final List<String> failures = new ArrayList<>();
  private int checks, ticks;
  private final Map<String, NativeBlockEntity> tiles = new LinkedHashMap<>();
  private final Map<String, BlockPos> terrain = new LinkedHashMap<>();
  private final List<Item> materials = new ArrayList<>();
  private final List<MaterialRecycler> recyclers = new ArrayList<>();
  private final int[] indices = new int[8], ages = new int[8];
  private ServerPlayer player;
  private AllMachinesVerification s;
  private boolean groupDone;

  public List<String> failureList() {
    return List.copyOf(failures);
  }

  private void check(boolean ok, String text) {
    checks++;
    if (!ok) failures.add(text);
  }

  private void row(String id, String operation, boolean ok, String evidence) {
    check(ok, id + " " + operation + " " + evidence);
    cases.add(
        Map.of("id", "ic2:" + id, "operation", operation, "passed", ok, "evidence", evidence));
    if (ok && AllItemsVerification.ACTIVE != null)
      AllItemsVerification.ACTIVE.mark(
          BuiltInRegistries.ITEM.getValue(ic2ce.neoforge.runtime.ProjectRuntime.getIdentifier(id)),
          operation,
          evidence);
  }

  private void save() {
    try {
      Files.writeString(
          Path.of("ic2-additional-items-verification.json"),
          new GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  Map.of(
                      "checks",
                      checks,
                      "cases",
                      cases,
                      "failures",
                      failures,
                      "ticks",
                      ticks,
                      "finished",
                      finished,
                      "complete",
                      finished && failures.isEmpty())));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent e) {
    if (player != null || !(e.getEntity() instanceof ServerPlayer p)) return;
    player = p;
    s = AllMachinesVerification.ACTIVE;
    for (var id : BuiltInRegistries.ITEM.keySet())
      if (id.getNamespace().equals("ic2")
          && BuiltInRegistries.ITEM.getValue(id).getClass() == Item.class)
        materials.add(BuiltInRegistries.ITEM.getValue(id));
    materials.sort(Comparator.comparing(i -> BuiltInRegistries.ITEM.getKey(i).toString()));
    for (int i = 0; i < 8; i++) {
      var pos = new BlockPos(512 + i * 16, 181, 1200);
      var m = (MaterialRecycler) s.helper("recycler", pos);
      s.level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
      s.helper("creative_generator", pos.west());
      m.upgradeSlot.put(0, AllMachinesVerification.stack("overclocker_upgrade", 2));
      m.setChanged();
      recyclers.add(m);
      indices[i] = i;
      feed(i);
    }
    for (String id :
        List.of("wooden_rotor", "bronze_rotor", "iron_rotor", "steel_rotor", "carbon_rotor")) {
      var pos = new BlockPos(512 + tiles.size() * 64, 224, 1008);
      s.level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
      var wind = (WindTurbine) s.helper("wind_kinetic_generator", pos);
      wind.rotorSlot.put(AllMachinesVerification.stack(id, 1));
      wind.setChanged();
      tiles.put(id, wind);
    }
    try {
      var field = ic2ce.neoforge.weather.WorldWind.class.getDeclaredField("windStrength");
      field.setAccessible(true);
      field.setInt(ic2ce.neoforge.world.WorldData.get(s.level).windSim, 20);
    } catch (ReflectiveOperationException error) {
      throw new RuntimeException(error);
    }
    int n = 0;
    for (String id :
        List.of(
            "chilling_tfbp",
            "cultivation_tfbp",
            "desertification_tfbp",
            "flatification_tfbp",
            "irrigation_tfbp",
            "mushroom_tfbp")) {
      var pos = new BlockPos(512 + (n++) * 96, 181, 752);
      terrain.put(id, pos);
      for (int x = -24; x <= 24; x++)
        for (int z = -24; z <= 24; z++) {
          var q = pos.offset(x, -1, z);
          for (int y = 0; y < q.getY(); y++)
            s.level.setBlock(
                new BlockPos(q.getX(), y, q.getZ()), Blocks.BEDROCK.defaultBlockState(), 2);
          for (int y = 1; y <= 25; y++)
            s.level.setBlock(q.above(y), Blocks.AIR.defaultBlockState(), 2);
          s.level.setBlock(
              q,
              id.equals("chilling_tfbp")
                  ? Blocks.WATER.defaultBlockState()
                  : id.equals("cultivation_tfbp") || id.equals("irrigation_tfbp")
                      ? Blocks.SAND.defaultBlockState()
                      : id.equals("mushroom_tfbp")
                          ? Blocks.GRASS_BLOCK.defaultBlockState()
                          : Blocks.DIRT.defaultBlockState(),
              2);
          s.level.setBlock(q.above(), Blocks.AIR.defaultBlockState(), 2);
          if (id.equals("flatification_tfbp"))
            s.level.setBlock(q.above(2), Blocks.DIRT.defaultBlockState(), 2);
        }
      s.level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
      var terra = (TerrainController) s.helper("terraformer", pos);
      s.helper("creative_generator", pos.above());
      terra.tfbpSlot.put(AllMachinesVerification.stack(id, 1));
      terra.setChanged();
      tiles.put(id, terra);
    }
    int bladeIndex = 0;
    for (String id :
        List.of("iron_cutting_blade", "steel_cutting_blade", "diamond_cutting_blade")) {
      var pos = new BlockPos(512 + (bladeIndex++) * 16, 181, 1296);
      s.level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
      var cutter = (PlateCutter) s.helper("block_cutter", pos);
      s.helper("creative_generator", pos.west());
      cutter.cutterSlot.put(AllMachinesVerification.stack(id, 1));
      cutter.inputSlot.put(new ItemStack(Items.OAK_LOG));
      cutter.setChanged();
      tiles.put(id, cutter);
    }
    int fuelIndex = 0;
    for (String id : List.of("coalfuel_cell", "biofuel_cell")) {
      var pos = new BlockPos(512 + (fuelIndex++) * 16, 181, 1360);
      s.level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
      var canner = (LegacyCellCanner) s.helper("classic_canner", pos);
      s.helper("creative_generator", pos.west());
      canner.inputSlot.put(AllMachinesVerification.stack("empty_fuel_can", 1));
      canner.resInputSlot.put(AllMachinesVerification.stack(id, 6));
      canner.setChanged();
      tiles.put(id, canner);
    }
    s.level.setChunkForced(560 >> 4, 1360 >> 4, true);
    var gen =
        (ic2ce.neoforge.power.CombustionGenerator)
            s.helper("generator", new BlockPos(560, 181, 1360));
    var fuel = AllMachinesVerification.stack("filled_fuel_can", 1);
    StackTag.update(
        fuel,
        data -> {
          data.putInt("value", 600);
          return data;
        });
    ((InventorySlot) AllMachinesVerification.get(gen, "fuelSlot")).put(fuel);
    s.helper("mfsu", gen.getBlockPos().east());
    tiles.put("filled_fuel_can", gen);
    terrainApi();
    save();
  }

  private void terrainApi() {
    var q = new BlockPos(1020, 181, -1020);
    for (var id : List.of("cultivation_tfbp", "desertification_tfbp", "mushroom_tfbp")) {
      for (int y = 0; y < q.getY(); y++)
        s.level.setBlock(
            new BlockPos(q.getX(), y, q.getZ()), Blocks.BEDROCK.defaultBlockState(), 2);
      for (int y = 1; y <= 25; y++) s.level.setBlock(q.above(y), Blocks.AIR.defaultBlockState(), 2);
      s.level.setBlock(
          q,
          id.equals("cultivation_tfbp")
              ? Blocks.DIRT.defaultBlockState()
              : Blocks.GRASS_BLOCK.defaultBlockState(),
          2);
      s.level.setBlock(q.above(), Blocks.AIR.defaultBlockState(), 2);
      var item =
          (TerrainBlueprint) BuiltInRegistries.ITEM.getValue(ProjectRuntime.getIdentifier(id));
      boolean applied = item.terraform(new ItemStack(item), s.level, q);
      var state = s.level.getBlockState(q);
      boolean ok =
          applied
              && (id.equals("cultivation_tfbp")
                  ? state.is(Blocks.GRASS_BLOCK)
                  : id.equals("desertification_tfbp") ? state.is(Blocks.SAND) : true);
      row(id, "direct_terrain_api", ok, "ground=" + state);
    }
    var biome =
        s.level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.MUSHROOM_FIELDS);
    for (int x : List.of(-1020, -1, 0, 15, 16, 1020))
      for (int z : List.of(-1017, 1017)) {
        var p = new BlockPos(x, 181, z);
        var chunk = s.level.getChunkAt(p);
        var neighbor = chunk.getNoiseBiome(((x & 15) >> 2) ^ 1, 0, (z & 15) >> 2);
        BiomeUtil.setBiomeAndNotify(s.level, p, biome);
        for (int y = s.level.getMinY(); y < s.level.getMaxY(); y += 4)
          check(
              chunk.getNoiseBiome(x >> 2, y >> 2, z >> 2).is(Biomes.MUSHROOM_FIELDS),
              "biome column at " + x + "," + y + "," + z);
        check(
            chunk.getNoiseBiome(((x & 15) >> 2) ^ 1, 0, (z & 15) >> 2).equals(neighbor),
            "biome leaves neighboring quart column unchanged " + p);
      }
  }

  private void feed(int i) {
    var m = recyclers.get(i);
    m.inputSlot.clear();
    m.outputSlot.clear();
    ages[i] = 0;
    if (indices[i] < materials.size()) {
      m.inputSlot.put(new ItemStack(materials.get(indices[i])));
      m.setChanged();
    }
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post e) {
    if (player == null || finished) return;
    ticks++;
    try {
      for (int i = 0; i < 8; i++)
        if (indices[i] < materials.size()) {
          var m = recyclers.get(i);
          ages[i]++;
          if (!m.inputSlot.isEmpty() && ages[i] < 2000) continue;
          var item = materials.get(indices[i]);
          boolean consumed = m.inputSlot.isEmpty();
          boolean valid = true;
          for (var out : m.outputSlot) valid &= out.isEmpty() || out.is(ItemDefinitions.SCRAP);
          row(
              BuiltInRegistries.ITEM.getKey(item).getPath(),
              "placed_recycler_material_input",
              consumed && valid,
              "input consumed=" + consumed + ", output=" + m.outputSlot);
          indices[i] += 8;
          feed(i);
        }
      if (ticks == 1800) {
        groupDone = true;
        for (var entry : tiles.entrySet()) {
          String id = entry.getKey();
          var te = entry.getValue();
          if (te instanceof WindTurbine wind) {
            int ku = wind.drawKineticEnergy(wind.getFacing().getOpposite(), 1, true);
            row(
                id,
                "placed_wind_rotor",
                ku > 0 && wind.rotorSlot.get().getDamageValue() > 0,
                "KU=" + ku + ", wear=" + wind.rotorSlot.get().getDamageValue());
          } else if (te instanceof TerrainController terra) {
            var p = terrain.get(id);
            int changed = 0;
            for (int x = -24; x <= 24; x++)
              for (int z = -24; z <= 24; z++) {
                var state = s.level.getBlockState(p.offset(x, -1, z));
                boolean match =
                    switch (id) {
                      case "chilling_tfbp" -> state.is(Blocks.ICE);
                      case "cultivation_tfbp", "irrigation_tfbp" ->
                          state.is(Blocks.DIRT)
                              || state.is(Blocks.GRASS_BLOCK)
                              || state.is(Blocks.FARMLAND);
                      case "desertification_tfbp" -> state.is(Blocks.SAND);
                      case "mushroom_tfbp" ->
                          state.is(Blocks.MYCELIUM)
                              || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                              || state.is(Blocks.RED_MUSHROOM_BLOCK);
                      default -> s.level.getBlockState(p.offset(x, 1, z)).isAir();
                    };
                if (match) changed++;
              }
            row(
                id,
                "placed_powered_terraformer",
                changed > 0 && terra.getActive(),
                "changed terrain cells="
                    + changed
                    + ", energy="
                    + terra.getComponent(ElectricalBuffer.class).getEnergy());
            terra.tfbpSlot.clear();
            terra.setChanged();
          } else if (te instanceof PlateCutter cutter) {
            row(
                id,
                "placed_block_cutter",
                cutter.inputSlot.isEmpty() && !cutter.outputSlot.isEmpty(),
                "output=" + cutter.outputSlot);
          } else if (te instanceof LegacyCellCanner canner) {
            var out = canner.outputSlot.get();
            int expected = id.equals("coalfuel_cell") ? 2548 * 6 : 868 * 6;
            var data = StackTag.get(out);
            row(
                id,
                "placed_classic_canner_fuel",
                canner.inputSlot.isEmpty()
                    && canner.resInputSlot.isEmpty()
                    && out.is(ItemDefinitions.FILLED_FUEL_CAN)
                    && data != null
                    && data.getIntOr("value", 0) == expected,
                "output=" + out + ", grade=" + data);
            row(
                "empty_fuel_can",
                "placed_classic_canner_container",
                out.is(ItemDefinitions.FILLED_FUEL_CAN),
                "filled with " + id);
          } else if (id.equals("filled_fuel_can")) {
            var fuel = (InventorySlot) AllMachinesVerification.get(te, "fuelSlot");
            var sink = s.level.getBlockEntity(te.getBlockPos().east());
            double eu =
                sink instanceof NativeBlockEntity t
                    ? t.getComponent(ElectricalBuffer.class).getEnergy()
                    : 0;
            row(
                id,
                "placed_fuel_generator",
                eu > 0 && !fuel.get().is(ItemDefinitions.FILLED_FUEL_CAN),
                "receiver EU=" + eu + ", remainder=" + fuel.get());
          }
        }
      }
      if (groupDone
          && java.util.stream.IntStream.range(0, 8).allMatch(i -> indices[i] >= materials.size())) {
        finished = true;
        save();
      } else if (ticks % 100 == 0) save();
    } catch (Throwable error) {
      error.printStackTrace();
      check(false, "suite exception: " + error);
      finished = true;
      save();
    }
  }
}
