package ic2ce.neoforge.menu.slot;

import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;

/**
 * Physical native armor slots apply equipment callbacks, binding restrictions and a single-item
 * capacity.
 */
public final class EquipmentItemSlot extends Slot {
  private final EquipmentSlot equipment;
  private final Player owner;

  public EquipmentItemSlot(Inventory inventory, EquipmentSlot equipment, int x, int y) {
    super(inventory, 36 + Objects.requireNonNull(equipment).getIndex(), x, y);
    if (equipment.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
      throw new IllegalArgumentException("Not a native humanoid armor slot");
    this.equipment = equipment;
    owner = inventory.player;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return stack != null && !stack.isEmpty() && stack.canEquip(equipment, owner);
  }

  @Override
  public int getMaxStackSize() {
    return 1;
  }

  @Override
  public boolean isActive() {
    return owner.canUseSlot(equipment);
  }

  @Override
  public boolean mayPickup(Player actor) {
    return actor == owner
        && (getItem().isEmpty()
            || actor.getAbilities().instabuild
            || !EnchantmentHelper.has(getItem(), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE));
  }

  @Override
  public void setByPlayer(ItemStack stack, ItemStack previous) {
    owner.onEquipItem(equipment, previous, stack);
    super.setByPlayer(stack, previous);
  }

  @Override
  public Identifier getNoItemIcon() {
    return switch (equipment) {
      case HEAD -> net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
      case CHEST -> net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
      case LEGS -> net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
      case FEET -> net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
      default -> throw new IllegalStateException("Non-humanoid equipment slot");
    };
  }
}
