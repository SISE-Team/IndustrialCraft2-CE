package ic2ce.neoforge.item.armor;

import ic2ce.api.item.PropulsionEquipment;
import ic2ce.neoforge.registry.ArmorProfiles;
import ic2ce.neoforge.registry.FluidDefinitions;
import net.minecraft.world.item.ItemStack;

/** Biogas thrust spends millibuckets only when the whole requested fuel bill is available. */
public class BiogasJetpack extends WearableFluidReservoir
    implements PropulsionEquipment, ic2ce.api.item.CreativeVariants {
  public BiogasJetpack(Properties properties) {
    super(ArmorProfiles.JET_PACK, properties, FluidDefinitions.BIOGAS.still, 30_000);
  }

  public void addCreativeVariants(java.util.function.Consumer<ItemStack> output) {
    var full = new ItemStack(this);
    filltank(full);
    output.accept(full);
    output.accept(new ItemStack(this));
  }

  @Override
  public boolean drainEnergy(ItemStack stack, int amount) {
    if (!stack.is(this) || amount < 0 || getCharge(stack) < amount) return false;
    return amount == 0 || drainMb(stack, amount, false, null).getAmountMb() == amount;
  }

  @Override
  public float getPower(ItemStack stack) {
    return 1;
  }

  @Override
  public float getDropPercentage(ItemStack stack) {
    return .2F;
  }

  @Override
  public boolean isJetpackActive(ItemStack stack) {
    return stack.is(this);
  }

  @Override
  public double getChargeLevel(ItemStack stack) {
    return getCharge(stack) / capacity;
  }

  @Override
  public float getHoverMultiplier(ItemStack stack, boolean upwards) {
    return .2F;
  }

  @Override
  public float getWorldHeightDivisor(ItemStack stack) {
    return 1;
  }
}
