package ic2ce.neoforge.kinetic;

import ic2ce.api.energy.tile.IKineticSource;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.core.fluid.Ic2FluidTank;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.profile.NotClassic;
import ic2ce.core.util.LiquidUtil;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.explosion.NativeBlast;
import ic2ce.neoforge.inventory.ConsumableInventory;
import ic2ce.neoforge.inventory.StackWhitelistInput;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.kinetic.menu.SteamTurbineMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.fluid.SteamCondenser;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Steam has a consumed per-tick shaft budget, owned condensation remainder and independent exhaust
 * transport.
 */
@NotClassic
public class SteamTurbine extends InventoryBlockEntity
    implements IKineticSource, IHasGui, IUpgradableBlock {
  protected final Ic2FluidTank steamTank, distilledWaterTank;
  public final UpgradeInventory upgradeSlot = new UpgradeInventory(this, "upgrade", 1);
  public final ConsumableInventory turbineSlot =
      new StackWhitelistInput(this, "Turbineslot", 1, new ItemStack(ItemDefinitions.STEAM_TURBINE));
  private static final double MULTIPLIER =
      NativeConfigValues.getDouble(
          ProjectConfiguration.get(), "balance/energy/kineticgenerator/steam");
  private int kUoutput, remainingKU, condensationProgress, condensationRemainder;
  private int wearTicks = ProjectRuntime.random.nextInt(20);
  private boolean ventingSteam, throttled, blockedByWater;
  protected final FluidPorts fluids = addComponent(new FluidPorts(this));
  private static final Direction[] SIDES = Direction.values();

  public SteamTurbine(BlockPos position, BlockState block) {
    super(BlockEntityDefinitions.STEAM_KINETIC_GENERATOR, position, block);
    turbineSlot.setStackSizeLimit(1);
    steamTank =
        fluids.addTankInsert(
            "steamTank",
            21000,
            FluidPorts.fluidPredicate(
                FluidDefinitions.STEAM.still, FluidDefinitions.SUPERHEATED_STEAM.still));
    distilledWaterTank =
        fluids.addTank(
            "distilledWaterTank",
            1000,
            FluidPorts.fluidPredicate(
                FluidDefinitions.DISTILLED_WATER.still,
                net.minecraft.world.level.material.Fluids.WATER));
  }

  private void settleCondensation() {
    int due = condensationProgress / 100;
    blockedByWater = distilledWaterTank.getFluidAmount() >= distilledWaterTank.getCapacity();
    if (due == 0) return;
    int accepted =
        distilledWaterTank.fillMbUnchecked(
            Ic2FluidStack.create(FluidDefinitions.DISTILLED_WATER.still, due), false);
    condensationProgress -= accepted * 100;
    blockedByWater =
        condensationProgress >= 100
            || distilledWaterTank.getFluidAmount() >= distilledWaterTank.getCapacity();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    remainingKU = kUoutput = 0;
    ventingSteam = false;
    settleCondensation();
    boolean worked = false;
    throttled = distilledWaterTank.getFluidAmount() > 0;
    if (hasTurbine()
        && !blockedByWater
        && !steamTank.isEmpty()
        && Double.isFinite(MULTIPLIER)
        && MULTIPLIER > 0) {
      boolean hot = steamTank.hasExactFluid(FluidDefinitions.SUPERHEATED_STEAM.still);
      if (hot || steamTank.hasExactFluid(FluidDefinitions.STEAM.still)) {
        int amount = steamTank.getFluidAmount();
        steamTank.drainMbUnchecked(amount, false);
        double throttle =
            1 - (double) distilledWaterTank.getFluidAmount() / distilledWaterTank.getCapacity();
        kUoutput =
            remainingKU =
                (int)
                    Math.clamp(
                        amount * (hot ? 4.0 : 2.0) * throttle * MULTIPLIER, 0, Integer.MAX_VALUE);
        int condensed = 0;
        if (!hot) {
          int sum = amount + condensationRemainder;
          condensed = sum / 10;
          condensationRemainder = sum % 10;
          condensationProgress += condensed;
        }
        exhaust(amount - condensed, hot);
        settleCondensation();
        worked = kUoutput > 0;
        if (++wearTicks >= getTickRate()) {
          if (worked) turbineSlot.damage(hot ? 1 : 2, false);
          wearTicks = 0;
        }
      }
    }
    setActive(worked);
    boolean changed = upgradeSlot.tickNoMark();
    if (worked || changed) super.setChanged();
  }

  private void exhaust(int quantity, boolean hot) {
    var world = getLevel();
    int remaining = quantity;
    for (var side : SIDES) {
      var target = world.getBlockEntity(worldPosition.relative(side));
      if (target instanceof SteamCondenser || hot && target instanceof SteamTurbine) {
        int accepted =
            LiquidUtil.fillTile(
                target,
                side.getOpposite(),
                Ic2FluidStack.create(FluidDefinitions.STEAM.still, remaining),
                false);
        remaining -= Math.clamp(accepted, 0, remaining);
        if (remaining == 0) break;
      }
    }
    ventingSteam = remaining > 0;
    if (ventingSteam && world.getRandom().nextInt(10) == 0)
      new NativeBlast(world, null, worldPosition, 1, 1, NativeBlast.Type.Heat).doExplosion();
  }

  public int getKUoutput() {
    return kUoutput;
  }

  @Override
  public int getConnectionBandwidth(Direction side) {
    return side == getFacing() ? kUoutput : 0;
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
    int amount = Math.min(request, remainingKU);
    if (!simulate) remainingKU -= amount;
    return amount;
  }

  public int gaugeLiquidScaled(int width, int tank) {
    return tank == 0
        ? (int)
            ((long) Math.max(0, width)
                * distilledWaterTank.getFluidAmount()
                / distilledWaterTank.getCapacity())
        : 0;
  }

  @Override
  public double getEnergy() {
    return 0;
  }

  @Override
  public boolean useEnergy(double amount) {
    return amount == 0;
  }

  public int getDistilledWaterTankFill() {
    return distilledWaterTank.getFluidAmount();
  }

  public Ic2FluidTank getDistilledWaterTank() {
    return distilledWaterTank;
  }

  public boolean hasTurbine() {
    return turbineSlot.get().is(ItemDefinitions.STEAM_TURBINE);
  }

  public boolean isVentingSteam() {
    return ventingSteam;
  }

  public boolean isThrottled() {
    return throttled;
  }

  public boolean isTurbineBlockedByWater() {
    return blockedByWater;
  }

  public int getTickRate() {
    return 20;
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    condensationProgress =
        Math.clamp(data.getIntOr("condensationprogress", 0), 0, Integer.MAX_VALUE - 2100);
    condensationRemainder = Math.clamp(data.getIntOr("condensationRemainder", 0), 0, 9);
    wearTicks = Math.clamp(data.getIntOr("wearTicks", 0), 0, getTickRate() - 1);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("condensationprogress", condensationProgress);
    data.putInt("condensationRemainder", condensationRemainder);
    data.putInt("wearTicks", wearTicks);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.FluidConsuming,
        UpgradableProperty.FluidProducing);
  }

  @Override
  public OwnedMenu<SteamTurbine> createServerScreenHandler(int id, Player player) {
    return new SteamTurbineMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new SteamTurbineMenu(id, inventory, this);
  }
}
