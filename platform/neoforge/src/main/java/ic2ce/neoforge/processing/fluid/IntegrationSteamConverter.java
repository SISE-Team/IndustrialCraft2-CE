package ic2ce.neoforge.processing.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.core.*;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.core.network.*;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.FluidGroups;
import ic2ce.neoforge.thermal.HeatSupply;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/** Bulk conversion reserves 10mB input, one HU and the configured integration output per batch. */
@NotClassic
public class IntegrationSteamConverter extends InventoryBlockEntity implements IHasGui {
  protected int currentHeat;
  @GuiSynced protected final Ic2FluidTank output, input;
  protected static final int CONSUMPTION = 10;
  protected final FluidPorts fluids;

  public IntegrationSteamConverter(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.STEAM_REPRESSURIZER, position, state);
    fluids = addComponent(new FluidPorts(this));
    input =
        fluids.addTankInsert(
            "input",
            10000,
            FluidPorts.fluidPredicate(
                FluidDefinitions.STEAM.still, FluidDefinitions.SUPERHEATED_STEAM.still));
    output = fluids.addTankExtract("output", 10000);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    currentHeat = Math.clamp(data.getIntOr("heat", 0), 0, 1000);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("heat", currentHeat);
  }

  private static Fluid getSteam() {
    var candidates = BuiltInRegistries.FLUID.getTagOrEmpty(FluidGroups.STEAM).iterator();
    return candidates.hasNext() ? candidates.next().value() : null;
  }

  public static boolean hasSteam() {
    return getSteam() != null;
  }

  protected int getOutput() {
    if (input.isEmpty()) return 0;
    var fluid = input.getFluidStack().getFluid();
    String key =
        fluid == FluidDefinitions.STEAM.still
            ? "steamPerSteam"
            : fluid == FluidDefinitions.SUPERHEATED_STEAM.still ? "steamPerSuperSteam" : null;
    return key == null
        ? 0
        : Math.max(
            0,
            NativeConfigValues.getInt(
                ProjectConfiguration.get(), "balance/steamRepressurizer/" + key));
  }

  protected boolean canOutput(int amount) {
    var steam = getSteam();
    return steam != null
        && amount > 0
        && output.fillMbUnchecked(Ic2FluidStack.create(steam, amount), true) == amount;
  }

  protected void getHeat() {
    int aim = Math.max(0, input.getFluidAmount() / CONSUMPTION - currentHeat);
    currentHeat += HeatSupply.collect(getLevel(), worldPosition, aim);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    var steam = getSteam();
    int ratio = getOutput();
    boolean worked = false;
    if (steam != null && ratio > 0 && input.getFluidAmount() >= CONSUMPTION) {
      int room = output.fillMbUnchecked(Ic2FluidStack.create(steam, Integer.MAX_VALUE), true);
      int batches = Math.min(input.getFluidAmount() / CONSUMPTION, room / ratio);
      if (batches > 0) {
        currentHeat +=
            HeatSupply.collect(getLevel(), worldPosition, Math.max(0, batches - currentHeat));
        batches = Math.min(batches, currentHeat);
        if (batches > 0) {
          int produced = batches * ratio;
          input.drainMbUnchecked(batches * CONSUMPTION, false);
          output.fillMbUnchecked(Ic2FluidStack.create(steam, produced), false);
          currentHeat -= batches;
          worked = true;
          setChanged();
        }
      }
    }
    setActive(worked);
  }

  @Override
  public boolean getGuiState(String key) {
    return key.equals("valid") ? hasSteam() : super.getGuiState(key);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return DocumentMenu.create(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(syncId, inventory, this);
  }
}
