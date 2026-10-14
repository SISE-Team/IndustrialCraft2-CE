package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.core.util.StackTag;
import ic2ce.core.util.StackUtil;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Real client block/entity interaction packets, including durability boundaries. */
public final class PainterVerification {
  private static final BlockPos POS = new BlockPos(72, 101, 72);

  private record Trial(
      DyeColor color, InteractionHand hand, boolean sheep, boolean creative, int exhaustion) {}

  private final List<Trial> trials = new ArrayList<>();
  private final List<String> failures = new ArrayList<>();
  private final List<Map<String, Object>> cases = new ArrayList<>();
  private ServerPlayer player;
  private ServerLevel level;
  private Sheep sheep;
  private int index = -1, age, checks;
  private boolean complete;
  private static volatile int current = -1, phase, entityId;
  private static volatile boolean entityTarget, done;
  private static volatile InteractionHand hand;
  private static volatile String clientError = "";
  private static volatile int clientActions;

  private static Item brush(DyeColor color) {
    return BuiltInRegistries.ITEM.getValue(
        Identifier.fromNamespaceAndPath("ic2", color.getName() + "_painter"));
  }

  private static ItemStack painted(DyeColor color, int damage, boolean refill) {
    var stack = new ItemStack(brush(color));
    stack.setDamageValue(damage);
    var tag = StackUtil.getOrCreateNbtData(stack);
    tag.putBoolean("autoRefill", refill);
    StackTag.set(stack, tag);
    return stack;
  }

  private void check(boolean ok, String message) {
    checks++;
    if (!ok) failures.add("case " + index + ": " + message);
  }

  private void save() {
    var report = new LinkedHashMap<String, Object>();
    report.put("checks", checks);
    report.put("cases", cases);
    report.put("failures", failures);
    report.put("complete", complete);
    report.put("clientError", clientError);
    try {
      Files.writeString(
          Path.of("ic2-painter-verification.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(report));
    } catch (Exception error) {
      throw new RuntimeException(error);
    }
  }

  private void finish() {
    complete = true;
    phase = 0;
    save();
    done = true;
  }

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (player != null || !(event.getEntity() instanceof ServerPlayer actor)) return;
    player = actor;
    level = player.level();
    for (var color : DyeColor.values())
      for (var selected : InteractionHand.values())
        for (boolean entity : List.of(false, true))
          trials.add(new Trial(color, selected, entity, false, 0));
    for (var selected : InteractionHand.values())
      for (boolean entity : List.of(false, true)) {
        trials.add(new Trial(DyeColor.RED, selected, entity, true, 0));
        for (int exhaustion = 1; exhaustion <= 3; exhaustion++)
          trials.add(new Trial(DyeColor.RED, selected, entity, false, exhaustion));
      }
    for (int x = 69; x <= 75; x++)
      for (int z = 69; z <= 75; z++) {
        level.setBlock(new BlockPos(x, 100, z), Blocks.STONE.defaultBlockState(), 3);
        for (int y = 101; y <= 104; y++)
          level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
      }
  }

  private void next() {
    index++;
    age = 0;
    phase = 0;
    clientActions = 0;
    if (index >= trials.size()) {
      finish();
      return;
    }
    var trial = trials.get(index);
    player.teleportTo(72.5, 101, 70.5);
    player.setDeltaMovement(Vec3.ZERO);
    player.setGameMode(trial.creative() ? GameType.CREATIVE : GameType.SURVIVAL);
    player.getInventory().clearContent();
    player.getInventory().setSelectedSlot(0);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
    player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STICK));
    player.setItemInHand(
        trial.hand(),
        painted(trial.color(), trial.exhaustion() > 0 ? 31 : 3, trial.exhaustion() >= 2));
    if (trial.exhaustion() == 3) player.getInventory().setItem(9, painted(trial.color(), 0, true));
    if (sheep != null) {
      sheep.discard();
      sheep = null;
    }
    DyeColor from = trial.color() == DyeColor.WHITE ? DyeColor.BLACK : DyeColor.WHITE;
    level.setBlock(
        POS,
        trial.sheep()
            ? Blocks.AIR.defaultBlockState()
            : BuiltInRegistries.BLOCK
                .getValue(Identifier.withDefaultNamespace(from.getName() + "_wool"))
                .defaultBlockState(),
        3);
    if (trial.sheep()) {
      sheep = EntityType.SHEEP.create(level, EntitySpawnReason.COMMAND);
      sheep.setPos(72.5, 101, 72.5);
      sheep.setNoAi(true);
      sheep.setInvulnerable(true);
      sheep.setColor(from);
      level.addFreshEntity(sheep);
      entityId = sheep.getId();
    }
    hand = trial.hand();
    entityTarget = trial.sheep();
    current = index;
    save();
  }

  private int emptyBrushes() {
    return player.getInventory().getNonEquipmentItems().stream()
            .filter(s -> s.is(ItemDefinitions.PAINTER))
            .mapToInt(ItemStack::getCount)
            .sum()
        + (player.getOffhandItem().is(ItemDefinitions.PAINTER)
            ? player.getOffhandItem().getCount()
            : 0);
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (player == null || complete) return;
    if (!clientError.isEmpty()) {
      check(false, "client interaction threw " + clientError);
      finish();
      return;
    }
    if (index < 0) {
      if (++age >= 40) next();
      return;
    }
    age++;
    if (age == 20) phase = 1;
    if (age == 35 && trials.get(index).exhaustion() == 0) phase = 2;
    if (age < 50) return;
    var trial = trials.get(index);
    boolean changed =
        trial.sheep()
            ? sheep.getColor() == trial.color()
            : level
                .getBlockState(POS)
                .is(
                    BuiltInRegistries.BLOCK.getValue(
                        Identifier.withDefaultNamespace(trial.color().getName() + "_wool")));
    check(
        clientActions == (trial.exhaustion() == 0 ? 2 : 1),
        "real client performed initial and applicable same-color action");
    check(changed, "server target has requested color");
    var held = player.getItemInHand(trial.hand());
    if (trial.exhaustion() == 0) {
      check(
          held.is(brush(trial.color())) && held.getDamageValue() == (trial.creative() ? 3 : 4),
          "single server durability charge; same color is free");
      check(emptyBrushes() == 0, "normal use creates no empty brush");
    } else if (trial.exhaustion() == 3) {
      check(
          held.is(brush(trial.color())) && held.getDamageValue() == 0,
          "last use installs reserve brush in the used hand");
      check(
          player.getInventory().getItem(9).isEmpty() && emptyBrushes() == 1,
          "refill consumes one reserve and returns one empty brush");
    } else {
      check(
          held.is(ItemDefinitions.PAINTER) && held.getCount() == 1,
          "last use returns one empty brush in the used hand");
      check(emptyBrushes() == 1, "exhaustion does not duplicate empty brush");
    }
    check(
        player
            .getItemInHand(
                trial.hand() == InteractionHand.MAIN_HAND
                    ? InteractionHand.OFF_HAND
                    : InteractionHand.MAIN_HAND)
            .is(Items.STICK),
        "unused hand is unchanged");
    cases.add(
        Map.of(
            "color",
            trial.color().getName(),
            "hand",
            trial.hand().toString(),
            "target",
            trial.sheep() ? "sheep" : "wool",
            "creative",
            trial.creative(),
            "exhaustion",
            trial.exhaustion(),
            "clientActions",
            clientActions,
            "serverColoredTarget",
            changed,
            "heldItem",
            held.toString(),
            "heldDamage",
            held.getDamageValue(),
            "emptyBrushes",
            emptyBrushes()));
    next();
  }

  public static final class Client {
    private int active = -1, usedPhase;
    private boolean hidden;

    @SubscribeEvent
    public void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      var mc = net.minecraft.client.Minecraft.getInstance();
      if (!hidden) {
        org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
        hidden = true;
      }
      if (done) {
        mc.stop();
        return;
      }
      if (phase == 0 || mc.player == null || mc.level == null || mc.gameMode == null) return;
      if (active != current) {
        active = current;
        usedPhase = 0;
      }
      if (usedPhase >= phase) return;
      try {
        if (entityTarget) {
          var target = mc.level.getEntity(entityId);
          if (!(target instanceof Sheep)) return;
          mc.gameMode.interact(
              mc.player,
              target,
              new EntityHitResult(target, target.position().add(0, .5, 0)),
              hand);
        } else
          mc.gameMode.useItemOn(
              mc.player,
              hand,
              new BlockHitResult(new Vec3(72.5, 101.5, 72), Direction.NORTH, POS, false));
        usedPhase = phase;
        clientActions++;
      } catch (Throwable error) {
        clientError = error.toString();
        usedPhase = phase;
      }
    }
  }
}
