package ic2.forge.model;

import com.mojang.serialization.MapCodec;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

/** Modern replacement for the original ic2:charge item override predicate. */
public record ChargeProperty() implements RangeSelectItemModelProperty {
   public static final MapCodec<ChargeProperty> CODEC = MapCodec.unit(new ChargeProperty());

   @Override
   public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
      if (!(stack.getItem() instanceof IElectricItem item)) {
         return 0;
      }
      double capacity = item.getMaxCharge(stack);
      return capacity > 0 ? (float)(ElectricItem.manager.getCharge(stack) / capacity) : 0;
   }

   @Override
   public MapCodec<ChargeProperty> type() {
      return CODEC;
   }
}
