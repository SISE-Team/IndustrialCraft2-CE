package ic2ce.verification;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.JsonOps;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.item.ElectricItem;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.power.CombustionGenerator;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.recipe.crafting.PositionedCrafting;
import ic2ce.neoforge.recipe.crafting.UnorderedCrafting;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Disposable fixtures, available only in migrationVerificationJar. */
public final class MigrationVerification {
  public static final java.util.concurrent.ConcurrentMap<String, Integer> CLIENT_GUI_TICKS =
      new java.util.concurrent.ConcurrentHashMap<>();
  private final Map<String, Object> report = new LinkedHashMap<>();
  private final List<String> failures = new ArrayList<>();
  private final Map<String, BlockPos> positions = new LinkedHashMap<>();
  private ServerLevel level;
  private int ticks;
  private int checks;
  private double disconnectedEnergy;
  private boolean finished;
  private final java.util.Set<String> powered = new java.util.HashSet<>();
  private final java.util.Set<String> active = new java.util.HashSet<>();
  private int guiCycleTicks;
  private int guiCycleIndex;
  private List<String> guiCycleNames;
  private boolean guiCycleComplete;

  private void check(boolean passed, String description) {
    checks++;
    if (!passed) {
      failures.add(description);
      System.err.println("IC2_VERIFY FAIL " + description);
    }
  }

  private void guard(String name, Runnable action) {
    try {
      action.run();
    } catch (Throwable error) {
      check(false, name + ": " + error);
      error.printStackTrace();
    }
  }

  private void saveReport() {
    report.put("checks", checks);
    report.put("ticks", ticks);
    report.put("failures", failures);
    report.put("finished", finished);
    if (Boolean.getBoolean("ic2.verifyGuiCycle")) {
      report.put("clientGuiTicks", new java.util.TreeMap<>(CLIENT_GUI_TICKS));
      report.put("clientGuiCycleComplete", guiCycleComplete);
    }
    try {
      Path path =
          Path.of(System.getProperty("ic2.verifyReport", "ic2-migration-verification.json"));
      Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(report));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SubscribeEvent
  public void started(ServerStartedEvent event) {
    if (Boolean.getBoolean("ic2.verifyFluidTooltip")) return;
    level = event.getServer().overworld();
    guard("crafting recipes", () -> recipes(event.getServer()));
    guard("fluid containers", this::fluids);
    check(
        ic2ce.neoforge.runtime.ProjectRuntime.envProxy.getBurnTime(new ItemStack(Items.COAL))
            == 1600,
        "vanilla coal fuel value");
    guard("machine fixtures", this::fixtures);
    saveReport();
    System.out.println("IC2_VERIFY START " + checks + " checks, " + failures.size() + " failures");
  }

  private ItemStack sample(SlotDisplay display) {
    ItemStack stack = display.resolveForFirstStack(SlotDisplayContext.fromLevel(level));
    if (stack.isEmpty()) throw new IllegalStateException("empty input display " + display);
    return stack;
  }

  private void recipes(MinecraftServer server) {
    int count = 0;
    int machineCount = 0;
    boolean machineBookMetadataValid = true;
    for (var holder : server.getRecipeManager().getRecipes()) {
      Recipe<?> recipe = holder.value();
      if (recipe instanceof ic2ce.neoforge.recipe.codec.RecipeHolder<?, ?>) {
        machineCount++;
        machineBookMetadataValid &= recipe.isSpecial() && recipe.display().isEmpty();
      }
      if (!(recipe instanceof PositionedCrafting) && !(recipe instanceof UnorderedCrafting))
        continue;
      count++;
      String id = holder.id().identifier().toString();
      guard(
          id,
          () -> {
            check(!recipe.placementInfo().isImpossibleToPlace(), id + " placement");
            boolean hidden =
                recipe instanceof PositionedCrafting shaped
                    ? !shaped.canShow()
                    : !((UnorderedCrafting) recipe).canShow();
            check(
                hidden == recipe.display().isEmpty(),
                id + " book visibility respects original hidden flag");
            List<ItemStack> inputs = new ArrayList<>();
            int width;
            int height;
            if (recipe instanceof PositionedCrafting shaped) {
              width = shaped.inputWidth;
              height = shaped.inputHeight;
              for (var input : shaped.getIngredients())
                inputs.add(input.map(i -> sample(i.display())).orElse(ItemStack.EMPTY));
            } else {
              UnorderedCrafting shapeless = (UnorderedCrafting) recipe;
              width = Math.min(3, shapeless.input.length);
              height = (shapeless.input.length + width - 1) / width;
              for (var input : shapeless.input) inputs.add(sample(input.getIngredient().display()));
              while (inputs.size() < width * height) inputs.add(ItemStack.EMPTY);
            }
            CraftingInput input = CraftingInput.of(width, height, inputs);
            @SuppressWarnings("unchecked")
            Recipe<CraftingInput> crafting = (Recipe<CraftingInput>) recipe;
            check(crafting.matches(input, level), id + " matching trimmed input");
            check(!crafting.assemble(input).isEmpty(), id + " assembling");
            if (recipe instanceof PositionedCrafting shaped) {
              List<ItemStack> mirrored = new ArrayList<>();
              for (int y = 0; y < height; y++)
                for (int x = width - 1; x >= 0; x--) mirrored.add(inputs.get(x + y * width));
              check(
                  shaped.matches(CraftingInput.of(width, height, mirrored), level),
                  id + " mirrored");
              List<ItemStack> offset = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
              for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                  offset.set(x + 3 - width + (y + 3 - height) * 3, inputs.get(x + y * width));
              check(shaped.matches(CraftingInput.of(3, 3, offset), level), id + " offset");
              List<ItemStack> placed = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
              net.minecraft.recipebook.PlaceRecipeHelper.placeRecipe(
                  3,
                  3,
                  shaped,
                  shaped.placementInfo().slotsToIngredientIndex(),
                  (ingredientIndex, gridIndex, x, y) -> {
                    if (ingredientIndex >= 0)
                      placed.set(
                          gridIndex,
                          sample(
                              shaped.placementInfo().ingredients().get(ingredientIndex).display()));
                  });
              check(
                  shaped.matches(CraftingInput.of(3, 3, placed), level),
                  id + " vanilla recipe book placement");
            }
            RegistryFriendlyByteBuf buf =
                new RegistryFriendlyByteBuf(
                    Unpooled.buffer(),
                    server.registryAccess(),
                    net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
            try {
              Recipe.STREAM_CODEC.encode(buf, recipe);
              @SuppressWarnings("unchecked")
              Recipe<CraftingInput> decoded =
                  (Recipe<CraftingInput>) Recipe.STREAM_CODEC.decode(buf);
              check(decoded.matches(input, level), id + " network roundtrip");
              check(
                  ItemStack.isSameItemSameComponents(
                      crafting.assemble(input), decoded.assemble(input)),
                  id + " network result");
            } finally {
              buf.release();
            }
          });
    }
    report.put("craftingRecipes", count);
    report.put("machineRecipes", machineCount);
    check(machineCount > 0, "IC2 machine recipes remain loaded");
    check(
        machineBookMetadataValid,
        "machine wrappers bypass workbench placement checks and displays");
    check(count >= 366, "all 366 IC2 crafting recipes loaded");
    // Overlapping shapeless inputs must find an assignment in either order.
    var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    var overlap =
        (UnorderedCrafting)
            Recipe.CODEC
                .parse(
                    ops,
                    JsonParser.parseString(
                        """
                        {"type":"ic2:shapeless","ingredients":[[{"item":"minecraft:iron_ingot"},{"item":"minecraft:gold_ingot"}],{"item":"minecraft:iron_ingot"}],"result":{"item":"ic2:bronze_ingot"}}
                        """))
                .getOrThrow();
    check(
        overlap.matches(
            CraftingInput.of(
                2, 1, List.of(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT))),
            level),
        "overlapping shapeless assignment");
    var charged =
        (UnorderedCrafting)
            Recipe.CODEC
                .parse(
                    ops,
                    JsonParser.parseString(
                        """
                        {"type":"ic2:shapeless","ingredients":[{"item":"ic2:re_battery"}],"result":{"item":"ic2:re_battery"}}
                        """))
                .getOrThrow();
    ItemStack battery = new ItemStack(item("re_battery"));
    ElectricItem.manager.charge(battery, 1000, Integer.MAX_VALUE, true, false);
    check(
        ElectricItem.manager.getCharge(charged.assemble(CraftingInput.of(1, 1, List.of(battery))))
            == 1000,
        "crafting preserves charge");
    for (String type : List.of("shaped", "shapeless")) {
      String recipeJson =
          type.equals("shaped")
              ? "{\"type\":\"ic2:shaped\",\"pattern\":[\"HI\"],\"key\":{\"H\":{\"item\":\"ic2:forge_hammer\"},\"I\":{\"item\":\"minecraft:iron_ingot\"}},\"result\":{\"item\":\"ic2:iron_plate\"}}"
              : "{\"type\":\"ic2:shapeless\",\"ingredients\":[{\"item\":\"ic2:forge_hammer\"},{\"item\":\"minecraft:iron_ingot\"}],\"result\":{\"item\":\"ic2:iron_plate\"}}";
      var toolRecipe =
          (net.minecraft.world.item.crafting.CraftingRecipe)
              Recipe.CODEC.parse(ops, JsonParser.parseString(recipeJson)).getOrThrow();
      ItemStack hammer = new ItemStack(item("forge_hammer"));
      var input = CraftingInput.of(2, 1, List.of(hammer, new ItemStack(Items.IRON_INGOT)));
      check(
          toolRecipe.getRemainingItems(input).getFirst().getDamageValue() == 1,
          type + " tool remainder");
      hammer.setDamageValue(hammer.getMaxDamage() - 1);
      check(
          toolRecipe.getRemainingItems(input).getFirst().isEmpty(),
          type + " tool breaks at durability limit");
    }
  }

  private static Item item(String name) {
    return BuiltInRegistries.ITEM
        .getOptional(Identifier.fromNamespaceAndPath("ic2", name))
        .orElseThrow();
  }

  private void fluids() {
    int sources = 0;
    for (var fluid : BuiltInRegistries.FLUID) {
      if (!BuiltInRegistries.FLUID.getKey(fluid).getNamespace().equals("ic2")
          || !fluid.isSource(fluid.defaultFluidState())) continue;
      sources++;
      ItemStack bucket = new ItemStack(fluid.getBucket());
      Ic2FluidStack contents = Ic2FluidStack.get(bucket);
      check(
          contents != null && contents.getFluid() == fluid && contents.getAmountMb() == 1000,
          "bucket fluid " + fluid);
    }
    report.put("sourceFluids", sources);
    check(sources == 17, "all 17 fluid source buckets");
    var coolant =
        BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("ic2", "coolant"));
    var fluid = Ic2FluidStack.create(coolant, 1000);
    var replacement = new org.apache.commons.lang3.mutable.MutableObject<ItemStack>();
    ItemStack empty = new ItemStack(Items.BUCKET);
    check(
        ic2ce.neoforge.fluid.FluidHandler.fillMb(empty, fluid, true, replacement) == 1000
            && empty.is(Items.BUCKET),
        "bucket simulated fill");
    check(
        ic2ce.neoforge.fluid.FluidHandler.fillMb(empty, fluid, false, replacement) == 1000,
        "bucket committed fill");
    ItemStack filled = replacement.getValue();
    check(filled.is(coolant.getBucket()), "bucket item replacement");
    check(
        ic2ce.neoforge.fluid.FluidHandler.drainMb(
                filled, Ic2FluidStack.create(coolant, 500), false, replacement)
            == 0,
        "bucket refuses partial extraction without losing fluid");
    check(Ic2FluidStack.get(filled).getAmountMb() == 1000, "partial extraction leaves full bucket");
    check(
        ic2ce.neoforge.fluid.FluidHandler.drainMb(filled, 1000, false, replacement).getAmountMb()
            == 1000,
        "bucket full extraction");
    check(replacement.getValue().is(Items.BUCKET), "bucket returns empty container");
  }

  private BlockEntity place(String name, int x, int z) {
    Block block =
        BuiltInRegistries.BLOCK
            .getOptional(Identifier.fromNamespaceAndPath("ic2", name))
            .orElseThrow();
    BlockPos pos = new BlockPos(x, 101, z);
    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    level.setBlockAndUpdate(pos, block.defaultBlockState());
    positions.put(name, pos);
    return level.getBlockEntity(pos);
  }

  private void fixtures() {
    level.setChunkForced(2, 2, true);
    for (int x = 30; x <= 47; x++)
      for (int z = 30; z <= 43; z++) {
        level.setBlockAndUpdate(new BlockPos(x, 100, z), Blocks.SMOOTH_STONE.defaultBlockState());
        for (int y = 101; y <= 104; y++)
          level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
      }
    CombustionGenerator generator = (CombustionGenerator) place("generator", 32, 32);
    generator.fuelSlot.put(new ItemStack(Items.COAL, 32));
    for (int x = 33; x <= 38; x++) place("tin_cable", x, 32);
    machine("macerator", 34, 31, new ItemStack(Items.IRON_ORE, 2));
    machine("electric_furnace", 35, 31, new ItemStack(Items.IRON_ORE, 2));
    machine("compressor", 36, 31, new ItemStack(Items.CLAY_BALL, 8));
    machine("extractor", 37, 31, new ItemStack(item("rubber_log"), 2));
    int z = 35;
    for (String name :
        List.of(
            "metal_former",
            "ore_washing_plant",
            "induction_furnace",
            "canner",
            "fluid_distributor",
            "batbox",
            "mfe",
            "mfsu")) {
      place(name, 32 + (z - 35) * 2 % 14, 35 + (z - 35) / 7 * 3);
      z++;
    }
    level.setBlockAndUpdate(new BlockPos(40, 101, 32), Blocks.CRAFTING_TABLE.defaultBlockState());
    place("bronze_tank", 44, 38);
    guard(
        "washer fluid capability",
        () ->
            tankTransfer(
                "ore_washing_plant", net.minecraft.world.level.material.Fluids.WATER, false));
    guard(
        "canner fluid capability",
        () -> tankTransfer("canner", net.minecraft.world.level.material.Fluids.WATER, false));
    guard(
        "coolant tank capability",
        () ->
            tankTransfer(
                "bronze_tank",
                BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("ic2", "coolant")),
                true));
    report.put("fixturePositions", positions.toString());
  }

  private long fluidAmount(
      net.neoforged.neoforge.transfer.ResourceHandler<
              net.neoforged.neoforge.transfer.fluid.FluidResource>
          handler) {
    long amount = 0;
    for (int tank = 0; tank < handler.size(); tank++) amount += handler.getAmountAsLong(tank);
    return amount;
  }

  private void tankTransfer(
      String name, net.minecraft.world.level.material.Fluid fluid, boolean canDrain) {
    var handler =
        level.getCapability(
            net.neoforged.neoforge.capabilities.Capabilities.Fluid.BLOCK,
            positions.get(name),
            net.minecraft.core.Direction.UP);
    check(handler != null && handler.size() > 0, name + " exposes fluid capability");
    if (handler == null) return;
    var resource = net.neoforged.neoforge.transfer.fluid.FluidResource.of(fluid);
    long before = fluidAmount(handler);
    try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
      check(handler.insert(resource, 3000, tx) == 3000, name + " accepts exact requested volume");
    }
    check(fluidAmount(handler) == before, name + " insertion rolls back");
    try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
      check(handler.insert(resource, 3000, tx) == 3000, name + " committed insertion");
      tx.commit();
    }
    check(fluidAmount(handler) == before + 3000, name + " stores exact volume");
    if (canDrain) {
      try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
        check(handler.extract(resource, 750, tx) == 750, name + " partial extraction");
      }
      check(fluidAmount(handler) == before + 3000, name + " extraction rolls back");
      try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
        check(handler.extract(resource, 750, tx) == 750, name + " committed extraction");
        tx.commit();
      }
      check(fluidAmount(handler) == before + 2250, name + " exact remainder after extraction");
    }
    check(
        ic2ce.neoforge.fluid.FluidHandler.fillMb(
                tile(name), net.minecraft.core.Direction.UP, Ic2FluidStack.create(fluid, 100), true)
            == 100,
        name + " legacy block entity API uses the actual tank capability");
    var saved = tile(name).saveWithFullMetadata(level.registryAccess());
    var loaded =
        (NativeBlockEntity)
            BlockEntity.loadStatic(
                positions.get(name), tile(name).getBlockState(), saved, level.registryAccess());
    check(
        loaded
                .getComponent(ic2ce.neoforge.component.FluidPorts.class)
                .getAllTanks()
                .iterator()
                .next()
                .getFluidAmount()
            == tile(name)
                .getComponent(ic2ce.neoforge.component.FluidPorts.class)
                .getAllTanks()
                .iterator()
                .next()
                .getFluidAmount(),
        name + " fluid persistence");
  }

  private void machine(String name, int x, int z, ItemStack input) {
    var machine = (PoweredProcessor<?, ?, ?>) place(name, x, z);
    machine.inputSlot.put(input);
    machine.setChanged();
  }

  private NativeBlockEntity tile(String name) {
    return (NativeBlockEntity) level.getBlockEntity(positions.get(name));
  }

  @SubscribeEvent
  public void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer player) || level == null) return;
    var server = player.level().getServer();
    server
        .getCommands()
        .performPrefixedCommand(
            server.createCommandSourceStack(), "gamemode creative " + player.getScoreboardName());
    server
        .getCommands()
        .performPrefixedCommand(
            server.createCommandSourceStack(),
            "tp " + player.getScoreboardName() + " 35.5 102 39.5 180 25");
    int menus = 0;
    for (String name : positions.keySet()) {
      if (!(level.getBlockEntity(positions.get(name)) instanceof IHasGui gui)) continue;
      guard(
          "menu " + name,
          () -> {
            var menu = gui.createServerScreenHandler(1, player);
            check(!menu.slots.isEmpty(), name + " server menu slots");
          });
      menus++;
    }
    report.put("serverMenus", menus);
    saveReport();
  }

  @SubscribeEvent
  public void commands(RegisterCommandsEvent event) {
    event
        .getDispatcher()
        .register(
            Commands.literal("ic2verify")
                .then(
                    Commands.literal("recipes")
                        .executes(
                            ctx -> {
                              ServerPlayer player = ctx.getSource().getPlayerOrException();
                              var server = player.level().getServer();
                              player.awardRecipes(
                                  server.getRecipeManager().getRecipes().stream()
                                      .filter(
                                          holder ->
                                              holder.value() instanceof PositionedCrafting
                                                  || holder.value() instanceof UnorderedCrafting)
                                      .toList());
                              player.getInventory().add(new ItemStack(item("bronze_ingot"), 2));
                              player.getInventory().add(new ItemStack(Items.STICK, 2));
                              player.getInventory().add(new ItemStack(Items.COPPER_INGOT, 2));
                              player.getInventory().add(new ItemStack(item("forge_hammer")));
                              server
                                  .getCommands()
                                  .performPrefixedCommand(
                                      server.createCommandSourceStack(),
                                      "tp " + player.getScoreboardName() + " 40.5 102 34.5 180 25");
                              player.openMenu(
                                  Blocks.CRAFTING_TABLE
                                      .defaultBlockState()
                                      .getMenuProvider(level, new BlockPos(40, 101, 32)));
                              return 1;
                            }))
                .then(
                    Commands.literal("gui")
                        .then(
                            Commands.argument("machine", StringArgumentType.word())
                                .executes(
                                    ctx -> {
                                      String name = StringArgumentType.getString(ctx, "machine");
                                      ServerPlayer player = ctx.getSource().getPlayerOrException();
                                      var pos = positions.get(name);
                                      if (pos == null
                                          || !(level.getBlockEntity(pos) instanceof IHasGui gui))
                                        return 0;
                                      var server = player.level().getServer();
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
                                      return gui.openManagedBe(player, InteractionHand.MAIN_HAND)
                                          ? 1
                                          : 0;
                                    })))
                .then(
                    Commands.literal("status")
                        .executes(
                            ctx -> {
                              ctx.getSource()
                                  .sendSuccess(
                                      () ->
                                          Component.literal(
                                              "IC2 verification: "
                                                  + checks
                                                  + " checks, "
                                                  + failures.size()
                                                  + " failures, tick "
                                                  + ticks),
                                      false);
                              return failures.isEmpty() ? 1 : 0;
                            })));
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (level == null) return;
    if (finished) {
      if (Boolean.getBoolean("ic2.verifyGuiCycle") && !guiCycleComplete)
        guiCycle(event.getServer());
      return;
    }
    ticks++;
    for (String name : List.of("macerator", "electric_furnace", "compressor", "extractor")) {
      var tile = tile(name);
      if (tile != null && tile.getComponent(ElectricalBuffer.class).getEnergy() > 0)
        powered.add(name);
      if (tile != null && tile.getActive()) active.add(name);
    }
    if (ticks == 100)
      guard(
          "energy delivery",
          () -> {
            check(tile("generator").getActive(), "coal generator produces energy");
          });
    if (ticks == 120)
      guard(
          "disconnect cable",
          () -> {
            disconnectedEnergy = tile("macerator").getComponent(ElectricalBuffer.class).getEnergy();
            level.setBlockAndUpdate(new BlockPos(33, 101, 32), Blocks.AIR.defaultBlockState());
          });
    if (ticks == 180)
      guard(
          "reconnect cable",
          () -> {
            check(
                tile("macerator").getComponent(ElectricalBuffer.class).getEnergy()
                    < disconnectedEnergy,
                "energy delivery stops with a cut cable");
            place("tin_cable", 33, 32);
          });
    if (ticks == 900) {
      guard(
          "machine results and persistence",
          () -> {
            for (String name :
                List.of("macerator", "electric_furnace", "compressor", "extractor")) {
              var machine = (PoweredProcessor<?, ?, ?>) tile(name);
              check(powered.contains(name), name + " received cable energy");
              check(active.contains(name), name + " processing active");
              check(!machine.outputSlot.get().isEmpty(), name + " produced output");
              report.put(name + "Output", machine.outputSlot.get().toString());
              CompoundTag saved = machine.saveWithFullMetadata(level.registryAccess());
              check(
                  saved.contains("ic2") && saved.getCompoundOrEmpty("ic2").contains("InvSlots"),
                  name + " writes inventory through ValueOutput");
              BlockEntity restored =
                  BlockEntity.loadStatic(
                      machine.getBlockPos(),
                      machine.getBlockState(),
                      saved,
                      level.registryAccess());
              check(
                  restored instanceof PoweredProcessor<?, ?, ?>,
                  name + " loads through ValueInput");
              if (restored instanceof PoweredProcessor<?, ?, ?> copy) {
                copy.setLevel(level);
                check(
                    ItemStack.matches(machine.outputSlot.get(), copy.outputSlot.get()),
                    name + " inventory roundtrip");
                check(
                    machine.getComponent(ElectricalBuffer.class).getEnergy()
                        == copy.getComponent(ElectricalBuffer.class).getEnergy(),
                    name + " energy roundtrip");
              }
            }
          });
      finished = true;
      saveReport();
      System.out.println(
          "IC2_VERIFY FINISHED " + checks + " checks, " + failures.size() + " failures");
    }
    if (ticks % 100 == 0) saveReport();
  }

  private void guiCycle(MinecraftServer server) {
    if (server.getPlayerList().getPlayers().isEmpty()) return;
    if (guiCycleNames == null) {
      guiCycleNames =
          positions.keySet().stream()
              .filter(name -> level.getBlockEntity(positions.get(name)) instanceof IHasGui)
              .toList();
    }
    ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
    String name = guiCycleNames.get(guiCycleIndex);
    if (guiCycleTicks == 0) {
      guard(
          "client GUI open " + name,
          () -> {
            var pos = positions.get(name);
            player.closeContainer();
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
            check(
                ((IHasGui) level.getBlockEntity(pos))
                    .openManagedBe(player, InteractionHand.MAIN_HAND),
                name + " opens client menu");
          });
    }
    guiCycleTicks++;
    if (guiCycleTicks >= 80) {
      check(
          CLIENT_GUI_TICKS.getOrDefault(name, 0) >= 15,
          name + " client GUI survives 15 render ticks");
      guiCycleTicks = 0;
      guiCycleIndex++;
      if (guiCycleIndex == guiCycleNames.size()) {
        guiCycleComplete = true;
        System.out.println(
            "IC2_VERIFY GUI FINISHED " + checks + " checks, " + failures.size() + " failures");
      }
      saveReport();
    }
  }
}
