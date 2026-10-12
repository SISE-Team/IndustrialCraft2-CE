package ic2.core.item.tool;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item.Properties;

public class Ic2Hoe extends HoeItem {
   public Ic2Hoe(net.minecraft.world.item.ToolMaterial material, int attackDamage, float attackSpeed, Properties settings) {
      super(material, attackDamage, attackSpeed, settings);
   }
}
