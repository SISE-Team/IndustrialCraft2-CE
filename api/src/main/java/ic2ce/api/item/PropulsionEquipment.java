package ic2ce.api.item;

import net.minecraft.world.item.ItemStack;

/**
 * Native propulsion contract uses a complete caller-requested bill, including the electric overhead
 * where applicable.
 */
public interface PropulsionEquipment {
  int EU_ENERGY_INCREASE = 6;

  boolean drainEnergy(ItemStack pack, int amount);

  float getPower(ItemStack pack);

  float getDropPercentage(ItemStack pack);

  double getChargeLevel(ItemStack pack);

  boolean isJetpackActive(ItemStack pack);

  float getHoverMultiplier(ItemStack pack, boolean upwards);

  float getWorldHeightDivisor(ItemStack pack);
}
