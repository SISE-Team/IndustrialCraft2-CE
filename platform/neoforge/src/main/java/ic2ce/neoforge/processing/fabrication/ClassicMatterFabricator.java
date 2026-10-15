package ic2ce.neoforge.processing.fabrication;

import ic2ce.api.energy.tile.IExplosionPowerOverride;
import ic2ce.api.recipe.*;
import ic2ce.engine.machine.AmplificationBudget;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.component.SignalInput;
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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Classic item fabrication uses fractional amplification and reserves one paid solid UU output. */
public class ClassicMatterFabricator extends PoweredBlockEntity
    implements IHasGui, IExplosionPowerOverride {
  @GuiSynced public int scrap;
  private double lastEnergy;
  private final AmplificationBudget amplifier = new AmplificationBudget();
  public final RecipeInputInventory<IRecipeInput, Integer, ItemStack> amplifierSlot;
  public final OutputInventory outputSlot;
  protected final SignalInput redstone;

  public ClassicMatterFabricator(BlockPos position, BlockState state) {
    super(
        BlockEntityDefinitions.CLASSIC_MASS_FABRICATOR,
        position,
        state,
        FabricationSettings.energyCost(),
        AmplifiedMatterFabricator.DEFAULT_TIER,
        false);
    amplifierSlot =
        new AmplifierInput(
            this,
            world ->
                Recipes.matterAmplifier == null
                    ? Recipes.matterFabricator.get(world)
                    : Recipes.matterAmplifier);
    outputSlot = new OutputInventory(this, "output", 1);
    redstone = addComponent(new SignalInput(this));
    redstone.subscribe(signal -> energy.setEnabled(signal == 0));
    comparator.setUpdate(
        () -> {
          int supplied = calcRedstoneFromInvSlots(amplifierSlot);
          return supplied > 0 ? supplied : amplifier.available() > 0 ? 1 : 0;
        });
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
    if (!getLevel().isClientSide()) energy.setEnabled(!redstone.hasRedstoneInput());
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (scrap != (int) amplifier.available()) amplifier.restore(scrap);
    boolean changed = false, amplified = false;
    if (!redstone.hasRedstoneInput()) {
      if (amplifier.available() < 10000) {
        var recipe = amplifierSlot.process();
        if (recipe != null
            && recipe.getOutput() > 0
            && recipe.getOutput() <= Integer.MAX_VALUE - amplifier.available()) {
          amplifierSlot.consume(recipe);
          amplifier.add(recipe.getOutput());
          changed = true;
        }
      }
      double consumed = amplifier.consume(Math.max(0, energy.getEnergy() - lastEnergy));
      if (consumed > 0) {
        energy.forceAddEnergy(5 * consumed);
        amplified = changed = true;
      }
      changed |= attemptGeneration();
    }
    scrap = (int) amplifier.available();
    lastEnergy = energy.getEnergy();
    setActiveState(!redstone.hasRedstoneInput() && energy.getEnergy() > 0, amplified);
    if (changed) setChanged();
  }

  public boolean amplificationIsAvailable() {
    var recipe = amplifierSlot.process();
    return amplifier.available() > 0 || recipe != null && recipe.getOutput() > 0;
  }

  public boolean attemptGeneration() {
    double cost = energy.getCapacity();
    var product = new ItemStack(ItemDefinitions.UU_MATTER);
    if (cost <= 0
        || energy.getEnergy() < cost
        || !outputSlot.canAdd(product)
        || !energy.useEnergy(cost)) return false;
    outputSlot.add(product);
    return true;
  }

  public void playScrapSound() {
    playLoopingSound(true);
  }

  public String getProgressAsString() {
    return Math.clamp((int) (100 * energy.getFillRatio()), 0, 100) + "%";
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
  public boolean getGuiState(String key) {
    return key.equals("scrap")
        ? scrap > 0
        : key.equals("dev") ? DevelopmentMode.active() : super.getGuiState(key);
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
