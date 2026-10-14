package ic2ce.neoforge.thermal.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.thermal.menu.IsotopeHeaterMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Radioisotope pellet heat displays actual transmitted and available output without fuel-burning
 * states.
 */
public class IsotopeHeaterScreen extends MachineScreen<IsotopeHeaterMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guirtheatgenerator.png");

  public IsotopeHeaterScreen(IsotopeHeaterMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(
        LiveLabel.create(
                this,
                49,
                66,
                79,
                13,
                LiveText.of(
                    (com.google.common.base.Supplier<String>)
                        () ->
                            menu.base.gettransmitHeat()
                                + " / "
                                + menu.base.getMaxHeatEmittedPerTick()),
                0x57c4da,
                false,
                true,
                true)
            .withTooltip("ic2.RTHeatGenerator.gui.tooltipheat"));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
