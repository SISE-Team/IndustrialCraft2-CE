package ic2ce.neoforge.thermal;

import ic2ce.core.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.thermal.menu.ElectricHeaterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Electric heat reserves its exact fractional EU bill before publishing whole heat units. */
@NotClassic
public class ElectricHeater extends BufferedHeatSource implements IHasGui {
  private boolean producing;
  public final DischargeInventory dischargeSlot;
  public final ConsumableInventory coilSlot =
      new StackWhitelistInput(this, "CoilSlot", 10, new ItemStack(ItemDefinitions.COIL));
  protected final ElectricalBuffer energy;
  public static final double outputMultiplier =
      NativeConfigValues.getDouble(
          ProjectConfiguration.get(), "balance/energy/heatgenerator/electric");

  public ElectricHeater(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.ELECTRIC_HEAT_GENERATOR, position, block);
    coilSlot.setStackSizeLimit(1);
    dischargeSlot = new DischargeInventory(this, InventorySlot.Access.NONE, 4);
    energy =
        addComponent(ElectricalBuffer.asBasicSink(this, 10000, 4).addManagedSlot(dischargeSlot));
  }

  @Override
  protected void updateEntityServer() {
    producing = false;
    super.updateEntityServer();
    setActive(producing);
  }

  @Override
  protected int fillHeatBuffer(int maximum) {
    if (maximum <= 0 || !Double.isFinite(outputMultiplier) || outputMultiplier <= 0) return 0;
    int available = (int) Math.min(maximum, Math.floor(energy.getEnergy() * outputMultiplier));
    if (available <= 0 || !energy.useEnergy(available / outputMultiplier)) return 0;
    producing = true;
    return available;
  }

  @Override
  public int getMaxHeatEmittedPerTick() {
    int coils = 0;
    for (int slot = 0; slot < coilSlot.size(); slot++)
      if (coilSlot.get(slot).is(ItemDefinitions.COIL)) coils++;
    return coils * 10;
  }

  public final float getChargeLevel() {
    return (float) Math.clamp(energy.getFillRatio(), 0, 1);
  }

  @Override
  public OwnedMenu<ElectricHeater> createServerScreenHandler(int id, Player player) {
    return new ElectricHeaterMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new ElectricHeaterMenu(id, inventory, this);
  }
}
