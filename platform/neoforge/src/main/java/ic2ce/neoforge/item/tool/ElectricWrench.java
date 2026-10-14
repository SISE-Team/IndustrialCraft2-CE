package ic2ce.neoforge.item.tool;

import ic2ce.api.item.ElectricItem;
import ic2ce.api.item.IBoxable;
import ic2ce.core.item.PriorityUsableItem;
import ic2ce.neoforge.registry.SoundDefinitions;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Electric wrench reserves exact 100/1000 EU before native rotation/removal and refunds rejected
 * operations.
 */
public class ElectricWrench extends PoweredTool implements PriorityUsableItem, IBoxable {
  public ElectricWrench(Properties properties) {
    super(properties, 100);
    tier = 1;
    maxCharge = 12000;
    transferLimit = 250;
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    var player = context.getPlayer();
    if (player == null || !stack.is(this)) return InteractionResult.PASS;
    boolean creative = player.getAbilities().instabuild;
    if (!creative && !canTakeDamage(stack, 1)) return InteractionResult.FAIL;
    var payment =
        new WrenchActions.Payment() {
          @Override
          public boolean reserve(int units) {
            if (creative) return true;
            double requested = units * 100.0;
            double paid =
                ElectricItem.manager.discharge(stack, requested, tier, true, false, false);
            if (paid + 1e-8 >= requested) return true;
            if (paid > 0) ElectricItem.manager.charge(stack, paid, tier, true, false);
            return false;
          }

          @Override
          public void refund(int units) {
            if (!creative) ElectricItem.manager.charge(stack, units * 100.0, tier, true, false);
          }
        };
    var action =
        WrenchActions.execute(
            context.getLevel(),
            context.getClickedPos(),
            context.getClickedFace(),
            player,
            creative || canTakeDamage(stack, 10),
            payment);
    if (action == WrenchActions.Action.NONE) return InteractionResult.FAIL;
    if (context.getLevel().isClientSide()) player.playSound(SoundDefinitions.ITEM_WRENCH_USE, 1, 1);
    return InteractionResult.SUCCESS;
  }

  public boolean canTakeDamage(ItemStack stack, double units) {
    return stack != null
        && stack.is(this)
        && Double.isFinite(units)
        && units > 0
        && ElectricItem.manager.getCharge(stack) + 1e-8 >= units * 100;
  }

  @Override
  public boolean consumeEnergy(ItemStack stack, double units, LivingEntity entity) {
    return Double.isFinite(units) && units > 0 && super.consumeEnergy(stack, units * 100, entity);
  }

  @Override
  public boolean canBeStoredInToolbox(ItemStack stack) {
    return stack != null && !stack.isEmpty();
  }
}
