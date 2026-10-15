package ic2ce.neoforge.processing.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.processing.menu.*;
import ic2ce.neoforge.runtime.SideServices;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Four actual canning policies have separate slot masks, fluid swap event, mode art and recipe
 * categories.
 */
public class CanningScreen extends MachineScreen<CanningMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guicanner.png");

  public CanningScreen(CanningMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 184);
    addElement(ElectricalGauge.asBolt(this, 12, 62, menu.base));
    var cursor =
        new OptionCursor(
            176,
            18,
            226,
            32,
            14,
            true,
            4,
            new OptionValue() {
              public int getValue() {
                return menu.base.getMode().ordinal();
              }

              public void onChange(int value) {
                SideServices
                    .network(false)
                    .initiateClientTileEntityEvent(menu.base, value);
              }
            });
    addElement(
        new AtlasButton(this, 63, 81, 50, 14, cursor, BACKGROUND, cursor)
            .withTooltip(() -> "ic2.Canner.gui.switch." + menu.base.getMode().name()));
    addElement(
        new AtlasButton(
                this,
                77,
                64,
                22,
                13,
                createEventSender(ic2ce.neoforge.processing.fluid.MultiPolicyCanner.eventSwapTanks))
            .withTooltip("ic2.Canner.gui.switchTanks"));
    addElement(ReservoirGauge.createNormal(this, 39, 42, menu.base.getInputTank()));
    addElement(ReservoirGauge.createNormal(this, 117, 42, menu.base.getOutputTank()));
    if (RecipeOverlay.canUse())
      for (var mode : ic2ce.neoforge.processing.fluid.MultiPolicyCanner.Mode.values())
        addElement(
            new RecipeOverlay(this, 74, 22, 23, 14, new String[] {"canner_" + mode.name()})
                .withEnableHandler(() -> menu.base.getMode() == mode));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float fraction) {
    super.extractBackground(graphics, mx, my, fraction);
    bindTexture();
    switch (menu.base.getMode()) {
      case BottleSolid -> {
        drawTexturedRect(graphics, 59, 53, 9, 18, 3, 4);
        drawTexturedRect(graphics, 99, 53, 18, 23, 3, 4);
      }
      case EmptyLiquid -> {
        drawTexturedRect(graphics, 71, 43, 26, 18, 196, 0);
        drawTexturedRect(graphics, 59, 53, 9, 18, 3, 4);
      }
      case BottleLiquid -> {
        drawTexturedRect(graphics, 99, 53, 18, 23, 3, 4);
        drawTexturedRect(graphics, 71, 43, 26, 18, 196, 0);
      }
      case EnrichLiquid -> {}
    }
    float value = menu.base.getProgress();
    int progress = Float.isNaN(value) ? 0 : Math.round(Math.clamp(value, 0, 1) * 23);
    if (progress > 0) drawTexturedRect(graphics, 74, 22, progress, 14, 233, 0);
  }
}
