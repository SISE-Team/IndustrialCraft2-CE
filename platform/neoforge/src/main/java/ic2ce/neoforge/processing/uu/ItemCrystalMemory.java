package ic2ce.neoforge.processing.uu;

import ic2ce.neoforge.runtime.profile.NotClassic;
import ic2ce.core.util.StackTag;
import ic2ce.core.util.Util;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Owned crystal records with pure reads and complete native component persistence. */
@NotClassic
public class ItemCrystalMemory extends Item {
  public static final String TOOLTIP_ITEM = "item.ic2.crystal_memory.tooltip.item",
      TOOLTIP_UU_MATTER = "item.ic2.crystal_memory.tooltip.uu_matter",
      TOOLTIP_ENERGY = "item.ic2.crystal_memory.tooltip.energy",
      TOOLTIP_EMPTY = "item.ic2.crystal_memory.tooltip.empty";

  public ItemCrystalMemory(Properties properties) {
    super(properties);
  }

  public ItemStack readItemStack(ItemStack crystal) {
    if (crystal == null || crystal.isEmpty()) return ItemStack.EMPTY;
    var data = StackTag.get(crystal);
    if (data == null || !data.contains("Pattern")) return ItemStack.EMPTY;
    var lookup = ProjectRuntime.envProxy.getRegistryLookup();
    if (lookup == null) return ItemStack.EMPTY;
    var recorded = StackTag.parse(lookup, data.getCompoundOrEmpty("Pattern"));
    return recorded.isEmpty() ? ItemStack.EMPTY : recorded.copy();
  }

  public void writecontentsTag(Level world, ItemStack crystal, ItemStack recorded) {
    if (crystal == null || crystal.isEmpty())
      throw new IllegalArgumentException("Crystal record target is empty");
    var data = StackTag.getOrCreate(crystal);
    if (recorded == null || recorded.isEmpty()) data.remove("Pattern");
    else
      data.put(
          "Pattern",
          StackTag.save(
              world.registryAccess(), recorded.copy(), new net.minecraft.nbt.CompoundTag()));
    StackTag.set(crystal, data);
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      TooltipDisplay display,
      Consumer<Component> tooltip,
      TooltipFlag flags) {
    var recorded = readItemStack(stack);
    if (recorded.isEmpty())
      tooltip.accept(Component.translatable(TOOLTIP_EMPTY).withStyle(ChatFormatting.GRAY));
    else {
      tooltip.accept(
          Component.translatable(TOOLTIP_ITEM)
              .append(" ")
              .append(recorded.getHoverName())
              .withStyle(ChatFormatting.GRAY));
      tooltip.accept(
          Component.translatable(TOOLTIP_UU_MATTER)
              .append(" " + Util.toSiString(UuIndex.instance.getInBuckets(recorded), 4) + "B")
              .withStyle(ChatFormatting.GRAY));
    }
  }
}
