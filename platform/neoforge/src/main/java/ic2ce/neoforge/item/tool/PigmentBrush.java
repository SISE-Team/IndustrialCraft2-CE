package ic2ce.neoforge.item.tool;

import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.item.paint.VanillaColorBlockId;
import ic2ce.neoforge.item.paint.PigmentPalette;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.SideServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Authoritative pigment use, native wear and owned inventory refills; the interaction keeps its
 * original stack alive.
 */
public class PigmentBrush extends DurableCraftingTool {
  private final PigmentPalette color;

  public PigmentBrush(Properties properties, PigmentPalette color) {
    super(properties);
    this.color = color;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var held = context.getItemInHand();
    if (color == null || held.isEmpty() || !held.is(this)) return InteractionResult.PASS;
    var world = context.getLevel();
    var position = context.getClickedPos();
    var player = context.getPlayer();
    if (player != null && !player.mayUseItemAt(position, context.getClickedFace(), held))
      return InteractionResult.PASS;
    if (world.isClientSide()) {
      if (!BlockPaintService.canPaint(world, position, color.dyeColor))
        return InteractionResult.PASS;
      if (player != null) player.playSound(SoundDefinitions.ITEM_PAINTER_USE, 1, 1);
      return InteractionResult.SUCCESS;
    }
    if (!BlockPaintService.paint(
        world,
        position,
        color.dyeColor,
        target ->
            player == null
                || world.mayInteract(player, target)
                    && player.mayUseItemAt(target, context.getClickedFace(), held)))
      return InteractionResult.PASS;
    damagePainter(held, player, context.getHand(), color);
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack held, Player player, LivingEntity entity, InteractionHand hand) {
    if (color == null
        || held.isEmpty()
        || !held.is(this)
        || !(entity instanceof Sheep sheep)
        || !sheep.isAlive()
        || sheep.getColor() == color.dyeColor) return InteractionResult.PASS;
    if (!player.level().isClientSide()) {
      sheep.setColor(color.dyeColor);
      damagePainter(held, player, hand, color);
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResult use(Level world, Player player, InteractionHand hand) {
    if (world.isClientSide() || !SideServices.keyboard().isModeSwitchKeyDown(player))
      return InteractionResult.PASS;
    var held = player.getItemInHand(hand);
    if (!held.is(this)) return InteractionResult.PASS;
    var data = StackTag.getOrCreate(held);
    boolean enabled = !data.getBooleanOr("autoRefill", false);
    data.putBoolean("autoRefill", enabled);
    StackTag.set(held, data);
    SideServices.proxy().messagePlayer(
        player, enabled ? "ic2.painter.refill.enabled" : "ic2.painter.refill.disabled");
    return InteractionResult.SUCCESS.heldItemTransformedTo(held);
  }

  public void damagePainter(
      ItemStack held, Player player, InteractionHand hand, PigmentPalette pigment) {
    if (pigment == null
        || !(player instanceof ServerPlayer serverPlayer)
        || player.getAbilities().instabuild
        || held != player.getItemInHand(hand)
        || !held.is(this)) return;
    var worn = held.copy();
    worn.hurtAndBreak(1, serverPlayer.level(), serverPlayer, _ -> {});
    if (!worn.isEmpty()) {
      held.setDamageValue(worn.getDamageValue());
      return;
    }
    var metadata = StackTag.get(held);
    boolean refill = metadata != null && metadata.getBooleanOr("autoRefill", false);
    ItemStack replacement = ItemStack.EMPTY;
    if (refill) {
      var inventory = player.getInventory();
      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
        var candidate = inventory.getItem(slot);
        if (candidate == held || !candidate.is(this) || getRemainingUses(candidate) <= 0) continue;
        replacement = inventory.removeItem(slot, 1);
        if (!replacement.isEmpty()) break;
      }
    }
    var blank = new ItemStack(ItemDefinitions.PAINTER);
    if (replacement.isEmpty()) player.setItemInHand(hand, blank);
    else {
      StackTag.update(
          replacement,
          data -> {
            data.putBoolean("autoRefill", true);
            return data;
          });
      player.setItemInHand(hand, replacement);
      if (!player.getInventory().add(blank)) player.drop(blank, false);
    }
    player.getInventory().setChanged();
  }

  public static boolean canColor(Block block, DyeColor color) {
    if (block == null || color == null) return false;
    var family = BlockPaintService.family(block.defaultBlockState());
    return family != null && BlockPaintService.colorBlock(color, family) != block;
  }

  public static BlockState getColorBlockState(DyeColor color, VanillaColorBlockId family) {
    return BlockPaintService.colorBlock(color, family).defaultBlockState();
  }

  public static BlockState getBlockStateWithProperties(
      DyeColor color, VanillaColorBlockId family, BlockState previous) {
    return BlockPaintService.colorBlock(color, family).withPropertiesOf(previous);
  }
}
