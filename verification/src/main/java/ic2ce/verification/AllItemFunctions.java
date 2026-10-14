package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.api.crops.*;
import ic2ce.api.item.*;
import ic2ce.core.item.*;
import ic2ce.neoforge.agriculture.CropPlot;
import ic2ce.neoforge.agriculture.item.EncodedSeed;
import ic2ce.neoforge.damage.RadiationEffect;
import ic2ce.neoforge.item.armor.EquipmentItem;
import ic2ce.neoforge.item.container.FixedCell;
import ic2ce.neoforge.item.energy.DisposableSupply;
import ic2ce.neoforge.item.energy.RechargeableSupply;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Native client packets for equipment, tools, boats, food, batteries, and every crop card. */
public final class AllItemFunctions {
  private record Trial(String id, String operation, InteractionHand hand, CropCard crop) {}

  private final List<Trial> trials = new ArrayList<>();
  private final List<Map<String, Object>> cases = new ArrayList<>();
  private final List<String> failures = new ArrayList<>();
  private final AllItemsVerification ledger;

  public AllItemFunctions() {
    this(new AllItemsVerification());
  }

  public AllItemFunctions(AllItemsVerification ledger) {
    this.ledger = ledger;
  }

  public static boolean finished() {
    return done;
  }

  private ServerPlayer player;
  private int index = -1, age, checks;
  private ItemStack original;
  private double beforeCharge;
  private Entity target;
  private static final BlockPos POS = new BlockPos(80, 200, 80);
  private static volatile int current = -1;
  private static volatile String expectedItem;
  private static volatile boolean ready, done, used;
  private static volatile String operation, error = "";
  private static volatile InteractionHand hand;
  private static volatile int entityId = -1;
  private final java.util.Map<String, Object> trace = new LinkedHashMap<>();
  private static volatile String clientResult = "";
  private static volatile int menuFrames;
  private static volatile boolean scanResultsSynced;
  private static volatile int obscuratorStage;
  private static volatile boolean wallMaterialPrinted;

  public List<String> failureList() {
    return List.copyOf(failures);
  }

  private void check(boolean value, String label) {
    checks++;
    if (!value) failures.add("case " + index + ": " + label);
  }

  private void save() {
    var report = new LinkedHashMap<String, Object>();
    report.put("checks", checks);
    report.put("cases", cases);
    report.put("failures", failures);
    report.put("finished", done);
    report.put("complete", done && failures.isEmpty());
    try {
      Files.writeString(
          Path.of("ic2-item-functions-verification.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(report));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (player != null || !(event.getEntity() instanceof ServerPlayer actor)) return;
    player = actor;
    player
        .level()
        .getServer()
        .getCommands()
        .performPrefixedCommand(
            player.level().getServer().createCommandSourceStack().withSuppressedOutput(),
            "weather clear");
    player.level().setRainLevel(0);
    player.level().setThunderLevel(0);
    player.setGameMode(GameType.SURVIVAL);
    player.setInvulnerable(true);
    for (var id : BuiltInRegistries.ITEM.keySet())
      if (id.getNamespace().equals("ic2")) {
        var item = BuiltInRegistries.ITEM.getValue(id);
        if (item instanceof EquipmentItem)
          trials.add(new Trial(id.getPath(), "equip", InteractionHand.MAIN_HAND, null));
        if (item instanceof RechargeableSupply || item instanceof DisposableSupply)
          for (var h : InteractionHand.values())
            trials.add(new Trial(id.getPath(), "battery", h, null));
      }
    for (String id :
        List.of(
            "bronze_pickaxe",
            "bronze_axe",
            "bronze_shovel",
            "drill",
            "diamond_drill",
            "iridium_drill",
            "chainsaw")) trials.add(new Trial(id, "mine", InteractionHand.MAIN_HAND, null));
    for (var h : InteractionHand.values()) trials.add(new Trial("bronze_hoe", "hoe", h, null));
    trials.add(new Trial("bronze_sword", "attack", InteractionHand.MAIN_HAND, null));
    for (String id : List.of("rubber_boat", "carbon_boat", "electric_boat"))
      for (var h : InteractionHand.values()) trials.add(new Trial(id, "boat", h, null));
    for (String id : List.of("filled_tin_can", "terra_wart"))
      for (var h : InteractionHand.values()) trials.add(new Trial(id, "eat", h, null));
    for (String id : List.of("scanner", "advanced_scanner", "tool_box"))
      for (var h : InteractionHand.values()) trials.add(new Trial(id, "menu", h, null));
    for (String id : List.of("wrench", "electric_wrench"))
      for (var h : InteractionHand.values()) trials.add(new Trial(id, "wrench", h, null));
    trials.add(new Trial("mining_laser", "laser", InteractionHand.MAIN_HAND, null));
    for (var h : InteractionHand.values()) {
      trials.add(new Trial("nano_saber", "saber_tick", h, null));
      trials.add(new Trial("obscurator", "obscurator", h, null));
      trials.add(new Trial("meter", "meter", h, null));
      trials.add(new Trial("wind_meter", "wind", h, null));
      trials.add(new Trial("debug_item", "debug", h, null));
      for (String op : List.of("frequency_record", "frequency_link", "frequency_clear"))
        trials.add(new Trial("frequency_transmitter", op, h, null));
    }
    for (var crop : Crops.instance.getCrops())
      if (crop != Crops.weed)
        for (var h : InteractionHand.values())
          trials.add(new Trial("crop_seed_bag", "plant", h, crop));
    for (String id : List.of("water_cell", "hydration_cell", "weed_ex_cell"))
      for (var h : InteractionHand.values()) trials.add(new Trial(id, "crop_cell", h, null));
    for (var row : FluidItemUsage.run(player)) {
      cases.add(row);
      boolean pass = Boolean.TRUE.equals(row.get("passed"));
      check(pass, "fluid item " + row);
      if (pass)
        ledger.mark(
            BuiltInRegistries.ITEM.getValue(
                net.minecraft.resources.Identifier.parse(row.get("id").toString())),
            "native_fluid_capability",
            row.toString());
    }
    for (var id : BuiltInRegistries.ITEM.keySet())
      if (id.getNamespace().equals("ic2")) {
        var item = BuiltInRegistries.ITEM.getValue(id);
        if (item instanceof FixedCell cell && cell.getCapacityMb(new ItemStack(item)) == 0)
          for (var h : InteractionHand.values())
            trials.add(new Trial(id.getPath(), "legacy_cell", h, null));
        if (item instanceof ic2ce.neoforge.item.consumable.RadioactiveMaterial)
          trials.add(new Trial(id.getPath(), "radioactive", InteractionHand.MAIN_HAND, null));
      }
    for (var h : InteractionHand.values()) trials.add(new Trial("crowbar", "crowbar", h, null));
    trials.add(
        new Trial(
            "remote_interface_upgrade", "unsupported_upgrade", InteractionHand.MAIN_HAND, null));
    String subset = System.getProperty("ic2.itemFunctionSubset", "all");
    if (!subset.equals("all"))
      trials.removeIf(
          t ->
              subset.equals("extra")
                  ? !List.of(
                          "meter",
                          "wind",
                          "debug",
                          "frequency_record",
                          "frequency_link",
                          "frequency_clear",
                          "saber_tick",
                          "obscurator",
                          "legacy_cell",
                          "radioactive",
                          "unsupported_upgrade",
                          "crowbar")
                      .contains(t.operation)
                  : !t.operation.equals(subset));
    trials.sort(Comparator.comparing(Trial::operation).thenComparing(Trial::id));
    next();
  }

  private void next() {
    ready = false;
    used = false;
    error = "";
    clientResult = "";
    trace.clear();
    menuFrames = 0;
    scanResultsSynced = false;
    obscuratorStage = 0;
    wallMaterialPrinted = false;
    age = 0;
    if (target != null) {
      target.discard();
      target = null;
    }
    index++;
    if (index >= trials.size()) {
      done = true;
      save();
      ledger.flush();
      return;
    }
    var trial = trials.get(index);
    operation = trial.operation;
    hand = trial.hand;
    current = index;
    expectedItem = "ic2:" + trial.id;
    var server = player.level().getServer();
    server
        .getCommands()
        .performPrefixedCommand(
            server.createCommandSourceStack().withSuppressedOutput(), "time set noon");
    player.closeContainer();
    player.setShiftKeyDown(false);
    player.stopUsingItem();
    player.removeAllEffects();
    player.getInventory().clearContent();
    for (var slot : EquipmentSlot.values()) player.setItemSlot(slot, ItemStack.EMPTY);
    player.getInventory().setSelectedSlot(0);
    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket(0));
    player.teleportTo(80.5, 200, 78.5);
    player.connection.teleport(80.5, 200, 78.5, 0, operation.equals("crop_cell") ? 30 : 55);
    player.setDeltaMovement(Vec3.ZERO);
    for (int x = -4; x <= 4; x++)
      for (int z = -4; z <= 4; z++) {
        for (int y = 0; y <= 5; y++)
          player.level().setBlock(POS.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        player.level().setBlock(POS.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
      }
    for (var item : player.level().getEntitiesOfClass(ItemEntity.class, new AABB(POS).inflate(6)))
      item.discard();
    for (var boat : player.level().getEntitiesOfClass(Boat.class, new AABB(POS).inflate(6)))
      boat.discard();
    original = AllMachinesVerification.stack(trial.id, 1);
    if (BuiltInRegistries.ITEM.getValue(ProjectRuntime.getIdentifier(trial.id))
        instanceof IElectricItem)
      ElectricItem.manager.charge(original, 1_000_000_000, Integer.MAX_VALUE, true, false);
    beforeCharge = ElectricItem.manager.getCharge(original);
    player.setItemInHand(hand, original);
    player.setItemInHand(
        hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
        new ItemStack(Items.STICK));
    if (operation.equals("legacy_cell"))
      player.level().setBlock(POS, Blocks.STONE.defaultBlockState(), 3);
    if (operation.equals("unsupported_upgrade"))
      player.level().setBlock(POS, BlockDefinitions.MACERATOR.defaultBlockState(), 3);
    if (operation.equals("saber_tick"))
      player.getInventory().setItem(1, new ItemStack(ItemDefinitions.NANO_SABER));
    if (operation.equals("obscurator")) {
      player.setShiftKeyDown(true);
      player.level().setBlock(POS, Blocks.STONE.defaultBlockState(), 3);
      player.level().setBlock(POS.east(3), BlockDefinitions.WHITE_WALL.defaultBlockState(), 3);
    }
    if (operation.equals("battery")) {
      player.getInventory().setItem(1, new ItemStack(ItemDefinitions.DRILL));
    }
    if (operation.equals("mine")) {
      Block block =
          trial.id.equals("bronze_axe") || trial.id.equals("chainsaw")
              ? Blocks.OAK_LOG
              : trial.id.equals("bronze_shovel") ? Blocks.DIRT : Blocks.STONE;
      player.level().setBlock(POS, block.defaultBlockState(), 3);
    }
    if (operation.equals("hoe")) {
      player.level().setBlock(POS, Blocks.DIRT.defaultBlockState(), 3);
      player.level().setBlock(POS.south(3).below(), Blocks.WATER.defaultBlockState(), 3);
    }
    if (operation.equals("boat")) {
      for (int x = -3; x <= 3; x++)
        for (int z = 0; z <= 3; z++)
          player.level().setBlock(POS.offset(x, -1, z), Blocks.WATER.defaultBlockState(), 3);
    }
    if (operation.equals("eat")) {
      player.getFoodData().setFoodLevel(10);
      if (trial.id.equals("filled_tin_can")) original.setCount(4);
      else RadiationEffect.radiation.applyTo(player, 400, 0);
    }
    if (operation.equals("plant") || operation.equals("crop_cell")) {
      player
          .level()
          .setBlock(
              POS.below(),
              Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7),
              3);
      player
          .level()
          .setBlock(
              POS,
              BlockDefinitions.CROP_STICK
                  .defaultBlockState()
                  .setValue(ic2ce.neoforge.block.MachineBlockType.CROSSING_BASE, false),
              3);
      var bed = (CropPlot) player.level().getBlockEntity(POS);
      bed.setStorageWater(100);
      bed.setStorageNutrients(100);
      bed.setStorageWeedEX(100);
      if (operation.equals("plant") && trial.crop.getId().equals("red_wheat")) {
        for (int x = -4; x <= 4; x++)
          for (int z = -4; z <= 4; z++)
            player.level().setBlock(POS.offset(x, 2, z), Blocks.STONE.defaultBlockState(), 3);
        var light = POS.west(6);
        player.level().setBlock(light.below(), Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(light, Blocks.TORCH.defaultBlockState(), 3);
      }
      if (operation.equals("plant")) {
        original = EncodedSeed.generateItemStackFromValues(trial.crop, 4, 5, 6, 4);
        player.setItemInHand(hand, original);
      } else {
        var crop = (CropPlot) player.level().getBlockEntity(POS);
        crop.setStorageWater(0);
        crop.setStorageWeedEX(0);
      }
    }
    if (operation.equals("menu") && !trial.id.equals("tool_box")) {
      player.level().setBlock(POS, Blocks.IRON_ORE.defaultBlockState(), 3);
      player.level().setBlock(POS.east(), Blocks.IRON_ORE.defaultBlockState(), 3);
    }
    if (operation.equals("wrench")) {
      player.level().setBlock(POS, BlockDefinitions.GENERATOR.defaultBlockState(), 3);
      var tile = (ic2ce.neoforge.block.NativeBlockEntity) player.level().getBlockEntity(POS);
      ledger.phases.put("wrenchInitial", tile.getFacing().getName());
    }
    if (operation.equals("meter") || operation.equals("debug") || operation.equals("crowbar")) {
      player.level().setBlock(POS, BlockDefinitions.GENERATOR.defaultBlockState(), 3);
      if (operation.equals("debug"))
        ic2ce.core.util.StackTag.update(
            original,
            n -> {
              n.putInt("mode", 2);
              return n;
            });
    }
    if (operation.startsWith("frequency")) {
      player.setShiftKeyDown(true);
      player.level().setBlock(POS, BlockDefinitions.TELEPORTER.defaultBlockState(), 3);
      player.level().setBlock(POS.west(3), BlockDefinitions.TELEPORTER.defaultBlockState(), 3);
      if (!operation.equals("frequency_record"))
        ic2ce.core.util.StackTag.update(
            original,
            n -> {
              n.putBoolean("targetSet", true);
              n.putBoolean("targetJustSet", false);
              n.putInt("targetX", POS.getX() - 3);
              n.putInt("targetY", POS.getY());
              n.putInt("targetZ", POS.getZ());
              return n;
            });
    }
    if (operation.equals("attack")) {
      var cow = EntityType.COW.create(player.level(), EntitySpawnReason.COMMAND);
      cow.setPos(80.5, 200, 80.5);
      cow.setNoAi(true);
      player.level().addFreshEntity(cow);
      target = cow;
      entityId = cow.getId();
    }
    save();
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (player == null || done) return;
    age++;
    if (operation.equals("obscurator")
        && obscuratorStage == 0
        && age >= 40
        && ic2ce.neoforge.item.tool.SurfaceImprinter.getState(
                ic2ce.core.util.StackUtil.getOrCreateNbtData(player.getItemInHand(hand)))
            != null) {
      player.setShiftKeyDown(false);
      obscuratorStage = 1;
    }
    if (age == (index == 0 ? 60 : 20)) ready = true;
    if (age < (index == 0 ? 160 : 100) || (!used && age < 300)) return;
    var trial = trials.get(index);
    boolean passed = used && error.isEmpty();
    check(used, "native client action issued");
    check(error.isEmpty(), "client exception " + error);
    var held = player.getItemInHand(hand);
    String evidence = "";
    check(
        player
            .getItemInHand(
                hand == InteractionHand.MAIN_HAND
                    ? InteractionHand.OFF_HAND
                    : InteractionHand.MAIN_HAND)
            .is(Items.STICK),
        "unused hand is preserved");
    switch (operation) {
      case "equip" -> {
        var armor =
            (EquipmentItem) BuiltInRegistries.ITEM.getValue(ProjectRuntime.getIdentifier(trial.id));
        passed &=
            player
                    .getItemBySlot(armor.getArmorSlot())
                    .is(BuiltInRegistries.ITEM.getValue(ProjectRuntime.getIdentifier(trial.id)))
                && held.isEmpty();
        evidence = "worn=" + player.getItemBySlot(armor.getArmorSlot());
      }
      case "battery" -> {
        double charged = ElectricItem.manager.getCharge(player.getInventory().getItem(1));
        passed &= charged > 0;
        evidence = "drillCharge=" + charged + ", source=" + held;
      }
      case "mine" -> {
        int drops =
            player.level().getEntitiesOfClass(ItemEntity.class, new AABB(POS).inflate(5)).size();
        for (int j = 0; j < player.getInventory().getContainerSize(); j++) {
          var loot = player.getInventory().getItem(j);
          if (loot.is(Items.COBBLESTONE) || loot.is(Items.OAK_LOG) || loot.is(Items.DIRT))
            drops += loot.getCount();
        }
        int damage = held.getDamageValue();
        double charge = ElectricItem.manager.getCharge(held);
        passed &=
            player.level().isEmptyBlock(POS)
                && drops > 0
                && (held.getItem() instanceof IElectricItem ? charge < beforeCharge : damage > 0);
        evidence = "drops=" + drops + ", damage=" + damage + ", charge=" + charge;
      }
      case "hoe" -> {
        passed &=
            player.level().getBlockState(POS).is(Blocks.FARMLAND) && held.getDamageValue() == 1;
        evidence = player.level().getBlockState(POS).toString();
      }
      case "attack" -> {
        passed &=
            target instanceof LivingEntity living
                && living.getHealth() < living.getMaxHealth()
                && held.getDamageValue() > 0;
        evidence =
            "targetHealth="
                + ((LivingEntity) target).getHealth()
                + ", damage="
                + held.getDamageValue();
      }
      case "boat" -> {
        var boats = player.level().getEntitiesOfClass(Boat.class, new AABB(POS).inflate(7));
        passed &= boats.size() == 1 && held.isEmpty();
        evidence = "boats=" + boats.stream().map(b -> b.getClass().getName()).toList();
      }
      case "eat" -> {
        passed &=
            held.isEmpty()
                && (trial.id.equals("terra_wart")
                    ? !player.hasEffect(RadiationEffect.radiationHolder())
                    : player.getFoodData().getFoodLevel() >= 14);
        evidence =
            "food="
                + player.getFoodData().getFoodLevel()
                + ", radiation="
                + player.hasEffect(RadiationEffect.radiationHolder());
      }
      case "menu" -> {
        passed &= player.containerMenu != player.inventoryMenu && menuFrames >= 15;
        if (!trial.id.equals("tool_box")) passed &= scanResultsSynced;
        evidence =
            "container="
                + player.containerMenu.getClass().getName()
                + ", frames="
                + menuFrames
                + ", clientReceivedIronOre="
                + scanResultsSynced;
      }
      case "plant" -> {
        var tile = player.level().getBlockEntity(POS);
        passed &=
            tile instanceof CropPlot crop
                && crop.getCrop() != null
                && crop.getCrop().getId().equals(trial.crop.getId())
                && crop.getStatGrowth() == 4
                && crop.getStatGain() == 5
                && crop.getStatResistance() == 6
                && held.isEmpty();
        evidence =
            "crop="
                + trial.crop.getId()
                + ", actual="
                + (tile instanceof CropPlot c && c.getCrop() != null ? c.getCrop().getId() : "none")
                + ", held="
                + held
                + ", hand="
                + hand;
      }
      case "crop_cell" -> {
        var crop = (CropPlot) player.level().getBlockEntity(POS);
        passed &=
            trial.id.equals("weed_ex_cell")
                ? crop.getStorageWeedEX() > 0
                : crop.getStorageWater() > 0;
        evidence =
            "water="
                + crop.getStorageWater()
                + ", weedEx="
                + crop.getStorageWeedEX()
                + ", held="
                + held;
      }
      case "wrench" -> {
        var tile = (ic2ce.neoforge.block.NativeBlockEntity) player.level().getBlockEntity(POS);
        passed &= tile.getFacing() == Direction.EAST;
        evidence = "facing=" + tile.getFacing();
      }
      case "crowbar" -> {
        passed &=
            held.is(ItemDefinitions.CROWBAR)
                && held.getDamageValue() == 0
                && player.level().getBlockState(POS).is(BlockDefinitions.GENERATOR);
        evidence = "native use safely rejects a non-cover-holder and preserves both hands";
      }
      case "legacy_cell" -> {
        passed &=
            held.is(original.getItem())
                && held.getCount() == 1
                && player.level().getBlockState(POS).is(Blocks.STONE);
        evidence = "non-fluid material cell returns safely without changing world or stack";
      }
      case "radioactive" -> {
        passed &=
            player.hasEffect(RadiationEffect.radiationHolder()) && held.is(original.getItem());
        evidence =
            "normal inventory ticks apply radiation="
                + player.hasEffect(RadiationEffect.radiationHolder());
      }
      case "unsupported_upgrade" -> {
        var m = (ic2ce.neoforge.processing.solid.OreGrinder) player.level().getBlockEntity(POS);
        var upgrade = (ic2ce.neoforge.item.upgrade.MachineUpgrade) held.getItem();
        boolean rejected = !m.upgradeSlot.accepts(held);
        passed &= rejected && held.getCount() == 1;
        evidence =
            "upstream RemotelyAccessible absent; native input rejects unsupported upgrade="
                + rejected;
      }
      case "saber_tick" -> {
        passed &=
            ic2ce.neoforge.item.tool.NanoBladeBase.isActive(held)
                && ElectricItem.manager.getCharge(held) < beforeCharge
                && !ic2ce.neoforge.item.tool.NanoBladeBase.isActive(
                    player.getInventory().getItem(1));
        evidence =
            "active saber drains with idle spare present: EU="
                + (beforeCharge - ElectricItem.manager.getCharge(held));
      }
      case "obscurator" -> {
        var tile = player.level().getBlockEntity(POS.east(3));
        passed &=
            wallMaterialPrinted
                && obscuratorStage == 1
                && tile instanceof ic2ce.neoforge.block.NativeBlockEntity t
                && t.hasComponent(ic2ce.neoforge.world.surface.SurfaceMap.class)
                && t.getComponent(ic2ce.neoforge.world.surface.SurfaceMap.class).hasObscuration()
                && ElectricItem.manager.getCharge(held) == beforeCharge - 25000;
        evidence =
            "recorded and applied face, stage="
                + obscuratorStage
                + ", EU cost="
                + (beforeCharge - ElectricItem.manager.getCharge(held))
                + ", target="
                + tile
                + ", native stone face rendered="
                + wallMaterialPrinted;
      }
      case "meter" -> {
        passed &=
            player.containerMenu instanceof ic2ce.neoforge.handheld.menu.EnergyMeterMenu
                && menuFrames >= 15;
        evidence = "native energy meter frames=" + menuFrames;
      }
      case "wind" -> {
        passed &= ElectricItem.manager.getCharge(held) == beforeCharge - 50;
        evidence = "wind reading EU cost=" + (beforeCharge - ElectricItem.manager.getCharge(held));
      }
      case "debug" -> {
        passed &= clientResult.contains("BaseGen:");
        evidence = "received debug chat=" + clientResult;
      }
      case "frequency_record" -> {
        var data = ic2ce.core.util.StackTag.get(held);
        passed &=
            data != null
                && data.getBooleanOr("targetSet", false)
                && data.getIntOr("targetX", 0) == POS.getX();
        evidence = "recorded target=" + data;
      }
      case "frequency_link" -> {
        var a = (ic2ce.neoforge.utility.LinkedTeleporter) player.level().getBlockEntity(POS);
        var b =
            (ic2ce.neoforge.utility.LinkedTeleporter) player.level().getBlockEntity(POS.west(3));
        passed &=
            a.hasTarget()
                && a.getTarget().equals(POS.west(3))
                && b.hasTarget()
                && b.getTarget().equals(POS);
        evidence = "bidirectional link=" + a.getTarget() + " / " + b.getTarget();
      }
      case "frequency_clear" -> {
        var data = ic2ce.core.util.StackTag.get(held);
        passed &= data != null && !data.getBooleanOr("targetSet", true);
        evidence = "unlinked=" + data;
      }
      case "laser" -> {
        passed &= ElectricItem.manager.getCharge(held) < beforeCharge;
        evidence = "charge=" + ElectricItem.manager.getCharge(held);
      }
    }
    if (operation.equals("plant") || operation.equals("hoe"))
      evidence += "; trace=" + trace + "; clientResult=" + clientResult;
    check(passed, trial.id + " " + operation + " " + evidence);
    cases.add(
        Map.of(
            "id",
            "ic2:" + trial.id,
            "operation",
            operation,
            "hand",
            hand.toString(),
            "evidence",
            evidence,
            "passed",
            passed,
            "clientError",
            error));
    if (passed)
      ledger.mark(
          BuiltInRegistries.ITEM.getValue(ProjectRuntime.getIdentifier(trial.id)),
          "real_client_" + operation,
          evidence);
    save();
    next();
  }

  @SubscribeEvent
  public void trace(
      net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
    if (player != null
        && event.getEntity() == player
        && (operation.equals("plant") || operation.equals("hoe"))) {
      var tile = player.level().getBlockEntity(POS);
      trace.put("held", event.getItemStack().toString());
      trace.put(
          "canTill",
          event
              .getItemStack()
              .canPerformAction(net.neoforged.neoforge.common.ItemAbilities.HOE_TILL));
      trace.put("above", player.level().getBlockState(POS.above()).toString());
      trace.put("serverY", player.getY());
      trace.put("card", String.valueOf(Crops.instance.getCropCard(event.getItemStack())));
      trace.put("position", event.getPos().toString());
      if (tile instanceof CropPlot crop) {
        trace.put("crossing", crop.isCrossingBase());
        trace.put("light", crop.getLightLevel());
        var card = Crops.instance.getCropCard(event.getItemStack());
        trace.put("canGrow", card != null && card.canGrow(crop));
      }
    }
  }

  public static final class Client {
    private boolean hidden;
    private int active = -1;
    private boolean issued;
    private int stage = -1, stageWait;

    @SubscribeEvent
    public void pre(net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) {
      var mc = net.minecraft.client.Minecraft.getInstance();
      if (active != current
          || !ready
          || done
          || !operation.equals("eat")
          || mc.player == null
          || !mc.player.isUsingItem()) mc.options.keyUse.setDown(false);
    }

    @SubscribeEvent
    public void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      var mc = net.minecraft.client.Minecraft.getInstance();
      if (!hidden) {
        org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
        hidden = true;
      }
      if (active != current) {
        active = current;
        issued = false;
        stage = -1;
        mc.options.keyUse.setDown(false);
        if (mc.player != null) mc.player.stopUsingItem();
        mc.setScreen(null);
      }
      if (done) {
        mc.options.keyUse.setDown(false);
        if (Boolean.getBoolean("ic2.verifyAllItems")) return;
        if (!System.getProperty("ic2.itemFunctionSubset", "all").equals("extra")
            || AdditionalItemUsage.finished && CanningRecipeVerification.finished) mc.stop();
        return;
      }
      if (!ready) {
        mc.options.keyUse.setDown(false);
        return;
      }
      if (mc.player == null || mc.gameMode == null || mc.level == null) return;
      mc.options.keyShift.setDown(operation.equals("obscurator") && obscuratorStage == 0);
      if (operation.equals("obscurator") && stage != obscuratorStage) {
        stage = obscuratorStage;
        issued = false;
        stageWait = 0;
      }
      if (operation.equals("obscurator") && obscuratorStage == 1 && !issued && stageWait++ < 10) {
        mc.player.input.keyPresses = net.minecraft.world.entity.player.Input.EMPTY;
        mc.player.connection.send(
            new net.minecraft.network.protocol.game.ServerboundPlayerInputPacket(
                net.minecraft.world.entity.player.Input.EMPTY));
        return;
      }
      if (!issued
          && !BuiltInRegistries.ITEM
              .getKey(mc.player.getItemInHand(hand).getItem())
              .toString()
              .equals(expectedItem)) return;
      if (mc.screen
          instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>)
        menuFrames++;
      if (mc.player.containerMenu instanceof ic2ce.neoforge.handheld.menu.ScannerMenu scanner
          && scanner.scanResults != null)
        scanResultsSynced =
            scanner.scanResults.stream().anyMatch(row -> row.a.is(Items.IRON_ORE) && row.b >= 2);
      if (operation.equals("obscurator") && obscuratorStage == 1) {
        var p = POS.east(3);
        var state = mc.level.getBlockState(p);
        var parts =
            new ArrayList<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>();
        mc.getModelManager()
            .getBlockStateModelSet()
            .get(state)
            .collectParts(mc.level, p, state, net.minecraft.util.RandomSource.create(1), parts);
        wallMaterialPrinted =
            parts.stream()
                .flatMap(part -> part.getQuads(Direction.NORTH).stream())
                .anyMatch(
                    q ->
                        q.materialInfo()
                            .sprite()
                            .contents()
                            .name()
                            .toString()
                            .equals("minecraft:block/stone"));
      }
      if (operation.equals("debug") && issued) {
        try {
          var f =
              net.minecraft.client.gui.components.ChatComponent.class.getDeclaredField(
                  "allMessages");
          f.setAccessible(true);
          for (var message :
              (List<net.minecraft.client.multiplayer.chat.GuiMessage>) f.get(mc.gui.getChat()))
            if (message.content().getString().contains("BaseGen:"))
              clientResult = message.content().getString();
        } catch (Exception e) {
          error = e.toString();
        }
      }
      try {
        if (operation.equals("mine")) {
          if (mc.level.getBlockState(POS).isAir()) return;
          if (!issued) mc.gameMode.startDestroyBlock(POS, Direction.NORTH);
          else mc.gameMode.continueDestroyBlock(POS, Direction.NORTH);
        } else {
          if (issued) return;
          boolean shift =
              operation.startsWith("frequency")
                  || operation.equals("obscurator") && obscuratorStage == 0;
          mc.player.input.keyPresses =
              new net.minecraft.world.entity.player.Input(
                  false, false, false, false, false, shift, false);
          mc.player.setShiftKeyDown(shift);
          if (operation.equals("hoe")
              || operation.equals("wrench")
              || operation.equals("plant")
              || operation.equals("meter")
              || operation.equals("debug")
              || operation.equals("crowbar")
              || operation.equals("frequency_record")
              || operation.equals("frequency_link")
              || operation.equals("obscurator"))
            clientResult =
                mc.gameMode
                    .useItemOn(
                        mc.player,
                        hand,
                        new BlockHitResult(
                            Vec3.atCenterOf(
                                operation.equals("obscurator") && obscuratorStage == 1
                                    ? POS.east(3)
                                    : POS),
                            operation.equals("wrench")
                                ? Direction.EAST
                                : operation.equals("obscurator") ? Direction.NORTH : Direction.UP,
                            operation.equals("obscurator") && obscuratorStage == 1
                                ? POS.east(3)
                                : POS,
                            false))
                    .toString();
          else if (operation.equals("attack")) {
            var target = mc.level.getEntity(entityId);
            if (target == null) return;
            mc.gameMode.attack(mc.player, target);
          } else {
            mc.options.keyUse.setDown(
                operation.equals("eat")
                    && mc.player.getItemInHand(hand).is(ItemDefinitions.TERRA_WART));
            mc.gameMode.useItem(mc.player, hand);
          }
        }
        issued = true;
        used = true;
      } catch (Throwable e) {
        error = e.toString();
        issued = true;
        used = true;
      }
    }
  }
}
