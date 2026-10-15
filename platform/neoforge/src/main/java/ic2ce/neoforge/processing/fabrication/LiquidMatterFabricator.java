package ic2ce.neoforge.processing.fabrication;

import ic2ce.api.energy.tile.IExplosionPowerOverride;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.*;
import ic2ce.api.upgrade.*;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.neoforge.material.MaterialItems;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.processing.ProcessingBlockEntities;
import ic2ce.neoforge.runtime.SideServices;
import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.engine.machine.AmplificationBudget;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.FabricationSettings;
import ic2ce.neoforge.processing.menu.MatterMenu;
import ic2ce.neoforge.recipe.processing.AmplifierRecipes;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fluid matter production owns fractional amplifier credit and pays each millibucket's complete EU
 * cost.
 */
@NotClassic
public class LiquidMatterFabricator extends PoweredBlockEntity
    implements IHasGui, IUpgradableBlock, IExplosionPowerOverride {
  private static final int DEFAULT_TIER =
      NativeConfigValues.getInt(ProjectConfiguration.get(), "balance/matterFabricatorTier");
  public int scrap;
  private double lastEnergy;
  private final AmplificationBudget amplifier = new AmplificationBudget();
  private int state;
  public boolean redstonePowered;
  public final UpgradeInventory upgradeSlot;
  public final RecipeInputInventory<IRecipeInput, Integer, ItemStack> amplifierSlot;
  public final OutputInventory outputSlot;
  public final FluidContainerInventory containerslot;
  @GuiSynced public final Ic2FluidTank fluidTank;
  protected final SignalInput redstone;
  protected final FluidPorts fluids;
  private static final Set<UpgradableProperty> PROPERTIES =
      Set.of(
          UpgradableProperty.RedstoneSensitive,
          UpgradableProperty.Transformer,
          UpgradableProperty.ItemConsuming,
          UpgradableProperty.ItemProducing,
          UpgradableProperty.FluidProducing);

  public LiquidMatterFabricator(BlockPos position, BlockState blockState) {
    super(
        ProcessingBlockEntities.MATTER_GENERATOR,
        position,
        blockState,
        FabricationSettings.energyCost(),
        DEFAULT_TIER);
    amplifierSlot = new AmplifierInput(this, Recipes.matterFabricator);
    outputSlot = new OutputInventory(this, "output", 1);
    containerslot =
        new FluidWhitelistInput(
            this,
            "container",
            InventorySlot.Access.I,
            1,
            InventorySlot.InvSide.TOP,
            FluidContainerInventory.OpType.Fill,
            FluidDefinitions.UU_MATTER.still);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
    redstone = addComponent(new SignalInput(this));
    redstone.subscribe(signal -> energy.setEnabled(signal == 0));
    fluids = addComponent(new FluidPorts(this));
    fluidTank =
        fluids.addTank(
            "fluidTank", 8000, FluidPorts.fluidPredicate(FluidDefinitions.UU_MATTER.still));
    comparator.setUpdate(
        () -> {
          int input = calcRedstoneFromInvSlots(amplifierSlot);
          return input > 0 ? input : amplifier.available() > 0 ? 1 : 0;
        });
  }

  public static void init() {
    Recipes.matterAmplifier = new AmplifierRecipes();
    addAmplifier(new ItemStack(MaterialItems.SCRAP), 1, 5000);
    addAmplifier(new ItemStack(MaterialItems.SCRAP_BOX), 1, 45000);
  }

  public static void addAmplifier(ItemStack input, int amount, int value) {
    addAmplifier(Recipes.inputFactory.forStack(input, amount), value);
  }

  public static void addAmplifier(IRecipeInput input, int value) {
    if (!(Recipes.matterAmplifier instanceof AmplifierRecipes registry))
      throw new IllegalStateException(
          "The installed amplifier registry does not support registration");
    registry.addRecipe(input, value, null, false);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    scrap = Math.max(0, data.getIntOr("scrap", 0));
    double fraction = data.getDoubleOr("scrapFraction", 0);
    amplifier.restore(
        scrap + (Double.isFinite(fraction) && fraction >= 0 && fraction < 1 ? fraction : 0));
    lastEnergy = data.getDoubleOr("lastEnergy", 0);
    if (!Double.isFinite(lastEnergy) || lastEnergy < 0) lastEnergy = 0;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("scrap", scrap);
    data.putDouble("scrapFraction", amplifier.available() - Math.floor(amplifier.available()));
    data.putDouble("lastEnergy", lastEnergy);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (!getLevel().isClientSide()) {
      setUpgradestat();
      energy.setEnabled(!redstone.hasRedstoneInput());
    }
  }

  public void setUpgradestat() {
    upgradeSlot.onChanged();
    int tier = upgradeSlot.getTier(DEFAULT_TIER);
    energy.setSinkTier(tier);
    dischargeSlot.setTier(tier);
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (getLevel() != null && !getLevel().isClientSide() && upgradeSlot != null) setUpgradestat();
  }

  private boolean receiveAmplifier() {
    if (amplifier.available() >= 10000) return false;
    var recipe = amplifierSlot.process();
    if (recipe == null
        || recipe.getOutput() <= 0
        || recipe.getOutput() > Integer.MAX_VALUE - amplifier.available()) return false;
    amplifierSlot.consume(recipe);
    amplifier.add(recipe.getOutput());
    return true;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean changed = upgradeSlot.tickNoMark();
    redstonePowered = redstone.hasRedstoneInput();
    if (scrap != (int) amplifier.available()) amplifier.restore(scrap);
    boolean amplified = false;
    if (!redstonePowered) {
      changed |= receiveAmplifier();
      double incoming = Math.max(0, energy.getEnergy() - lastEnergy);
      double spent = amplifier.consume(incoming);
      if (spent > 0) {
        energy.forceAddEnergy(5 * spent);
        amplified = changed = true;
      }
      changed |= attemptGeneration();
      changed |= containerslot.processFromTank(fluidTank, outputSlot);
    }
    scrap = (int) amplifier.available();
    lastEnergy = energy.getEnergy();
    int next = redstonePowered || energy.getEnergy() <= 0 ? 0 : amplified ? 2 : 1;
    if (state != next) {
      state = next;
      SideServices.network(true).updateTileEntityField(this, "state");
    }
    setActiveState(next != 0, amplified);
    if (changed) super.setChanged();
  }

  public boolean amplificationIsAvailable() {
    var recipe = amplifierSlot.process();
    return amplifier.available() > 0 || recipe != null && recipe.getOutput() > 0;
  }

  public boolean attemptGeneration() {
    double cost = energy.getCapacity();
    if (cost <= 0 || energy.getEnergy() < cost) return false;
    var product = Ic2FluidStack.create(FluidDefinitions.UU_MATTER.still, 1);
    if (fluidTank.fillMbUnchecked(product, true) != 1) return false;
    if (!energy.useEnergy(cost)) return false;
    fluidTank.fillMbUnchecked(product, false);
    return true;
  }

  public String getProgressAsString() {
    return Math.clamp((int) (100 * energy.getFillRatio()), 0, 100) + "%";
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.add("state");
    return fields;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_FABRICATOR_LOOP;
  }

  @Override
  public SoundEvent getSubLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_FABRICATOR_SCRAP;
  }

  @Override
  public double getEnergy() {
    return energy.getEnergy();
  }

  @Override
  public boolean useEnergy(double amount) {
    return energy.useEnergy(amount);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return PROPERTIES;
  }

  @Override
  public boolean shouldExplode() {
    return true;
  }

  @Override
  public float getExplosionPower(int tier, float defaultPower) {
    return 15;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new MatterMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new MatterMenu(syncId, inventory, this);
  }
}
