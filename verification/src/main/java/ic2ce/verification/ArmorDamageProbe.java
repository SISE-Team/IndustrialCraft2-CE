package ic2ce.verification;


import ic2ce.api.item.ElectricItem;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;

/** Native damage dispatch must drain worn electric armor while preserving held spare armor. */
public final class ArmorDamageProbe {
  public static List<Map<String, Object>> run(ServerPlayer player) {
    var rows = new ArrayList<Map<String, Object>>();
    player.setGameMode(GameType.SURVIVAL);
    player.setInvulnerable(false);
    for (var id : BuiltInRegistries.ITEM.keySet())
      if (id.getNamespace().equals("ic2")
          && (id.getPath().startsWith("nano_") || id.getPath().startsWith("quantum_"))
          && BuiltInRegistries.ITEM.getValue(id)
              instanceof ic2ce.neoforge.item.armor.ChargedArmor armor) {
        for (var slot : EquipmentSlot.values()) player.setItemSlot(slot, ItemStack.EMPTY);
        var worn = new ItemStack(armor);
        var spare = new ItemStack(armor);
        ElectricItem.manager.charge(worn, 1000000, Integer.MAX_VALUE, true, false);
        ElectricItem.manager.charge(spare, 1000000, Integer.MAX_VALUE, true, false);
        player.setItemSlot(armor.getArmorSlot(), worn);
        player.setItemSlot(EquipmentSlot.MAINHAND, spare);
        player.setHealth(20);
        player.invulnerableTime = 0;
        double before = ElectricItem.manager.getCharge(worn),
            spareBefore = ElectricItem.manager.getCharge(spare);
        boolean hurt = player.hurtServer(player.level(), player.damageSources().generic(), 4);
        double after = ElectricItem.manager.getCharge(worn),
            spareAfter = ElectricItem.manager.getCharge(spare);
        boolean ok = hurt && after < before && spareAfter == spareBefore;
        rows.add(
            Map.of(
                "id",
                id.toString(),
                "passed",
                ok,
                "nativeDamage",
                hurt,
                "wornEUConsumed",
                before - after,
                "heldEUConsumed",
                spareBefore - spareAfter,
                "healthAfter",
                player.getHealth()));
      }
    for (var slot : EquipmentSlot.values()) player.setItemSlot(slot, ItemStack.EMPTY);
    player.setHealth(20);
    player.invulnerableTime = 0;
    return rows;
  }
}
