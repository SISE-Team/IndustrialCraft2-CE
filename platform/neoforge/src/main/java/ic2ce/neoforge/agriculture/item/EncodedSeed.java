package ic2ce.neoforge.agriculture.item;

import ic2ce.api.crops.CropCard;
import ic2ce.api.crops.Crops;
import ic2ce.api.crops.ICropSeed;
import ic2ce.neoforge.agriculture.AgricultureItems;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.agriculture.CropPlot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

/** Owned component-backed seed genetics with bounded scan disclosure and authoritative planting. */
public class EncodedSeed extends Item implements ICropSeed, ic2ce.api.item.CreativeVariants {
  public EncodedSeed(Properties properties) {
    super(properties);
  }

  private static int value(ItemStack stack, String key, int maximum) {
    if (stack == null || stack.isEmpty()) return -1;
    var data = StackTag.get(stack);
    return data == null ? -1 : Math.clamp(data.getIntOr(key, 0), 0, maximum);
  }

  private static void write(ItemStack stack, String key, int value, int maximum) {
    if (stack == null || stack.isEmpty()) return;
    StackTag.update(
        stack,
        data -> {
          data.putInt(key, Math.clamp(value, 0, maximum));
          return data;
        });
  }

  public String getDescriptionId(ItemStack stack) {
    int scan = getScannedFromStack(stack);
    if (scan == 0 || stack == null) return "ic2.crop.unknown";
    var species = getCropFromStack(stack);
    return scan < 0 || species == null ? "ic2.crop.invalid" : species.getUnlocalizedName();
  }

  @Override
  public Component getName(ItemStack stack) {
    var species = getCropFromStack(stack);
    return Component.translatable(
        species == null ? "ic2.crop.seeds" : species.getSeedType(),
        Component.translatable(getDescriptionId(stack)));
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      net.minecraft.world.item.component.TooltipDisplay display,
      java.util.function.Consumer<Component> text,
      TooltipFlag flags) {
    if (getScannedFromStack(stack) < 4) return;
    text.accept(
        Component.translatable("ic2.crop.stat.growth", getGrowthFromStack(stack))
            .withStyle(ChatFormatting.DARK_GREEN));
    text.accept(
        Component.translatable("ic2.crop.stat.gain", getGainFromStack(stack))
            .withStyle(ChatFormatting.GOLD));
    text.accept(
        Component.translatable("ic2.crop.stat.resistance", getResistanceFromStack(stack))
            .withStyle(ChatFormatting.DARK_AQUA));
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof CropPlot plot))
      return InteractionResult.PASS;
    var stack = context.getItemInHand();
    var species = getCropFromStack(stack);
    if (species == null || plot.getCrop() != null || plot.isCrossingBase())
      return InteractionResult.PASS;
    if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
    if (!plot.tryPlantIn(
        species,
        0,
        getGrowthFromStack(stack),
        getGainFromStack(stack),
        getResistanceFromStack(stack),
        getScannedFromStack(stack))) return InteractionResult.PASS;
    var player = context.getPlayer();
    if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
    return InteractionResult.SUCCESS;
  }

  public void addCreativeVariants(java.util.function.Consumer<ItemStack> output) {
    if (Crops.instance != null)
      for (var species : Crops.instance.getCrops())
        output.accept(generateItemStackFromValues(species, 1, 1, 1, 4));
  }

  public static ItemStack generateItemStackFromValues(
      CropCard species, int growth, int gain, int resistance, int scan) {
    if (species == null) return ItemStack.EMPTY;
    var stack = new ItemStack(AgricultureItems.CROP_SEED_BACK);
    var data = new net.minecraft.nbt.CompoundTag();
    data.putString("owner", species.getOwner());
    data.putString("id", species.getId());
    data.putInt("growth", Math.clamp(growth, 0, 31));
    data.putInt("gain", Math.clamp(gain, 0, 31));
    data.putInt("resistance", Math.clamp(resistance, 0, 31));
    data.putInt("scan", Math.clamp(scan, 0, 4));
    StackTag.set(stack, data);
    return stack;
  }

  @Override
  public CropCard getCropFromStack(ItemStack stack) {
    return Crops.instance == null || stack == null || stack.isEmpty()
        ? null
        : Crops.instance.getCropCard(stack);
  }

  @Override
  public void setCropFromStack(ItemStack stack, CropCard species) {
    if (stack == null || stack.isEmpty()) return;
    StackTag.update(
        stack,
        data -> {
          if (species == null) {
            data.remove("owner");
            data.remove("id");
          } else {
            data.putString("owner", species.getOwner());
            data.putString("id", species.getId());
          }
          return data;
        });
  }

  @Override
  public int getGrowthFromStack(ItemStack stack) {
    return value(stack, "growth", 31);
  }

  @Override
  public int getGainFromStack(ItemStack stack) {
    return value(stack, "gain", 31);
  }

  @Override
  public int getResistanceFromStack(ItemStack stack) {
    return value(stack, "resistance", 31);
  }

  @Override
  public int getScannedFromStack(ItemStack stack) {
    return value(stack, "scan", 4);
  }

  @Override
  public void setGrowthFromStack(ItemStack stack, int value) {
    write(stack, "growth", value, 31);
  }

  @Override
  public void setGainFromStack(ItemStack stack, int value) {
    write(stack, "gain", value, 31);
  }

  @Override
  public void setResistanceFromStack(ItemStack stack, int value) {
    write(stack, "resistance", value, 31);
  }

  @Override
  public void setScannedFromStack(ItemStack stack, int value) {
    write(stack, "scan", value, 4);
  }

  @Override
  public void incrementScannedFromStack(ItemStack stack) {
    setScannedFromStack(stack, Math.max(0, getScannedFromStack(stack)) + 1);
  }
}
