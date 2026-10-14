package ic2ce.neoforge.thermal.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.thermal.menu.FluidHeaterMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Fluid combustion shows actual fuel reservoir, current transfer and configured maximum emission.
 */
public class FluidHeaterScreen extends MachineScreen<FluidHeaterMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guifluidheatgenerator.png");

  public FluidHeaterScreen(FluidHeaterMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(ReservoirGauge.createNormal(this, 70, 20, menu.base.getFluidTank()));
    addElement(
        LiveLabel.create(
            this,
            96,
            33,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        LocalizedText.translate("ic2.FluidHeatGenerator.gui.info.Emit")
                            + menu.base.gettransmitHeat()),
            0x57c4da,
            false));
    addElement(
        LiveLabel.create(
            this,
            96,
            52,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        LocalizedText.translate("ic2.FluidHeatGenerator.gui.info.MaxEmit")
                            + menu.base.getMaxHeatEmittedPerTick()),
            0x57c4da,
            false));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
