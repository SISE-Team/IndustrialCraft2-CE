package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.api.item.ElectricItem;
import ic2ce.api.item.IElectricItem;
import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.damage.RadiationEffect;
import ic2ce.neoforge.item.armor.EquipmentItem;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.EnvProxy;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Diagnostic control for native API dispatch, tags, equipment and actual coffee use. */
public final class ReviewInventoryProbe {
  private final Map<String, Object> report = new LinkedHashMap<>();
  private final List<String> failures = new ArrayList<>();
  private final List<Map<String, Object>> drinks = new ArrayList<>();
  private ServerPlayer player;
  private int ticks, index = -1, age, checks;
  private float radiationHealth;
  private final List<Item> mugs =
      List.of(
          ItemDefinitions.EMPTY_MUG,
          ItemDefinitions.COLD_COFFEE_MUG,
          ItemDefinitions.DARK_COFFEE_MUG,
          ItemDefinitions.COFFEE_MUG);
  private static volatile boolean ready, done, used;
  private static volatile int current = -1;
  private static volatile InteractionHand hand;
  private static volatile String clientError = "";

  private void check(boolean passed, String name) {
    checks++;
    if (!passed) failures.add(name);
  }

  private void save() {
    report.put("checks", checks);
    report.put("failures", failures);
    report.put("drinks", drinks);
    try {
      Files.writeString(
          Path.of("ic2-review-inventory-probe.json"),
          new GsonBuilder().setPrettyPrinting().create().toJson(report));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent event) {
    if (player != null || !(event.getEntity() instanceof ServerPlayer actor)) return;
    player = actor;
    player.setGameMode(GameType.SURVIVAL);
    player.setInvulnerable(false);
    var items = new ArrayList<Map<String, Object>>();
    var armor = new ArrayList<Map<String, Object>>();
    for (var id : BuiltInRegistries.ITEM.keySet())
      if (id.getNamespace().equals("ic2")) {
        var item = BuiltInRegistries.ITEM.getValue(id);
        var stack = new ItemStack(item);
        var row = new LinkedHashMap<String, Object>();
        row.put("id", id.toString());
        row.put("class", item.getClass().getName());
        row.put("maxCount", stack.getMaxStackSize());
        row.put("maxDamage", stack.getMaxDamage());
        row.put("nativeUseDuration", stack.getUseDuration(player));
        if (item instanceof EquipmentItem suit) {
          row.put("armorSlot", suit.getArmorSlot().toString());
          var equipment = stack.get(DataComponents.EQUIPPABLE);
          row.put("hasEquipmentAsset", equipment != null && equipment.assetId().isPresent());
          check(
              equipment != null && equipment.assetId().isPresent(),
              id + " has an equipment model binding");
          if (suit.getArmorMaterial().durability() > 0) {
            int expected =
                ic2ce.neoforge.registry.ArmorProfiles.typeFor(suit.getArmorSlot())
                    .getDurability(suit.getArmorMaterial().durability());
            row.put("expectedDurability", expected);
            check(stack.getMaxDamage() == expected, id + " native slot durability");
          }
          if (item instanceof IElectricItem) {
            ElectricItem.manager.charge(stack, 1_000_000, Integer.MAX_VALUE, true, false);
            double[] protection = {0};
            stack.forEachModifier(
                suit.getArmorSlot(),
                (attribute, modifier) -> {
                  if (attribute.equals(Attributes.ARMOR)) protection[0] += modifier.amount();
                });
            row.put("chargedArmor", protection[0]);
            if (item instanceof ic2ce.neoforge.item.armor.NanoArmor
                || item instanceof ic2ce.neoforge.item.armor.QuantumArmor)
              check(
                  protection[0] == new int[] {3, 6, 8, 3}[suit.getArmorSlot().getIndex()],
                  id + " charged armor protection");
          }
          armor.add(row);
        }
        items.add(row);
      }
    report.put("items", items);
    report.put("armor", armor);
    report.put("registeredItems", items.size());
    check(items.size() == 508, "all 508 registered items inventoried");
    var ores = new ArrayList<Map<String, Object>>();
    for (String name :
        List.of(
            "tin_ore",
            "lead_ore",
            "uranium_ore",
            "deepslate_tin_ore",
            "deepslate_lead_ore",
            "deepslate_uranium_ore")) {
      var state =
          BuiltInRegistries.BLOCK.getValue(ProjectIds.id(name)).defaultBlockState();
      boolean tag = state.is(BlockTags.MINEABLE_WITH_PICKAXE);
      boolean drops = new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(state);
      ores.add(Map.of("id", name, "pickaxeTag", tag, "diamondToolCorrect", drops));
      check(tag && drops, name + " participates in native pickaxe harvesting");
    }
    report.put("ores", ores);
    var biomes = player.level().registryAccess().lookupOrThrow(Registries.BIOME);
    var climates = new ArrayList<Map<String, Object>>();
    for (var pair :
        List.of(
            Map.entry(Biomes.DESERT, ic2ce.api.crops.BiomeType.HOT),
            Map.entry(Biomes.DESERT, ic2ce.api.crops.BiomeType.SANDY),
            Map.entry(Biomes.SNOWY_PLAINS, ic2ce.api.crops.BiomeType.COLD),
            Map.entry(Biomes.JUNGLE, ic2ce.api.crops.BiomeType.JUNGLE),
            Map.entry(Biomes.SWAMP, ic2ce.api.crops.BiomeType.SWAMP),
            Map.entry(Biomes.FOREST, ic2ce.api.crops.BiomeType.FOREST),
            Map.entry(Biomes.PLAINS, ic2ce.api.crops.BiomeType.PLAINS))) {
      boolean result =
          EnvProxy.get().biomeHasType(biomes.getOrThrow(pair.getKey()), pair.getValue());
      climates.add(
          Map.of(
              "biome",
              pair.getKey().identifier().toString(),
              "type",
              pair.getValue().toString(),
              "result",
              result));
      check(result, pair + " biome type is available");
    }
    report.put("biomes", climates);
    var transport = ic2ce.neoforge.TransferReviewProbe.run(player.level());
    report.put("transport", transport);
    checks += ((Number) transport.get("checks")).intValue();
    failures.addAll((List<String>) transport.get("failures"));
    var nativeBehavior = NativeBehaviorReview.run(player);
    report.put("nativeBehavior", nativeBehavior);
    checks += ((Number) nativeBehavior.get("checks")).intValue();
    failures.addAll((List<String>) nativeBehavior.get("failures"));
    report.put("cropDispatch", CropDispatchProbe.run(player));
    radiationHealth = player.getHealth();
    RadiationEffect.radiation.applyTo(player, 200, 0);
    report.put("radiationHealthBefore", radiationHealth);
    save();
  }

  private void next() {
    ready = false;
    used = false;
    clientError = "";
    age = 0;
    index++;
    if (index == 8) {
      report.put("complete", failures.isEmpty());
      save();
      done = true;
      return;
    }
    player.stopUsingItem();
    player.removeAllEffects();
    player.getInventory().clearContent();
    player.getInventory().setSelectedSlot(0);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
    player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STICK));
    hand = index < 4 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    player.setItemInHand(hand, new ItemStack(mugs.get(index % 4)));
    current = index;
    save();
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    if (player == null || done) return;
    ticks++;
    if (index < 0) {
      if (ticks < 80) return;
      report.put("radiationHealthAfter", player.getHealth());
      check(player.getHealth() < radiationHealth, "radiation executes its native damage tick");
      var damageRows = ArmorDamageProbe.run(player);
      for (var row : damageRows)
        check(Boolean.TRUE.equals(row.get("passed")), "native electric armor damage " + row);
      report.put("nativeArmorDamage", damageRows);
      player.removeAllEffects();
      RadiationEffect.radiation.applyTo(player, 100, 0);
      ItemDefinitions.TERRA_WART.finishUsingItem(
          new ItemStack(ItemDefinitions.TERRA_WART), player.level(), player);
      check(
          !player.hasEffect(RadiationEffect.radiationHolder()),
          "terra wart removes short radiation with the registered holder");
      next();
      return;
    }
    age++;
    if (age == 20) ready = true;
    if (age < 100) return;
    check(used, "coffee interaction dispatched " + index);
    check(clientError.isEmpty(), "coffee client exception " + clientError);
    boolean empty = index % 4 == 0;
    boolean transformed = player.getItemInHand(hand).is(ItemDefinitions.EMPTY_MUG);
    boolean effect =
        player.hasEffect(net.minecraft.world.effect.MobEffects.SPEED)
            && player.hasEffect(net.minecraft.world.effect.MobEffects.HASTE);
    if (!empty)
      check(
          transformed && effect, "coffee completes, returns its mug, and applies effects " + index);
    else check(!effect, "empty mug applies no effects " + index);
    drinks.add(
        Map.of(
            "index",
            index,
            "hand",
            hand.toString(),
            "id",
            BuiltInRegistries.ITEM.getKey(mugs.get(index % 4)).toString(),
            "used",
            used,
            "clientError",
            clientError,
            "held",
            player.getItemInHand(hand).toString(),
            "speedAndHaste",
            effect));
    next();
  }

  public static final class Client {
    private boolean hidden, modelChecked;
    private int active = -1;

    @SubscribeEvent
    public void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      var mc = net.minecraft.client.Minecraft.getInstance();
      if (!hidden) {
        org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
        hidden = true;
      }
      if (!modelChecked && mc.player != null && mc.level != null) {
        ItemModelStateProbe.run(mc);
        modelChecked = true;
      }
      if (done) {
        mc.options.keyUse.setDown(false);
        mc.stop();
        return;
      }
      if (!ready) {
        mc.options.keyUse.setDown(false);
        return;
      }
      if (current == active || mc.player == null || mc.gameMode == null) return;
      active = current;
      try {
        mc.options.keyUse.setDown(current % 4 != 0);
        mc.gameMode.useItem(mc.player, hand);
      } catch (Throwable e) {
        clientError = e.toString();
      }
      used = true;
    }
  }
}
