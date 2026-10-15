package ic2ce.neoforge.item.tool;

import ic2ce.api.item.IEnhancedOverlayProvider;
import ic2ce.neoforge.transport.cover.ICoverHolder;
import ic2ce.neoforge.item.PriorityUsableItem;
import ic2ce.core.util.RotationUtil;
import ic2ce.neoforge.material.CommonMaterialTags;
import ic2ce.neoforge.registry.ItemGroups;
import ic2ce.neoforge.registry.SoundDefinitions;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Permission-checked cover removal uses block-local hit coordinates and one actual server wear
 * bill.
 */
public class CoverCrowbar extends Item implements IEnhancedOverlayProvider, PriorityUsableItem {
  public CoverCrowbar(ToolMaterial material, Properties properties) {
    super(properties.durability(material.durability()).repairable(ItemGroups.BRONZE_INGOTS));
  }

  public boolean canTakeDamage(ItemStack stack, int amount) {
    return stack != null
        && stack.is(this)
        && amount >= 0
        && amount <= stack.getMaxDamage() - stack.getDamageValue();
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    var owner = context.getPlayer();
    var world = context.getLevel();
    var pos = context.getClickedPos();
    if (owner == null
        || !stack.is(this)
        || !owner.mayBuild()
        || !owner.mayUseItemAt(pos, context.getClickedFace(), stack)) return InteractionResult.PASS;
    if (!canTakeDamage(stack, 1)) return InteractionResult.FAIL;
    var tile = world.getBlockEntity(pos);
    if (!(tile instanceof ICoverHolder covers)) return InteractionResult.FAIL;
    var hit = context.getClickLocation();
    var selected =
        RotationUtil.rotateByHit(
            context.getClickedFace(),
            (float) (hit.x - pos.getX()),
            (float) (hit.y - pos.getY()),
            (float) (hit.z - pos.getZ()));
    if (!covers.canRemoveCover(world, pos, selected)) return InteractionResult.PASS;
    if (world.isClientSide()) return InteractionResult.SUCCESS;
    covers.removeCover(world, pos, selected);
    if (!owner.getAbilities().instabuild) stack.hurtAndBreak(1, owner, context.getHand());
    world.playSound(null, pos, SoundDefinitions.ITEM_CROWBAR_USE, SoundSource.BLOCKS, 1, 1);
    return InteractionResult.SUCCESS;
  }

  public boolean isValidRepairItem(ItemStack tool, ItemStack input) {
    return input != null && CommonMaterialTags.matches(input, ItemGroups.BRONZE_INGOTS);
  }

  public boolean isEnchantable(ItemStack stack) {
    return false;
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      Item.TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> output,
      TooltipFlag flags) {
    output.accept(Component.translatable("ic2.crowbar.tooltip"));
  }

  @Override
  public boolean providesEnhancedOverlay(
      Level world, BlockPos pos, Direction side, Player owner, ItemStack stack) {
    return stack.is(this) && world.getBlockEntity(pos) instanceof ICoverHolder;
  }
}
