package ic2ce.neoforge.recipe.input;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** Registry-safe book metadata; physical eligibility uses live native fluid capabilities. */
public record FluidContentsIngredient(Identifier fluidId, int amount) implements ICustomIngredient {
  public static final MapCodec<FluidContentsIngredient> CODEC =
      RecordCodecBuilder.mapCodec(
          builder ->
              builder
                  .group(
                      Identifier.CODEC.fieldOf("fluid").forGetter(FluidContentsIngredient::fluidId),
                      ExtraCodecs.POSITIVE_INT
                          .fieldOf("amount")
                          .forGetter(FluidContentsIngredient::amount))
                  .apply(builder, FluidContentsIngredient::new));
  public static final IngredientType<FluidContentsIngredient> TYPE = new IngredientType<>(CODEC);

  public FluidContentsIngredient {
    Objects.requireNonNull(fluidId);
    InputDescriptor.positive(amount);
  }

  @Override
  public boolean test(ItemStack stack) {
    var fluid = BuiltInRegistries.FLUID.getOptional(fluidId).orElse(null);
    return fluid != null
        && FluidContentsInput.matchesContents(stack, fluid == Fluids.EMPTY ? null : fluid, amount);
  }

  // Capability providers may support variant-dependent filling. Conservative holders are safe
  // before component binding.
  @Override
  public Stream<Holder<Item>> items() {
    return BuiltInRegistries.ITEM.stream()
        .filter(item -> item != Items.AIR)
        .map(item -> (Holder<Item>) item.builtInRegistryHolder());
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
    var fluid = BuiltInRegistries.FLUID.getOptional(fluidId).orElse(null);
    if (fluid == null) return SlotDisplay.Empty.INSTANCE;
    var bucket = fluid == Fluids.EMPTY ? Items.BUCKET : fluid.getBucket();
    if (bucket != Items.AIR) return new SlotDisplay.ItemSlotDisplay(bucket);
    // IC2 fluids without native bucket items still need a visible book descriptor before world
    // binding.
    return new SlotDisplay.Composite(
        BuiltInRegistries.ITEM.stream()
            .filter(item -> item instanceof ic2ce.api.fluid.Ic2FluidItem)
            .<SlotDisplay>map(SlotDisplay.ItemSlotDisplay::new)
            .toList());
  }
}
