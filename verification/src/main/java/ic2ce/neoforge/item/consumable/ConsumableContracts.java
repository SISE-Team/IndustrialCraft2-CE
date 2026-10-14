package ic2ce.neoforge.item.consumable;

import ic2ce.api.item.ElectricItem;
import ic2ce.core.item.*;
import ic2ce.neoforge.damage.RadiationEffect;
import ic2ce.neoforge.registry.EntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import ic2ce.neoforge.vehicle.CarbonHull;
import ic2ce.neoforge.vehicle.ElectricHull;
import ic2ce.neoforge.vehicle.RubberHull;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;

/** Actual coffee/can quantities, medicinal and radioactive effects, independent native hulls. */
public final class ConsumableContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native consumables: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ConsumableContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var owner =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("815d4b35-51df-48ec-9886-51b132e10b62"), "IC2ConsumptionProbe"));
    owner.getInventory().clearContent();
    owner.getAbilities().instabuild = false;
    owner.setHealth(20);
    owner.removeAllEffects();
    for (Item item :
        List.of(
            ItemDefinitions.COLD_COFFEE_MUG,
            ItemDefinitions.DARK_COFFEE_MUG,
            ItemDefinitions.COFFEE_MUG)) {
      owner.removeAllEffects();
      var stack = new ItemStack(item);
      var remainder = item.finishUsingItem(stack, world, owner);
      check(
          remainder.is(ItemDefinitions.EMPTY_MUG)
              && remainder.getCount() == 1
              && owner.hasEffect(MobEffects.SPEED)
              && owner.hasEffect(MobEffects.HASTE),
          "each actual coffee recipe applies both stimulation effects and returns exactly one"
              + " physical mug: "
              + item);
    }
    owner.removeAllEffects();
    var many = new ItemStack(ItemDefinitions.COFFEE_MUG, 2);
    var remainder = many.getItem().finishUsingItem(many, world, owner);
    int empties = 0;
    for (int slot = 0; slot < owner.getInventory().getContainerSize(); slot++)
      if (owner.getInventory().getItem(slot).is(ItemDefinitions.EMPTY_MUG))
        empties += owner.getInventory().getItem(slot).getCount();
    check(
        remainder.is(ItemDefinitions.COFFEE_MUG) && remainder.getCount() == 1 && empties == 1,
        "even malformed stacked filled mugs conserve the unused drink and one empty container");
    owner.getInventory().clearContent();
    owner.getFoodData().setFoodLevel(17);
    var can = new ItemStack(ItemDefinitions.FILLED_TIN_CAN, 10);
    owner.setItemInHand(InteractionHand.MAIN_HAND, can);
    ((CannedFood) can.getItem()).use(world, owner, InteractionHand.MAIN_HAND);
    int cans = 0;
    for (int slot = 0; slot < owner.getInventory().getContainerSize(); slot++)
      if (owner.getInventory().getItem(slot).is(ItemDefinitions.TIN_CAN))
        cans += owner.getInventory().getItem(slot).getCount();
    check(
        owner.getFoodData().getFoodLevel() == 20 && can.getCount() == 7 && cans == 3,
        "physical instant canned food consumes exactly three cans to fill three hunger points and"
            + " returns every container");
    owner.removeAllEffects();
    owner.addEffect(new MobEffectInstance(MobEffects.POISON, 1000));
    RadiationEffect.radiation.applyTo(owner, 1200, 0);
    var wart = new ItemStack(ItemDefinitions.TERRA_WART);
    wart.getItem().finishUsingItem(wart, world, owner);
    check(
        !owner.hasEffect(MobEffects.POISON)
            && owner.getEffect(RadiationEffect.radiationHolder()).getDuration() == 600,
        "native medicinal wart removes poison and subtracts exactly six hundred radiation ticks");
    owner.removeAllEffects();
    var nuclear = new ItemStack(ItemDefinitions.URANIUM);
    ((RadioactiveMaterial) nuclear.getItem())
        .inventoryTick(nuclear, world, owner, EquipmentSlot.MAINHAND);
    check(
        owner.hasEffect(RadiationEffect.radiationHolder()),
        "actual exposed radioactive material applies native radiation");
    owner.removeAllEffects();
    owner.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemDefinitions.HAZMAT_HELMET));
    owner.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemDefinitions.HAZMAT_CHESTPLATE));
    owner.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemDefinitions.HAZMAT_LEGGINGS));
    owner.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemDefinitions.RUBBER_BOOTS));
    ((RadioactiveMaterial) nuclear.getItem())
        .inventoryTick(nuclear, world, owner, EquipmentSlot.MAINHAND);
    check(
        !owner.hasEffect(RadiationEffect.radiationHolder()),
        "the actual complete sealed native garments prevent inventory radiation");
    var rubber = new RubberHull(EntityDefinitions.RUBBER_BOAT, world);
    var carbon = new CarbonHull(EntityDefinitions.CARBON_BOAT, world);
    var electric = new ElectricHull(EntityDefinitions.ELECTRIC_BOAT, world);
    check(
        rubber.brokenByFalling()
            && !carbon.brokenByFalling()
            && !electric.brokenByFalling()
            && rubber.getExtraDropItemStack().is(ItemDefinitions.BROKEN_RUBBER_BOAT),
        "native hulls preserve distinct rubber fall/drop versus resistant carbon/electrical"
            + " policies");
    electric.setPos(10800, 100, 10800);
    world.setChunkForced(10800 >> 4, 10800 >> 4, true);
    world.getChunkAt(new net.minecraft.core.BlockPos(10800, 100, 10800));
    world.addFreshEntity(electric);
    var profile =
        new com.mojang.authlib.GameProfile(
            UUID.fromString("f0b8100a-b700-4028-b415-ece655603b70"), "IC2BoatDriver");
    var driver =
        new net.minecraft.server.level.ServerPlayer(
            world.getServer(),
            world,
            profile,
            net.minecraft.server.level.ClientInformation.createDefault());
    new net.minecraft.server.network.ServerGamePacketListenerImpl(
        world.getServer(),
        new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),
        driver,
        net.minecraft.server.network.CommonListenerCookie.createInitial(profile, false));
    driver.setPos(10800, 100, 10800);
    check(
        driver.startRiding(electric, true, false),
        "actual native server driver rides its electrical hull");
    ProjectRuntime.keyboard.processKeyUpdate(driver, 1 << 2);
    var battery = new ItemStack(ItemDefinitions.RE_BATTERY);
    ElectricItem.manager.charge(battery, 3, 1, true, false);
    driver.setItemSlot(EquipmentSlot.CHEST, battery);
    electric.tick();
    check(
        ElectricItem.manager.getCharge(battery) == 3,
        "electrical hull cannot spend a partial three-EU propulsion bill");
    ElectricItem.manager.charge(battery, 1, 1, true, false);
    electric.tick();
    check(
        ElectricItem.manager.getCharge(battery) == 0,
        "actual electrical-hull driver pays one full four-EU equipment bill");
    driver.stopRiding();
    ProjectRuntime.keyboard.processKeyUpdate(driver, 0);
    electric.discard();
    rubber.discard();
    carbon.discard();
    ProjectRuntime.keyboard.processKeyUpdate(owner, 0);
    owner.getInventory().clearContent();
    for (var slot : EquipmentSlot.values()) owner.setItemSlot(slot, ItemStack.EMPTY);
    owner.removeAllEffects();
    return new Result(checks, List.copyOf(failures));
  }
}
