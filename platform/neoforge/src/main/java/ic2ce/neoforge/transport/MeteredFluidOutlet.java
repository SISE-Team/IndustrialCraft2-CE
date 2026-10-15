package ic2ce.neoforge.transport;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.network.GuiSynced;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.transport.menu.MeteredFluidMenu;
import ic2ce.neoforge.world.DirectionSets;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Pays ten EU per delivered metered batch, with independent per-second/per-tick timing. */
@NotClassic
public class MeteredFluidOutlet extends PoweredBlockEntity
    implements IHasGui, INetworkClientTileEntityEventListener {
  private int mode, updateTicker, outputmb;
  public final OutputInventory wasseroutputSlot;
  public final TankContainerInput wasserinputSlot;
  protected final FluidPorts fluids;
  @GuiSynced protected final FluidPorts.InternalFluidTank fluidTank;

  public MeteredFluidOutlet(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.FLUID_REGULATOR, position, state, 10000, 4);
    fluids = addComponent(new FluidPorts(this));
    fluidTank = fluids.addTank("fluidTank", 10000, InventorySlot.Access.NONE);
    wasserinputSlot =
        new TankContainerInput(
            this,
            "wasserinputSlot",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.TOP,
            FluidContainerInventory.OpType.Drain,
            fluidTank);
    wasseroutputSlot = new OutputInventory(this, "wasseroutputSlot", 1);
    updateTicker = Math.floorMod(position.hashCode(), getTickRate());
    updateConnectivity();
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    outputmb = Math.clamp(data.getIntOr("outputmb", 0), 0, 1000);
    mode = data.getIntOr("mode", 0) == 1 ? 1 : 0;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("outputmb", outputmb);
    data.putInt("mode", mode);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    updateConnectivity();
  }

  @Override
  protected void setFacing(Level world, Direction facing) {
    super.setFacing(world, facing);
    if (fluids != null) updateConnectivity();
  }

  private void updateConnectivity() {
    fluids.changeConnectivity(fluidTank, DirectionSets.except(getFacing()), Set.of());
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (wasserinputSlot.processIntoTank(fluidTank, wasseroutputSlot)) super.setChanged();
    updateTicker++;
    if (updateTicker >= Math.max(1, getTickRate())) updateTicker = 0;
    if (mode == 1 || updateTicker == 0) setActive(work());
  }

  private boolean work() {
    if (outputmb == 0 || fluidTank.isEmpty() || !energy.useEnergy(10)) return false;
    int sent;
    try {
      sent = FluidOutlet.send(this, fluidTank, getFacing(), outputmb);
    } catch (RuntimeException | Error failure) {
      energy.forceAddEnergy(10);
      throw failure;
    }
    if (sent == 0) energy.forceAddEnergy(10);
    else super.setChanged();
    return sent > 0;
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (getLevel() == null || getLevel().isClientSide()) return;
    int oldMode = mode, oldRate = outputmb;
    switch (event) {
      case 1001 -> mode = 1;
      case 1002 -> mode = 0;
      default -> outputmb = (int) Math.clamp((long) outputmb + event, 0, 1000);
    }
    if (mode != oldMode || outputmb != oldRate) super.setChanged();
  }

  public int getTickRate() {
    return 20;
  }

  public int gaugeLiquidScaled(int scale, int tank) {
    return tank == 0
        ? (int)
            Math.clamp(
                (long) fluidTank.getFluidAmount() * Math.max(0, scale) / fluidTank.getCapacity(),
                0,
                Integer.MAX_VALUE)
        : 0;
  }

  public int getoutputmb() {
    return outputmb;
  }

  public String getmodegui() {
    return LocalizedText.translate(mode == 1 ? "ic2.generic.text.tick" : "ic2.generic.text.sec");
  }

  public Ic2FluidTank getFluidTank() {
    return fluidTank;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new MeteredFluidMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new MeteredFluidMenu(syncId, inventory, this);
  }
}
