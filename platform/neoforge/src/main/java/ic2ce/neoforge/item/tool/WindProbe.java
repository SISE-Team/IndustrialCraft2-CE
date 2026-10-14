package ic2ce.neoforge.item.tool;

import ic2ce.core.event.WorldData;
import ic2ce.core.item.PriorityUsableItem;
import ic2ce.neoforge.kinetic.WindTurbine;
import ic2ce.neoforge.power.WindMill;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Fifty-EU atmospheric measurement and separate loaded wind-machine obstruction diagnostics. */
public class WindProbe extends PoweredTool implements PriorityUsableItem {
  public WindProbe(Properties properties) {
    super(properties, 50);
    maxCharge = 10_000;
    transferLimit = 100;
    tier = 1;
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      Item.TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> output,
      TooltipFlag flags) {
    super.appendHoverText(stack, context, display, output, flags);
    output.accept(Component.translatable("ic2.wind_meter.tooltipA"));
    output.accept(Component.translatable("ic2.wind_meter.tooltipB"));
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    var owner = context.getPlayer();
    var world = context.getLevel();
    if (owner == null || owner.isShiftKeyDown() || !stack.is(this)) return InteractionResult.PASS;
    var tile = world.getBlockEntity(context.getClickedPos());
    if (!(tile instanceof WindTurbine || tile instanceof WindMill)) return InteractionResult.PASS;
    if (world.isClientSide())
      return canUse(stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    if (tile instanceof WindTurbine turbine && !turbine.getActive()) {
      ProjectRuntime.sideProxy.messagePlayer(
          owner,
          turbine.hasRotor()
              ? "ic2.wind_meter.info.rotor.blocked"
              : "ic2.wind_meter.info.rotor.none");
      return InteractionResult.FAIL;
    }
    if (!consumeEnergy(stack, 50, owner)) return InteractionResult.PASS;
    if (tile instanceof WindTurbine turbine) {
      if (turbine.getObstructions() < 0)
        ProjectRuntime.sideProxy.messagePlayer(
            owner, "ic2.wind_meter.info.blocked", turbine.getRotorDiameter() * 3);
      else report(owner, turbine.calcWindStrength(), turbine.getObstructions());
    } else {
      var mill = (WindMill) tile;
      report(
          owner,
          WorldData.get(world).windSim.getWindAt(context.getClickedPos().getY())
              * Math.max(0, 1 - mill.getObstructions() / 567.0),
          mill.getObstructions());
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResult use(Level world, Player owner, InteractionHand hand) {
    var stack = owner.getItemInHand(hand);
    if (!stack.is(this)) return InteractionResult.PASS;
    if (world.isClientSide())
      return canUse(stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    if (!consumeEnergy(stack, 50, owner)) return InteractionResult.PASS;
    ProjectRuntime.sideProxy.messagePlayer(
        owner,
        "ic2.wind_meter.info",
        rounded(WorldData.get(world).windSim.getWindAt(owner.getY())));
    return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
  }

  private static void report(Player owner, double wind, int obstructions) {
    var value = rounded(wind);
    if (value <= 0)
      ProjectRuntime.sideProxy.messagePlayer(owner, "ic2.wind_meter.info.obstructed", obstructions);
    else ProjectRuntime.sideProxy.messagePlayer(owner, "ic2.wind_meter.info.effective", value);
  }

  private static float rounded(double wind) {
    return Double.isFinite(wind) ? (float) (Math.round(Math.max(0, wind) * 100) / 100.0) : 0;
  }
}
