package ic2ce.neoforge.kinetic.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.kinetic.menu.WaterTurbineMenu;
import ic2ce.neoforge.menu.document.LiveText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Water biome eligibility, missing rotor and obstructed swept volume are evaluated independently of
 * wind rules.
 */
public class WaterTurbineScreen extends MachineScreen<WaterTurbineMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guiwaterkineticgenerator.png");

  public WaterTurbineScreen(WaterTurbineMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(
        LiveLabel.create(
            this,
            38,
            52,
            LiveText.of((com.google.common.base.Supplier<String>) this::status),
            0x20ebbe,
            false));
    addElement(
        LiveLabel.create(
            this,
            45,
            69,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        invalidBiome()
                            ? LocalizedText.translate("ic2.WaterKineticGenerator.gui.wrongbiome2")
                            : ready() ? menu.base.getRotorHealth() : ""),
            0x20ebbe,
            false));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  private boolean invalidBiome() {
    return menu.base.type == ic2ce.neoforge.kinetic.WaterTurbine.BiomeState.INVALID;
  }

  private boolean ready() {
    return !invalidBiome()
        && !menu.base.rotorSlot.isEmpty()
        && menu.base.checkSpace(menu.base.getRotorDiameter(), true) == 0;
  }

  private String status() {
    if (invalidBiome()) return LocalizedText.translate("ic2.WaterKineticGenerator.gui.wrongbiome1");
    if (menu.base.rotorSlot.isEmpty())
      return LocalizedText.translate("ic2.WaterKineticGenerator.gui.rotormiss");
    if (menu.base.checkSpace(menu.base.getRotorDiameter(), true) != 0)
      return LocalizedText.translate("ic2.WaterKineticGenerator.gui.rotorspace");
    return LocalizedText.translate("ic2.WaterKineticGenerator.gui.output", menu.base.getKuOutput());
  }
}
