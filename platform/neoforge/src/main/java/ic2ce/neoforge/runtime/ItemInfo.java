package ic2ce.neoforge.runtime;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.info.IInfoProvider;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.item.energy.BatteryItems;
import ic2ce.neoforge.material.MaterialItems;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;

/**
 * Distinct disposable electrical values and actual native fuel billing preserve scrap/lava
 * configuration policies.
 */
public final class ItemInfo implements IInfoProvider {
  @Override
  public double getEnergyValue(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return 0;
    if (stack.is(Items.REDSTONE)) return 800;
    if (stack.is(BatteryItems.SINGLE_USE_BATTERY)) return 1200;
    return stack.is(MaterialItems.ENERGIUM_DUST) ? 16000 : 0;
  }

  @Override
  public int getFuelValue(ItemStack stack, boolean lavaAllowed) {
    if (stack == null || stack.isEmpty()) return 0;
    if ((stack.is(MaterialItems.SCRAP) || stack.is(MaterialItems.SCRAP_BOX))
        && !NativeConfigValues.getBool(ProjectConfiguration.get(), "misc/allowBurningScrap"))
      return 0;
    var contents = Ic2FluidStack.getAll(stack);
    boolean lava = false;
    if (contents != null)
      for (var fluid : contents)
        if (fluid != null && !fluid.isEmpty() && fluid.getFluid() == Fluids.LAVA) {
          lava = true;
          break;
        }
    if (lava && !lavaAllowed) return 0;
    int burn = Math.max(0, EnvProxy.get().getBurnTime(stack));
    return lava ? burn / 10 : burn;
  }
}
