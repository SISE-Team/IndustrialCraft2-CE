package ic2ce.neoforge.item.armor;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.item.*;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Native equipment state, complete bills, distinct falls, optical effects and actual fuelled
 * movement.
 */
public final class ArmorContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native armor: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ArmorContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var manager = ElectricItem.manager;
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("0243e24f-1b71-4a1d-8ed2-4f58249c7006"), "IC2ArmorProbe"));
    player.getInventory().clearContent();
    for (var slot : EquipmentSlot.values()) player.setItemSlot(slot, ItemStack.EMPTY);
    player.getAbilities().instabuild = false;
    player.setHealth(20);
    ProjectRuntime.keyboard.processKeyUpdate(player, 0);
    for (Item item :
        List.of(
            ItemDefinitions.NANO_HELMET,
            ItemDefinitions.NANO_CHESTPLATE,
            ItemDefinitions.NANO_LEGGINGS,
            ItemDefinitions.NANO_BOOTS,
            ItemDefinitions.QUANTUM_HELMET,
            ItemDefinitions.QUANTUM_CHESTPLATE,
            ItemDefinitions.QUANTUM_LEGGINGS,
            ItemDefinitions.QUANTUM_BOOTS,
            ItemDefinitions.HAZMAT_HELMET,
            ItemDefinitions.HAZMAT_CHESTPLATE,
            ItemDefinitions.HAZMAT_LEGGINGS,
            ItemDefinitions.RUBBER_BOOTS,
            ItemDefinitions.NIGHT_VISION_GOGGLES,
            ItemDefinitions.JETPACK,
            ItemDefinitions.CF_PACK)) {
      var armor = (EquipmentItem) item;
      var stack = new ItemStack(item);
      check(
          stack.get(DataComponents.EQUIPPABLE).slot() == armor.getArmorSlot()
              && armor.getArmorAttributes() == armor.getArmorAttributes(),
          "actual registered equipment owns a native slot and stable immutable attributes: "
              + item);
    }
    var nano = charged(ItemDefinitions.NANO_BOOTS, 4999);
    var nanoItem = (NanoArmor) nano.getItem();
    check(
        !nanoItem.absorbFall(nano, player, 4) && manager.getCharge(nano) == 4999,
        "nano boots reject an incomplete fall bill without draining energy");
    manager.charge(nano, 1, 3, true, false);
    check(
        nanoItem.absorbFall(nano, player, 4) && manager.getCharge(nano) == 0,
        "nano boots pay exactly one damage at five thousand EU");
    manager.charge(nano, 1_000_000, 3, true, false);
    double before = manager.getCharge(nano);
    check(
        !nanoItem.absorbFall(nano, player, 11)
            && manager.getCharge(nano) == before
            && !nanoItem.absorbFall(nano, player, Float.NaN),
        "nano short-fall limit and malformed distance cannot spend charge");
    var quantum = charged(ItemDefinitions.QUANTUM_BOOTS, 40_000);
    var quantumItem = (QuantumArmor) quantum.getItem();
    check(
        quantumItem.absorbFall(quantum, player, 12) && manager.getCharge(quantum) == 0,
        "quantum boots independently absorb a longer fall with twenty-thousand-EU damage units");
    var chest = charged(ItemDefinitions.QUANTUM_CHESTPLATE, 7);
    var quantumChest = (QuantumArmor) chest.getItem();
    check(
        !quantumChest.drainEnergy(chest, 2) && manager.getCharge(chest) == 7,
        "quantum thrusters reject seven EU for an eight-EU complete bill");
    manager.charge(chest, 1, 4, true, false);
    check(
        quantumChest.drainEnergy(chest, 2) && manager.getCharge(chest) == 0,
        "quantum chest thrust pays base fuel plus six-EU overhead exactly once");
    check(
        !quantumItem.isJetpackActive(quantum) && !quantumItem.drainEnergy(quantum, 1),
        "quantum boots cannot impersonate a chest thruster");
    var tint = new ItemStack(ItemDefinitions.QUANTUM_HELMET);
    StackTag.update(
        tint,
        data -> {
          data.putString("owned", "retained");
          var display = new net.minecraft.nbt.CompoundTag();
          display.putInt("color", 0xAABBCC);
          display.putString("label", "retained");
          data.put("display", display);
          return data;
        });
    var tintItem = (QuantumArmor) tint.getItem();
    check(
        tintItem.getColor(tint) == 0xAABBCC, "quantum native color reads legacy display metadata");
    tintItem.setColor(tint, 0x123456);
    tintItem.clearColor(tint);
    check(
        tintItem.getColor(tint) == -1
            && StackTag.get(tint)
                .getCompoundOrEmpty("display")
                .getStringOr("label", "")
                .equals("retained")
            && StackTag.get(tint).getStringOr("owned", "").equals("retained"),
        "native dye removal preserves unrelated display and owned metadata");
    var boots = new ItemStack(ItemDefinitions.RUBBER_BOOTS);
    var rubber = (SealedGarment) boots.getItem();
    check(
        rubber.absorbFall(boots, player, 7)
            && boots.getDamageValue() == 2
            && !rubber.absorbFall(boots, player, 11),
        "rubber footwear has a physical 64-wear budget and its own short-fall bound");
    player.setItemSlot(EquipmentSlot.FEET, boots);
    player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemDefinitions.HAZMAT_LEGGINGS));
    player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemDefinitions.HAZMAT_CHESTPLATE));
    player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemDefinitions.HAZMAT_HELMET));
    check(
        IHazmatLike.hasCompleteHazmat(player),
        "the actual four sealed native equipment slots provide complete chemical protection");
    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    check(
        !IHazmatLike.hasCompleteHazmat(player),
        "missing head protection invalidates the sealed set");
    check(
        SealedGarment.hazmatAbsorbs(
                world.damageSources().source(ic2ce.neoforge.damage.DamageProfiles.ELECTRICITY_KEY))
            && !SealedGarment.hazmatAbsorbs(world.damageSources().generic()),
        "independently created native electricity source matches its registry key without"
            + " object-identity dependence");
    var helmet = new ItemStack(ItemDefinitions.NANO_HELMET);
    var helmetItem = (NanoArmor) helmet.getItem();
    player.setItemSlot(EquipmentSlot.HEAD, helmet);
    check(
        helmetItem.getHudMode(helmet) == HudMode.DISABLED && !StackTag.has(helmet),
        "reading a blank helmet HUD cannot manufacture persisted metadata");
    helmetItem.inventoryTick(helmet, world, player, EquipmentSlot.HEAD);
    check(!StackTag.has(helmet), "inactive worn nano helmet performs no component writes");
    StackTag.update(
        helmet,
        data -> {
          data.putShort("HudMode", (short) -1);
          return data;
        });
    check(
        helmetItem.getHudMode(helmet) == HudMode.ADVANCED,
        "negative saved HUD mode cannot throw an array-index exception");
    var goggles = new ItemStack(ItemDefinitions.NIGHT_VISION_GOGGLES);
    var gogglesItem = (NightVisionGoggles) goggles.getItem();
    player.setItemSlot(EquipmentSlot.HEAD, goggles);
    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200));
    gogglesItem.inventoryTick(goggles, world, player, EquipmentSlot.HEAD);
    check(
        player.getEffect(MobEffects.NIGHT_VISION).getDuration() == 1200 && !StackTag.has(goggles),
        "inactive goggles preserve a real potion night-vision effect and blank components");
    var pack = new ItemStack(ItemDefinitions.JETPACK);
    var jetpack = (BiogasJetpack) pack.getItem();
    jetpack.fillMb(pack, Ic2FluidStack.create(FluidDefinitions.BIOGAS.still, 1), false, null);
    check(
        !jetpack.drainEnergy(pack, 2)
            && jetpack.getCharge(pack) == 1
            && jetpack.drainEnergy(pack, 1)
            && jetpack.getCharge(pack) == 0,
        "biogas propulsion rejects partial fuel and spends an exact millibucket independently from"
            + " EU");
    check(
        jetpack.fillMb(
                pack,
                Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000),
                false,
                null)
            == 0,
        "biogas tank rejects another physical fluid");
    var foam = new ItemStack(ItemDefinitions.CF_PACK);
    var foamItem = (FoamReservoir) foam.getItem();
    foamItem.filltank(foam);
    check(
        foamItem.getCharge(foam) == 80_000
            && !(foam.getItem() instanceof ic2ce.api.item.PropulsionEquipment),
        "construction foam reservoir fills its separate eighty-bucket capacity and never adds"
            + " propulsion");
    var pos = new BlockPos(9472, 100, 9472);
    world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
    world.getChunkAt(pos);
    player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
    player.setOnGround(false);
    player.setDeltaMovement(Vec3.ZERO);
    player.fallDistance = 20;
    jetpack.fillMb(pack, Ic2FluidStack.create(FluidDefinitions.BIOGAS.still, 2), false, null);
    player.setItemSlot(EquipmentSlot.CHEST, pack);
    ProjectRuntime.keyboard.processKeyUpdate(player, 1 << 4);
    WearablePropulsion.tick(player);
    check(
        jetpack.getCharge(pack) == 0 && player.getDeltaMovement().y > 0 && player.fallDistance == 0,
        "native synchronized jump input performs real server movement only after the full biogas"
            + " thrust bill");
    player.setDeltaMovement(Vec3.ZERO);
    WearablePropulsion.tick(player);
    check(
        player.getDeltaMovement().equals(Vec3.ZERO),
        "empty biogas cannot repeat already paid propulsion");
    jetpack.fillMb(pack, Ic2FluidStack.create(FluidDefinitions.BIOGAS.still, 2), false, null);
    StackTag.update(
        pack,
        tag -> {
          tag.putBoolean("hoverMode", true);
          return tag;
        });
    ProjectRuntime.keyboard.processKeyUpdate(player, 0);
    player.setDeltaMovement(0, -.5, 0);
    WearablePropulsion.tick(player);
    check(
        jetpack.getCharge(pack) == 1 && player.getDeltaMovement().y == 0,
        "paid biogas hover arrests downward movement with its distinct one-millibucket bill");
    var poorChest = charged(ItemDefinitions.QUANTUM_CHESTPLATE, 7);
    player.setItemSlot(EquipmentSlot.CHEST, poorChest);
    ProjectRuntime.keyboard.processKeyUpdate(player, 1 << 4);
    player.setDeltaMovement(Vec3.ZERO);
    WearablePropulsion.tick(player);
    check(
        manager.getCharge(poorChest) == 7 && player.getDeltaMovement().equals(Vec3.ZERO),
        "real low-energy quantum thrust preserves both movement and the unpaid seven EU");
    player.removeAllEffects();
    player.setItemSlot(EquipmentSlot.HEAD, goggles);
    manager.charge(goggles, 10, 1, true, false);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        for (int y = -1; y <= 2; y++)
          world.setBlock(
              pos.offset(x, y, z),
              x == -2 || x == 2 || z == -2 || z == 2 || y == -1 || y == 2
                  ? Blocks.STONE.defaultBlockState()
                  : Blocks.AIR.defaultBlockState(),
              3);
    var darkWorld = world.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
    darkWorld.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
    darkWorld.getChunkAt(pos);
    var darkPlayer =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            darkWorld,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("98e6feb0-b940-4a36-af7e-c736c6c25be5"), "IC2OpticalProbe"));
    darkPlayer.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
    darkPlayer.setItemSlot(EquipmentSlot.HEAD, goggles);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        for (int y = -1; y <= 2; y++)
          darkWorld.setBlock(pos.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
    check(
        darkWorld.getMaxLocalRawBrightness(pos) <= 8,
        "the actual native dimension provides the goggles' required low light");
    ProjectRuntime.keyboard.processKeyUpdate(darkPlayer, 1 | (1 << 3));
    gogglesItem.inventoryTick(goggles, darkWorld, darkPlayer, EquipmentSlot.HEAD);
    check(
        StackTag.get(goggles).getBooleanOr("active", false)
            && darkPlayer.hasEffect(MobEffects.NIGHT_VISION)
            && manager.getCharge(goggles) == 9,
        "synchronized native mode input enables real night vision and spends exactly one EU");
    darkPlayer.removeAllEffects();
    darkPlayer.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    ProjectRuntime.keyboard.processKeyUpdate(darkPlayer, 0);
    ProjectRuntime.keyboard.processKeyUpdate(player, 0);
    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    before = manager.getCharge(goggles);
    gogglesItem.inventoryTick(goggles, world, player, EquipmentSlot.MAINHAND);
    check(
        manager.getCharge(goggles) == before,
        "unworn goggles cannot drain optical power or use another slot as the head");
    player.setItemSlot(EquipmentSlot.HEAD, goggles);
    player.removeAllEffects();
    manager.discharge(goggles, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false, false);
    gogglesItem.inventoryTick(goggles, world, player, EquipmentSlot.HEAD);
    check(
        !player.hasEffect(MobEffects.NIGHT_VISION),
        "an empty active pair of goggles cannot generate a new night-vision effect");
    player.removeAllEffects();
    for (var slot : EquipmentSlot.values()) player.setItemSlot(slot, ItemStack.EMPTY);
    player.getInventory().clearContent();
    ProjectRuntime.keyboard.processKeyUpdate(player, 0);
    for (var armorItem :
        List.of(
            ItemDefinitions.QUANTUM_HELMET,
            ItemDefinitions.QUANTUM_CHESTPLATE,
            ItemDefinitions.QUANTUM_LEGGINGS,
            ItemDefinitions.QUANTUM_BOOTS)) {
      var nativeStack = new ItemStack(armorItem);
      var data = new net.minecraft.nbt.CompoundTag();
      data.putString("sentinel", "keep");
      data.put("display", new net.minecraft.nbt.CompoundTag());
      ic2ce.neoforge.item.StackTag.set(nativeStack, data);
      var colorPolicy = (QuantumArmor) armorItem;
      colorPolicy.setColor(nativeStack, 0x2468ac);
      colorPolicy.clearColor(nativeStack);
      var remaining = ic2ce.neoforge.item.StackTag.get(nativeStack);
      check(
          !remaining.contains("display") && remaining.getStringOr("sentinel", "").equals("keep"),
          "native dye washing also prunes a preexisting empty legacy display without disturbing"
              + " independent data");
    }
    return new Result(checks, List.copyOf(failures));
  }

  private static ItemStack charged(Item item, double amount) {
    var result = new ItemStack(item);
    ElectricItem.manager.charge(result, amount, Integer.MAX_VALUE, true, false);
    return result;
  }
}
