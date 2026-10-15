package ic2ce.neoforge.thermal;

import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.ConsumableInventory;
import ic2ce.neoforge.inventory.StackWhitelistInput;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.thermal.menu.IsotopeHeaterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Permanent radioisotope heat depends on valid pellets, without consuming fuel or EU. */
@NotClassic
public class RadioisotopeHeater extends BufferedHeatSource implements IHasGui {
  public final ConsumableInventory fuelSlot =
      new StackWhitelistInput(this, "fuelSlot", 6, new ItemStack(ItemDefinitions.RTG_PELLET));
  public static final float outputMultiplier =
      2
          * NativeConfigValues.getFloat(
              ProjectConfiguration.get(), "balance/energy/heatgenerator/radioisotope");

  public RadioisotopeHeater(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.RT_HEAT_GENERATOR, position, block);
    fuelSlot.setStackSizeLimit(1);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    setActive(HeatBuffer > 0);
  }

  @Override
  protected int fillHeatBuffer(int maximum) {
    return Math.min(Math.max(0, maximum), getMaxHeatEmittedPerTick());
  }

  @Override
  public int getMaxHeatEmittedPerTick() {
    int pellets = 0;
    for (int slot = 0; slot < fuelSlot.size(); slot++)
      if (fuelSlot.get(slot).is(ItemDefinitions.RTG_PELLET)) pellets++;
    return pellets == 0 || !Float.isFinite(outputMultiplier) || outputMultiplier <= 0
        ? 0
        : (int) Math.min(Integer.MAX_VALUE, (1 << (pellets - 1)) * (double) outputMultiplier);
  }

  @Override
  public OwnedMenu<RadioisotopeHeater> createServerScreenHandler(int id, Player player) {
    return new IsotopeHeaterMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new IsotopeHeaterMenu(id, inventory, this);
  }
}
