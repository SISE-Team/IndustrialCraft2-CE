package ic2ce.neoforge.item.tool;

import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Charged active blade damage uses its own native per-stack attribute snapshot. */
public class NanoBlade extends NanoBladeBase {
  public NanoBlade(Properties properties) {
    super(properties);
  }

  @Override
  public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
    return ItemAttributeModifiers.builder()
        .add(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(
                ProjectRuntime.getIdentifier("nano_saber/damage"),
                isActive(stack) && ElectricItem.manager.canUse(stack, 400) ? 20 : 4,
                AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND)
        .add(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(
                ProjectRuntime.getIdentifier("nano_saber/speed"),
                -3,
                AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND)
        .build();
  }
}
