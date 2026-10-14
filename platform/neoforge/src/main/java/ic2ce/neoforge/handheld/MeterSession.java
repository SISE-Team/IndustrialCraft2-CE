package ic2ce.neoforge.handheld;

import ic2ce.core.network.GrowingBuffer;
import ic2ce.neoforge.handheld.menu.EnergyMeterMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;

/** Energy observation session closes natively when its actual sampled endpoint disappears. */
public class MeterSession extends HandheldStorage {
  public MeterSession(Player owner, InteractionHand hand, ItemStack stack) {
    super(owner, hand, stack, 0);
  }

  public void closeGUI() {
    if (player instanceof ServerPlayer actor) actor.closeContainer();
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player actor) {
    return new EnergyMeterMenu(id, this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new EnergyMeterMenu(id, this);
  }
}
