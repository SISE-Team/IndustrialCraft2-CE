package ic2.core.entity.boat;

import ic2.api.entity.boat.AbstractBoatEntity;
import ic2.api.entity.boat.BoatType;
import ic2.core.ref.Ic2BoatTypes;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RubberBoatEntity extends AbstractBoatEntity {
   public RubberBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world) {
      super(entityType, world, () -> ic2.core.ref.Ic2Items.RUBBER_BOAT);
   }

   public RubberBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world, double x, double y, double z) {
      super(entityType, world, () -> ic2.core.ref.Ic2Items.RUBBER_BOAT, x, y, z);
   }


   @Override
   public ItemStack getExtraDropItemStack() {
      return new ItemStack(Ic2Items.BROKEN_RUBBER_BOAT);
   }

   @Override
   public BoatType getOverrideBoatType() {
      return Ic2BoatTypes.RUBBER;
   }
}
