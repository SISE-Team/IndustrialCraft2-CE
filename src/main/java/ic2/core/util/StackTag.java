package ic2.core.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Bridges the 1.19-era ItemStack tag accessors onto the 26.1.2 data
 * component system. Custom data is stored in {@link DataComponents#CUSTOM_DATA}.
 * All getters return a mutable copy; callers mutating the result must call
 * {@link #set} (or use {@link #update}) to persist the change.
 */
public final class StackTag {
    private StackTag() {}

    public static boolean has(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /** Returns a copy, preserving the old getTag() null semantics when absent. */
    public static net.minecraft.nbt.CompoundTag get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        return data.copyTag();
    }

    /** Returns a mutable copy (empty when absent); mutations require {@link #set}. */
    public static net.minecraft.nbt.CompoundTag getOrCreate(ItemStack stack) {
        net.minecraft.nbt.CompoundTag tag = get(stack);
        return tag != null ? tag : new net.minecraft.nbt.CompoundTag();
    }

    public static void set(ItemStack stack, net.minecraft.nbt.CompoundTag tag) {
        if (tag != null) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        } else {
            // 1.19-era setTag(null) semantics: drop the component entirely.
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }

    /** Applies an update function to the custom data and stores the result. */
    public static void update(ItemStack stack, java.util.function.UnaryOperator<net.minecraft.nbt.CompoundTag> operator) {
        net.minecraft.nbt.CompoundTag tag = getOrCreate(stack);
        set(stack, operator.apply(tag));
    }

    /** Parses an ItemStack from its codec-encoded NBT (replaces ItemStack#of). */
    public static ItemStack parse(net.minecraft.core.HolderLookup.Provider provider, net.minecraft.nbt.CompoundTag tag) {
        return ItemStack.CODEC
            .parse(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag)
            .result().orElse(ItemStack.EMPTY);
    }

    /**
     * Provider-less parse helper resolving the registry access from the running
     * server or remote client level, so handheld filters work on dedicated servers.
     */
    public static ItemStack parseHolder(net.minecraft.nbt.CompoundTag tag) {
        var lookup = ic2.core.IC2.envProxy != null ? ic2.core.IC2.envProxy.getRegistryLookup() : null;
        if (lookup == null) {
            return ItemStack.EMPTY;
        }
        return parse(lookup, tag);
    }

    /** Serializes an ItemStack with its codec (replaces ItemStack#save(CompoundTag)). */
    public static net.minecraft.nbt.CompoundTag save(net.minecraft.core.HolderLookup.Provider provider, ItemStack stack, net.minecraft.nbt.CompoundTag tag) {
        ItemStack.CODEC
            .encodeStart(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack)
            .result()
            .ifPresent(encoded -> {
                net.minecraft.nbt.CompoundTag encodedTag = (net.minecraft.nbt.CompoundTag)encoded;
                tag.put("id", net.minecraft.nbt.StringTag.valueOf(encodedTag.getStringOr("id", "minecraft:air")));
                tag.putInt("count", encodedTag.getIntOr("count", stack.getCount()));
                if (encodedTag.contains("components")) {
                    tag.put("components", encodedTag.getCompoundOrEmpty("components"));
                }
            });
        return tag;
    }
}
