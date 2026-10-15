package ic2ce.neoforge.item.tool;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.*;
import ic2ce.api.item.IBoxable;
import ic2ce.neoforge.handheld.IHandHeldInventory;
import ic2ce.neoforge.handheld.MeterSession;
import ic2ce.neoforge.handheld.menu.EnergyMeterMenu;
import ic2ce.neoforge.item.PriorityUsableItem;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.runtime.SideServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

/** Loaded energy-endpoint observer binds one native tile to a distinct time-series menu. */
public class EnergyProbe extends Item implements IBoxable, IHandHeldInventory, PriorityUsableItem {
  public EnergyProbe(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    var owner = context.getPlayer();
    var world = context.getLevel();
    if (owner == null || !stack.is(this)) return InteractionResult.PASS;
    if (world.isClientSide()) return InteractionResult.PASS;
    var endpoint = EnergyNet.instance.getTile(world, context.getClickedPos());
    if (!(endpoint instanceof IEnergySource
        || endpoint instanceof IEnergyConductor
        || endpoint instanceof IEnergySink)) {
      SideServices.proxy().messagePlayer(owner, "ic2.meter.not_energy_tile");
      return InteractionResult.SUCCESS;
    }
    if (getInventory(owner, context.getHand(), stack)
            .openManagedItem(owner, context.getHand(), null)
        && owner.containerMenu instanceof EnergyMeterMenu menu) menu.setUut(endpoint);
    return InteractionResult.SUCCESS;
  }

  public boolean onDroppedByPlayer(ItemStack stack, Player owner) {
    if (owner instanceof ServerPlayer actor
        && owner.containerMenu instanceof EnergyMeterMenu menu
        && menu.base.isThisContainer(stack)) {
      menu.base.saveAsThrown(stack);
      actor.closeContainer();
    }
    return true;
  }

  @Override
  public boolean canBeStoredInToolbox(ItemStack stack) {
    return stack != null && !stack.isEmpty();
  }

  @Override
  public IHasGui getInventory(Player owner, InteractionHand hand, ItemStack stack) {
    return new MeterSession(owner, hand, stack);
  }
}
