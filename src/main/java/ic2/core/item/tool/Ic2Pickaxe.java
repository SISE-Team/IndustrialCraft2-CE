package ic2.core.item.tool;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/** 26.1.2 removed PickaxeItem; the tool behavior comes from item components. */
public class Ic2Pickaxe extends Item {
   public Ic2Pickaxe(ToolMaterial material, int attackDamage, float attackSpeed, Properties settings) {
      super(settings.pickaxe(material, (float)attackDamage, attackSpeed));
   }
}
