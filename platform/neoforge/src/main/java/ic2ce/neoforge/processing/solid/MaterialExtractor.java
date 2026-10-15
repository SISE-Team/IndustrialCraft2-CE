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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Extraction policy binds live extraction recipes to the standard processing clock. */
public class MaterialExtractor
    extends PoweredProcessor<IRecipeInput, Collection<ItemStack>, ItemStack> {
  public MaterialExtractor(BlockPos position, BlockState state) {
    super(ProcessingBlockEntities.EXTRACTOR, position, state, 2, 300, 1);
    inputSlot = new ItemRecipeInput(this, "input", 1, Recipes.extractor);
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_EXTRACTOR_OPERATE;
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
    return getClass() == MaterialExtractor.class;
  }
}
