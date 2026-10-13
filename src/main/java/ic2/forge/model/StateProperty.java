package ic2.forge.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import ic2.api.item.ElectricItem;
import ic2.core.item.tool.AbstractItemNanoSaber;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.item.upgrade.ItemUpgradeModule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Stateful legacy item overrides expressed through native range dispatch. */
public record StateProperty(String kind) implements RangeSelectItemModelProperty {
   public static final MapCodec<StateProperty> CODEC = Codec.STRING.fieldOf("kind").xmap(StateProperty::new, StateProperty::kind);
   @Override public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
      if (kind.equals("nano_saber_active")) {
         if (!AbstractItemNanoSaber.isActive(stack) || !ElectricItem.manager.canUse(stack, 1)) return 0;
         return level == null ? 0.1F : (float)(Math.floorMod(level.getGameTime() / 5, 10) + 1) / 10;
      }
      if (kind.equals("tool_box_open")) return owner instanceof Player player && player.containerMenu instanceof ContainerToolbox box && box.base.isThisContainer(stack) ? 1 : 0;
      var direction = ItemUpgradeModule.getDirection(stack);
      return direction != null && direction.getSerializedName().equals(kind) ? 1 : 0;
   }
   @Override public MapCodec<StateProperty> type() { return CODEC; }
}
