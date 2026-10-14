package ic2ce.neoforge.kinetic.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.kinetic.menu.MotorMenu;
import ic2ce.neoforge.menu.document.LiveText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Ten installed electric motors display actual available and theoretical kinetic output. */
public class MotorScreen extends MachineScreen<MotorMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guielectrickineticgenerator.png");

  public MotorScreen(MotorMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(
        new InventoryGrid(this, 43, 26, 5, 2, InventoryGrid.SlotStyle.Normal)
            .withTooltip("ic2.ElectricKineticGenerator.gui.motors"));
    addElement(ElectricalGauge.asBolt(this, 12, 44, menu.base));
    addElement(
        LiveLabel.create(
                this,
                29,
                66,
                119,
                13,
                LiveText.of(
                    (com.google.common.base.Supplier<String>)
                        () ->
                            LocalizedText.translate(
                                "ic2.ElectricKineticGenerator.gui.kUmax",
                                menu.base.getMaxKU(),
                                menu.base.getMaxKUForGUI())),
                0x57c4da,
                false,
                true,
                true)
            .withTooltip("ic2.ElectricKineticGenerator.gui.tooltipkin"));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
