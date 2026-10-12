package ic2.core.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;

/** Custom-data ({@link DataComponents#CUSTOM_DATA}) bridge for fluid stacks. */
public final class FluidTag {
    private FluidTag() {}

    public static boolean has(FluidStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    public static CompoundTag get(FluidStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return new CompoundTag();
        }
        return data.copyTag();
    }

    public static CompoundTag getOrCreate(FluidStack stack) {
        return get(stack);
    }

    public static void set(FluidStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void update(FluidStack stack, java.util.function.UnaryOperator<CompoundTag> operator) {
        set(stack, operator.apply(get(stack)));
    }
}
