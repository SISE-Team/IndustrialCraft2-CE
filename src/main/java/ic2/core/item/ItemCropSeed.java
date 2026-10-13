package ic2.core.item;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropSeed;
import ic2.core.crop.TileEntityCrop;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

public class ItemCropSeed extends Item implements ICropSeed {
   public ItemCropSeed(Properties settings) {
      super(settings);
   }

   public String getDescriptionId(ItemStack itemstack) {
      if (itemstack == null) {
         return "ic2.crop.unknown";
      } else {
         CropCard cropCard = Crops.instance.getCropCard(itemstack);
         int level = this.getScannedFromStack(itemstack);
         if (level == 0) {
            return "ic2.crop.unknown";
         } else {
            return level >= 0 && cropCard != null ? cropCard.getUnlocalizedName() : "ic2.crop.invalid";
         }
      }
   }

   public Component getName(ItemStack stack) {
      CropCard crop = Crops.instance.getCropCard(stack);
      return Component.translatable(crop == null ? "ic2.crop.seeds" : crop.getSeedType(), Component.translatable(this.getDescriptionId(stack)));
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack stack, Item.TooltipContext world, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> info, TooltipFlag debugTooltips) {
      if (this.getScannedFromStack(stack) >= 4) {
         info.accept(Component.translatable("ic2.crop.stat.growth", this.getGrowthFromStack(stack)).withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
         info.accept(Component.translatable("ic2.crop.stat.gain", this.getGainFromStack(stack)).withStyle(net.minecraft.ChatFormatting.GOLD));
         info.accept(Component.translatable("ic2.crop.stat.resistance", this.getResistanceFromStack(stack)).withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
      }
   }

   public InteractionResult useOn(UseOnContext context) {
      if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TileEntityCrop crop) {
         ItemStack stack = context.getItemInHand();
         if (context.getLevel().isClientSide()) return Crops.instance.getCropCard(stack) != null ? InteractionResult.SUCCESS : InteractionResult.PASS;
         if (crop.tryPlantIn(
            Crops.instance.getCropCard(stack),
            0,
            this.getGrowthFromStack(stack),
            this.getGainFromStack(stack),
            this.getResistanceFromStack(stack),
            this.getScannedFromStack(stack)
         )) {
            Player player = context.getPlayer();
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   public void addCreativeVariants(java.util.function.Consumer<ItemStack> output) {
         for (CropCard crop : Crops.instance.getCrops()) {
            output.accept(generateItemStackFromValues(crop, 1, 1, 1, 4));
   }
   }

   public static ItemStack generateItemStackFromValues(CropCard crop, int statGrowth, int statGain, int statResistance, int scan) {
      ItemStack stack = new ItemStack(Ic2Items.CROP_SEED_BACK);
      CompoundTag tag = new CompoundTag();
      tag.putString("owner", crop.getOwner());
      tag.putString("id", crop.getId());
      tag.putByte("growth", (byte)statGrowth);
      tag.putByte("gain", (byte)statGain);
      tag.putByte("resistance", (byte)statResistance);
      tag.putByte("scan", (byte)scan);
      ic2.core.util.StackTag.set(stack, tag);
      return stack;
   }

   @Override
   public CropCard getCropFromStack(ItemStack is) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      if (nbt != null && nbt.contains("owner") && nbt.contains("id")) {
         String owner = nbt.getStringOr("owner", "");
         String id = nbt.getStringOr("id", "");
         return Crops.instance.getCropCard(owner, id);
      } else {
         return null;
      }
   }

   @Override
   public void setCropFromStack(ItemStack is, CropCard crop) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      if (nbt != null) {
         nbt.putString("owner", crop.getOwner());
         ic2.core.util.StackTag.set(is, nbt);
         nbt.putString("id", crop.getId());
         ic2.core.util.StackTag.set(is, nbt);
      }
   }

   @Override
   public int getGrowthFromStack(ItemStack is) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      return nbt == null ? -1 : nbt.getByteOr("growth", (byte)0);
   }

   @Override
   public void setGrowthFromStack(ItemStack is, int value) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      if (nbt != null) {
         nbt.putByte("growth", (byte)value);
         ic2.core.util.StackTag.set(is, nbt);
      }
   }

   @Override
   public int getGainFromStack(ItemStack is) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      return nbt == null ? -1 : nbt.getByteOr("gain", (byte)0);
   }

   @Override
   public void setGainFromStack(ItemStack is, int value) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      if (nbt != null) {
         nbt.putByte("gain", (byte)value);
         ic2.core.util.StackTag.set(is, nbt);
      }
   }

   @Override
   public int getResistanceFromStack(ItemStack is) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      return nbt == null ? -1 : nbt.getByteOr("resistance", (byte)0);
   }

   @Override
   public void setResistanceFromStack(ItemStack is, int value) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      if (nbt != null) {
         nbt.putByte("resistance", (byte)value);
         ic2.core.util.StackTag.set(is, nbt);
      }
   }

   @Override
   public int getScannedFromStack(ItemStack is) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      return nbt == null ? -1 : nbt.getByteOr("scan", (byte)0);
   }

   @Override
   public void setScannedFromStack(ItemStack is, int value) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      if (nbt != null) {
         nbt.putByte("scan", (byte)value);
         ic2.core.util.StackTag.set(is, nbt);
      }
   }

   @Override
   public void incrementScannedFromStack(ItemStack is) {
      CompoundTag nbt = ic2.core.util.StackTag.get(is);
      if (nbt != null) {
         nbt.putByte("scan", (byte)(this.getScannedFromStack(is) + 1));
         ic2.core.util.StackTag.set(is, nbt);
      }
   }
}
