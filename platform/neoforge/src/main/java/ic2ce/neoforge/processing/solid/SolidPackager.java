package ic2ce.neoforge.processing.solid;

import ic2ce.api.recipe.ICannerBottleRecipeManager;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.neoforge.inventory.SolidCanningContainerInput;
import ic2ce.neoforge.inventory.SolidCanningInput;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.processing.ProcessingBlockEntities;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** A solid-only canner commits its container and filling as one recipe adjustment. */
public class SolidPackager
    extends PoweredProcessor<
        ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> {
  public final SolidCanningContainerInput canInputSlot;

  public SolidPackager(BlockPos position, BlockState state) {
    super(ProcessingBlockEntities.SOLID_CANNER, position, state, 2, 200, 1);
    inputSlot = new SolidCanningInput(this, "input", 1);
    canInputSlot = new SolidCanningContainerInput(this, "canInput", 1);
  }

  @Override
  protected Collection<ItemStack> getOutput(ItemStack output) {
    return List.of(output.copy());
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_CANNER_OPERATE;
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
}
