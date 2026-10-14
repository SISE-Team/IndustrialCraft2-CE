package ic2ce.neoforge.item.tool;

import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Iridium drilling keeps its distinct mining policy and toggles only its controlled fortune/silk
 * enchantments.
 */
public class IridiumDrill extends MiningDrill {
  private static final ToolMaterial MATERIAL =
      new ToolMaterial(
          net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
          3000,
          15,
          9,
          33,
          net.minecraft.tags.TagKey.create(
              Registries.ITEM, ProjectRuntime.getIdentifier("iridium_plate")));

  public IridiumDrill(Properties properties) {
    super(properties, 800, MATERIAL, 300000, 1000, 3, 24);
  }

  private static void enchant(
      ItemStack stack, net.minecraft.core.HolderLookup.Provider lookup, boolean silk) {
    var registry = lookup.lookupOrThrow(Registries.ENCHANTMENT);
    var fortune = registry.get(Enchantments.FORTUNE).orElse(null);
    var delicate = registry.get(Enchantments.SILK_TOUCH).orElse(null);
    EnchantmentHelper.updateEnchantments(
        stack,
        values -> {
          if (fortune != null) values.set(fortune, silk ? 0 : 3);
          if (delicate != null) values.set(delicate, silk ? 1 : 0);
        });
  }

  @Override
  protected ItemStack getItemStack(double charge) {
    var stack = super.getItemStack(charge);
    var server = ProjectRuntime.envProxy.getServer();
    var world = server == null ? ProjectRuntime.sideProxy.getPlayerWorld() : server.overworld();
    if (world != null) enchant(stack, world.registryAccess(), false);
    return stack;
  }

  @Override
  public InteractionResult use(Level world, Player player, InteractionHand hand) {
    var stack = player.getItemInHand(hand);
    if (!world.isClientSide()
        && stack.is(this)
        && ProjectRuntime.keyboard.isModeSwitchKeyDown(player)) {
      var silk =
          world
              .registryAccess()
              .lookupOrThrow(Registries.ENCHANTMENT)
              .get(Enchantments.SILK_TOUCH)
              .orElse(null);
      boolean enabled = silk != null && EnchantmentHelper.getItemEnchantmentLevel(silk, stack) == 0;
      enchant(stack, world.registryAccess(), enabled);
      ProjectRuntime.sideProxy.messagePlayer(
          player,
          "ic2.tooltip.mode",
          enabled ? "ic2.tooltip.mode.silkTouch" : "ic2.tooltip.mode.normal");
      return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }
    return super.use(world, player, hand);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    return context.getPlayer() != null
            && ProjectRuntime.keyboard.isModeSwitchKeyDown(context.getPlayer())
        ? InteractionResult.PASS
        : super.useOn(context);
  }
}
