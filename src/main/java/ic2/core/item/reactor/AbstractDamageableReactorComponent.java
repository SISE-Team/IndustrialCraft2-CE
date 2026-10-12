package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public abstract class AbstractDamageableReactorComponent extends Item implements IReactorComponent {
   public static final String TOOLTIP_DURABILITY = "ic2.reactoritem.durability";
   private final int maxUse;

   protected AbstractDamageableReactorComponent(Properties settings, int maxUse) {
      super(settings);
      this.maxUse = maxUse;
   }

   @Override
   public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatrun) {
   }

   @Override
   public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun) {
      return false;
   }

   @Override
   public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y) {
      return false;
   }

   @Override
   public int getMaxHeat(ItemStack stack, IReactor reactor, int x, int y) {
      return 0;
   }

   @Override
   public int getCurrentHeat(ItemStack stack, IReactor reactor, int x, int y) {
      return 0;
   }

   @Override
   public int alterHeat(ItemStack stack, IReactor reactor, int x, int y, int heat) {
      return heat;
   }

   @Override
   public float influenceExplosion(ItemStack stack, IReactor reactor) {
      return 0.0F;
   }

   public void appendHoverText(ItemStack stack, Item.TooltipContext world, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag advanced) {
      tooltip.accept(
         Component.literal(Localization.translate("ic2.reactoritem.durability") + " " + (this.getMaxUse() - this.getUse(stack)) + "/" + this.getMaxUse())
            .withStyle(ChatFormatting.GRAY)
      );
   }

   @Override
   public boolean canBePlacedIn(ItemStack stack, IReactor reactor) {
      return true;
   }

   protected int getUse(ItemStack stack) {
      CompoundTag nbt = ic2.core.util.StackTag.get(stack);
      return nbt != null ? nbt.getIntOr("use", 0) : 0;
   }

   public void setUse(ItemStack stack, int use) {
      ic2.core.util.StackTag.update(stack, ic2Data -> { ic2Data.putInt("use", use); return ic2Data; });
   }

   protected void incrementUse(ItemStack stack) {
      ic2.core.util.StackTag.update(stack, ic2Data -> { ic2Data.putInt("use", Math.min(this.getUse(stack) + 1, this.maxUse)); return ic2Data; });
   }

   protected int getMaxUse() {
      return this.maxUse;
   }

   public double getUseFraction(ItemStack stack) {
      return Util.limit((double)this.getUse(stack) / this.maxUse, 0.0, 1.0);
   }

   public boolean isBarVisible(ItemStack stack) {
      return true;
   }

   public int getBarWidth(ItemStack stack) {
      return (int)Math.round((1.0 - this.getUseFraction(stack)) * 13.0);
   }

   public int getBarColor(ItemStack stack) {
      return Mth.hsvToRgb((float)((1.0 - this.getUseFraction(stack)) / 3.0), 1.0F, 1.0F);
   }
}
