package ic2ce.neoforge.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.runtime.EnvProxy;
import java.util.Objects;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** Native component-preserving value adapter with legacy quantity/save contracts. */
public final class Ic2FluidStackImpl implements Ic2FluidStack {
  final FluidStack parent;

  public Ic2FluidStackImpl(FluidStack value) {
    parent = Objects.requireNonNull(value);
  }

  @Override
  public Ic2FluidStack copy() {
    return new Ic2FluidStackImpl(parent.copy());
  }

  @Override
  public Fluid getFluid() {
    return parent.getFluid();
  }

  @Override
  public int getAmountMb() {
    return parent.getAmount();
  }

  @Override
  public void setAmountMb(int amount) {
    if (amount < 0) throw new IllegalArgumentException("Negative fluid quantity");
    parent.setAmount(amount);
  }

  @Override
  public boolean hasExactFluid(Fluid fluid) {
    return parent.getFluid() == fluid && parent.getComponentsPatch().isEmpty();
  }

  @Override
  public boolean hasExactFluid(Ic2FluidStack other) {
    if (other == null) return false;
    return other instanceof Ic2FluidStackImpl nativeValue
        ? FluidStack.isSameFluidSameComponents(parent, nativeValue.parent)
        : parent.getFluid() == other.getFluid()
            && parent.getComponentsPatch().isEmpty()
            && other.hasExactFluid(getFluid());
  }

  @Override
  public void toNbt(CompoundTag data) {
    data.putString("FluidName", BuiltInRegistries.FLUID.getKey(parent.getFluid()).toString());
    data.putInt("Amount", parent.getAmount());
    CompoundTag custom = FluidTag.get(parent);
    if (custom.isEmpty()) data.remove("Tag");
    else data.put("Tag", custom);
    if (parent.getComponentsPatch().isEmpty()) data.remove("Components");
    else
      data.put(
          "Components",
          DataComponentPatch.CODEC
              .encodeStart(serializationOps(), parent.getComponentsPatch())
              .getOrThrow());
  }

  static Ic2FluidStack read(CompoundTag data) {
    Identifier id = Identifier.tryParse(data.getStringOr("FluidName", ""));
    int amount = data.getIntOr("Amount", 0);
    if (id == null || amount < 0) return null;
    Fluid fluid =
        BuiltInRegistries.FLUID.get(id).map(net.minecraft.core.Holder::value).orElse(null);
    if (fluid == null) return null;
    var value = new FluidStack(fluid, amount);
    if (data.contains("Tag")) FluidTag.set(value, data.getCompoundOrEmpty("Tag"));
    if (data.contains("Components")) {
      var patch =
          DataComponentPatch.CODEC.parse(serializationOps(), data.get("Components")).result();
      if (patch.isEmpty()) return null;
      value.applyComponents(patch.get());
    }
    return new Ic2FluidStackImpl(value);
  }

  private static com.mojang.serialization.DynamicOps<Tag> serializationOps() {
    var lookup = EnvProxy.get().getRegistryLookup();
    return lookup != null
        ? lookup.createSerializationContext(NbtOps.INSTANCE)
        : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
            .createSerializationContext(NbtOps.INSTANCE);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Ic2FluidStack value
        && getAmountMb() == value.getAmountMb()
        && hasExactFluid(value);
  }

  @Override
  public int hashCode() {
    return getFluid().hashCode() ^ getAmountMb();
  }

  @Override
  public String toString() {
    return getAmountMb()
        + "x"
        + BuiltInRegistries.FLUID.getKey(getFluid())
        + "@"
        + parent.getComponentsPatch();
  }
}
