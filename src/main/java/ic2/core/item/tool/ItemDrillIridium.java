package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

@NotClassic
public class ItemDrillIridium extends ItemDrill {
   private static final net.minecraft.world.item.ToolMaterial IRIDIUM_TOOL_MATERIAL = new net.minecraft.world.item.ToolMaterial(
      net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3000, 15.0F, 9.0F, 33,
      net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, ic2.core.IC2.getIdentifier("iridium_plate"))
   );

   public ItemDrillIridium(Properties settings) {
      super(settings, 800, IRIDIUM_TOOL_MATERIAL, 300000, 1000, 3, 24.0F);
   }

   private static void applyEnchantments(ItemStack stack, boolean silkTouch) {
      // 26.1.2: enchantments are data-driven; resolve holders from the running server.
      net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
      if (server == null) {
         return;
      }
      var registry = server.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
      net.minecraft.world.item.enchantment.ItemEnchantments.Mutable enchantments = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(
         net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY
      );
      enchantments.set(registry.getOrThrow(Enchantments.FORTUNE), 3);
      if (silkTouch) {
         enchantments.set(registry.getOrThrow(Enchantments.SILK_TOUCH), 1);
      }

      EnchantmentHelper.setEnchantments(stack, enchantments.toImmutable());
   }

   @Override
   protected ItemStack getItemStack(double charge) {
      ItemStack ret = super.getItemStack(charge);
      applyEnchantments(ret, false);
      return ret;
   }

   @Override
   public InteractionResult use(Level world, Player player, InteractionHand hand) {
      if (!world.isClientSide() && IC2.keyboard.isModeSwitchKeyDown(player)) {
         ItemStack stack = StackUtil.get(player, hand);
         var lookup = player.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
         boolean silkTouch = EnchantmentHelper.getItemEnchantmentLevel(lookup.getOrThrow(Enchantments.SILK_TOUCH), stack) == 0;
         applyEnchantments(stack, silkTouch);
         IC2.sideProxy.messagePlayer(player, "ic2.tooltip.mode", silkTouch ? "ic2.tooltip.mode.silkTouch" : "ic2.tooltip.mode.normal");
      }

      return super.use(world, player, hand);
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      return IC2.keyboard.isModeSwitchKeyDown(context.getPlayer()) ? InteractionResult.PASS : super.useOn(context);
   }
}
