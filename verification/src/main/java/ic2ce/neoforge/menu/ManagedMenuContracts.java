package ic2ce.neoforge.menu;

import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Real player inventory coordinates, stale held-item packets and impossible submenus are rejected.
 */
public final class ManagedMenuContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run(ServerLevel world) {
    var failures = new ArrayList<String>();
    int checks = 0;
    var actor = ic2ce.neoforge.runtime.AutomationActors.get(world);
    var inventory = actor.getInventory();
    var main = actor.getMainHandItem();
    var off = actor.getOffhandItem();
    int selected = inventory.getSelectedSlot();
    try {
      actor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemDefinitions.TOOL_BOX));
      actor.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ItemDefinitions.TOOL_BOX));
      for (var hand : InteractionHand.values()) {
        var buffer = new GrowingBuffer();
        ManagedMenus.writeItem(actor, hand, null, buffer);
        buffer.flip();
        int location = buffer.readInt();
        checks++;
        if (location != (hand == InteractionHand.MAIN_HAND ? selected : -hand.ordinal())
            || buffer.readBoolean())
          failures.add(
              "managed menus: physical hand packet changed its native selected-slot encoding");
      }
      var stale = new GrowingBuffer();
      ManagedMenus.writeItem(actor, InteractionHand.MAIN_HAND, null, stale);
      stale.flip();
      actor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
      boolean rejected = false;
      var packet = stale.toByteBuf(false);
      try {
        ManagedMenus.readItem(1, inventory, packet);
      } catch (IllegalStateException expected) {
        rejected = true;
      } finally {
        packet.release();
      }
      checks++;
      if (!rejected)
        failures.add("managed menus: stale held-item packet admitted an unrelated physical item");
      for (int slot : new int[] {-2, Integer.MIN_VALUE, 9, Integer.MAX_VALUE}) {
        var buffer = new GrowingBuffer();
        buffer.writeInt(slot);
        buffer.writeBoolean(false);
        buffer.flip();
        packet = buffer.toByteBuf(false);
        rejected = false;
        try {
          ManagedMenus.readItem(1, inventory, packet);
        } catch (IllegalStateException expected) {
          rejected = true;
        } finally {
          packet.release();
        }
        checks++;
        if (!rejected)
          failures.add("managed menus: invalid native hand/slot coordinate was admitted");
      }
      actor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemDefinitions.TOOL_BOX));
      rejected = false;
      try {
        ManagedMenus.writeItem(actor, InteractionHand.MAIN_HAND, 1, new GrowingBuffer());
      } catch (IllegalStateException expected) {
        rejected = true;
      }
      checks++;
      if (!rejected)
        failures.add("managed menus: ordinary toolbox accepted a nonexistent nested inventory");
    } finally {
      inventory.setSelectedSlot(selected);
      actor.setItemInHand(InteractionHand.MAIN_HAND, main);
      actor.setItemInHand(InteractionHand.OFF_HAND, off);
    }
    return new Result(checks, List.copyOf(failures));
  }
}
