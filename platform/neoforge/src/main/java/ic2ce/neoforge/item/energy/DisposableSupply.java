package ic2ce.neoforge.item.energy;

import ic2ce.api.item.*;
import ic2ce.core.item.*;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Disposable supply spends one physical cell for a bounded single-use hotbar budget. */
public class DisposableSupply extends Item implements IBoxable, IItemHudInfo {
  private static final ThreadLocal<Set<ItemStack>> ACTIVE =
      ThreadLocal.withInitial(() -> java.util.Collections.newSetFromMap(new IdentityHashMap<>()));
  public final int capacity, tier;

  public DisposableSupply(Properties properties, int capacity, int tier) {
    super(properties);
    if (capacity <= 0 || tier < 0)
      throw new IllegalArgumentException("Invalid disposable battery descriptor");
    this.capacity = capacity;
    this.tier = tier;
  }

  @Override
  public InteractionResult use(Level world, Player player, InteractionHand hand) {
    var source = player.getItemInHand(hand);
    if (!source.is(this) || source.isEmpty()) return InteractionResult.PASS;
    if (world.isClientSide()) return InteractionResult.SUCCESS;
    var active = ACTIVE.get();
    if (!active.add(source)) return InteractionResult.PASS;
    try {
      boolean useful = false;
      for (int index = 0; index < 9; index++) {
        var target = player.getInventory().getItem(index);
        if (target != source
            && ElectricItem.manager.charge(target, capacity, tier, true, true) > 0) {
          useful = true;
          break;
        }
      }
      if (!useful) return InteractionResult.PASS;
      boolean creative = player.getAbilities().instabuild;
      if (!creative) source.shrink(1);
      double remaining = capacity;
      for (int index = 0; index < 9 && remaining > 0; index++) {
        var target = player.getInventory().getItem(index);
        if (target == source || target.isEmpty()) continue;
        double accepted = ElectricItem.manager.charge(target, remaining, tier, true, false);
        remaining -= Math.min(remaining, accepted);
      }
      if (remaining == capacity && !creative) source.grow(1);
      player.getInventory().setChanged();
      player.containerMenu.broadcastChanges();
      return InteractionResult.SUCCESS.heldItemTransformedTo(source);
    } finally {
      active.remove(source);
    }
  }

  @Override
  public boolean canBeStoredInToolbox(ItemStack stack) {
    return stack != null && !stack.isEmpty();
  }

  @Override
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    return List.of(capacity + " EU");
  }
}
