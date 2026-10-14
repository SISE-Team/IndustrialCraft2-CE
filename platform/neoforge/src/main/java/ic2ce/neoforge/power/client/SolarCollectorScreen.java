package ic2ce.neoforge.power.client;

import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.power.menu.SolarCollectorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Sunlight selects distinct halves of the retained atlas; charging keeps a physical native slot.
 */
public class SolarCollectorScreen extends PlayerInventoryScreen<SolarCollectorMenu> {
  private static final Identifier SUN =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/overlay/solar_sun.png");

  public SolarCollectorScreen(SolarCollectorMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    addElement(new InventoryGrid(this, 79, 25, InventoryGrid.SlotStyle.Normal));
    addElement(ResourceImage.create(this, 81, 45, 14, 14, SUN, 28, 14, 0, 0, 14, 14));
    addElement(
        ResourceImage.create(this, 81, 45, 14, 14, SUN, 28, 14, 14, 0, 28, 14)
            .withEnableHandler(menu.base::isSunlight));
  }
}
