package ic2ce.neoforge.kinetic;

import ic2ce.api.energy.tile.IKineticSource;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.kinetic.menu.MotorMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Electric motors set shaft bandwidth; fractional KU storage preserves its exact EU conversion
 * bill.
 */
@NotClassic
public class ElectricMotor extends InventoryBlockEntity implements IKineticSource, IHasGui {
  public final StackWhitelistInput slotMotor;
  public final DischargeInventory dischargeSlot;
  private final double kuPerEU =
      4
          * NativeConfigValues.getDouble(
              ProjectConfiguration.get(), "balance/energy/kineticgenerator/electric");
  public double ku;
  public final int maxKU = 1000;
  protected final ElectricalBuffer energy;

  public ElectricMotor(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.ELECTRIC_KINETIC_GENERATOR, position, block);
    slotMotor =
        new StackWhitelistInput(
            this, "slotMotor", 10, new ItemStack(ItemDefinitions.ELECTRIC_MOTOR));
    slotMotor.setStackSizeLimit(1);
    dischargeSlot = new DischargeInventory(this, InventorySlot.Access.NONE, 4);
    energy =
        addComponent(ElectricalBuffer.asBasicSink(this, 10000, 4).addManagedSlot(dischargeSlot));
    updateDirections();
  }

  private void updateDirections() {
    energy.setDirections(ic2ce.neoforge.world.DirectionSets.except(getFacing()), Set.of());
  }

  @Override
  protected void setFacing(Level world, Direction side) {
    super.setFacing(world, side);
    if (energy != null) updateDirections();
  }

  @Override
  public int getConnectionBandwidth(Direction side) {
    return side == getFacing() ? getMaxKU() : 0;
  }

  public int getMaxKU() {
    int motors = 0;
    for (int slot = 0; slot < slotMotor.size(); slot++)
      if (slotMotor.get(slot).is(ItemDefinitions.ELECTRIC_MOTOR)) motors++;
    return motors * 100;
  }

  public int getMaxKUForGUI() {
    return maxKU;
  }

  @Override
  public int maxrequestkineticenergyTick(Direction side) {
    return drawKineticEnergy(side, Integer.MAX_VALUE, true);
  }

  @Override
  public int requestkineticenergy(Direction side, int amount) {
    return drawKineticEnergy(side, amount, false);
  }

  @Override
  public int drawKineticEnergy(Direction side, int request, boolean simulate) {
    if (side != getFacing() || request <= 0) return 0;
    int amount = Math.min(request, Math.min(getMaxKU(), (int) ku));
    if (!simulate && amount > 0) {
      ku -= amount;
      setChanged();
    }
    return amount;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean active = false;
    if (getMaxKU() > 0 && Double.isFinite(kuPerEU) && kuPerEU > 0 && ku < maxKU) {
      double created = Math.min(maxKU - ku, energy.getEnergy() * kuPerEU);
      if (created > 0 && energy.useEnergy(created / kuPerEU)) {
        ku += created;
        active = true;
        setChanged();
      }
    }
    setActive(active);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    double saved = data.getDoubleOr("ku", 0);
    ku = Double.isFinite(saved) ? Math.clamp(saved, 0, maxKU) : 0;
    updateDirections();
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putDouble("ku", ku);
  }

  @Override
  public OwnedMenu<ElectricMotor> createServerScreenHandler(int id, Player player) {
    return new MotorMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new MotorMenu(id, inventory, this);
  }
}
