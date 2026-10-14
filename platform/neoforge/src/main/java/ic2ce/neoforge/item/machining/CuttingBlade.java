package ic2ce.neoforge.item.machining;

import ic2ce.api.item.IBlockCuttingBlade;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;

/** Native block-cutter tooling reads one immutable machining-material descriptor. */
public class CuttingBlade extends Item implements IBlockCuttingBlade {
  private final BladeMaterial material;

  public CuttingBlade(Properties properties, BladeMaterial material) {
    super(properties);
    this.material = Objects.requireNonNull(material);
  }

  @Override
  public int getHardness(ItemStack stack) {
    return material.hardness;
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> output,
      TooltipFlag flags) {
    output.accept(Component.translatable(material.tooltip));
    output.accept(Component.translatable("ic2.CuttingBlade.hardness", material.hardness));
  }
}
