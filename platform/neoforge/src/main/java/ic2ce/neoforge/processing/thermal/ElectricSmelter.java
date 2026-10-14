package ic2ce.neoforge.processing.thermal;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.recipe.MachineRecipeResult;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.engine.machine.ExperienceAccount;
import ic2ce.neoforge.inventory.SmeltingInput;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Smelting preserves fractional experience, paying it only on an authorized server interaction. */
public class ElectricSmelter extends PoweredProcessor<ItemStack, ItemStack, ItemStack>
    implements INetworkClientTileEntityEventListener {
  protected double xp;

  public ElectricSmelter(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.ELECTRIC_FURNACE, position, state, 3, 100, 1);
    inputSlot = new SmeltingInput(this, "input", 1);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    xp = ExperienceAccount.normalize(data.getDoubleOr("xp", 0));
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putDouble("xp", xp);
  }

  @Override
  protected Collection<ItemStack> getOutput(ItemStack output) {
    return List.of(output.copy());
  }

  @Override
  public void operateOnce(
      MachineRecipeResult<ItemStack, ItemStack, ItemStack> recipe, Collection<ItemStack> output) {
    super.operateOnce(recipe, output);
    var metadata = recipe.getRecipe().getMetaData();
    float earned = metadata == null ? 0 : metadata.getFloatOr("experience", 0);
    xp = ExperienceAccount.earn(xp, earned);
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event == 0
        && !getLevel().isClientSide()
        && player.level() == getLevel()
        && player.distanceToSqr(
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5)
            <= 64) {
      xp = FuelSmelter.spawnXP(player, xp);
      setChanged();
    }
  }

  @Override
  public SoundEvent getStartSoundEvent() {
    return SoundDefinitions.MACHINE_FURNACE_ELECTRIC_START;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_FURNACE_ELECTRIC_LOOP;
  }

  @Override
  public SoundEvent getInterruptSoundEvent() {
    return SoundDefinitions.MACHINE_INTERRUPT1;
  }

  @Override
  public SoundEvent getStopSoundEvent() {
    return SoundDefinitions.MACHINE_FURNACE_ELECTRIC_STOP;
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.Processing,
        UpgradableProperty.Transformer,
        UpgradableProperty.EnergyStorage,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing);
  }
}
