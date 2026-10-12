package ic2.core.item.tool;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item.Properties;

public class Ic2Axe extends AxeItem {
   public Ic2Axe(net.minecraft.world.item.ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
      super(material, attackDamage, attackSpeed, settings);
   }
}
