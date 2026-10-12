package ic2.core.slot;

import com.mojang.datafixers.util.Pair;
import ic2.core.util.ReflectionUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SlotArmor extends Slot {
   private final EquipmentSlot armorType;
   private final net.minecraft.world.entity.player.Player owner;

   public SlotArmor(Inventory inventory, EquipmentSlot armorType, int x, int y) {
      super(inventory, 36 + armorType.getIndex(), x, y);
      this.armorType = armorType;
      this.owner = inventory.player;
   }

   public boolean mayPlace(ItemStack stack) {
      Item item = stack.getItem();
      return item == null ? false : stack.canEquip(this.armorType, this.owner);
   }

   @Override
   public Identifier getNoItemIcon() {
      return switch (this.armorType) {
         case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
         case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
         case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
         case FEET -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
         default -> null;
      };
   }
}
