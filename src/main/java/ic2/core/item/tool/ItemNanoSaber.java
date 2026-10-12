package ic2.core.item.tool;

import ic2.core.IC2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import ic2.api.item.ElectricItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemNanoSaber extends AbstractItemNanoSaber {
   public ItemNanoSaber(Properties settings) {
      super(settings);
   }

   @Override
   public net.minecraft.world.item.component.ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
      int dmg = 4;
      if (ElectricItem.manager.canUse(stack, 400.0) && isActive(stack)) {
         dmg = 20;
      }

      return net.minecraft.world.item.component.ItemAttributeModifiers.builder()
         .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(IC2.getIdentifier("nano_saber/damage"), dmg, AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
         .add(Attributes.ATTACK_SPEED, new AttributeModifier(IC2.getIdentifier("nano_saber/speed"), -3.0, AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
         .build();
   }
}
