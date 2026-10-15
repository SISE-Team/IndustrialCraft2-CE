package ic2ce.neoforge.item.armor;

import ic2ce.api.item.*;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Worn-head controls write only changed state; short owned night vision never deletes potion
 * effects.
 */
public final class HelmetControls {
  private HelmetControls() {}

  public static HudMode hud(ItemStack stack) {
    var data = StackTag.get(stack);
    return HudMode.getFromID(data == null ? 0 : data.getIntOr("HudMode", 0));
  }

  public static void tick(ItemStack stack, Player owner, String activeKey, boolean hud) {
    var data = StackTag.get(stack);
    boolean enabled = data != null && data.getBooleanOr(activeKey, false);
    int timer = data == null ? 0 : Math.clamp(data.getIntOr("toggleTimer", 0), 0, 10);
    if (timer > 0) {
      int next = timer - 1;
      StackTag.update(
          stack,
          tag -> {
            if (next == 0) tag.remove("toggleTimer");
            else tag.putByte("toggleTimer", (byte) next);
            return tag;
          });
    } else if (ProjectRuntime.keyboard.isAltKeyDown(owner)) {
      if (ProjectRuntime.keyboard.isModeSwitchKeyDown(owner)) {
        enabled = !enabled;
        boolean active = enabled;
        StackTag.update(
            stack,
            tag -> {
              tag.putBoolean(activeKey, active);
              tag.putByte("toggleTimer", (byte) 10);
              return tag;
            });
        ProjectRuntime.sideProxy.messagePlayer(
            owner, enabled ? "ic2.nightvision.enabled" : "ic2.nightvision.disabled");
      } else if (hud && ProjectRuntime.keyboard.isHudModeKeyDown(owner)) {
        var next = HudMode.getFromID(hud(stack).getID() + 1);
        StackTag.update(
            stack,
            tag -> {
              tag.putShort("HudMode", (short) next.getID());
              tag.putByte("toggleTimer", (byte) 10);
              return tag;
            });
        ProjectRuntime.sideProxy.messagePlayer(owner, next.getTranslationKey());
      }
    }
    if (!enabled
        || owner.level().getMaxLocalRawBrightness(owner.blockPosition()) > 8
        || !ElectricItem.manager.use(stack, 1, owner)) return;
    var effect = owner.getEffect(MobEffects.NIGHT_VISION);
    if (effect == null || effect.getDuration() < 220 && effect.isAmbient())
      owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, true, false));
  }
}
