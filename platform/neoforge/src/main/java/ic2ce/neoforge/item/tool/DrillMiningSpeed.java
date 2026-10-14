package ic2ce.neoforge.item.tool;


import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * The native event already owns the miner, so speed compensation never scans another player's
 * inventory.
 */
@EventBusSubscriber(modid = "ic2")
public final class DrillMiningSpeed {
  private DrillMiningSpeed() {}

  @SubscribeEvent
  public static void speed(PlayerEvent.BreakSpeed event) {
    if (!(event.getEntity() instanceof Player player)) return;
    var held = player.getMainHandItem();
    if (!(held.getItem() instanceof MiningDrill drill)
        || drill.getDestroySpeed(held, event.getState()) <= 1
        || event.getNewSpeed() <= 0) return;
    float multiplier = 1;
    if (player.isEyeInFluid(FluidTags.WATER)) {
      var affinity =
          player
              .level()
              .registryAccess()
              .lookupOrThrow(Registries.ENCHANTMENT)
              .get(Enchantments.AQUA_AFFINITY)
              .orElse(null);
      if (affinity == null
          || EnchantmentHelper.getItemEnchantmentLevel(
                  affinity, player.getItemBySlot(EquipmentSlot.HEAD))
              == 0) multiplier *= 3;
    }
    if (!player.onGround()) multiplier *= 3;
    event.setNewSpeed(event.getNewSpeed() * multiplier);
  }
}
