package ic2ce.neoforge.model;

import com.mojang.serialization.MapCodec;
import ic2ce.api.item.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

/** Electrical range dispatch admits only a finite native ledger ratio in [0,1]. */
public record ChargeProperty() implements RangeSelectItemModelProperty {
  private static final ChargeProperty INSTANCE = new ChargeProperty();
  public static final MapCodec<ChargeProperty> CODEC = MapCodec.unit(INSTANCE);

  @Override
  public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
    if (stack.isEmpty() || !(stack.getItem() instanceof IElectricItem item)) return 0;
    double capacity = item.getMaxCharge(stack), charge = ElectricItem.manager.getCharge(stack);
    return !Double.isFinite(capacity) || capacity <= 0 || !Double.isFinite(charge)
        ? 0
        : (float) Math.clamp(charge / capacity, 0, 1);
  }

  @Override
  public MapCodec<ChargeProperty> type() {
    return CODEC;
  }
}
