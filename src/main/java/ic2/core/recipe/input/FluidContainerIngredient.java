package ic2.core.recipe.input;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ic2.core.fluid.Ic2FluidItem;
import ic2.core.fluid.Ic2FluidStack;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** Registry-only recipe book metadata; the actual predicate still checks fluid contents. */
public record FluidContainerIngredient(Identifier fluidId, int amount) implements ICustomIngredient {
   public static final MapCodec<FluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      Identifier.CODEC.fieldOf("fluid").forGetter(FluidContainerIngredient::fluidId),
      ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidContainerIngredient::amount)
   ).apply(i, FluidContainerIngredient::new));
   public static final IngredientType<FluidContainerIngredient> TYPE = new IngredientType<>(CODEC);

   private Fluid fluid() {
      return BuiltInRegistries.FLUID.getOptional(this.fluidId).orElseThrow();
   }

   @Override
   public boolean test(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }
      Ic2FluidStack contents = Ic2FluidStack.get(stack);
      return contents != null && !contents.isEmpty() && contents.getFluid() == this.fluid() && contents.getAmountMb() >= this.amount;
   }

   @Override
   public Stream<Holder<Item>> items() {
      Item bucket = this.fluid().getBucket();
      return BuiltInRegistries.ITEM.stream().filter(item -> item == bucket || item instanceof Ic2FluidItem)
         .map(item -> (Holder<Item>)item.builtInRegistryHolder());
   }

   @Override
   public boolean isSimple() {
      return false;
   }

   @Override
   public IngredientType<?> getType() {
      return TYPE;
   }

   @Override
   public SlotDisplay display() {
      return new SlotDisplay.ItemSlotDisplay(this.fluid().getBucket());
   }
}
