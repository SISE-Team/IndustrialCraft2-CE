package ic2ce.neoforge.processing.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.processing.menu.ElectrolysisMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Five output identities choose distinct manifold artwork; only the actual connected configuration
 * is visible.
 */
public class ElectrolysisScreen extends MachineScreen<ElectrolysisMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guielectrolyzer.png");

  public ElectrolysisScreen(ElectrolysisMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    addElement(ElectricalGauge.asBolt(this, 12, 44, menu.base));
    addElement(ReservoirSlot.createFluidSlot(this, 78, 16, menu.base.getInput()));
    var outputs = new ElectrolysisTanks[5];
    for (int index = 0; index < outputs.length; index++) {
      outputs[index] = new ElectrolysisTanks(this, 36 + 21 * index, 61, index);
      addElement(outputs[index]);
    }
    for (var shape : ElectrolyzerGauges.values()) {
      int index = shape.ordinal();
      addElement(
          new SupplierGauge(this, 36 + shape.offset, 36, menu.base, shape.properties)
              .withEnableHandler(outputs[index]::isActive));
    }
    if (RecipeOverlay.canUse())
      addElement(new RecipeOverlay(this, 39, 36, 96, 23, new String[] {"electrolyzer"}));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  public enum ElectrolyzerGauges {
    ONE_TANK(57, 232, 12, 23, 48),
    TWO_TANK(1, 232, 54, 23, 24),
    THREE_TANK(41, 159, 54, 23, 24),
    FOUR_TANK(1, 208, 96, 23, 3),
    FIVE_TANK(1, 184, 96, 23, 3);
    public final int offset;
    public final FillGauge.GaugeProperties properties;

    ElectrolyzerGauges(int u, int v, int width, int height, int offset) {
      this.offset = offset;
      properties =
          new FillGauge.GaugePropertyBuilder(
                  u, v, width, height, FillGauge.GaugePropertyBuilder.GaugeOrientation.Down)
              .build();
    }
  }
}
