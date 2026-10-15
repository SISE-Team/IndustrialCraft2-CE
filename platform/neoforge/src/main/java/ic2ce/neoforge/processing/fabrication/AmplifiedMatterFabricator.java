package ic2ce.neoforge.processing.fabrication;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.IExplosionPowerOverride;
import ic2ce.api.recipe.*;
import ic2ce.api.upgrade.*;
import ic2ce.engine.machine.AmplificationBudget;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.diagnostics.DevelopmentMode;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.processing.FabricationSettings;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

/** Mandatory-amplifier fabrication meters tenfold scrap credit independently of its stored EU. */
public class AmplifiedMatterFabricator extends PoweredBlockEntity
    implements IHasGui, IUpgradableBlock, IExplosionPowerOverride {
  @GuiSynced public int scrap, consumedScrap;
  protected double maxScrapConsumption;
  public static final int DEFAULT_TIER =
      NativeConfigValues.getInt(ProjectConfiguration.get(), "balance/massFabricatorTier");
  private final int requiredScrap = FabricationSettings.energyCost();
  private final AmplificationBudget amplifier = new AmplificationBudget();
  private double processedScrap;
  public final RecipeInputInventory<IRecipeInput, Integer, ItemStack> amplifierSlot;
  public final OutputInventory outputSlot;
  public final UpgradeInventory upgradeSlot;
  protected final SignalInput redstone;
  private static final Set<UpgradableProperty> PROPERTIES =
      Set.of(
          UpgradableProperty.RedstoneSensitive,
          UpgradableProperty.Transformer,
          UpgradableProperty.ItemConsuming,
          UpgradableProperty.ItemProducing);

  public AmplifiedMatterFabricator(BlockPos position, BlockState state) {
    super(
        BlockEntityDefinitions.MASS_FABRICATOR,
        position,
        state,
        FabricationSettings.energyCost(),
        DEFAULT_TIER,
        false);
    amplifierSlot = new AmplifierInput(this, Recipes.matterFabricator);
    outputSlot = new OutputInventory(this, "output", 1);
    upgradeSlot = new UpgradeInventory(this, "upgrade", 4);
    redstone = addComponent(new SignalInput(this));
    redstone.subscribe(signal -> energy.setEnabled(signal == 0));
    maxScrapConsumption = EnergyNet.instance.getPowerFromTier(DEFAULT_TIER);
    comparator.setUpdate(
        () -> {
          int input = calcRedstoneFromInvSlots(amplifierSlot);
          return input > 0 ? input : amplifier.available() > 0 ? 1 : 0;
        });
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    scrap = Math.max(0, data.getIntOr("scrap", 0));
    consumedScrap = Math.clamp(data.getIntOr("consumedScrap", 0), 0, requiredScrap);
    double fraction = data.getDoubleOr("scrapFraction", 0);
    amplifier.restore(
        scrap + (Double.isFinite(fraction) && fraction >= 0 && fraction < 1 ? fraction : 0));
    fraction = data.getDoubleOr("consumedScrapFraction", 0);
    processedScrap =
        Math.min(
            requiredScrap,
            consumedScrap
                + (Double.isFinite(fraction) && fraction >= 0 && fraction < 1 ? fraction : 0));
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("scrap", scrap);
    data.putInt("consumedScrap", consumedScrap);
    data.putDouble("scrapFraction", amplifier.available() - Math.floor(amplifier.available()));
    data.putDouble("consumedScrapFraction", processedScrap - Math.floor(processedScrap));
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (!getLevel().isClientSide()) {
      updateUpgrades();
      energy.setEnabled(!redstone.hasRedstoneInput());
    }
  }

  @Override
  public void setChanged() {
    super.setChanged();
    if (getLevel() != null && !getLevel().isClientSide() && upgradeSlot != null) updateUpgrades();
  }

  public void updateUpgrades() {
    upgradeSlot.onChanged();
    int tier = upgradeSlot.getTier(DEFAULT_TIER);
    energy.setSinkTier(tier);
    dischargeSlot.setTier(tier);
    maxScrapConsumption = EnergyNet.instance.getPowerFromTier(tier);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean changed = upgradeSlot.tickNoMark(), worked = false;
    if (scrap != (int) amplifier.available()) amplifier.restore(scrap);
    if (consumedScrap != (int) processedScrap)
      processedScrap = Math.clamp(consumedScrap, 0, requiredScrap);
    if (!redstone.hasRedstoneInput() && energy.getEnergy() > 0) {
      if (amplifier.available() < 100000) {
        var recipe = amplifierSlot.process();
        long supplied = recipe == null ? 0 : (long) recipe.getOutput() * 10;
        if (supplied > 0 && supplied <= Integer.MAX_VALUE - amplifier.available()) {
          amplifierSlot.consume(recipe);
          amplifier.add((int) supplied);
          changed = true;
        }
      }
      double eligible =
          Math.min(
              Math.max(0, energy.getEnergy() - processedScrap),
              Math.min(Math.max(0, maxScrapConsumption), requiredScrap - processedScrap));
      double consumed = amplifier.consume(eligible);
      processedScrap += consumed;
      if (consumed > 0) worked = changed = true;
      var output = new ItemStack(ItemDefinitions.UU_MATTER);
      if (processedScrap >= requiredScrap
          && energy.getEnergy() >= energy.getCapacity()
          && outputSlot.canAdd(output)
          && energy.useEnergy(energy.getCapacity())) {
        outputSlot.add(output);
        processedScrap -= requiredScrap;
        worked = changed = true;
      }
    }
    scrap = (int) amplifier.available();
    consumedScrap = (int) processedScrap;
    setActiveState(worked, worked);
    if (changed) super.setChanged();
  }

  public int getScrap() {
    return Math.max(0, scrap) / 10;
  }

  public int getScrapProgress() {
    return Math.clamp((int) (100.0 * Math.max(0, consumedScrap) / requiredScrap), 0, 100);
  }

  public int getEnergyProgress() {
    return Math.clamp((int) (100 * energy.getFillRatio()), 0, 100);
  }

  @Override
  public boolean getGuiState(String key) {
    return key.equals("scrap")
        ? scrap > 0
        : key.equals("dev") ? DevelopmentMode.active() : super.getGuiState(key);
  }

  @Override
  public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag flags) {
    super.addInformation(stack, tooltip, flags);
    tooltip.add(
        LocalizedText.translate(
            "ic2.tooltip.preferred_machine",
            LocalizedText.translate(ItemDefinitions.MATTER_GENERATOR.getDescriptionId())));
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
    return DocumentMenu.create(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(syncId, inventory, this);
  }
}
