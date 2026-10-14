package ic2ce.neoforge.item.client;

import ic2ce.api.item.IKineticRotor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.*;

/** Physical gearbox screen context is separate from common material descriptors. */
@OnlyIn(Dist.CLIENT)
public final class RotorTooltip {
  private RotorTooltip() {}

  public static void append(
      IKineticRotor rotor, ItemStack stack, java.util.function.Consumer<Component> output) {
    var screen = net.minecraft.client.Minecraft.getInstance().screen;
    IKineticRotor.GearboxType type = null;
    if (screen instanceof ic2ce.neoforge.kinetic.client.WaterTurbineScreen)
      type = IKineticRotor.GearboxType.WATER;
    else if (screen instanceof ic2ce.neoforge.kinetic.client.WindTurbineScreen)
      type = IKineticRotor.GearboxType.WIND;
    if (type != null)
      output.accept(
          Component.translatable("ic2.itemrotor.fitsin." + rotor.isAcceptedType(stack, type))
              .withStyle(ChatFormatting.GRAY));
  }
}
