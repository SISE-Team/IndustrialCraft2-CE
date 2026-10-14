package ic2ce.neoforge.block;

import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.inventory.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Electrical native machines create one bounded sink ledger and explicitly managed physical
 * discharge inventory.
 */
public abstract class PoweredBlockEntity extends ActiveBlockEntity {
  protected final ElectricalBuffer energy;
  public final DischargeInventory dischargeSlot;

  public PoweredBlockEntity(
      BlockEntityType<? extends PoweredBlockEntity> type,
      BlockPos pos,
      BlockState state,
      int capacity,
      int tier) {
    this(type, pos, state, capacity, tier, true);
  }

  public PoweredBlockEntity(
      BlockEntityType<? extends PoweredBlockEntity> type,
      BlockPos pos,
      BlockState state,
      int capacity,
      int tier,
      boolean redstoneFuel) {
    if (capacity < 0 || tier < 0)
      throw new IllegalArgumentException("Invalid native electrical machine capacity or tier");
    super(type, pos, state);
    dischargeSlot =
        new DischargeInventory(
            this, InventorySlot.Access.NONE, tier, redstoneFuel, InventorySlot.InvSide.ANY);
    energy =
        addComponent(
            ElectricalBuffer.asBasicSink(this, capacity, tier).addManagedSlot(dischargeSlot));
  }

  public boolean hasEnergy() {
    return energy.getEnergy() > 0;
  }
}
