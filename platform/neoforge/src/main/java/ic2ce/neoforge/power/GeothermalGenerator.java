package ic2ce.neoforge.power;

import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.network.GuiSynced;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;

/** Each advancing geothermal tick reserves exactly two millibuckets of lava. */
public class GeothermalGenerator extends BufferedGenerator {
  public final FluidContainerInventory fluidSlot;
  public final OutputInventory outputSlot;
  @GuiSynced protected final Ic2FluidTank fluidTank;
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));

  public GeothermalGenerator(BlockPos position, BlockState block) {
    super(
        BlockEntityDefinitions.GEO_GENERATOR,
        position,
        block,
        Math.round(
            20
                * NativeConfigValues.getDouble(
                    ProjectConfiguration.get(), "balance/energy/generator/geothermal")),
        1,
        2400);
    fluidTank =
        fluids.addTankInsert(
            "fluid",
            8000,
            FluidPorts.fluidPredicate(net.minecraft.world.level.material.Fluids.LAVA));
    fluidSlot =
        new TankContainerInput(
            this,
            "fluidSlot",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.ANY,
            FluidContainerInventory.OpType.Drain,
            fluidTank);
    outputSlot = new OutputInventory(this, "output", 1);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (fluidSlot.processIntoTank(fluidTank, outputSlot)) setChanged();
  }

  @Override
  public boolean gainFuel() {
    if (!fluidTank.hasExactFluid(net.minecraft.world.level.material.Fluids.LAVA)
        || fluidTank.getFluidAmount() < 2) return false;
    fluidTank.drainMbUnchecked(2, false);
    fuel++;
    return true;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.GENERATOR_GEOTHERMAL_LOOP;
  }
}
