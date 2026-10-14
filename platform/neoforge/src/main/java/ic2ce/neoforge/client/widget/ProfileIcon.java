package ic2ce.neoforge.client.widget;

import com.mojang.authlib.GameProfile;
import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ResolvableProfile;

/**
 * The native profile component resolves player skins without obsolete SkullOwner custom data or
 * futures per frame.
 */
public class ProfileIcon extends ItemIcon {
  private final GameProfile profile;

  public ProfileIcon(MachineScreen<?> gui, int x, int y, GameProfile profile) {
    var owner = Objects.requireNonNull(profile);
    var skull = new ItemStack(Items.PLAYER_HEAD);
    skull.set(DataComponents.PROFILE, ResolvableProfile.createResolved(owner));
    super(gui, x, y, () -> skull);
    this.profile = owner;
  }

  @Override
  protected List<Component> getToolTip() {
    return profile.name() == null || profile.name().isBlank()
        ? List.of()
        : List.of(Component.literal(profile.name()));
  }
}
