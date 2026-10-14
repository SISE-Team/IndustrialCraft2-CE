package ic2ce.neoforge.model;

import com.mojang.serialization.*;
import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.handheld.menu.ToolboxMenu;
import ic2ce.neoforge.item.tool.NanoBladeBase;
import ic2ce.neoforge.item.upgrade.MachineUpgrade;
import java.util.Objects;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Saber animation, bound-toolbox ownership and directional upgrades remain distinct native display
 * protocols.
 */
public record StateProperty(String kind) implements RangeSelectItemModelProperty {
  public StateProperty {
    Objects.requireNonNull(kind);
  }

  public static final MapCodec<StateProperty> CODEC =
      Codec.STRING.fieldOf("kind").xmap(StateProperty::new, StateProperty::kind);

  @Override
  public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
    if (stack.isEmpty()) return 0;
    return switch (kind) {
      case "nano_saber_active" ->
          stack.getItem() instanceof NanoBladeBase
                  && NanoBladeBase.isActive(stack)
                  && ElectricItem.manager.canUse(stack, 1)
              ? level == null ? .1F : (Math.floorMod(level.getGameTime() / 5, 10) + 1) / 10F
              : 0;
      case "tool_box_open" ->
          owner instanceof Player actor
                  && actor.containerMenu instanceof ToolboxMenu toolbox
                  && toolbox.base.isThisContainer(stack)
              ? 1
              : 0;
      case "down", "up", "north", "south", "west", "east" -> {
        var side =
            stack.getItem() instanceof MachineUpgrade ? MachineUpgrade.getDirection(stack) : null;
        yield side != null && side.getSerializedName().equals(kind) ? 1 : 0;
      }
      default -> 0;
    };
  }

  @Override
  public MapCodec<StateProperty> type() {
    return CODEC;
  }
}
