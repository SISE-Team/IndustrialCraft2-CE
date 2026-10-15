package ic2ce.neoforge.item.container;

import ic2ce.api.fluid.Ic2FluidItem;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.agriculture.CropPlot;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.Mutable;

/** Fixed-volume cells and finite crop-care doses have separate quantity protocols. */
public class FixedCell extends WorldFluidCell implements Ic2FluidItem {
  private static final Map<Fluid, FixedCell> FILLED = new IdentityHashMap<>();
  private final Fluid fluid;
  private final int charges;

  public FixedCell(Properties properties, Fluid fluid, int charges) {
    super(fluid, properties);
    if (charges < 0) throw new IllegalArgumentException("Negative cell charges");
    this.fluid = fluid;
    this.charges = charges;
    if (fluid != null && fluid != Fluids.EMPTY) FILLED.put(fluid, this);
  }

  @Override
  public Item getEmptiedBucketItem() {
    return ItemDefinitions.EMPTY_CELL;
  }

  @Override
  public List<Fluid> getDrainableFluidList() {
    return List.of(Fluids.LAVA, Fluids.WATER);
  }

  @Override
  public Item getBucketItem(Fluid fluid) {
    return fluid == Fluids.WATER
        ? ItemDefinitions.WATER_CELL
        : fluid == Fluids.LAVA ? ItemDefinitions.LAVA_CELL : ItemDefinitions.EMPTY_CELL;
  }

  @Override
  public boolean bucketUseOnBlock(UseOnContext context) {
    if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof CropPlot crop)
        || this != ItemDefinitions.WATER_CELL
            && this != ItemDefinitions.WEED_EX_CELL
            && this != ItemDefinitions.HYDRATION_CELL) return false;
    if (context.getLevel().isClientSide()) return true;
    var player = context.getPlayer();
    var held = context.getItemInHand();
    boolean creative = player != null && player.getAbilities().instabuild;
    if (!useOnCrop(creative ? held.copy() : held, crop, true)) return false;
    if (!creative && this == ItemDefinitions.WATER_CELL && player != null) {
      var empty = new ItemStack(ItemDefinitions.EMPTY_CELL);
      if (held.isEmpty()) player.setItemInHand(context.getHand(), empty);
      else if (!player.getInventory().add(empty)) player.drop(empty, false);
    }
    return true;
  }

  public boolean useOnCrop(ItemStack stack, CropPlot crop, boolean manual) {
    if (stack == null
        || stack.isEmpty()
        || !stack.is(this)
        || crop.getLevel() == null
        || crop.getLevel().isClientSide()) return false;
    if (this == ItemDefinitions.WATER_CELL) {
      if (crop.getStorageWater() >= 10) return false;
      crop.setStorageWater(10);
      crop.setChanged();
      stack.shrink(1);
      return true;
    }
    int used = usage(stack), remaining = charges - used;
    if (remaining <= 0) return false;
    if (this == ItemDefinitions.WEED_EX_CELL) {
      if (crop.applyWeedEx(50, true, manual, false) != 50) return false;
      spend(stack, used + 1);
      return true;
    }
    if (this == ItemDefinitions.HYDRATION_CELL) {
      int offer = Math.min(manual ? 200 : 180, remaining);
      int accepted = crop.applyHydration(offer, false);
      if (accepted <= 0) return false;
      spend(stack, used + Math.min(remaining, accepted + 1));
      return true;
    }
    return false;
  }

  private int usage(ItemStack stack) {
    var data = StackTag.get(stack);
    return charges <= 1 || data == null ? 0 : Math.clamp(data.getIntOr("uses", 0), 0, charges);
  }

  private void spend(ItemStack stack, int used) {
    if (used >= charges) {
      stack.shrink(1);
      if (!stack.isEmpty())
        StackTag.update(
            stack,
            data -> {
              data.remove("uses");
              return data;
            });
    } else
      StackTag.update(
          stack,
          data -> {
            if (used == 0) data.remove("uses");
            else data.putInt("uses", used);
            return data;
          });
  }

  private double fraction(ItemStack stack) {
    return charges <= 0 ? 0 : (double) (charges - usage(stack)) / charges;
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return charges > 1 && usage(stack) > 0;
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    return (int) Math.clamp(Math.round(fraction(stack) * 13), 0, 13);
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return Mth.hsvToRgb((float) (fraction(stack) / 3), 1, 1);
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      Item.TooltipContext context,
      net.minecraft.world.item.component.TooltipDisplay display,
      java.util.function.Consumer<Component> text,
      TooltipFlag flags) {
    super.appendHoverText(stack, context, display, text, flags);
    if (charges > 1 && stack.getCount() == 1 && flags.isAdvanced())
      text.accept(
          Component.translatable("item.durability", charges - usage(stack), charges)
              .withStyle(ChatFormatting.GRAY));
  }

  @Override
  public Ic2FluidStack getFluidStack(ItemStack stack) {
    return fluid == null
        ? null
        : fluid == Fluids.EMPTY || stack.isEmpty()
            ? Ic2FluidStack.EMPTY
            : Ic2FluidStack.create(fluid, 1000);
  }

  @Override
  public int getCapacityMb(ItemStack stack) {
    return fluid == null ? 0 : 1000;
  }

  private static void initial(Mutable<ItemStack> result, ItemStack stack) {
    if (result != null) result.setValue(stack.copy());
  }

  private static boolean single(ItemStack stack) {
    return stack != null && stack.getCount() == 1;
  }

  private static void replace(
      ItemStack stack, Item replacement, boolean simulate, Mutable<ItemStack> result) {
    if (result != null) result.setValue(new ItemStack(replacement));
    if (!simulate) stack.shrink(1);
  }

  @Override
  public Ic2FluidStack drainMb(
      ItemStack stack, int amount, boolean simulate, Mutable<ItemStack> result) {
    initial(result, stack);
    if (fluid == null) return null;
    if (!single(stack) || fluid == Fluids.EMPTY || amount < 1000) return Ic2FluidStack.EMPTY;
    replace(stack, ItemDefinitions.EMPTY_CELL, simulate, result);
    return Ic2FluidStack.create(fluid, 1000);
  }

  @Override
  public int drainMb(
      ItemStack stack, Ic2FluidStack requested, boolean simulate, Mutable<ItemStack> result) {
    initial(result, stack);
    if (!single(stack)
        || requested == null
        || requested.getAmountMb() < 1000
        || fluid == null
        || fluid == Fluids.EMPTY
        || !requested.hasExactFluid(fluid)) return 0;
    replace(stack, ItemDefinitions.EMPTY_CELL, simulate, result);
    return 1000;
  }

  @Override
  public int fillMb(
      ItemStack stack, Ic2FluidStack offered, boolean simulate, Mutable<ItemStack> result) {
    initial(result, stack);
    if (!single(stack) || fluid != Fluids.EMPTY || offered == null || offered.getAmountMb() < 1000)
      return 0;
    var filled = FILLED.get(offered.getFluid());
    if (filled == null || !offered.hasExactFluid(filled.fluid)) return 0;
    replace(stack, filled, simulate, result);
    return 1000;
  }
}
