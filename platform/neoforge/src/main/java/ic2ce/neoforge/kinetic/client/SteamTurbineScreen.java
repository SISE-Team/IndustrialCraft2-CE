package ic2ce.neoforge.kinetic.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.kinetic.menu.SteamTurbineMenu;
import ic2ce.neoforge.menu.document.LiveText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Steam venting, condensation throttling, water blockage and rotor availability remain distinct
 * states.
 */
public class SteamTurbineScreen extends MachineScreen<SteamTurbineMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guisteamkineticgenerator.png");

  public SteamTurbineScreen(SteamTurbineMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(ReservoirGauge.createPlain(this, 75, 21, 26, 26, menu.base.getDistilledWaterTank()));
    addElement(
        new InventoryGrid(this, 80, 26, InventoryGrid.SlotStyle.Plain)
            .withTooltip(
                () -> menu.base.hasTurbine() ? null : "ic2.SteamKineticGenerator.gui.turbineslot"));
    addElement(
        ResourceImage.create(this, 36, 20, 30, 26, BACKGROUND, 256, 256, 176, 0, 206, 26)
            .withEnableHandler(() -> menu.base.hasTurbine() && menu.base.isVentingSteam())
            .withTooltip("ic2.SteamKineticGenerator.gui.ventingWarning"));
    addElement(
        ResourceImage.create(this, 110, 20, 30, 26, BACKGROUND, 256, 256, 176, 0, 206, 26)
            .withEnableHandler(() -> menu.base.hasTurbine() && menu.base.isThrottled())
            .withTooltip("ic2.SteamKineticGenerator.gui.condensationwarrning"));
    addElement(
        LiveLabel.create(
            this,
            8,
            51,
            160,
            13,
            LiveText.of((com.google.common.base.Supplier<String>) this::status),
            () -> ready() ? 0x20ebbe : 0xe4102c,
            false,
            4,
            0,
            false,
            true));
    addElement(
        LiveLabel.create(
                this,
                8,
                68,
                160,
                13,
                LiveText.of(
                    (com.google.common.base.Supplier<String>)
                        () ->
                            LocalizedText.translate(
                                "ic2.SteamKineticGenerator.gui.turbine.ouput",
                                menu.base.getKUoutput())),
                0x20ebbe,
                false,
                4,
                0,
                false,
                true)
            .withEnableHandler(this::ready));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  private boolean ready() {
    return menu.base.hasTurbine() && !menu.base.isTurbineBlockedByWater();
  }

  private String status() {
    return LocalizedText.translate(
        !menu.base.hasTurbine()
            ? "ic2.SteamKineticGenerator.gui.error.noturbine"
            : menu.base.isTurbineBlockedByWater()
                ? "ic2.SteamKineticGenerator.gui.error.filledupwithwater"
                : menu.base.getActive()
                    ? "ic2.SteamKineticGenerator.gui.aktive"
                    : "ic2.SteamKineticGenerator.gui.waiting");
  }
}
