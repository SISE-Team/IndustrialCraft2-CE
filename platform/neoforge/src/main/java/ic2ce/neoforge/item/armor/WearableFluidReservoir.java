package ic2ce.neoforge.item.armor;


import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.item.IItemHudInfo;
import ic2ce.core.fluid.StandardFluidItem;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.level.material.Fluid;

/** Chest-mounted single-fluid tank keeps a bounded fuel/foam reservoir and native displays. */
public abstract class WearableFluidReservoir extends UtilityGarment
    implements StandardFluidItem, IItemHudInfo {
  protected final int capacity;
  protected final Fluid allowfluid;

  public WearableFluidReservoir(
      ArmorMaterial material, Properties properties, Fluid fluid, int capacity) {
    super(material, properties, EquipmentSlot.CHEST);
    if (fluid == null || capacity <= 0)
      throw new IllegalArgumentException("Invalid wearable reservoir");
    allowfluid = fluid;
    this.capacity = capacity;
  }

  @Override
  public int getCapacityMb(ItemStack stack) {
    return capacity;
  }

  @Override
  public boolean canFill(ItemStack stack, Ic2FluidStack incoming) {
    return incoming != null && !incoming.isEmpty() && incoming.getFluid() == allowfluid;
  }

  public void filltank(ItemStack stack) {
    fillMb(stack, Ic2FluidStack.create(allowfluid, capacity), false, null);
  }

  public double getCharge(ItemStack stack) {
    var fluid = Ic2FluidStack.get(stack);
    return fluid.getFluid() == allowfluid ? Math.clamp(fluid.getAmountMb(), 0, capacity) : 0;
  }

  public double getMaxCharge(ItemStack stack) {
    return capacity;
  }

  public boolean isEmpty(ItemStack stack) {
    return getCharge(stack) == 0;
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      Item.TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> output,
      TooltipFlag flags) {
    output.accept(description(stack));
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return true;
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    return (int) Math.round(getCharge(stack) * 13 / capacity);
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return Mth.hsvToRgb((float) (getCharge(stack) / capacity / 3), 1, 1);
  }

  @Override
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    return List.of(description(stack).getString());
  }

  private static Component description(ItemStack stack) {
    var fluid = Ic2FluidStack.get(stack);
    return fluid.isEmpty()
        ? Component.translatable("ic2.item.FluidContainer.Empty")
        : Component.literal("< ")
            .append(fluid.getFluid().getFluidType().getDescription())
            .append(", " + fluid.getAmountMb() + " ")
            .append(Component.translatable("ic2.generic.text.mb"))
            .append(" >");
  }

  public boolean isValidRepairItem(ItemStack first, ItemStack second) {
    return false;
  }
}
