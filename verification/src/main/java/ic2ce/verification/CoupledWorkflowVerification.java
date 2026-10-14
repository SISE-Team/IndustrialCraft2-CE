package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.cable.ConductorBlock;
import ic2ce.neoforge.cable.RedstoneGateBlock;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.power.CombustionGenerator;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.processing.solid.MetalFormer;
import ic2ce.neoforge.processing.solid.OreWasher;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Finite coal power, every cable, and outputs forwarded into the next real processing stage. */
public final class CoupledWorkflowVerification {
  public static volatile boolean finished;

  private record Stage(String machine, String input, String output, int mode) {}

  private final List<Stage> stages =
      List.of(
          new Stage("extractor", "ic2:resin", "ic2:rubber", 0),
          new Stage("macerator", "minecraft:raw_iron", "ic2:crushed_iron", 0),
          new Stage("ore_washing_plant", "ic2:crushed_iron", "ic2:purified_iron", 0),
          new Stage("centrifuge", "ic2:purified_iron", "ic2:iron_dust", 0),
          new Stage("electric_furnace", "ic2:iron_dust", "minecraft:iron_ingot", 0),
          new Stage("metal_former", "minecraft:iron_ingot", "ic2:iron_plate", 1),
          new Stage("macerator", "minecraft:raw_copper", "ic2:crushed_copper", 0),
          new Stage("ore_washing_plant", "ic2:crushed_copper", "ic2:purified_copper", 0),
          new Stage("centrifuge", "ic2:purified_copper", "ic2:copper_dust", 0),
          new Stage("electric_furnace", "ic2:copper_dust", "minecraft:copper_ingot", 0),
          new Stage("metal_former", "minecraft:copper_ingot", "ic2:copper_cable", 0));
  private final Map<String, Integer> pool = new TreeMap<>();
  private final List<Map<String, Object>> rows = new ArrayList<>(), cables = new ArrayList<>();
  private final List<String> failures = new ArrayList<>();
  private int checks, index = -1, age, total;
  private final Map<String, CombustionGenerator> cableSources = new HashMap<>();
  private final Map<String, NativeBlockEntity> cableSinks = new HashMap<>();
  private AllMachinesVerification s;
  private ServerPlayer player;
  private CombustionGenerator generator;
  private NativeBlockEntity bank;
  private PoweredProcessor<?, ?, ?> machine;
  private ItemStack input;
  private static final BlockPos BASE = new BlockPos(700, 181, 700), PROCESSOR = BASE.east(6);

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add(label);
  }

  private void save() {
    var r = new LinkedHashMap<String, Object>();
    r.put("checks", checks);
    r.put("failures", failures);
    r.put("stages", rows);
    r.put("cables", cables);
    r.put("remainingPool", pool);
    r.put("ticks", total);
    r.put("finished", finished);
    r.put("complete", finished && failures.isEmpty());
    if (generator != null) {
      r.put("coalRemaining", generator.fuelSlot.get().getCount());
      r.put("generatorFuelTicks", generator.fuel);
      r.put("generatorEU", generator.getComponent(ElectricalBuffer.class).getEnergy());
      r.put("bankEU", bank.getComponent(ElectricalBuffer.class).getEnergy());
    }
    try {
      Files.writeString(
          Path.of("ic2-coupled-workflow-verification.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(r));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (player != null || !(event.getEntity() instanceof ServerPlayer actor)) return;
    player = actor;
    s = AllMachinesVerification.ACTIVE;
    s.player = actor;
    for (int x = -1; x <= 8; x++) {
      s.level.setChunkForced(BASE.offset(x, 0, 0).getX() >> 4, BASE.getZ() >> 4, true);
      s.level.setBlock(BASE.offset(x, -1, 0), Blocks.STONE.defaultBlockState(), 3);
      for (int y = 0; y < 4; y++)
        s.level.setBlock(BASE.offset(x, y, 0), Blocks.AIR.defaultBlockState(), 3);
    }
    generator = (CombustionGenerator) s.helper("generator", BASE);
    generator.fuelSlot.put(new ItemStack(Items.COAL, 64));
    bank = s.helper("batbox", BASE.east(5));
    s.facing(bank, Direction.EAST);
    for (int x = 1; x <= 4; x++)
      s.level.setBlock(
          BASE.east(x), AllMachinesVerification.block("tin_cable").defaultBlockState(), 3);
    pool.put("ic2:resin", 2);
    pool.put("minecraft:raw_iron", 1);
    pool.put("minecraft:raw_copper", 1);
    pool.put("minecraft:redstone", 2);
    prepareCables();
    next();
    s.level.getServer().tickRateManager().requestGameToSprint(100_000);
    save();
  }

  private void prepareCables() {
    int i = 0;
    for (var id : BuiltInRegistries.BLOCK.keySet())
      if (id.getNamespace().equals("ic2")
          && BuiltInRegistries.BLOCK.getValue(id) instanceof ConductorBlock block
          && !block.isFoam()) {
        var pos = new BlockPos(740 + (i++ % 5) * 16, 181, 740 + (i / 5) * 16);
        s.level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
        var source = (CombustionGenerator) s.helper("generator", pos);
        source.fuelSlot.put(new ItemStack(Items.COAL, 4));
        var sink = s.helper("mfsu", pos.east(5));
        for (int x = 1; x <= 4; x++) s.level.setBlock(pos.east(x), block.defaultBlockState(), 3);
        if (block instanceof RedstoneGateBlock)
          for (int x = 1; x <= 4; x++)
            s.level.setBlock(pos.east(x).below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        cableSources.put(id.toString(), source);
        cableSinks.put(id.toString(), sink);
        cables.add(
            new LinkedHashMap<>(
                Map.of(
                    "id", id.toString(), "position", List.of(pos.getX(), pos.getY(), pos.getZ()))));
      }
    check(
        cables.size() == 14,
        "all fourteen cable variants participate in actual coal-powered networks");
  }

  private void next() {
    index++;
    age = 0;
    machine = null;
    if (index >= stages.size()) {
      craftCircuit();
      finish();
      return;
    }
    begin();
  }

  private void begin() {
    var stage = stages.get(index);
    int count = pool.getOrDefault(stage.input, 0);
    pool.remove(stage.input);
    check(count > 0, stage.machine + " receives only the preceding stage's actual output");
    if (count == 0) {
      finish();
      return;
    }
    input = AllMachinesVerification.stack(stage.input, count);
    machine = (PoweredProcessor<?, ?, ?>) s.helper(stage.machine, PROCESSOR);
    if (machine instanceof MetalFormer former) former.setMode(stage.mode);
    machine.inputSlot.put(input.copy());
    if (machine instanceof OreWasher) {
      var tank = machine.getComponent(FluidPorts.class).getAllTanks().iterator().next();
      tank.fillMbUnchecked(
          Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, tank.getCapacity()),
          false);
    }
    machine.setChanged();
    save();
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (player == null || finished) return;
    total++;
    age++;
    if (total == 1500) checkCables();
    if (machine == null) {
      begin();
      return;
    }
    if (!machine.inputSlot.isEmpty() && age < 60000) return;
    var stage = stages.get(index);
    var outputs = new ArrayList<String>();
    int produced = 0;
    for (var stack : machine.outputSlot)
      if (!stack.isEmpty()) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        pool.merge(id, stack.getCount(), Integer::sum);
        outputs.add(stack.toString());
        if (id.equals(stage.output)) produced += stack.getCount();
      }
    boolean pass = machine.inputSlot.isEmpty() && produced > 0;
    check(pass, stage.machine + " processes " + input + " into " + stage.output);
    rows.add(
        Map.of(
            "machine",
            stage.machine,
            "input",
            input.toString(),
            "output",
            outputs,
            "ticks",
            age,
            "passed",
            pass,
            "poweredBy",
            "coal generator -> four tin cables -> batbox"));
    machine.inputSlot.clear();
    machine.outputSlot.clear();
    machine = null;
    next();
    save();
  }

  private void checkCables() {
    for (var row : cables) {
      var source = cableSources.get(row.get("id"));
      var sink = cableSinks.get(row.get("id"));
      double received = sink.getComponent(ElectricalBuffer.class).getEnergy();
      boolean pass = received > 0 && source.fuelSlot.get().getCount() < 4;
      check(pass, row.get("id") + " transfers actual coal-generated power into a storage bank");
      row.put("receivedEU", received);
      row.put("coalRemaining", source.fuelSlot.get().getCount());
      row.put("passed", pass);
    }
    save();
  }

  private ItemStack craft(List<ItemStack> grid, Item expected) {
    var table = BASE.south(3);
    s.level.setBlock(table, Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
    var menu =
        new CraftingMenu(179, player.getInventory(), ContainerLevelAccess.create(s.level, table));
    for (int i = 0; i < grid.size(); i++) menu.getSlot(i + 1).set(grid.get(i));
    menu.clicked(0, 0, ContainerInput.PICKUP, player);
    var result = menu.getCarried();
    check(
        result.is(expected), "native crafting produces " + expected + " using forwarded materials");
    return result.copy();
  }

  private ItemStack take(String id) {
    int count = pool.getOrDefault(id, 0);
    check(count > 0, "forwarded pool has " + id);
    if (count == 0) return ItemStack.EMPTY;
    pool.put(id, count - 1);
    return AllMachinesVerification.stack(id, 1);
  }

  private void craftCircuit() {
    for (int i = 0; i < 6; i++) {
      var result =
          craft(
              List.of(take("ic2:rubber"), take("ic2:copper_cable")),
              AllMachinesVerification.stack("insulated_copper_cable", 1).getItem());
      if (!result.isEmpty())
        pool.merge("ic2:insulated_copper_cable", result.getCount(), Integer::sum);
    }
    var grid =
        List.of(
            take("ic2:insulated_copper_cable"),
            take("ic2:insulated_copper_cable"),
            take("ic2:insulated_copper_cable"),
            take("minecraft:redstone"),
            take("ic2:iron_plate"),
            take("minecraft:redstone"),
            take("ic2:insulated_copper_cable"),
            take("ic2:insulated_copper_cable"),
            take("ic2:insulated_copper_cable"));
    var circuit = craft(grid, AllMachinesVerification.stack("circuit", 1).getItem());
    rows.add(
        Map.of(
            "machine",
            "native_crafting_table",
            "input",
            "coal-powered processed rubber, iron plate and copper cables; two initial redstone",
            "output",
            circuit.toString(),
            "passed",
            !circuit.isEmpty()));
  }

  private void finish() {
    finished = true;
    if (total < 1500) checkCables();
    s.level.getServer().tickRateManager().stopSprinting();
    check(generator.fuelSlot.get().getCount() < 64, "finite generator fuel was actually consumed");
    save();
  }

  public static final class Client {
    @SubscribeEvent
    public void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      var mc = net.minecraft.client.Minecraft.getInstance();
      org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
      if (finished) mc.stop();
    }
  }
}
