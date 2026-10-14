package ic2ce.neoforge.transport.client;

import ic2ce.core.util.StackUtil;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.transport.menu.BatchItemMenu;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Each side shows its live outlet availability and the independent default-route selection. */
public class BatchItemScreen extends MachineScreen<BatchItemMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guisortingmachine.png");

  public BatchItemScreen(BatchItemMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 212, 243);
    addElement(ElectricalGauge.asBolt(this, 174, 220, menu.base));
    for (var side : Direction.values()) {
      int y = 18 + 20 * side.ordinal();
      addElement(
          ResourceImage.create(
              this,
              60,
              y,
              18,
              18,
              TEXTURE,
              256,
              256,
              region(212, () -> StackUtil.ENV.getAdjacentInventory(menu.base, side) == null)));
      addElement(
          new AtlasButton(
                  this,
                  42,
                  y,
                  18,
                  18,
                  region(230, () -> menu.base.defaultRoute == side),
                  TEXTURE,
                  createEventSender(side.ordinal()))
              .withTooltip(
                  () ->
                      menu.base.defaultRoute == side
                          ? "ic2.SortingMachine.default"
                          : "ic2.SortingMachine.whitelist"));
    }
  }

  private static AtlasRegion region(int u, java.util.function.BooleanSupplier marked) {
    return new DynamicAtlasRegion(18) {
      @Override
      public int getUS() {
        return u;
      }

      @Override
      public int getVS() {
        return marked.getAsBoolean() ? 33 : 15;
      }
    };
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }
}
