package ic2.core.item.tool;

import com.google.common.collect.UnmodifiableIterator;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.Ic2Color;
import ic2.core.util.StackUtil;
import ic2.core.util.VanillaColorBlockId;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class ItemToolPainter extends ItemToolCrafting implements IBoxable {
   Ic2Color color = null;
   private static final int maxDamage = 32;

   public ItemToolPainter(Properties settings, Ic2Color color) {
      super(settings);
      this.color = color;
   }

   public InteractionResult useOn(UseOnContext context) {
      if (this.color == null) {
         return InteractionResult.PASS;
      } else {
         ItemStack stack = context.getItemInHand();
         Level world = context.getLevel();
         BlockPos pos = context.getClickedPos();
         Player player = context.getPlayer();
         InteractionHand hand = context.getHand();
         if (!(stack.getItem() instanceof ItemToolPainter)) {
            return InteractionResult.PASS;
         } else {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (this.colorBlock(world, pos, block, state, this.color)) {
               this.damagePainter(stack, player, hand, this.color);
               if (world.isClientSide() && player != null) {
                  player.playSound(Ic2SoundEvents.ITEM_PAINTER_USE, 1.0F, 1.0F);
               }

               return world.isClientSide() ? InteractionResult.PASS : InteractionResult.SUCCESS;
            } else {
               return InteractionResult.PASS;
            }
         }
      }
   }

   private boolean colorBlock(Level world, BlockPos pos, Block block, BlockState state, Ic2Color color) {
      DyeColor newColor = color.dyeColor;
      java.util.Iterator<net.minecraft.world.level.block.state.properties.Property<?>> tagList = state.getProperties().iterator();

      while (tagList.hasNext()) {
         Property<?> property = (Property<?>)tagList.next();
         if (property.getValueClass() == DyeColor.class) {
            DyeColor oldColor = (DyeColor)state.getValue(property);
            if (oldColor != newColor && property.getPossibleValues().contains(newColor)) {
               world.setBlockAndUpdate(pos, (BlockState)setValueGeneric(state, property, newColor));
               return true;
            }

            return false;
         }
      }

      if (!canColor(block, color.dyeColor)) {
         return false;
      } else {
         List<TagKey<Block>> tagListx = block.builtInRegistryHolder().tags().toList();
         if (tagListx.contains(BlockTags.WOOL)) {
            world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.WOOL));
            return true;
         } else if (block instanceof StainedGlassBlock || block.defaultBlockState().is(Blocks.GLASS)) {
            world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.STAINED_GLASS));
            return true;
         } else if (block instanceof StainedGlassPaneBlock || block.defaultBlockState().is(Blocks.GLASS_PANE)) {
            world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.STAINED_GLASS_PANE, state));
            return true;
         } else if (tagListx.contains(BlockTags.BEDS)) {
            BedBlock bedBlock = (BedBlock)block;
            BlockPos bedBlockPos2 = pos.relative(BedBlock.getConnectedDirection(state));
            BlockState bedBlockState2 = world.getBlockState(bedBlockPos2);
            if (bedBlockState2.is(bedBlock)) {
               world.setBlock(pos, Blocks.AIR.defaultBlockState(), 48);
               world.setBlock(bedBlockPos2, Blocks.AIR.defaultBlockState(), 48);
               world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.BED, state));
               world.setBlockAndUpdate(bedBlockPos2, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.BED, bedBlockState2));
            }

            return true;
         } else if (tagListx.contains(BlockTags.CANDLES)) {
            world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.CANDLE, state));
            return true;
         } else if (block instanceof BannerBlock) {
            world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.BANNER, state));
            return true;
         } else if (block instanceof WallBannerBlock) {
            world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.WALL_BANNER, state));
            return true;
         } else if (tagListx.contains(BlockTags.TERRACOTTA)) {
            world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.TERRACOTTA));
            return true;
         } else if (block instanceof GlazedTerracottaBlock) {
            world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.GLAZED_TERRACOTTA, state));
            return true;
         } else if (block instanceof ConcretePowderBlock) {
            world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.CONCRETE_POWDER));
            return true;
         } else if (tagListx.contains(BlockTags.WOOL_CARPETS)) {
            world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.CARPET));
            return true;
         } else if (tagListx.contains(BlockTags.SHULKER_BOXES)) {
            BlockEntity shulkerBlockEntity = world.getBlockEntity(pos);
            if (shulkerBlockEntity == null) {
               return false;
            } else {
               CompoundTag shulkerNbt = shulkerBlockEntity.saveWithFullMetadata(shulkerBlockEntity.getLevel().registryAccess());
               BlockState newShulkerBoxState = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(color.dyeColor.getName() + "_shulker_box")).withPropertiesOf(state);
               world.setBlockAndUpdate(pos, newShulkerBoxState);
               BlockEntity newShulkerBlockEntity = BlockEntity.loadStatic(pos, newShulkerBoxState, shulkerNbt, world.registryAccess());
               world.setBlockEntity(newShulkerBlockEntity);
               return true;
            }
         } else {
            Identifier identifier = BuiltInRegistries.BLOCK.getKey(block);
            if (identifier.getNamespace().equals("minecraft") && identifier.getPath().contains("concrete")) {
               world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.CONCRETE));
               return true;
            } else {
               return false;
            }
         }
      }
   }

   public static boolean canColor(Block block, DyeColor color) {
      Identifier identifier = BuiltInRegistries.BLOCK.getKey(block);
      return !identifier.getPath().contains(color.getName());
   }

   public static BlockState getColorBlockState(DyeColor color, VanillaColorBlockId vanillaColorBlock) {
      Identifier identifier = Identifier.fromNamespaceAndPath("minecraft", color.getName() + "_" + vanillaColorBlock.id);
      return ((Block)BuiltInRegistries.BLOCK.getValue(identifier)).defaultBlockState();
   }

   public static BlockState getBlockStateWithProperties(DyeColor color, VanillaColorBlockId vanillaColorBlock, BlockState state) {
      Identifier identifier = Identifier.fromNamespaceAndPath("minecraft", color.getName() + "_" + vanillaColorBlock.id);
      return ((Block)BuiltInRegistries.BLOCK.getValue(identifier)).withPropertiesOf(state);
   }

   public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
      if (this.color == null) {
         return InteractionResult.PASS;
      } else if (entity instanceof Sheep sheep && sheep.getColor() != this.color.dyeColor) {
         sheep.setColor(this.color.dyeColor);
         this.damagePainter(stack, user, user.getUsedItemHand(), this.color);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResult use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      if (!world.isClientSide() && IC2.keyboard.isModeSwitchKeyDown(player)) {
         CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
         boolean newValue = !nbtData.getBooleanOr("autoRefill", false);
         nbtData.putBoolean("autoRefill", newValue);
         ic2.core.util.StackTag.set(stack, nbtData);
         if (newValue) {
            IC2.sideProxy.messagePlayer(player, "ic2.painter.refill.enabled");
         } else {
            IC2.sideProxy.messagePlayer(player, "ic2.painter.refill.disabled");
         }

         return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
      } else {
         return InteractionResult.PASS;
      }
   }

   public void damagePainter(ItemStack stack, Player player, InteractionHand hand, Ic2Color color) {
      assert color != null;

      stack.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)player.level(), player instanceof ServerPlayer serverPlayer ? serverPlayer : null, broken -> {});
      if (stack.getDamageValue() >= stack.getMaxDamage()) {
         CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
         if (!nbtData.getBooleanOr("autoRefill", false)) {
            player.setItemInHand(hand, new ItemStack(Ic2Items.PAINTER, 1));
            return;
         }

         ItemStack consumedStack = StackUtil.consumeFromPlayerInventoryAndGet(player, StackUtil.sameItem(stack.getItem()), 1, true);
         if (consumedStack.isEmpty()) {
            player.setItemInHand(hand, new ItemStack(Ic2Items.PAINTER, 1));
         } else {
            player.setItemInHand(hand, consumedStack);
            StackUtil.addToPlayerInventory(player, new ItemStack(Ic2Items.PAINTER, 1));
         }
      }
   }

   @Override
   public boolean canBeStoredInToolbox(ItemStack itemstack) {
      return super.canBeStoredInToolbox(itemstack);
   }
   @SuppressWarnings({"unchecked", "rawtypes"})
   private static BlockState setValueGeneric(BlockState state, net.minecraft.world.level.block.state.properties.Property property, Comparable value) {
      return state.setValue(property, value);
   }
}
