package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.api.recipe.*;
import ic2ce.core.util.Config;
import ic2ce.neoforge.recipe.codec.DeferredInput;
import ic2ce.neoforge.recipe.input.ExactStackInput;
import ic2ce.neoforge.recipe.input.NativeIngredientInput;
import ic2ce.neoforge.recipe.processing.BasicMachineRecipes;
import ic2ce.neoforge.recipe.processing.ListedMachineInputs;
import ic2ce.neoforge.recipe.processing.OrderedMachineRecipes;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Baseline-compatible native regressions for recipe candidates, config reads and reloads. */
public final class HotpathRegression {
  private final List<String> failures = new ArrayList<>();
  private final Map<String, Integer> groups = new LinkedHashMap<>();
  private MinecraftServer server;
  private CompletableFuture<Void> reload;
  private int stage, ticks, checks;
  private boolean finished;
  private ProbeManager tagged;
  private IRecipeInput liveMaterial;
  private ProbeManager liveMaterialManager;
  private Object previousManager;
  private Path tagFile;
  private Path biomeTagFile;
  private Holder<net.minecraft.world.level.biome.Biome> cachedBiome;

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add(label);
  }

  private void group(String name, Runnable action) {
    int before = checks;
    try {
      action.run();
    } catch (Throwable error) {
      failures.add(name + ": " + error);
      error.printStackTrace();
    }
    groups.put(name, checks - before);
  }

  @SubscribeEvent
  public void started(ServerStartedEvent event) {
    server = event.getServer();
    liveMaterial =
        new ic2ce.neoforge.recipe.input.InputFactory().forTag("ic2:performance_probe", 1);
    liveMaterial.getInputs();
    liveMaterialManager = new ProbeManager();
    liveMaterialManager.add(liveMaterial, "material");
    group("recipeCandidates", this::recipes);
    group("recipeReplacement", this::replacement);
    group("configPaths", this::config);
    group(
        "fuelCanContracts",
        () -> {
          var result = ic2ce.neoforge.item.consumable.FuelCanContracts.run();
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "definitionContracts",
        () -> {
          var result = ic2ce.neoforge.registry.DefinitionContracts.run();
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "managedMenuContracts",
        () -> {
          var result = ic2ce.neoforge.menu.ManagedMenuContracts.run(server.overworld());
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "creativeContracts",
        () -> {
          var result = ic2ce.neoforge.registry.CreativeContracts.run();
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "materialLookupContracts",
        () -> {
          var result = ic2ce.neoforge.cable.MaterialLookupContracts.run();
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "publicProtocolContracts",
        () -> {
          var result = ic2ce.api.energy.PublicProtocolContracts.run();
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "remainderContracts",
        () -> {
          var result = ic2ce.neoforge.recipe.crafting.RemainderContracts.run();
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "platformContracts",
        () -> {
          var result = ic2ce.neoforge.PlatformContracts.run(server.overworld());
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "stackValueContracts",
        () -> {
          var result = ic2ce.core.util.StackValueContracts.run(server.overworld());
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "bootstrapContracts",
        () -> {
          var result = ic2ce.neoforge.runtime.BootstrapContracts.run(server.overworld());
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "weatherContracts",
        () -> {
          var result = ic2ce.neoforge.weather.WeatherContracts.run(server.overworld());
          checks += result.checks();
          failures.addAll(result.failures());
        });
    group(
        "configurationContracts",
        () -> {
          var result = ic2ce.neoforge.diagnostics.ConfigurationContracts.run();
          checks += result.checks();
          failures.addAll(result.failures());
        });
    EnergyEffectsRegression.prepare(server.overworld());
    var pipeChunk = new net.minecraft.core.BlockPos(8520, 100, 8520);
    server.overworld().setChunkForced(pipeChunk.getX() >> 4, pipeChunk.getZ() >> 4, true);
    server.overworld().getChunkAt(pipeChunk);
    var utilityChunk = new net.minecraft.core.BlockPos(8560, 100, 8560);
    server.overworld().setChunkForced(utilityChunk.getX() >> 4, utilityChunk.getZ() >> 4, true);
    server.overworld().getChunkAt(utilityChunk);
    var terrainChunk = new net.minecraft.core.BlockPos(8640, 100, 8640);
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++)
        server
            .overworld()
            .setChunkForced((terrainChunk.getX() >> 4) + x, (terrainChunk.getZ() >> 4) + z, true);
    server.overworld().getChunkAt(terrainChunk);
    var cropChunk = new net.minecraft.core.BlockPos(9120, 100, 9120);
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++) {
        var preparedCrop = cropChunk.offset(x * 16, 0, z * 16);
        server.overworld().setChunkForced(preparedCrop.getX() >> 4, preparedCrop.getZ() >> 4, true);
        server.overworld().getChunkAt(preparedCrop);
      }
    var speciesChunk = new net.minecraft.core.BlockPos(9168, 100, 9168);
    server.overworld().setChunkForced(speciesChunk.getX() >> 4, speciesChunk.getZ() >> 4, true);
    server.overworld().getChunkAt(speciesChunk);
    var wrenchChunk = new net.minecraft.core.BlockPos(9328, 100, 9328);
    server.overworld().setChunkForced(wrenchChunk.getX() >> 4, wrenchChunk.getZ() >> 4, true);
    server.overworld().getChunkAt(wrenchChunk);
    var resinChunk = new net.minecraft.core.BlockPos(9360, 100, 9360);
    server.overworld().setChunkForced(resinChunk.getX() >> 4, resinChunk.getZ() >> 4, true);
    server.overworld().getChunkAt(resinChunk);
    var poweredToolChunk = new net.minecraft.core.BlockPos(9408, 100, 9408);
    server
        .overworld()
        .setChunkForced(poweredToolChunk.getX() >> 4, poweredToolChunk.getZ() >> 4, true);
    server.overworld().getChunkAt(poweredToolChunk);
    var paintChunk = new net.minecraft.core.BlockPos(9248, 100, 9248);
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++) {
        var preparedPaint = paintChunk.offset(x * 16, 0, z * 16);
        server
            .overworld()
            .setChunkForced(preparedPaint.getX() >> 4, preparedPaint.getZ() >> 4, true);
        server.overworld().getChunkAt(preparedPaint);
      }
    try {
      previousManager = Recipes.macerator.get(server.overworld());
      Path pack = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve("ic2-hotpath-probe");
      Files.createDirectories(pack);
      Files.writeString(
          pack.resolve("pack.mcmeta"),
          "{\"pack\":{\"description\":\"IC2 hotpath reload"
              + " regression\",\"min_format\":[101,1],\"max_format\":[101,1]}}");
      tagFile = pack.resolve("data/ic2/tags/item/performance_probe.json");
      Files.createDirectories(tagFile.getParent());
      Files.writeString(tagFile, "{\"replace\":true,\"values\":[\"minecraft:raw_iron\"]}");
      biomeTagFile = pack.resolve("data/c/tags/worldgen/biome/is_swamp.json");
      Files.createDirectories(biomeTagFile.getParent());
      Files.writeString(biomeTagFile, "{\"replace\":true,\"values\":[\"minecraft:plains\"]}");
      server.getPackRepository().reload();
      var selected = new ArrayList<>(server.getPackRepository().getSelectedIds());
      selected.add("file/ic2-hotpath-probe");
      reload = server.reloadResources(selected);
    } catch (Throwable error) {
      failures.add("start native reload: " + error);
      finish();
    }
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (server == null || finished) return;
    if (++ticks > 1200) {
      failures.add("native reload timeout");
      finish();
      return;
    }
    if (reload == null || !reload.isDone()) return;
    try {
      reload.join();
      if (stage == 0) {
        group(
            "actionContracts",
            () -> {
              try {
                var result = ic2ce.core.network.ActionContracts.run(server.overworld());
                checks += result.checks();
                failures.addAll(result.failures());
              } catch (Exception error) {
                throw new RuntimeException(error);
              }
            });
        group(
            "wireContracts",
            () -> {
              try {
                var result = ic2ce.core.network.WireContracts.run();
                checks += result.checks();
                failures.addAll(result.failures());
              } catch (Exception error) {
                throw new RuntimeException(error);
              }
            });
        group(
            "reactorPartContracts",
            () -> {
              var result = ic2ce.neoforge.reactor.part.ReactorPartContracts.run();
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "reactorCoreContracts",
            () -> {
              var result = ic2ce.neoforge.reactor.ReactorContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "registryContracts",
            () -> {
              var result = ic2ce.core.block.comp.RegistryContracts.run();
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "worldLifecycleContracts",
            () -> {
              var result = ic2ce.core.event.WorldLifecycleContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "containerContracts",
            () -> {
              var result = ic2ce.neoforge.inventory.ContainerContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "portableStorageContracts",
            () -> {
              var result =
                  ic2ce.core.block.storage.PortableStorageContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "rotorContracts",
            () -> {
              var result = ic2ce.neoforge.kinetic.RotorContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "kineticContracts",
            () -> {
              var result = ic2ce.neoforge.kinetic.KineticContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "heatContracts",
            () -> {
              var result = ic2ce.neoforge.thermal.HeatContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "generatorContracts",
            () -> {
              var result = ic2ce.neoforge.power.GeneratorContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "storageBankContracts",
            () -> {
              var result =
                  ic2ce.neoforge.energy.storage.StorageBankContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "transformerContracts",
            () -> {
              var result =
                  ic2ce.neoforge.energy.transformer.TransformerContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "cableContracts",
            () -> {
              var result = ic2ce.neoforge.cable.CableContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "pumpContracts",
            () -> {
              var result = ic2ce.neoforge.processing.fluid.PumpContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "transportContracts",
            () -> {
              var result = ic2ce.neoforge.transport.TransportContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "miningContracts",
            () -> {
              var mining = ic2ce.neoforge.mining.MiningContracts.run(server.overworld());
              checks += mining.checks();
              failures.addAll(mining.failures());
            });
        group(
            "pipeMiningContracts",
            () -> {
              var result = ic2ce.neoforge.mining.PipeMiningContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "utilityContracts",
            () -> {
              var utility = ic2ce.neoforge.utility.UtilityContracts.run(server.overworld());
              checks += utility.checks();
              failures.addAll(utility.failures());
            });
        group(
            "chunkLeaseContracts",
            () -> {
              var result = ic2ce.neoforge.utility.ChunkLeaseContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "terrainTeleportContracts",
            () -> {
              var result = ic2ce.neoforge.terrain.TerrainTeleportContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "sortingContracts",
            () -> {
              var result = ic2ce.neoforge.transport.SortingContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "personalContracts",
            () -> {
              var result = ic2ce.neoforge.personal.PersonalContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "energySalesContracts",
            () -> {
              var result = ic2ce.neoforge.personal.EnergySalesContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "itemSalesContracts",
            () -> {
              var result = ic2ce.neoforge.personal.ItemSalesContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "itemTransferContracts",
            () -> {
              var result = ic2ce.neoforge.ItemTransferContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "automaticCraftingContracts",
            () -> {
              var result =
                  ic2ce.neoforge.crafting.AutomaticCraftingContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "workbenchContracts",
            () -> {
              var result = ic2ce.neoforge.crafting.WorkbenchContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "agricultureContracts",
            () -> {
              var result = ic2ce.neoforge.agriculture.AgricultureContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "cropControllerContracts",
            () -> {
              var result =
                  ic2ce.neoforge.agriculture.CropControllerContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "cropCatalogContracts",
            () -> {
              var result = ic2ce.neoforge.agriculture.CropCatalogContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "cropSpeciesContracts",
            () -> {
              var result = ic2ce.neoforge.agriculture.CropSpeciesContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "paintContracts",
            () -> {
              var result = ic2ce.neoforge.item.tool.PaintContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "wrenchContracts",
            () -> {
              var result = ic2ce.neoforge.item.tool.WrenchContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "resinContracts",
            () -> {
              var result = ic2ce.neoforge.item.tool.ResinContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "poweredToolContracts",
            () -> {
              var result = ic2ce.neoforge.item.tool.PoweredToolContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "chargeContracts",
            () -> {
              var result = ic2ce.neoforge.item.tool.ChargeContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "batteryContracts",
            () -> {
              var result = ic2ce.neoforge.item.energy.BatteryContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "armorDamageContracts",
            () -> {
              var result = ic2ce.neoforge.item.armor.ArmorDamageContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "armorContracts",
            () -> {
              var result = ic2ce.neoforge.item.armor.ArmorContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "upgradeContracts",
            () -> {
              var result = ic2ce.neoforge.item.upgrade.UpgradeContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "handheldContracts",
            () -> {
              var result = ic2ce.neoforge.item.upgrade.HandheldContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "upgradeMenuContracts",
            () -> {
              var result = ic2ce.neoforge.item.upgrade.UpgradeMenuContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "observationContracts",
            () -> {
              var result = ic2ce.neoforge.item.tool.ObservationContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "laserContracts",
            () -> {
              var result = ic2ce.neoforge.item.tool.LaserContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "obscuratorContracts",
            () -> {
              var result = ic2ce.neoforge.item.tool.ObscuratorContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "consumableContracts",
            () -> {
              var result =
                  ic2ce.neoforge.item.consumable.ConsumableContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "energyBufferContracts",
            () -> {
              var result = ic2ce.api.energy.prefab.EnergyBufferContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "worldGenerationContracts",
            () -> {
              var result =
                  ic2ce.neoforge.world.generation.WorldGenerationContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "scaffoldContracts",
            () -> {
              var result = ic2ce.neoforge.world.block.ScaffoldContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "guiDocumentContracts",
            () -> {
              var result = ic2ce.neoforge.menu.document.GuiDocumentContracts.run();
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "textContracts",
            () -> {
              var result = ic2ce.neoforge.menu.document.TextContracts.run();
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "signalContracts",
            () -> {
              var result = ic2ce.neoforge.component.SignalContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "processingComponentContracts",
            () -> {
              var result =
                  ic2ce.neoforge.component.ProcessingComponentContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "surfaceContracts",
            () -> {
              var result = ic2ce.neoforge.world.block.SurfaceContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "blastContracts",
            () -> {
              var result = ic2ce.neoforge.explosion.BlastContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "explosiveBlockContracts",
            () -> {
              var result = ic2ce.neoforge.explosion.ExplosiveBlockContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "canningRegistryContracts",
            () -> {
              var result = ic2ce.neoforge.recipe.processing.CanningRegistryContracts.run();
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "fluidPackagingContracts",
            () -> {
              var result =
                  ic2ce.neoforge.processing.FluidPackagingContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "legacyCanningContracts",
            () -> {
              var result = ic2ce.neoforge.processing.LegacyCanningContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "craftingMenuContracts",
            () -> {
              var result = ic2ce.neoforge.crafting.CraftingMenuContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "baseMenuContracts",
            () -> {
              var result = ic2ce.neoforge.menu.MenuContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "constructionContracts",
            () -> {
              var result = ic2ce.neoforge.world.block.ConstructionContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "recipeFamilyContracts",
            () -> {
              var result =
                  ic2ce.neoforge.recipe.codec.RecipeFamilyContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "scrapboxContracts",
            () -> {
              var result = ic2ce.neoforge.recipe.processing.ScrapboxContracts.run();
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "recipeInputContracts",
            () -> {
              var result = ic2ce.neoforge.recipe.input.RecipeInputContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "worldCellContracts",
            () -> {
              var result = ic2ce.neoforge.item.WorldCellContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "materialContracts",
            () -> {
              var result = ic2ce.core.uu.MaterialContracts.run();
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "uuContracts",
            () -> {
              var result = ic2ce.neoforge.processing.UuContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "canningContracts",
            () -> {
              var result = ic2ce.neoforge.processing.CanningContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "fluidCapabilityContracts",
            () -> {
              var result = ic2ce.neoforge.FluidCapabilityContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "coverContracts",
            () -> {
              var result = ic2ce.core.block.transport.cover.CoverContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "fluidServiceContracts",
            () -> {
              var result = ic2ce.core.fluid.FluidServiceContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "fluidContracts",
            () -> {
              var result = ic2ce.core.fluid.FluidContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "fluidMachineContracts",
            () -> {
              var result =
                  ic2ce.neoforge.processing.fluid.FluidMachineContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "electrolysisContracts",
            () -> {
              var result = ic2ce.neoforge.processing.ElectrolysisContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "fabricationContracts",
            () -> {
              var result = ic2ce.neoforge.processing.FabricationContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "inventoryContracts",
            () -> {
              var result = ic2ce.neoforge.inventory.InventoryContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "publicApiContracts",
            () -> {
              var result = ic2ce.neoforge.NativeApiContracts.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "energyLifecycle",
            () -> {
              var result = ic2ce.core.energy.grid.NetworkLifecycleProbe.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "cableEffects",
            () -> {
              var result = EnergyEffectsRegression.run(server.overworld());
              checks += result.checks();
              failures.addAll(result.failures());
            });
        var ingredient =
            Ingredient.of(
                BuiltInRegistries.ITEM.getOrThrow(
                    TagKey.create(
                        Registries.ITEM,
                        Identifier.fromNamespaceAndPath("ic2", "performance_probe"))));
        tagged = new ProbeManager();
        tagged.add(lazy(ingredient), "tagged");
        check(
            "tagged".equals(tagged.find(new ItemStack(Items.RAW_IRON))),
            "first reload tag accepts iron");
        check(
            tagged.find(new ItemStack(Items.DIAMOND)) == null, "cache initial negative tag lookup");
        check(
            previousManager != Recipes.macerator.get(server.overworld()),
            "native recipe map replaces manager");
        previousManager = Recipes.macerator.get(server.overworld());
        check(
            Recipes.macerator.get(server.overworld()).apply(new ItemStack(Items.RAW_IRON), false)
                != null,
            "native processing recipe after first reload");
        cachedBiome = server.overworld().getBiome(new net.minecraft.core.BlockPos(9120, 100, 9120));
        check(
            ic2ce.neoforge.runtime.ProjectRuntime.envProxy.biomeHasType(
                    cachedBiome, ic2ce.api.crops.BiomeType.SWAMP)
                && ic2ce.api.crops.Crops.instance.getNutrientBiomeBonus(cachedBiome) == 10,
            "first native biome tag reload establishes a warm positive crop bonus");
        check(
            liveMaterial.matches(new ItemStack(Items.RAW_IRON))
                && !liveMaterial.matches(new ItemStack(Items.DIAMOND)),
            "preexisting unresolved material descriptor observes first real tag publication");
        check(
            "material".equals(liveMaterialManager.find(new ItemStack(Items.RAW_IRON))),
            "factory material index warms positive before reload");
        Files.writeString(tagFile, "{\"replace\":true,\"values\":[\"minecraft:diamond\"]}");
        Files.writeString(biomeTagFile, "{\"replace\":true,\"values\":[]}");
        stage = 1;
        reload = server.reloadResources(server.getPackRepository().getSelectedIds());
      } else {
        if (!ic2ce.api.energy.prefab.EnergyBufferContracts.ready()) return;
        group(
            "energyBufferWorldTicks",
            () -> {
              var result = ic2ce.api.energy.prefab.EnergyBufferContracts.afterWorldTicks();
              checks += result.checks();
              failures.addAll(result.failures());
            });
        group(
            "publicApiDeferredTracking",
            () -> {
              var result = ic2ce.neoforge.NativeApiContracts.afterWorldTicks();
              checks += result.checks();
              failures.addAll(result.failures());
            });
        check(
            tagged.find(new ItemStack(Items.RAW_IRON)) == null,
            "tag reload removes cached positive");
        check(
            "tagged".equals(tagged.find(new ItemStack(Items.DIAMOND))),
            "tag reload invalidates cached negative");
        check(
            previousManager != Recipes.macerator.get(server.overworld()),
            "second native recipe map replaces manager");
        check(
            Recipes.macerator.get(server.overworld()).apply(new ItemStack(Items.RAW_IRON), false)
                != null,
            "native processing recipe after second reload");
        check(
            cachedBiome
                    == server.overworld().getBiome(new net.minecraft.core.BlockPos(9120, 100, 9120))
                && !ic2ce.neoforge.runtime.ProjectRuntime.envProxy.biomeHasType(
                    cachedBiome, ic2ce.api.crops.BiomeType.SWAMP)
                && ic2ce.api.crops.Crops.instance.getNutrientBiomeBonus(cachedBiome) == 0,
            "actual tag publication invalidates the warm crop cache despite retaining the same"
                + " native biome holder");
        check(
            !liveMaterial.matches(new ItemStack(Items.RAW_IRON))
                && liveMaterial.matches(new ItemStack(Items.DIAMOND)),
            "live material predicate observes changed actual native tag holders");
        check(
            liveMaterial.getInputs().stream().anyMatch(stack -> stack.is(Items.DIAMOND))
                && liveMaterial.getInputs().stream().noneMatch(stack -> stack.is(Items.RAW_IRON)),
            "native material display samples follow tag reload");
        check(
            liveMaterialManager.find(new ItemStack(Items.RAW_IRON)) == null
                && "material".equals(liveMaterialManager.find(new ItemStack(Items.DIAMOND))),
            "factory material index retains newly added native tag members");
        groups.put("nativeReload", 15);
        finish();
      }
    } catch (Throwable error) {
      failures.add("native reload stage=" + stage + ": " + error);
      finish();
    }
  }

  private static DeferredInput lazy(Ingredient ingredient) {
    var input = new DeferredInput(() -> new NativeIngredientInput(ingredient, 1));
    input.unwrap();
    return input;
  }

  private void recipes() {
    var manager = new ProbeManager();
    check(
        manager.find(null) == null && manager.apply(null, false) == null,
        "null input retains empty-query behavior");
    var list = new ListedMachineInputs();
    list.add(new ExactStackInput(new ItemStack(Items.IRON_INGOT)));
    check(
        list.contains(new ItemStack(Items.IRON_INGOT)),
        "warm list recipe cache before iterator removal");
    var listIterator = list.iterator();
    listIterator.next();
    listIterator.remove();
    check(
        list.isEmpty() && !list.contains(new ItemStack(Items.IRON_INGOT)),
        "list iterator removal invalidates membership cache");
    boolean[] enabled = {true};
    var custom = new PredicateInput(stack -> enabled[0] && stack.is(Items.IRON_INGOT));
    manager.add(custom, "custom-first");
    var vanilla = lazy(Ingredient.of(Items.IRON_INGOT));
    manager.add(vanilla, "vanilla-second");
    for (int i = 0; i < 40; i++) {
      enabled[0] = i % 2 == 0;
      check(
          (enabled[0] ? "custom-first" : "vanilla-second")
              .equals(manager.find(new ItemStack(Items.IRON_INGOT))),
          "preserve fallback order and dynamic predicate " + i);
      check(manager.find(new ItemStack(Items.DIAMOND)) == null, "repeated negative lookup " + i);
    }
    manager.add(lazy(Ingredient.of(Items.DIAMOND)), "new-diamond");
    check(
        "new-diamond".equals(manager.find(new ItemStack(Items.DIAMOND))),
        "addition invalidates negative cache");
    manager.remove(vanilla);
    enabled[0] = false;
    check(
        manager.find(new ItemStack(Items.IRON_INGOT)) == null,
        "removal invalidates cached candidate");
    var iterator = manager.getRecipes().iterator();
    while (iterator.hasNext())
      if (iterator.next().getOutput().equals("new-diamond")) iterator.remove();
    check(
        manager.find(new ItemStack(Items.DIAMOND)) == null,
        "iterator removal invalidates candidate");

    var order = new ProbeManager();
    order.add(lazy(Ingredient.of(Items.IRON_INGOT)), "deferred-first");
    order.add(new PredicateInput(stack -> stack.is(Items.IRON_INGOT)), "custom-second");
    check(
        "deferred-first".equals(order.find(new ItemStack(Items.IRON_INGOT))),
        "deferred precedes later custom input");
    order.add(new ExactStackInput(new ItemStack(Items.IRON_INGOT)), "existing-exact-cache");
    check(
        "existing-exact-cache".equals(order.find(new ItemStack(Items.IRON_INGOT))),
        "retain existing exact-cache precedence");

    var named = new ItemStack(Items.IRON_INGOT);
    named.set(DataComponents.CUSTOM_NAME, Component.literal("named"));
    var components = new ProbeManager();
    var exact =
        new DeferredInput(
            () ->
                new PredicateInput(
                    stack ->
                        stack.is(Items.IRON_INGOT)
                            && Objects.equals(
                                named.get(DataComponents.CUSTOM_NAME),
                                stack.get(DataComponents.CUSTOM_NAME))));
    exact.unwrap();
    components.add(exact, "named");
    check(
        components.find(new ItemStack(Items.IRON_INGOT)) == null,
        "component mismatch remains negative");
    check(
        "named".equals(components.find(named.copy())),
        "same item with matching component stays eligible");
    check(
        components.find(new ItemStack(Items.IRON_INGOT)) == null,
        "component positive does not bypass later matching");

    var customIngredient =
        new Ingredient(
            new ICustomIngredient() {
              public boolean test(ItemStack stack) {
                return enabled[0] && stack.is(Items.GOLD_INGOT);
              }

              public java.util.stream.Stream<Holder<Item>> items() {
                return java.util.stream.Stream.of(Items.GOLD_INGOT.builtInRegistryHolder());
              }

              public boolean isSimple() {
                return false;
              }

              public IngredientType<?> getType() {
                throw new UnsupportedOperationException("verification only");
              }
            });
    var dynamic = new ProbeManager();
    dynamic.add(lazy(customIngredient), "dynamic");
    enabled[0] = false;
    check(
        dynamic.find(new ItemStack(Items.GOLD_INGOT)) == null,
        "custom ingredient initially rejects");
    enabled[0] = true;
    check(
        "dynamic".equals(dynamic.find(new ItemStack(Items.GOLD_INGOT))),
        "custom ingredient is never negatively filtered");

    @SuppressWarnings("unchecked")
    var customHolders =
        (net.minecraft.core.HolderSet<Item>)
            java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] {net.minecraft.core.HolderSet.class},
                (proxy, method, args) ->
                    switch (method.getName()) {
                      case "isImmediatelyResolvable" -> false;
                      case "contains" ->
                          enabled[0] && args[0] == Items.COAL.builtInRegistryHolder();
                      case "hashCode" -> System.identityHashCode(proxy);
                      case "equals" -> proxy == args[0];
                      case "toString" -> "verification dynamic holder set";
                      default -> throw new UnsupportedOperationException(method.getName());
                    });
    var holderManager = new ProbeManager();
    holderManager.add(lazy(Ingredient.of(customHolders)), "holders");
    enabled[0] = false;
    check(
        holderManager.find(new ItemStack(Items.COAL)) == null,
        "custom holder set initially rejects");
    enabled[0] = true;
    check(
        "holders".equals(holderManager.find(new ItemStack(Items.COAL))),
        "custom holder set remains dynamic without tag event");

    var deferred = new ProbeManager();
    deferred.add(new PredicateInput(stack -> true), "first");
    deferred.add(
        new DeferredInput(
            () -> {
              throw new AssertionError("forced unused lazy parse");
            }),
        "unused");
    check(
        "first".equals(deferred.find(new ItemStack(Items.COAL))),
        "unresolved lazy input is not forced by index creation");

    var random = new Random(20261013L);
    var finite = new ProbeManager();
    Item[] items = {
      Items.IRON_INGOT,
      Items.GOLD_INGOT,
      Items.DIAMOND,
      Items.COAL,
      Items.COPPER_INGOT,
      Items.RAW_IRON
    };
    var entries = new ArrayList<Map.Entry<IRecipeInput, String>>();
    for (int i = 0; i < 48; i++) {
      Item item = items[random.nextInt(items.length)];
      IRecipeInput input =
          i % 4 == 0
              ? new PredicateInput(stack -> stack.is(item) && stack.getCount() % 3 == 0)
              : lazy(Ingredient.of(item));
      String output = "case-" + i;
      finite.add(input, output);
      entries.add(Map.entry(input, output));
    }
    for (Item item : items)
      for (int count = 1; count <= 64; count++) {
        var stack = new ItemStack(item, count);
        String expected =
            entries.stream()
                .filter(e -> e.getKey().matches(stack))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        check(
            Objects.equals(expected, finite.find(stack)),
            "ordered linear oracle " + item + "/" + count);
      }

    var concurrent = new ProbeManager();
    var queries =
        BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR).limit(128).toList();
    for (int i = 0; i < 32; i++) concurrent.add(lazy(Ingredient.of(queries.get(i))), "item-" + i);
    try (var executor = Executors.newFixedThreadPool(8, Thread.ofPlatform().factory())) {
      var barrier = new CyclicBarrier(8);
      List<Callable<Boolean>> jobs = new ArrayList<>();
      for (int worker = 0; worker < 8; worker++) {
        int offset = worker * 17;
        jobs.add(
            () -> {
              barrier.await();
              for (int i = 0; i < 10000; i++) {
                int index = (i + offset) % queries.size();
                String expected = index < 32 ? "item-" + index : null;
                if (!Objects.equals(expected, concurrent.find(new ItemStack(queries.get(index)))))
                  return false;
              }
              return true;
            });
      }
      for (var result : executor.invokeAll(jobs))
        check(result.get(), "concurrent cold candidate cache lookup");
    } catch (Exception error) {
      throw new IllegalStateException(error);
    }
  }

  private void replacement() {
    for (boolean objectOverload : List.of(false, true)) {
      var manager = new BoundedBasic();
      manager.addRecipe(
          lazy(Ingredient.of(Items.RAW_IRON)), List.of(new ItemStack(Items.COAL)), null, false);
      var replacement = lazy(Ingredient.of(Items.RAW_IRON));
      try {
        boolean accepted =
            objectOverload
                ? manager.addRecipe(
                    new MachineRecipe<>(replacement, List.of(new ItemStack(Items.DIAMOND))), true)
                : manager.addRecipe(replacement, List.of(new ItemStack(Items.DIAMOND)), null, true);
        var result = manager.apply(new ItemStack(Items.RAW_IRON), false);
        check(
            accepted && result != null && result.getOutput().iterator().next().is(Items.DIAMOND),
            "replace distinct input object overload=" + objectOverload);
        check(
            java.util.stream.StreamSupport.stream(manager.getRecipes().spliterator(), false).count()
                == 1,
            "replace leaves one recipe overload=" + objectOverload);
      } catch (Throwable error) {
        check(false, "bounded replacement overload=" + objectOverload + ": " + error);
      }
    }
    var list = new BoundedList();
    var first = lazy(Ingredient.of(Items.COAL));
    var second = lazy(Ingredient.of(Items.COAL));
    list.addRecipe(first, "first", null, false);
    try {
      list.addRecipe(second, "second", null, true);
      check(
          list.getInputs().size() == 1 && list.getInputs().getFirst() == second,
          "list replacement removes old object");
    } catch (Throwable error) {
      check(false, "bounded list replacement: " + error);
    }
  }

  private void config() {
    var config = new Config("probe");
    config.set("wide/\u3000option", 17);
    check(
        config.get("wide/option") != null && config.get("wide/option").getInt() == 17,
        "leading Unicode path whitespace retained");
    check(
        config.get("wide/\u2003option") != null && config.get("wide/\u2003option").getInt() == 17,
        "equivalent Unicode path whitespace");
    config.set("directNumber", 11);
    check(config.get("directNumber").getInt() == 11, "warm parsed numeric value");
    config.get("directNumber").value = "22";
    check(
        config.get("directNumber").getInt() == 22,
        "direct numeric field mutation invalidates parsed value");
    config.clear();
    for (String key :
        List.of(
            "simple",
            "misc/option",
            " leading / trailing ",
            "quoted/\"a/b\"",
            "escaped/a\\/b",
            "deep/a/b/c")) {
      for (int value = 0; value < 12; value++) {
        config.set(key, value);
        check(config.get(key).getInt() == value, "path value remains live " + key);
        check(config.get(new String(key)).getInt() == value, "equal noninterned path " + key);
        config.get(key).set(value + 1);
        check(config.get(key).getInt() == value + 1, "direct config value mutation " + key);
      }
      config.remove(key);
      check(config.get(key) == null, "path removal " + key);
      config.set(key, 19);
      check(config.get(key).getInt() == 19, "path reinsert " + key);
      config.clear();
      check(config.get(key) == null, "path clear " + key);
    }
    config.set("misc/a", 11);
    config.set("misc/b", 22);
    try (var executor = Executors.newFixedThreadPool(8, Thread.ofPlatform().factory())) {
      List<Callable<Boolean>> jobs = new ArrayList<>();
      for (int worker = 0; worker < 8; worker++)
        jobs.add(
            () -> {
              for (int i = 0; i < 10000; i++)
                if (config.get(i % 2 == 0 ? "misc/a" : "misc/b").getInt() != (i % 2 == 0 ? 11 : 22))
                  return false;
              return true;
            });
      for (var result : executor.invokeAll(jobs))
        check(result.get(), "concurrent read-only path cache");
    } catch (Exception error) {
      throw new IllegalStateException(error);
    }
  }

  private void finish() {
    finished = true;
    try {
      Files.writeString(
          Path.of("ic2-hotpath-regression.json"),
          new GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  Map.of(
                      "complete",
                      failures.isEmpty(),
                      "checks",
                      checks,
                      "failures",
                      failures,
                      "groups",
                      groups,
                      "nativeReloads",
                      stage + 1)));
    } catch (Exception error) {
      throw new IllegalStateException(error);
    }
    server.halt(false);
  }

  private static final class ProbeManager extends OrderedMachineRecipes<IRecipeInput, String> {
    protected IRecipeInput getForInput(IRecipeInput input) {
      return input;
    }

    void add(IRecipeInput input, String output) {
      var recipe = new MachineRecipe<>(input, output);
      recipes.put(input, recipe);
      addToCache(recipe);
    }

    void remove(IRecipeInput input) {
      recipes.remove(input);
      removeCachedRecipes(input);
    }

    String find(ItemStack input) {
      var result = getRecipe(input);
      return result == null ? null : result.getOutput();
    }
  }

  private record PredicateInput(Predicate<ItemStack> predicate) implements IRecipeInput {
    public boolean matches(ItemStack stack) {
      return predicate.test(stack);
    }

    public int getAmount() {
      return 1;
    }

    public List<ItemStack> getInputs() {
      return List.of();
    }
  }

  private static final class BoundedBasic extends BasicMachineRecipes {
    private int calls;

    @Override
    protected MachineRecipe<IRecipeInput, Collection<ItemStack>> getRecipe(ItemStack input) {
      if (++calls > 64) throw new IllegalStateException("replacement loop exceeded 64 lookups");
      return super.getRecipe(input);
    }
  }

  private static final class BoundedList extends ListedMachineInputs {
    private int calls;

    @Override
    protected MachineRecipe<IRecipeInput, Object> getRecipe(ItemStack input) {
      if (++calls > 64) throw new IllegalStateException("replacement loop exceeded 64 lookups");
      return super.getRecipe(input);
    }
  }
}
