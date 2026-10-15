package ic2ce.neoforge.item;

import ic2ce.neoforge.runtime.EnvProxy;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Owned native custom data and complete codec snapshots; reads are inert and reused save targets
 * drop stale components.
 */
public final class StackTag {
  private StackTag() {}

  public static boolean has(ItemStack stack) {
    return stack != null && stack.has(DataComponents.CUSTOM_DATA);
  }

  public static CompoundTag get(ItemStack stack) {
    if (stack == null) return null;
    var value = stack.get(DataComponents.CUSTOM_DATA);
    return value == null ? null : value.copyTag();
  }

  public static CompoundTag getOrCreate(ItemStack stack) {
    var value = get(stack);
    return value == null ? new CompoundTag() : value;
  }

  public static void set(ItemStack stack, CompoundTag data) {
    Objects.requireNonNull(stack);
    if (data == null) {
      stack.remove(DataComponents.CUSTOM_DATA);
      return;
    }
    var previous = stack.get(DataComponents.CUSTOM_DATA);
    if (previous != null && previous.copyTag().equals(data)) return;
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data.copy()));
  }

  public static void update(ItemStack stack, UnaryOperator<CompoundTag> operation) {
    set(stack, Objects.requireNonNull(operation).apply(getOrCreate(stack)));
  }

  public static ItemStack parse(HolderLookup.Provider lookup, CompoundTag data) {
    if (lookup == null || data == null) return ItemStack.EMPTY;
    return ItemStack.CODEC
        .parse(lookup.createSerializationContext(NbtOps.INSTANCE), data)
        .result()
        .orElse(ItemStack.EMPTY);
  }

  public static ItemStack parseHolder(CompoundTag data) {
    var lookup = EnvProxy.get().getRegistryLookup();
    return parse(lookup, data);
  }

  public static CompoundTag save(
      HolderLookup.Provider lookup, ItemStack stack, CompoundTag target) {
    Objects.requireNonNull(lookup);
    Objects.requireNonNull(stack);
    Objects.requireNonNull(target);
    var encoded =
        ItemStack.CODEC
            .encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE), stack)
            .getOrThrow();
    if (!(encoded instanceof CompoundTag snapshot))
      throw new IllegalStateException("Native item codec produced a noncompound snapshot");
    target.remove("id");
    target.remove("count");
    target.remove("components");
    target.putString("id", snapshot.getStringOr("id", "minecraft:air"));
    target.putInt("count", snapshot.getIntOr("count", stack.getCount()));
    if (snapshot.contains("components"))
      target.put("components", snapshot.getCompoundOrEmpty("components").copy());
    return target;
  }
}
