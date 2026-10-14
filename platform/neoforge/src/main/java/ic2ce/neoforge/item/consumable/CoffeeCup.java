package ic2ce.neoforge.item.consumable;

import ic2ce.core.item.*;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * Coffee types own different stimulation ceilings, durations and overdose thresholds with one
 * physical mug remainder.
 */
public class CoffeeCup extends Item {
  public final MugType mugType;

  public CoffeeCup(Properties properties, MugType type) {
    super(properties);
    mugType = Objects.requireNonNull(type);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
    if (world.isClientSide()
        || mugType == MugType.empty
        || !(user instanceof Player owner)
        || !stack.is(this)) return stack;
    int ceiling = mugType == MugType.cold_coffee ? 1 : mugType == MugType.dark_coffee ? 5 : 6;
    int duration = mugType == MugType.cold_coffee ? 600 : 1200;
    int intensity =
        Math.max(
            stimulate(owner, MobEffects.SPEED, ceiling, duration),
            stimulate(owner, MobEffects.HASTE, ceiling, duration));
    if (mugType == MugType.coffee) intensity -= 2;
    if (intensity >= 3) {
      owner.addEffect(new MobEffectInstance(MobEffects.NAUSEA, (intensity - 2) * 200));
      if (intensity >= 4)
        owner.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, intensity - 3));
    }
    if (owner.getAbilities().instabuild) return stack;
    stack.shrink(1);
    var empty = new ItemStack(ItemDefinitions.EMPTY_MUG);
    if (stack.isEmpty()) return empty;
    if (!owner.getInventory().add(empty)) owner.drop(empty, false);
    return stack;
  }

  private static int stimulate(Player owner, Holder<MobEffect> effect, int ceiling, int added) {
    var previous = owner.getEffect(effect);
    int amplitude = previous == null ? 0 : Math.min(ceiling, previous.getAmplifier() + 1);
    int duration =
        previous == null
            ? 300
            : (int) Math.min(Integer.MAX_VALUE, (long) previous.getDuration() + added);
    owner.addEffect(new MobEffectInstance(effect, duration, amplitude));
    return previous == null ? 1 : amplitude;
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity user) {
    return mugType == MugType.empty ? 0 : 32;
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack stack) {
    return mugType == MugType.empty ? ItemUseAnimation.NONE : ItemUseAnimation.DRINK;
  }

  @Override
  public InteractionResult use(Level world, Player owner, InteractionHand hand) {
    if (mugType == MugType.empty || !owner.getItemInHand(hand).is(this))
      return InteractionResult.PASS;
    owner.startUsingItem(hand);
    return InteractionResult.CONSUME;
  }

  public enum MugType {
    empty,
    cold_coffee,
    dark_coffee,
    coffee;
  }
}
