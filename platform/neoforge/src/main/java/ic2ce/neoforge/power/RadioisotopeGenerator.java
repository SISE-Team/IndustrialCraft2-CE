package ic2ce.neoforge.power;

import ic2ce.neoforge.item.consumable.ConsumableItems;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.ConsumableInventory;
import ic2ce.neoforge.inventory.StackWhitelistInput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Radioisotope pellets are permanent sources; their count selects a power-of-two output. */
@NotClassic
public class RadioisotopeGenerator extends BufferedGenerator {
  public final ConsumableInventory fuelSlot =
      new StackWhitelistInput(this, "fuel", 6, new ItemStack(ConsumableItems.RTG_PELLET));
  private static final double EFFICIENCY =
      NativeConfigValues.getDouble(
          ProjectConfiguration.get(), "balance/energy/generator/radioisotope");

  public RadioisotopeGenerator(BlockPos position, BlockState block) {
    super(PowerBlockEntities.RT_GENERATOR, position, block, 16, 1, 20000);
    fuelSlot.setStackSizeLimit(1);
  }

  @Override
  public boolean gainEnergy() {
    int pellets = 0;
    for (int slot = 0; slot < fuelSlot.size(); slot++)
      if (fuelSlot.get(slot).is(ConsumableItems.RTG_PELLET)) pellets++;
    if (pellets == 0 || !Double.isFinite(EFFICIENCY) || EFFICIENCY <= 0) return false;
    energy.addEnergy((1 << (pellets - 1)) * EFFICIENCY);
    return true;
  }

  @Override
  public boolean gainFuel() {
    return false;
  }

  @Override
  public boolean needsFuel() {
    return false;
  }

  @Override
  protected boolean delayActiveUpdate() {
    return true;
  }
}
