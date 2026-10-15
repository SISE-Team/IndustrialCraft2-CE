package ic2ce.neoforge.item.armor;


import ic2ce.api.item.PropulsionEquipment;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.runtime.SideServices;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-authoritative chest thrust reads synchronized IC2 inputs and pays fuel before changing
 * movement.
 */
@EventBusSubscriber(modid = "ic2")
public final class WearablePropulsion {
  private WearablePropulsion() {}

  @SubscribeEvent
  public static void playerTick(PlayerTickEvent.Post event) {
    tick(event.getEntity());
  }

  public static void tick(Player owner) {
    if (owner.level().isClientSide()
        || !owner.isAlive()
        || owner.isSpectator()
        || owner.isPassenger()
        || owner.getAbilities().flying) return;
    var stack = owner.getItemBySlot(EquipmentSlot.CHEST);
    if (!(stack.getItem() instanceof PropulsionEquipment pack) || !pack.isJetpackActive(stack))
      return;
    var data = StackTag.get(stack);
    boolean hover = data != null && data.getBooleanOr("hoverMode", false);
    int cooldown = data == null ? 0 : Math.clamp(data.getIntOr("jetpackToggleTimer", 0), 0, 10);
    if (cooldown > 0) {
      int next = cooldown - 1;
      StackTag.update(
          stack,
          tag -> {
            if (next == 0) tag.remove("jetpackToggleTimer");
            else tag.putByte("jetpackToggleTimer", (byte) next);
            return tag;
          });
    } else if (SideServices.keyboard().isModeSwitchKeyDown(owner)
        && SideServices.keyboard().isJumpKeyDown(owner)) {
      hover = !hover;
      boolean value = hover;
      StackTag.update(
          stack,
          tag -> {
            tag.putBoolean("hoverMode", value);
            tag.putByte("jetpackToggleTimer", (byte) 10);
            return tag;
          });
    }
    boolean upwards = SideServices.keyboard().isJumpKeyDown(owner);
    if (!upwards && (!hover || owner.onGround())) return;
    double ceiling =
        owner.level().getMinY()
            + owner.level().getHeight() * Math.clamp(pack.getWorldHeightDivisor(stack), .1F, 1F);
    if (owner.getY() >= ceiling || pack.getChargeLevel(stack) <= 0) return;
    boolean creative = owner.getAbilities().instabuild;
    if (!creative && !pack.drainEnergy(stack, hover ? 1 : 2)) return;
    var motion = owner.getDeltaMovement();
    double power = Math.clamp(pack.getPower(stack), 0, 2);
    double vertical =
        hover
            ? (upwards
                ? pack.getHoverMultiplier(stack, true)
                : SideServices.keyboard().isSneakKeyDown(owner)
                    ? -pack.getHoverMultiplier(stack, false)
                    : 0)
            : Math.min(.6 * power, motion.y + .2 * power);
    double x = motion.x, z = motion.z;
    if (SideServices.keyboard().isForwardKeyDown(owner)) {
      var look = owner.getLookAngle();
      double horizontal = .08 * power;
      x += look.x * horizontal;
      z += look.z * horizontal;
    }
    owner.setDeltaMovement(x, vertical, z);
    owner.fallDistance = 0;
    owner.hurtMarked = true;
  }
}
