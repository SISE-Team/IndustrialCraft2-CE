package ic2ce.neoforge.crafting.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.crafting.menu.AutomaticCraftingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Native view of this device's own synchronized ledger and inventory layout. */
public class AutomaticCraftingScreen extends MachineScreen<AutomaticCraftingMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guibatchcrafter.png");

  public AutomaticCraftingScreen(AutomaticCraftingMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 176, 206);
    addElement(ElectricalGauge.asBolt(this, 12, 45, menu.base));
    addElement(
        new DeviceGauge(this, 90, 35, menu.base, "progress", FillGauge.GaugeStyle.ProgressArrow));
    addElement(
        new StackIcon(
            this,
            94,
            14,
            () ->
                menu.base.recipeOutput == null
                    ? net.minecraft.world.item.ItemStack.EMPTY
                    : menu.base.recipeOutput));
  }

  @Override
  public Identifier getTexture() {
    return TEXTURE;
  }
}
