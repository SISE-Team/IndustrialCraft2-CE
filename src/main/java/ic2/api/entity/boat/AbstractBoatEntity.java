package ic2.api.entity.boat;

import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.item.Item;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * IC2 boats. The 1.19.2 implementation re-implemented boat physics through
 * reflection into vanilla private fields; 26.1.2 restructured those internals,
 * so this port rides the vanilla tick loop and keeps IC2's drop behaviour.
 */
public abstract class AbstractBoatEntity extends Boat {
   protected boolean isExtraItemDropped = false;

   public AbstractBoatEntity(EntityType<? extends Boat> entityType, Level world, java.util.function.Supplier<Item> dropItem) {
      super(entityType, world, dropItem);
   }

   public AbstractBoatEntity(
      EntityType<? extends Boat> entityType, Level world, java.util.function.Supplier<Item> dropItem, double x, double y, double z
   ) {
      this(entityType, world, dropItem);
      this.setPos(x, y, z);
      this.xo = x;
      this.yo = y;
      this.zo = z;
   }

   public ItemStack getExtraDropItemStack() {
      return ItemStack.EMPTY;
   }

   public abstract BoatType getOverrideBoatType();

   public boolean brokenByFalling() {
      return true;
   }

   public boolean canFloatOn(FluidState fluidState) {
      return fluidState.is(FluidTags.WATER);
   }

   protected SoundEvent getPaddleSound() {
      return SoundEvents.BOAT_PADDLE_WATER;
   }

   @Override
   public void tick() {
      super.tick();
      List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(0.2F, -0.01F, 0.2F), EntitySelector.pushableBy(this));
      if (!list.isEmpty()) {
         boolean bl = !this.level().isClientSide() && !(this.getControllingPassenger() instanceof Player);

         for (Entity entity : list) {
            if (!entity.hasPassenger(this)) {
               if (bl
                  && this.getPassengers().size() < this.getMaxPassengers()
                  && !entity.isPassenger()
                  && entity.getBbWidth() < this.getBbWidth()
                  && entity instanceof LivingEntity
                  && !(entity instanceof WaterAnimal)
                  && !(entity instanceof Player)) {
                  entity.startRiding(this);
               } else {
                  this.push(entity);
               }
            }
         }
      }
   }

   // 26.1.2: vanilla boat drops use destroy(ServerLevel, Item); keep IC2's extra broken-boat drop there.
   @Override
   public void destroy(net.minecraft.server.level.ServerLevel level, Item dropItem) {
      if (!this.isExtraItemDropped && level.getGameRules().get(GameRules.ENTITY_DROPS)) {
         ItemStack extra = this.getExtraDropItemStack();
         if (!extra.isEmpty()) {
            this.isExtraItemDropped = true;
            this.spawnAtLocation(level, extra);
         }
      }
      super.destroy(level, dropItem);
   }

   @Override
   public void resetFallDistance() {
      super.resetFallDistance();
      this.isExtraItemDropped = false;
   }
}
