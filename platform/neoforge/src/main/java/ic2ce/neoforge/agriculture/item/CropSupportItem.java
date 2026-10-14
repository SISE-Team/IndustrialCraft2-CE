package ic2ce.neoforge.agriculture.item;

import ic2ce.api.crops.CropSoilType;
import ic2ce.api.item.IBoxable;
import ic2ce.neoforge.registry.BlockDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crop supports enforce their soil policy before the native collision/permission placement gate.
 */
public class CropSupportItem extends BlockItem implements IBoxable {
  public CropSupportItem(Properties properties) {
    super(BlockDefinitions.CROP_STICK, properties);
  }

  @Override
  protected boolean canPlace(BlockPlaceContext context, BlockState state) {
    var world = context.getLevel();
    var position = context.getClickedPos();
    return !world.isOutsideBuildHeight(position)
        && CropSoilType.contains(world.getBlockState(position.below()).getBlock())
        && super.canPlace(context, state);
  }

  @Override
  public boolean canBeStoredInToolbox(ItemStack stack) {
    return stack != null && !stack.isEmpty();
  }
}
