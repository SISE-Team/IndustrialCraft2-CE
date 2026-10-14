package ic2ce.neoforge.item.armor;

import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.living.*;

/**
 * Real hurtServer sequences prove cancellation, invulnerability and the last electrical protection
 * bill.
 */
public final class ArmorDamageContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String text) {
    checks++;
    if (!ok) failures.add("armor damage: " + text);
  }

  public static Result run(ServerLevel world) {
    return new ArmorDamageContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var actor =
        FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("74ed53bc-1111-4111-8111-555500001515"), "ArmorDamageProbe"));
    actor.setGameMode(GameType.SURVIVAL);
    actor.connection.markClientLoaded();
    actor.getAbilities().instabuild = false;
    actor.getAbilities().invulnerable = false;
    actor.setInvulnerable(false);
    var pos = new BlockPos(1728, 150, 1300);
    world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
    world.getChunkAt(pos);
    actor.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
    for (var slot : EquipmentSlot.values()) actor.setItemSlot(slot, ItemStack.EMPTY);
    actor.removeAllEffects();
    actor.setHealth(20);
    actor.invulnerableTime = 0;
    var armor = new ItemStack(ItemDefinitions.NANO_CHESTPLATE);
    ElectricItem.manager.charge(armor, 5000, Integer.MAX_VALUE, true, false);
    actor.setItemSlot(EquipmentSlot.CHEST, armor);
    // FakePlayer.tick is intentionally empty; invoke the actual native equipment publication before
    // hurtServer.
    try {
      var publication =
          net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("detectEquipmentUpdates");
      publication.setAccessible(true);
      publication.invoke(actor);
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
    check(
        !world.damageSources().cactus().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR),
        "actual native test damage participates in the armor reduction stage");
    double charge = ElectricItem.manager.getCharge(armor);
    check(
        charge == 5000 && actor.getArmorValue() > 0,
        "actual worn charged equipment publishes its protection before a damage sequence (charge="
            + charge
            + ",armor="
            + actor.getArmorValue()
            + ")");
    Consumer<LivingIncomingDamageEvent> cancel =
        event -> {
          if (event.getEntity() == actor) event.setCanceled(true);
        };
    NeoForge.EVENT_BUS.addListener(cancel);
    try {
      boolean hurt = actor.hurtServer(world, world.damageSources().cactus(), 4);
      check(
          !hurt && ElectricItem.manager.getCharge(armor) == charge && actor.getHealth() == 20,
          "cancelled real native attack preserves charge and health");
    } finally {
      NeoForge.EVENT_BUS.unregister(cancel);
    }
    actor.setInvulnerable(true);
    check(
        !actor.hurtServer(world, world.damageSources().cactus(), 4)
            && ElectricItem.manager.getCharge(armor) == charge,
        "native invulnerability cannot trigger another armor bill");
    actor.setInvulnerable(false);
    float[] absorbed = {0};
    Consumer<LivingDamageEvent.Post> observe =
        event -> {
          if (event.getEntity() == actor)
            absorbed[0] = event.getReduction(DamageContainer.Reduction.ARMOR);
        };
    NeoForge.EVENT_BUS.addListener(observe);
    try {
      actor.invulnerableTime = 0;
      boolean hurt = actor.hurtServer(world, world.damageSources().cactus(), 4);
      check(
          hurt
              && absorbed[0] > 0
              && actor.getHealth() > 16
              && ElectricItem.manager.getCharge(armor) == 0,
          "last exact five-thousand-EU armor bill protects the current attack before discharge"
              + " (accepted="
              + hurt
              + ",armorReduction="
              + absorbed[0]
              + ",health="
              + actor.getHealth()
              + ",charge="
              + ElectricItem.manager.getCharge(armor)
              + ")");
    } finally {
      NeoForge.EVENT_BUS.unregister(observe);
    }
    ElectricItem.manager.charge(armor, 5000, Integer.MAX_VALUE, true, false);
    charge = ElectricItem.manager.getCharge(armor);
    check(
        !actor.hurtServer(world, world.damageSources().cactus(), 4)
            && ElectricItem.manager.getCharge(armor) == charge,
        "same-strength native hurt cooldown does not double-pay the completed attack");
    actor.invulnerableTime = 0;
    float health = actor.getHealth();
    actor.hurtServer(world, world.damageSources().fellOutOfWorld(), 1);
    check(
        ElectricItem.manager.getCharge(armor) == charge && actor.getHealth() < health,
        "armor-bypassing actual damage cannot spend electrical protection (health="
            + health
            + "->"
            + actor.getHealth()
            + ",charge="
            + charge
            + "->"
            + ElectricItem.manager.getCharge(armor)
            + ",bypass="
            + world
                .damageSources()
                .fellOutOfWorld()
                .is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)
            + ")");
    actor.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
    actor.setHealth(20);
    actor.invulnerableTime = 0;
    world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, false);
    return new Result(checks, List.copyOf(failures));
  }
}
