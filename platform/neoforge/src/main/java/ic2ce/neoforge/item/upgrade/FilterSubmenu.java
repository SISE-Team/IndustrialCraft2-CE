package ic2ce.neoforge.item.upgrade;

import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.handheld.HandheldStorage;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.nbt.CompoundTag;

/**
 * Submenu retains the opened physical UID and writes its settings into the actual root component.
 */
public abstract class FilterSubmenu extends HandheldStorage {
  protected final String name;

  protected FilterSubmenu(AdvancedFilterInventory parent, String key) {
    super(parent.getPlayer(), parent.getHand(), parent.getContainerStack(), 9);
    name = key;
    addMaintainedPlayer(player);
  }

  protected CompoundTag getNBT() {
    return AdvancedFilterInventory.getTag(StackTag.get(containerStack), name);
  }

  protected void writeNBT(CompoundTag settings) {
    if (!player.level().isClientSide())
      StackTag.update(
          containerStack,
          root -> {
            root.put(name + "Settings", settings.copy());
            return root;
          });
  }

  ActionButton<?> getBackButton(MachineScreen<?> gui, int x, int y) {
    return new NativeButton(
            gui, x, y, 50, 15, ignored -> ProjectRuntime.network.get(false).requestGUI(this))
        .withText(LocalizedText.translate("ic2.upgrade.advancedGUI.back"));
  }
}
