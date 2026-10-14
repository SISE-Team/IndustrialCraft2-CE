package ic2ce.neoforge.kinetic.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.kinetic.menu.WindTurbineMenu;
import ic2ce.neoforge.menu.document.LiveText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Rotor clearance, weak wind, output, durability and overload artwork follow separate live
 * observations.
 */
public class WindTurbineScreen extends MachineScreen<WindTurbineMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guiwindkineticgenerator.png");

  public WindTurbineScreen(WindTurbineMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(
        LiveLabel.create(
            this,
            17,
            48,
            143,
            13,
            LiveText.of((com.google.common.base.Supplier<String>) this::status),
            0x20ebbe,
            false,
            4,
            0,
            false,
            true));
    addElement(
        LiveLabel.create(
            this,
            17,
            66,
            143,
            13,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        !menu.base.hasRotor() || !menu.base.rotorHasSpace()
                            ? ""
                            : !menu.base.isWindStrongEnough()
                                ? LocalizedText.translate("ic2.WindKineticGenerator.gui.windweak2")
                                : menu.base.getRotorHealth() + " %"),
            0x20ebbe,
            false,
            4,
            0,
            false,
            true));
    for (int x : new int[] {44, 102})
      addElement(
          ResourceImage.create(this, x, 20, 30, 26, BACKGROUND, 256, 256, 176, 0, 206, 26)
              .withEnableHandler(menu.base::isRotorOverloaded)
              .withTooltip("ic2.WindKineticGenerator.error.overload"));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  private String status() {
    if (!menu.base.hasRotor())
      return LocalizedText.translate("ic2.WindKineticGenerator.gui.rotormiss");
    if (!menu.base.rotorHasSpace())
      return LocalizedText.translate("ic2.WindKineticGenerator.gui.rotorspace");
    return !menu.base.isWindStrongEnough()
        ? LocalizedText.translate("ic2.WindKineticGenerator.gui.windweak1")
        : LocalizedText.translate("ic2.WindKineticGenerator.gui.output", menu.base.getKuOutput());
  }
}
