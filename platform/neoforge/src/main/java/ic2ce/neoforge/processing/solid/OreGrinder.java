package ic2ce.neoforge.processing.solid;

import ic2ce.api.recipe.IRecipeInput;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.neoforge.inventory.ItemRecipeInput;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.processing.ProcessingBlockEntities;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Crushing recipes and client exhaust are separate from the energy/processing state machine. */
public class OreGrinder extends PoweredProcessor<IRecipeInput, Collection<ItemStack>, ItemStack> {
  public OreGrinder(BlockPos position, BlockState state) {
    super(ProcessingBlockEntities.MACERATOR, position, state, 2, 300, 1);
    inputSlot = new ItemRecipeInput(this, "input", 1, Recipes.macerator);
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    var world = getLevel();
    var random = world.getRandom();
    if (!getActive() || random.nextInt(8) != 0) return;
    for (int count = 0; count < 4; count++)
      world.addParticle(
          ParticleTypes.SMOKE,
          worldPosition.getX() + 0.2 + random.nextFloat() * 0.6,
          worldPosition.getY() + 1 + random.nextFloat() * 0.2,
          worldPosition.getZ() + 0.2 + random.nextFloat() * 0.6,
          0,
          0,
          0);
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_MACERATOR_OPERATE;
  }

  @Override
  public SoundEvent getInterruptSoundEvent() {
    return SoundDefinitions.MACHINE_INTERRUPT1;
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

  @Override
  protected boolean directRecipeOutputPreview() {
    return getClass() == OreGrinder.class;
  }
}
