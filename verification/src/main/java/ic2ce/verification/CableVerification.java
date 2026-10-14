package ic2ce.verification;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import ic2ce.api.energy.EnergyNet;
import ic2ce.neoforge.cable.ConductorBlock;
import ic2ce.neoforge.cable.RedstoneGateBlock;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Model-derived shape checks and real client mining packets in a disposable world. */
public final class CableVerification {
  private static final BlockPos POS = new BlockPos(64, 101, 64);
  private static volatile int caseNumber = -1;
  private static volatile boolean ready, picked, requested, connected, done;
  private static volatile double diameter;
  private static volatile String clientDiagnostics = "";
  private ServerPlayer player;
  private ServerLevel level;
  private List<ConductorBlock> cables;
  private final List<Trial> trials = new ArrayList<>();

  private record Trial(
      ConductorBlock block, boolean creative, boolean connected, boolean powered) {}

  private final List<String> failures = new ArrayList<>();
  private final List<Map<String, Object>> cases = new ArrayList<>();
  private final java.util.concurrent.ConcurrentLinkedQueue<Map<String, Object>> actions =
      new java.util.concurrent.ConcurrentLinkedQueue<>();
  private int checks, states, index = -1, age, destroyedAt;
  private boolean complete;

  private void check(boolean ok, String message) {
    checks++;
    if (!ok) failures.add(message);
  }

  private void save() {
    var result = new LinkedHashMap<String, Object>();
    result.put("checks", checks);
    result.put("shapeStates", states);
    result.put("cases", cases);
    result.put("failures", failures);
    result.put("complete", complete);
    result.put("miningActions", actions);
    try {
      Files.writeString(
          Path.of("ic2-cable-verification.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(result));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void finish() {
    complete = true;
    ready = false;
    save();
    done = true;
  }

  private static AABB modelBox(String model) throws Exception {
    try (var stream =
        CableVerification.class.getResourceAsStream(
            "/assets/ic2/models/" + model.substring(4) + ".json")) {
      if (stream == null) throw new IllegalStateException(model);
      var json =
          JsonParser.parseReader(
                  new InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8))
              .getAsJsonObject();
      if (!json.has("elements")) return modelBox(json.get("parent").getAsString());
      var element = json.getAsJsonArray("elements").get(0).getAsJsonObject();
      var from = element.getAsJsonArray("from");
      var to = element.getAsJsonArray("to");
      return new AABB(
          from.get(0).getAsDouble() / 16,
          from.get(1).getAsDouble() / 16,
          from.get(2).getAsDouble() / 16,
          to.get(0).getAsDouble() / 16,
          to.get(1).getAsDouble() / 16,
          to.get(2).getAsDouble() / 16);
    }
  }

  private static AABB core(ConductorBlock block) throws Exception {
    String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
    try (var stream =
        CableVerification.class.getResourceAsStream("/assets/ic2/blockstates/" + id + ".json")) {
      var json =
          JsonParser.parseReader(
                  new InputStreamReader(
                      Objects.requireNonNull(stream), java.nio.charset.StandardCharsets.UTF_8))
              .getAsJsonObject();
      return modelBox(
          json.getAsJsonArray("multipart")
              .get(0)
              .getAsJsonObject()
              .getAsJsonObject("apply")
              .get("model")
              .getAsString());
    }
  }

  private void geometry() throws Exception {
    for (var block : cables) {
      AABB box = core(block);
      int bad = 0;
      int count = 0;
      check(
          !block.defaultBlockState().requiresCorrectToolForDrops(),
          "upstream cable harvest remains tool-independent: " + block);
      for (BlockState state : block.getStateDefinition().getPossibleStates()) {
        VoxelShape expected = Shapes.create(box);
        for (Direction direction : Direction.values())
          if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
            expected =
                Shapes.or(
                    expected,
                    Shapes.create(
                        new AABB(
                            direction == Direction.WEST ? 0 : box.minX,
                            direction == Direction.DOWN ? 0 : box.minY,
                            direction == Direction.NORTH ? 0 : box.minZ,
                            direction == Direction.EAST ? 1 : box.maxX,
                            direction == Direction.UP ? 1 : box.maxY,
                            direction == Direction.SOUTH ? 1 : box.maxZ)));
          }
        var actual = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        if (Shapes.joinIsNotEmpty(actual, expected, BooleanOp.NOT_SAME)) bad++;
        if (Shapes.joinIsNotEmpty(
            state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO),
            expected,
            BooleanOp.NOT_SAME)) bad++;
        count++;
        states++;
      }
      check(
          bad == 0,
          BuiltInRegistries.BLOCK.getKey(block)
              + ": model/selection/collision mismatch in "
              + bad
              + " of "
              + count
              + " states");
    }
  }

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (player != null || !(event.getEntity() instanceof ServerPlayer actual)) return;
    player = actual;
    level = player.level();
    cables =
        BuiltInRegistries.BLOCK.stream()
            .filter(b -> b instanceof ConductorBlock c && !c.isFoam())
            .map(b -> (ConductorBlock) b)
            .sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey(b).toString()))
            .toList();
    for (var block : cables)
      for (int variant = 0; variant < 4; variant++)
        trials.add(new Trial(block, variant % 2 == 0, variant >= 2, false));
    for (var block : cables)
      if (block instanceof RedstoneGateBlock)
        for (int variant = 0; variant < 4; variant++)
          trials.add(new Trial(block, variant % 2 == 0, variant >= 2, true));
    try {
      geometry();
    } catch (Exception e) {
      check(false, "shape suite: " + e);
    }
    for (int x = 61; x <= 67; x++)
      for (int z = 60; z <= 67; z++) {
        level.setBlock(new BlockPos(x, 100, z), Blocks.STONE.defaultBlockState(), 3);
        for (int y = 101; y <= 104; y++)
          level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
      }
    // Allow the initial login packets to settle before changing the actor.
  }

  private void next() {
    index++;
    age = 0;
    destroyedAt = -1;
    ready = false;
    picked = false;
    requested = false;
    if (index >= trials.size()) {
      finish();
      return;
    }
    var trial = trials.get(index);
    var block = trial.block();
    connected = trial.connected();
    player.teleportTo(64.5, 101, 62.5);
    player.setDeltaMovement(Vec3.ZERO);
    player.setGameMode(trial.creative() ? GameType.CREATIVE : GameType.SURVIVAL);
    player.getInventory().clearContent();
    player.setHealth(player.getMaxHealth());
    player.getFoodData().setFoodLevel(20);
    level
        .getEntitiesOfClass(ItemEntity.class, new AABB(POS).inflate(3))
        .forEach(ItemEntity::discard);
    level.setBlock(POS, Blocks.AIR.defaultBlockState(), 3);
    level.setBlock(POS.west(), Blocks.AIR.defaultBlockState(), 3);
    level.setBlock(POS.east(), Blocks.AIR.defaultBlockState(), 3);
    for (var position : List.of(POS, POS.west(), POS.east()))
      level.setBlock(
          position.south(),
          trial.powered()
              ? Blocks.REDSTONE_BLOCK.defaultBlockState()
              : Blocks.AIR.defaultBlockState(),
          3);
    if (connected) {
      place(block, POS.west());
      place(block, POS.east());
    }
    place(block, POS);
    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    try {
      diameter = core(block).getXsize();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    caseNumber = index;
    save();
  }

  private void place(ConductorBlock block, BlockPos position) {
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(block.asItem(), 1));
    var context =
        new BlockPlaceContext(
            level,
            player,
            InteractionHand.MAIN_HAND,
            player.getMainHandItem(),
            new BlockHitResult(
                Vec3.atBottomCenterOf(position), Direction.UP, position.below(), false));
    ((net.minecraft.world.item.BlockItem) block.asItem()).place(context);
    check(
        level.getBlockState(position).is(block),
        "real item placement: " + block + " at " + position);
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (player == null || complete) return;
    if (index < 0) {
      if (++age >= 40) next();
      return;
    }
    age++;
    if (age == 20) {
      var trial = trials.get(index);
      boolean conductor = !(trial.block() instanceof RedstoneGateBlock) || trial.powered();
      check(
          (EnergyNet.instance.getTile(level, POS) != null) == conductor,
          "placed cable registration matches redstone state: " + index);
      if (trial.block() instanceof RedstoneGateBlock)
        check(
            level.getBlockState(POS).getValue(RedstoneGateBlock.active) == trial.powered(),
            "splitter placement honors real redstone signal: " + index);
      if (connected)
        check(
            level.getBlockState(POS).getValue(PipeBlock.WEST)
                && level.getBlockState(POS).getValue(PipeBlock.EAST),
            "placed cable connects to both neighbors: " + index);
      ready = true;
    }
    if (level.getBlockState(POS).isAir()) {
      if (destroyedAt < 0) destroyedAt = age;
      // Network changes and newly spawned drop entities are deferred.
      if (age - destroyedAt < 20) return;
      check(picked, "client targets visible cable edge: " + index);
      check(requested, "client sends mining action: " + index);
      check(
          actions.stream()
              .anyMatch(
                  a ->
                      a.get("case").equals(index)
                          && a.get("side").equals("server")
                          && a.get("action").equals("START")
                          && a.get("canceled").equals(false)),
          "server receives uncanceled mining action: " + index);
      check(
          EnergyNet.instance.getTile(level, POS) == null,
          "destroyed cable removed from energy network: " + index);
      check(
          level.getBlockState(POS.below()).is(Blocks.STONE),
          "backing floor survives mining: " + index);
      var trial = trials.get(index);
      var block = trial.block();
      int drops = player.getInventory().countItem(block.asItem());
      for (var item : level.getEntitiesOfClass(ItemEntity.class, new AABB(POS).inflate(3)))
        if (item.getItem().is(block.asItem())) drops += item.getItem().getCount();
      check(
          drops == (trial.creative() ? 0 : 1),
          "exact survival drop / no creative drop: " + index + " count=" + drops);
      if (connected) {
        check(
            level.getBlockState(POS.west()).is(block) && level.getBlockState(POS.east()).is(block),
            "neighbor cables survive mining: " + index);
        check(
            !level.getBlockState(POS.west()).getValue(PipeBlock.EAST)
                && !level.getBlockState(POS.east()).getValue(PipeBlock.WEST),
            "neighbor connections update after removal: " + index);
        boolean conductor = !(block instanceof RedstoneGateBlock) || trial.powered();
        check(
            (EnergyNet.instance.getTile(level, POS.west()) != null) == conductor
                && (EnergyNet.instance.getTile(level, POS.east()) != null) == conductor,
            "neighbor registration matches redstone state after mining: " + index);
      }
      cases.add(
          Map.of(
              "cable",
              BuiltInRegistries.BLOCK.getKey(block).toString(),
              "mode",
              trial.creative() ? "creative" : "survival",
              "layout",
              connected ? "connected_arm" : "isolated_core_edge",
              "powered",
              trial.powered(),
              "clientPickedCable",
              picked,
              "miningPacketSent",
              requested,
              "serverDestroyedCable",
              true,
              "drops",
              drops,
              "ticks",
              age));
      next();
    } else if (age >= 160) {
      var trial = trials.get(index);
      check(
          false,
          "client cannot mine visible cable: "
              + BuiltInRegistries.BLOCK.getKey(trial.block())
              + " creative="
              + trial.creative());
      cases.add(
          Map.of(
              "cable",
              BuiltInRegistries.BLOCK.getKey(trial.block()).toString(),
              "mode",
              trial.creative() ? "creative" : "survival",
              "layout",
              connected ? "connected_arm" : "isolated_core_edge",
              "clientPickedCable",
              picked,
              "miningPacketSent",
              requested,
              "serverDestroyedCable",
              false,
              "serverPosition",
              player.position().toString(),
              "clientDiagnostics",
              clientDiagnostics));
      // The first failed real target suffices to preserve the pre-fix reproduction.
      finish();
    }
  }

  @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
  public void mining(
      net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
    if (!event.getPos().equals(POS) || event.getAction().toString().equals("CLIENT_HOLD")) return;
    actions.add(
        Map.of(
            "case",
            index,
            "side",
            event.getLevel().isClientSide() ? "client" : "server",
            "action",
            event.getAction().toString(),
            "canceled",
            event.isCanceled(),
            "position",
            event.getEntity().position().toString(),
            "heldItem",
            event.getEntity().getMainHandItem().toString()));
  }

  public static final class Client {
    private int activeCase = -1;
    private boolean started, hidden;

    @SubscribeEvent
    public void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      var mc = net.minecraft.client.Minecraft.getInstance();
      // Keep this disposable verification client in the background.
      if (!hidden) {
        org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
        hidden = true;
      }
      if (done) {
        mc.stop();
        return;
      }
      if (!ready
          || mc.player == null
          || mc.level == null
          || mc.gameMode == null
          || mc.getSingleplayerServer() == null) return;
      if (caseNumber != activeCase) {
        activeCase = caseNumber;
        started = false;
      }
      var target =
          new Vec3(
              POS.getX() + .5 + (connected ? -.42 : diameter * .4),
              POS.getY() + .5,
              POS.getZ() + .5);
      var delta = target.subtract(mc.player.getEyePosition());
      mc.player.setYRot((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
      mc.player.setXRot(
          (float)
              -Math.toDegrees(
                  Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))));
      var hit = mc.player.pick(4.5, 1.0f, false);
      mc.hitResult = hit;
      clientDiagnostics =
          "position="
              + mc.player.position()
              + " creative="
              + mc.player.getAbilities().instabuild
              + " state="
              + mc.level.getBlockState(POS);
      if (hit instanceof BlockHitResult blockHit
          && hit.getType() == HitResult.Type.BLOCK
          && blockHit.getBlockPos().equals(POS)) {
        picked = true;
        if (!started) {
          requested = mc.gameMode.startDestroyBlock(POS, blockHit.getDirection());
          started = true;
        } else mc.gameMode.continueDestroyBlock(POS, blockHit.getDirection());
      }
    }
  }
}
