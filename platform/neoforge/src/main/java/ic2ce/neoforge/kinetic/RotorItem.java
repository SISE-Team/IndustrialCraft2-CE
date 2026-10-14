package ic2ce.neoforge.kinetic;

import ic2ce.api.item.IKineticRotor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Validated fixed rotor descriptors expose independent material efficiency, diameter and accepted
 * gearbox media.
 */
public class RotorItem extends Item implements IKineticRotor {
  private record Profile(
      int diameter,
      boolean water,
      float efficiency,
      int minimum,
      int maximum,
      Identifier material) {
    Profile {
      if (diameter < 1
          || !Float.isFinite(efficiency)
          || efficiency <= 0
          || minimum < 0
          || maximum < minimum
          || material == null) throw new IllegalArgumentException("Invalid rotor descriptor");
    }
  }

  private final Profile profile;

  public RotorItem(
      Properties properties,
      int diameter,
      boolean water,
      float efficiency,
      int minimum,
      int maximum,
      Identifier material) {
    super(properties);
    profile = new Profile(diameter, water, efficiency, minimum, maximum, material);
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> output,
      TooltipFlag flags) {
    output.accept(
        Component.translatable("ic2.itemrotor.wind.info", profile.minimum, profile.maximum)
            .withStyle(ChatFormatting.GRAY));
    if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())
      ic2ce.neoforge.item.client.RotorTooltip.append(this, stack, output);
  }

  @Override
  public int getDiameter(ItemStack stack) {
    return profile.diameter;
  }

  @Override
  public Identifier getRotorRenderTexture(ItemStack stack) {
    return profile.material;
  }

  @Override
  public float getEfficiency(ItemStack stack) {
    return profile.efficiency;
  }

  @Override
  public int getMinWindStrength(ItemStack stack) {
    return profile.minimum;
  }

  @Override
  public int getMaxWindStrength(ItemStack stack) {
    return profile.maximum;
  }

  @Override
  public boolean isAcceptedType(ItemStack stack, GearboxType gearbox) {
    return gearbox == GearboxType.WIND || gearbox == GearboxType.WATER && profile.water;
  }
}
