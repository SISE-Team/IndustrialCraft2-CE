package ic2ce.neoforge.item.consumable;


import ic2ce.api.item.IHazmatLike;
import ic2ce.api.reactor.*;
import ic2ce.core.item.*;
import ic2ce.neoforge.damage.RadiationEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;

/**
 * Radioactive materials refresh a bounded native effect only as needed and respect actual complete
 * sealed equipment.
 */
public class RadioactiveMaterial extends Item implements IBaseReactorComponent {
  private final int radiationTicks, amplifier;

  public RadioactiveMaterial(Properties properties, int seconds, int amplifier) {
    super(properties);
    if (seconds < 0 || amplifier < 0)
      throw new IllegalArgumentException("Invalid radiation descriptor");
    radiationTicks = (int) Math.min(Integer.MAX_VALUE, (long) seconds * 20);
    this.amplifier = amplifier;
  }

  @Override
  public boolean canBePlacedIn(ItemStack stack, IReactor reactor) {
    return false;
  }

  @Override
  public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
    if (stack.isEmpty()
        || !(entity instanceof LivingEntity owner)
        || radiationTicks == 0
        || IHazmatLike.hasCompleteHazmat(owner)) return;
    var effect = owner.getEffect(RadiationEffect.radiationHolder());
    if (effect == null
        || effect.getAmplifier() < amplifier
        || effect.getDuration() < Math.max(1, radiationTicks - 20))
      RadiationEffect.radiation.applyTo(owner, radiationTicks, amplifier);
  }
}
