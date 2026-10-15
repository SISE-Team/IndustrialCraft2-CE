package ic2ce.verification;


import com.google.gson.GsonBuilder;
import ic2ce.api.item.ElectricItem;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.item.PhysicalWear;
import ic2ce.neoforge.item.armor.QuantumArmor;
import ic2ce.neoforge.item.tool.NanoBladeBase;
import ic2ce.neoforge.item.tool.PoweredTool;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.world.block.ResinLog;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Real client tool interactions plus explicitly labeled client/server API probes. */
public final class ToolInteractionVerification {
  private static final BlockPos POS = new BlockPos(80, 101, 80);

  private record Trial(String operation, InteractionHand hand, String condition) {}

  private final List<Trial> trials = new ArrayList<>();
  private final List<String> failures = new ArrayList<>();
  private final List<Map<String, Object>> cases = new ArrayList<>();
  private ServerPlayer player;
  private ServerLevel level;
  private Sheep sheep;
  private int index = -1, age, checks;
  private boolean complete;
  private double initialCharge;
  private int initialDamage;
  private Item tool;
  private String serverError = "";
  private static volatile int current = -1, entityId;
  private static volatile boolean ready, done, used;
  private static volatile String operation, condition, clientError = "", clientObservation = "";
  private static volatile InteractionHand hand;

  private void check(boolean ok, String message) {
    checks++;
    if (!ok) failures.add("case " + index + " " + operation + " " + condition + ": " + message);
  }

  private void save() {
    var report = new LinkedHashMap<String, Object>();
    report.put("checks", checks);
    report.put("cases", cases);
    report.put("failures", failures);
    report.put("finished", complete);
    report.put("complete", complete && failures.isEmpty());
    try {
      Files.writeString(
          Path.of("ic2-tool-interaction-verification.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(report));
    } catch (Exception error) {
      throw new RuntimeException(error);
    }
  }

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (player != null || !(event.getEntity() instanceof ServerPlayer actor)) return;
    player = actor;
    level = player.level();
    for (var selected : InteractionHand.values()) {
      for (String op : List.of("treetap", "cutter_add"))
        for (String mode : List.of("normal", "last", "creative"))
          trials.add(new Trial(op, selected, mode));
      for (String mode : List.of("charged", "exact", "empty", "disabled", "already", "creative"))
        trials.add(new Trial("chainsaw_sheep", selected, mode));
      for (String mode : List.of("charged", "exact", "empty"))
        trials.add(new Trial("electric_treetap", selected, mode));
      trials.add(new Trial("client_damage_api", selected, "normal"));
      trials.add(new Trial("client_energy_api", selected, "exact"));
      for (String mode : List.of("normal", "last"))
        trials.add(new Trial("server_damage_api", selected, mode));
      for (String op : List.of("nano_on", "nano_off", "nano_empty", "nano_shutdown_api"))
        trials.add(new Trial(op, selected, "normal"));
      for (String corrupt : List.of("unknown_block", "missing_tints"))
        trials.add(new Trial("obscurator_reset", selected, corrupt));
    }
    for (String mode : List.of("normal", "last", "stale", "stale_last"))
      trials.add(new Trial("cutter_remove", InteractionHand.MAIN_HAND, mode));
    for (String mode : List.of("charged", "exact", "disabled", "empty"))
      trials.add(new Trial("chainsaw_leaf", InteractionHand.MAIN_HAND, mode));
    for (String mode : List.of("normal", "last"))
      trials.add(new Trial("machine_damage_api", InteractionHand.MAIN_HAND, mode));
    for (String mode :
        List.of("normal", "last", "creative", "stale", "stale_last", "stale_creative"))
      trials.add(new Trial("axe_strip", InteractionHand.MAIN_HAND, mode));
    for (String armor : List.of("helmet", "chestplate", "leggings", "boots"))
      trials.add(new Trial("armor_color_api", InteractionHand.MAIN_HAND, armor));
    for (var selected : InteractionHand.values())
      for (String armor : List.of("nano", "quantum"))
        trials.add(new Trial("nano_armor_api", selected, armor));
    for (int x = 77; x <= 83; x++)
      for (int z = 77; z <= 83; z++) {
        level.setBlock(new BlockPos(x, 100, z), Blocks.STONE.defaultBlockState(), 3);
        for (int y = 101; y <= 104; y++)
          level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
      }
  }

  private void next() {
    index++;
    age = 0;
    ready = false;
    used = false;
    clientError = "";
    clientObservation = "";
    serverError = "";
    if (index >= trials.size()) {
      complete = true;
      save();
      done = true;
      return;
    }
    var trial = trials.get(index);
    operation = trial.operation();
    condition = trial.condition();
    hand = trial.hand();
    player.teleportTo(80.5, 101, 78.5);
    player.setDeltaMovement(Vec3.ZERO);
    player.setGameMode(condition.contains("creative") ? GameType.CREATIVE : GameType.SURVIVAL);
    player.getInventory().clearContent();
    player.getInventory().setSelectedSlot(0);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
    player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STICK));
    level
        .getEntitiesOfClass(ItemEntity.class, new AABB(POS).inflate(3))
        .forEach(ItemEntity::discard);
    if (sheep != null) {
      sheep.discard();
      sheep = null;
    }
    level.setBlock(POS, Blocks.AIR.defaultBlockState(), 3);
    tool =
        operation.startsWith("nano_")
            ? ItemDefinitions.NANO_SABER
            : operation.equals("axe_strip")
                ? Items.WOODEN_AXE
                : operation.startsWith("treetap")
                    ? ItemDefinitions.TREETAP
                    : operation.equals("electric_treetap")
                        ? ItemDefinitions.ELECTRIC_TREETAP
                        : operation.startsWith("chainsaw") || operation.equals("client_energy_api")
                            ? ItemDefinitions.CHAINSAW
                            : ItemDefinitions.CUTTER;
    if (operation.equals("armor_color_api"))
      tool =
          switch (condition) {
            case "helmet" -> ItemDefinitions.QUANTUM_HELMET;
            case "chestplate" -> ItemDefinitions.QUANTUM_CHESTPLATE;
            case "leggings" -> ItemDefinitions.QUANTUM_LEGGINGS;
            default -> ItemDefinitions.QUANTUM_BOOTS;
          };
    if (operation.equals("obscurator_reset")) tool = ItemDefinitions.OBSCURATOR;
    var stack = new ItemStack(tool);
    initialDamage =
        condition.contains("last")
            ? stack.getMaxDamage() - (operation.equals("cutter_remove") ? 3 : 1)
            : 3;
    if (tool instanceof ic2ce.api.item.IElectricItem) {
      initialDamage = 0;
      initialCharge =
          operation.equals("obscurator_reset")
              ? 10000
              : operation.startsWith("nano_")
                  ? (operation.equals("nano_empty") || operation.equals("nano_shutdown_api")
                      ? 0
                      : 10000)
                  : condition.equals("empty")
                      ? 0
                      : condition.equals("exact")
                          ? (tool == ItemDefinitions.CHAINSAW ? 100 : 50)
                          : 1000;
      ElectricItem.manager.charge(stack, initialCharge, Integer.MAX_VALUE, true, false);
      var tag = StackUtil.getOrCreateNbtData(stack);
      tag.putBoolean("disableShear", condition.equals("disabled"));
      tag.putBoolean("active", operation.equals("nano_off") || operation.equals("nano_armor_api"));
      StackTag.set(stack, tag);
    } else {
      initialCharge = 0;
      stack.setDamageValue(initialDamage);
    }
    player.setItemInHand(hand, stack);
    if (operation.equals("obscurator_reset")) {
      level.setBlock(POS, Blocks.STONE.defaultBlockState(), 3);
      StackTag.update(
          stack,
          tag -> {
            tag.putString(
                "refBlock",
                condition.equals("unknown_block")
                    ? "ic2:missing_verification_block"
                    : "minecraft:stone");
            tag.putString("refVariant", "");
            tag.putByte("refSide", (byte) 2);
            tag.putInt("refColorMul", 0xffffff);
            if (condition.equals("unknown_block"))
              tag.putIntArray("refColorMuls", new int[] {0xffffff});
            tag.putString("auditSentinel", "keep");
            return tag;
          });
    }
    if (operation.equals("treetap")
        || operation.equals("electric_treetap")
        || operation.equals("axe_strip"))
      level.setBlock(
          POS,
          BlockDefinitions.RUBBER_LOG
              .defaultBlockState()
              .setValue(ResinLog.stateProperty, ResinLog.RubberWoodState.wet_north),
          3);
    if (operation.equals("cutter_add")) {
      level.setBlock(POS, BlockDefinitions.COPPER_CABLE.defaultBlockState(), 3);
      player.getInventory().setItem(9, new ItemStack(ItemDefinitions.RUBBER, 2));
    }
    if (operation.equals("cutter_remove"))
      level.setBlock(POS, BlockDefinitions.INSULATED_COPPER_CABLE.defaultBlockState(), 3);
    if (condition.startsWith("stale")) {
      player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BREAD));
      player.startUsingItem(InteractionHand.OFF_HAND);
      player.stopUsingItem();
    }
    if (operation.equals("chainsaw_sheep")) {
      sheep = EntityType.SHEEP.create(level, EntitySpawnReason.COMMAND);
      sheep.setPos(80.5, 101, 80.5);
      sheep.setNoAi(true);
      sheep.setInvulnerable(true);
      sheep.setSheared(condition.equals("already"));
      level.addFreshEntity(sheep);
      entityId = sheep.getId();
    }
    if (operation.equals("nano_armor_api")) {
      sheep = EntityType.SHEEP.create(level, EntitySpawnReason.COMMAND);
      sheep.setPos(80.5, 101, 80.5);
      sheep.setNoAi(true);
      sheep.setInvulnerable(true);
      var armor =
          new ItemStack(
              condition.equals("nano")
                  ? ItemDefinitions.NANO_CHESTPLATE
                  : ItemDefinitions.QUANTUM_CHESTPLATE);
      ElectricItem.manager.charge(
          armor, condition.equals("nano") ? 48000 : 300000, Integer.MAX_VALUE, true, false);
      sheep.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, armor);
      level.addFreshEntity(sheep);
    }
    if (operation.equals("chainsaw_leaf"))
      level.setBlock(
          POS,
          Blocks.OAK_LEAVES
              .defaultBlockState()
              .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true),
          3);
    current = index;
    save();
  }

  private int quantity(Item item) {
    int total =
        player.getInventory().getNonEquipmentItems().stream()
                .filter(s -> s.is(item))
                .mapToInt(ItemStack::getCount)
                .sum()
            + (player.getOffhandItem().is(item) ? player.getOffhandItem().getCount() : 0);
    for (var drop : level.getEntitiesOfClass(ItemEntity.class, new AABB(POS).inflate(3)))
      if (drop.getItem().is(item)) total += drop.getItem().getCount();
    return total;
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
      if (operation.equals("armor_color_api")) {
        try {
          var stack = player.getItemInHand(hand);
          var armor = (QuantumArmor) tool;
          StackTag.update(
              stack,
              tag -> {
                tag.putString("auditSentinel", "keep");
                var display = new net.minecraft.nbt.CompoundTag();
                display.putString("customMarker", "keep");
                tag.put("display", display);
                return tag;
              });
          check(!armor.hasCustomColor(stack), "uncolored armor has no custom color");
          armor.setColor(stack, 0x123456);
          check(
              armor.hasCustomColor(stack) && armor.getColor(stack) == 0x123456,
              "first color persisted");
          var restored =
              StackTag.parse(
                  level.registryAccess(),
                  StackTag.save(
                      level.registryAccess(), stack, new net.minecraft.nbt.CompoundTag()));
          check(
              armor.getColor(restored) == 0x123456
                  && ElectricItem.manager.getCharge(restored) == initialCharge,
              "color and charge survive codec roundtrip");
          armor.setColor(stack, 0xabcdef);
          check(armor.getColor(stack) == 0xabcdef, "replacement color persisted");
          armor.clearColor(stack);
          check(
              !armor.hasCustomColor(stack) && armor.getColor(stack) == -1, "washing clears color");
          var tag = StackTag.get(stack);
          check(
              tag.getStringOr("auditSentinel", "").equals("keep")
                  && tag.getCompoundOrEmpty("display")
                      .getStringOr("customMarker", "")
                      .equals("keep"),
              "unrelated custom data preserved");
          StackTag.update(
              stack,
              root -> {
                root.getCompoundOrEmpty("display").remove("customMarker");
                return root;
              });
          armor.setColor(stack, 0x2468ac);
          armor.clearColor(stack);
          check(
              !StackTag.get(stack).contains("display"),
              "washing the sole display field removes its empty compound");
        } catch (Throwable error) {
          serverError = error.toString();
        }
        used = true;
      } else if (operation.equals("nano_shutdown_api") || operation.equals("nano_armor_api")) {
        try {
          var stack = player.getItemInHand(hand);
          StackTag.update(
              stack,
              tag -> {
                tag.putBoolean("active", true);
                return tag;
              });
          if (operation.equals("nano_shutdown_api"))
            check(
                !((PoweredTool) tool).consumeEnergy(stack, 64, player),
                "empty saber rejects energy consumption");
          else {
            initialCharge = ElectricItem.manager.getCharge(stack);
            ((NanoBladeBase) tool).hurtEnemy(stack, sheep, player);
            StackTag.update(
                stack,
                tag -> {
                  tag.putBoolean("active", false);
                  return tag;
                });
          }
        } catch (Throwable error) {
          serverError = error.toString();
        }
        used = true;
      } else if (operation.equals("server_damage_api")) {
        PhysicalWear.damage(player.getItemInHand(hand), 1, player, hand);
        used = true;
      } else if (operation.equals("machine_damage_api")) {
        var stack = player.getItemInHand(hand);
        boolean broken = PhysicalWear.damage(stack, 1, level, null, null);
        check(broken == condition.equals("last"), "entity-free wear reports threshold");
        check(
            stack.getCount() == 1 && stack.getDamageValue() == initialDamage + 1,
            "machine retains item for its own consumption");
        used = true;
      } else ready = true;
    }
    if (age < (operation.equals("nano_on") ? 110 : 50)) return;
    check(used, "client/server action executed");
    check(clientError.isEmpty(), "client exception: " + clientError);
    check(serverError.isEmpty(), "server exception: " + serverError);
    var held = player.getItemInHand(hand);
    double charge =
        tool instanceof ic2ce.api.item.IElectricItem ? ElectricItem.manager.getCharge(held) : 0;
    if (clientError.isEmpty() && serverError.isEmpty()) {
      if (operation.equals("treetap")) {
        check(
            level.getBlockState(POS).getValue(ResinLog.stateProperty)
                == ResinLog.RubberWoodState.dry_north,
            "resin extracted on server");
        check(
            quantity(ItemDefinitions.RESIN) >= 1 && quantity(ItemDefinitions.RESIN) <= 3,
            "one extraction drop range");
        wear(held, 1);
      } else if (operation.equals("cutter_add")) {
        check(
            level.getBlockState(POS).is(BlockDefinitions.INSULATED_COPPER_CABLE),
            "insulation applied");
        check(
            quantity(ItemDefinitions.RUBBER) == (condition.equals("creative") ? 2 : 1),
            "exact rubber cost");
        wear(held, 1);
      } else if (operation.equals("cutter_remove")) {
        check(
            level.getBlockState(POS).is(BlockDefinitions.COPPER_CABLE),
            "left click removes insulation without mining cable");
        check(quantity(ItemDefinitions.RUBBER) == 1, "one rubber returned");
        wear(held, 3);
      } else if (operation.equals("chainsaw_sheep")) {
        boolean success = !List.of("empty", "disabled", "already").contains(condition);
        check(sheep.isSheared() == (success || condition.equals("already")), "shearing state");
        check(
            quantity(Items.WHITE_WOOL) >= (success ? 1 : 0)
                && quantity(Items.WHITE_WOOL) <= (success ? 3 : 0),
            "single wool drop range");
        check(
            charge == initialCharge - (success && !condition.equals("creative") ? 100 : 0),
            "exact survival energy cost / failed shear is free / creative interaction restores"
                + " charge");
      } else if (operation.equals("electric_treetap")) {
        boolean success = !condition.equals("empty");
        check(
            level.getBlockState(POS).getValue(ResinLog.stateProperty)
                == (success
                    ? ResinLog.RubberWoodState.dry_north
                    : ResinLog.RubberWoodState.wet_north),
            "electric extraction state");
        check(charge == initialCharge - (success ? 50 : 0), "one electric extraction energy cost");
        check(
            quantity(ItemDefinitions.RESIN) >= (success ? 1 : 0)
                && quantity(ItemDefinitions.RESIN) <= (success ? 3 : 0),
            "electric resin drop range");
      } else if (operation.equals("chainsaw_leaf")) {
        check(level.getBlockState(POS).isAir(), "leaf removed");
        check(charge == Math.max(0, initialCharge - 100), "one mining/shearing energy cost");
        if (!List.of("empty", "disabled").contains(condition))
          check(quantity(Items.OAK_LEAVES) == 1, "one sheared leaf item");
      } else if (operation.equals("server_damage_api")) wear(held, 1);
      else if (operation.equals("axe_strip")) {
        check(
            level.getBlockState(POS).is(BlockDefinitions.STRIPPED_RUBBER_LOG),
            "rubber log stripped");
        check(
            quantity(ItemDefinitions.RESIN) >= 1 && quantity(ItemDefinitions.RESIN) <= 2,
            "one stripping resin drop range");
        check(
            clientObservation.equals("no_client_drops"),
            "client prediction spawns no local drop entities");
        wear(held, 1);
      } else if (operation.equals("client_damage_api"))
        check(
            held.getDamageValue() == initialDamage && clientObservation.equals("unchanged"),
            "client API probe leaves durability unchanged");
      else if (operation.equals("client_energy_api"))
        check(
            charge == initialCharge && clientObservation.equals("unchanged"),
            "client energy simulation does not consume charge");
      else if (operation.equals("nano_on")) {
        check(NanoBladeBase.isActive(held), "saber activation persisted");
        check(charge > 0 && charge < initialCharge, "active saber consumes idle energy");
      } else if (operation.equals("nano_off")) {
        check(!NanoBladeBase.isActive(held), "saber deactivation persisted");
        check(charge > 0 && charge <= initialCharge, "deactivation retains valid charge");
      } else if (operation.equals("nano_empty") || operation.equals("nano_shutdown_api")) {
        check(!NanoBladeBase.isActive(held), "empty saber is inactive");
        check(charge == 0, "empty saber charge remains zero");
      } else if (operation.equals("nano_armor_api")) {
        check(
            sheep.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty(),
            "exhausted armor cleared with an empty stack");
        check(charge == initialCharge - 2400, "single attack and armor-discharge energy cost");
      } else if (operation.equals("armor_color_api"))
        check(charge == initialCharge, "color operations preserve electric charge");
      else if (operation.equals("obscurator_reset")) {
        var tag = StackTag.get(held);
        check(
            List.of("refBlock", "refVariant", "refSide", "refColorMul", "refColorMuls").stream()
                .noneMatch(tag::contains),
            "all invalid scan fields cleared and saved");
        check(
            tag.getStringOr("auditSentinel", "").equals("keep"),
            "unrelated obscurator data preserved");
        check(charge == initialCharge, "invalid scan reset consumes no charge");
      }
      check(
          player
              .getItemInHand(
                  hand == InteractionHand.MAIN_HAND
                      ? InteractionHand.OFF_HAND
                      : InteractionHand.MAIN_HAND)
              .is(condition.startsWith("stale") ? Items.BREAD : Items.STICK),
          "other hand preserved");
    }
    var result = new LinkedHashMap<String, Object>();
    result.put("operation", operation);
    result.put("condition", condition);
    result.put("hand", hand.toString());
    result.put(
        "execution",
        operation.contains("api")
            ? (operation.startsWith("client") ? "client_api" : "server_api")
            : "real_client_packet");
    result.put("clientError", clientError);
    result.put("serverError", serverError);
    result.put("clientObservation", clientObservation);
    result.put("charge", charge);
    result.put("heldItem", held.toString());
    result.put("heldDamage", held.getDamageValue());
    cases.add(result);
    next();
  }

  private void wear(ItemStack held, int amount) {
    if (condition.contains("last"))
      check(
          quantity(tool) == 0, "one final tool consumed; picked-up drops may fill its empty slot");
    else
      check(
          held.is(tool)
              && held.getDamageValue()
                  == initialDamage + (condition.contains("creative") ? 0 : amount),
          "single durability cost");
  }

  public static final class Client {
    private int active = -1;
    private boolean issued, hidden;

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
      if (!ready || mc.player == null || mc.level == null || mc.gameMode == null) return;
      if (active != current) {
        active = current;
        issued = false;
      }
      try {
        if (operation.equals("chainsaw_sheep")) {
          if (issued) return;
          var target = mc.level.getEntity(entityId);
          if (!(target instanceof Sheep)) return;
          mc.gameMode.interact(
              mc.player,
              target,
              new EntityHitResult(target, target.position().add(0, .5, 0)),
              hand);
        } else if (operation.equals("client_damage_api")) {
          if (issued) return;
          var stack = mc.player.getItemInHand(hand);
          int before = stack.getDamageValue();
          boolean result = PhysicalWear.damage(stack, 1, mc.player, hand);
          clientObservation = !result && stack.getDamageValue() == before ? "unchanged" : "changed";
        } else if (operation.equals("client_energy_api")) {
          if (issued) return;
          var stack = mc.player.getItemInHand(hand);
          double before = ElectricItem.manager.getCharge(stack);
          PoweredTool.consumeEnergy(stack, 100, 1, mc.player);
          clientObservation =
              ElectricItem.manager.getCharge(stack) == before ? "unchanged" : "consumed";
        } else if (operation.equals("chainsaw_leaf")) {
          if (mc.level.getBlockState(POS).isAir()) return;
          if (!issued) mc.gameMode.startDestroyBlock(POS, Direction.NORTH);
          else mc.gameMode.continueDestroyBlock(POS, Direction.NORTH);
        } else if (operation.equals("cutter_remove")) {
          if (issued) return;
          if (condition.startsWith("stale")) {
            mc.player.startUsingItem(InteractionHand.OFF_HAND);
            mc.player.stopUsingItem();
          }
          mc.gameMode.startDestroyBlock(POS, Direction.NORTH);
          mc.gameMode.stopDestroyBlock();
        } else if (operation.startsWith("nano_")) {
          if (issued) return;
          mc.gameMode.useItem(mc.player, hand);
        } else {
          if (issued) return;
          if (condition.startsWith("stale")) {
            mc.player.startUsingItem(InteractionHand.OFF_HAND);
            mc.player.stopUsingItem();
          }
          mc.gameMode.useItemOn(
              mc.player,
              hand,
              new BlockHitResult(new Vec3(80.5, 101.5, 80), Direction.NORTH, POS, false));
          if (operation.equals("axe_strip"))
            clientObservation =
                mc.level.getEntitiesOfClass(ItemEntity.class, new AABB(POS).inflate(3)).isEmpty()
                    ? "no_client_drops"
                    : "client_drops";
        }
        issued = true;
        used = true;
      } catch (Throwable error) {
        clientError = error.toString();
        issued = true;
        used = true;
      }
    }
  }
}
