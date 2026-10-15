package ic2ce.neoforge.processing.client;

import ic2ce.engine.text.SiFormat;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Stopped, single and continuous replication show real paid UU progress and quoted EU cost, without
 * invented zero EU percentage.
 */
public class ReplicationScreen extends MachineScreen<ReplicationMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guireplicator.png");

  public ReplicationScreen(ReplicationMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 184);
    addElement(ElectricalGauge.asBolt(this, 136, 84, menu.base));
    addElement(ReservoirGauge.createNormal(this, 27, 30, menu.base.fluidTank));
    addElement(
        new ItemIcon(this, 91, 17, () -> menu.base.pattern)
            .withTooltip(
                () ->
                    hasPattern()
                        ? menu.base.pattern.getHoverName().getString()
                            + " UU: "
                            + SiFormat.format(menu.base.patternUu, 4)
                            + LocalizedText.translate("ic2.generic.text.bucketUnit")
                            + " EU: "
                            + SiFormat.format(menu.base.patternEu, 4)
                            + LocalizedText.translate("ic2.generic.text.EU")
                        : null));
    button(80, 16, 9, 18, 0, "ic2.Replicator.gui.info.last");
    button(109, 16, 9, 18, 1, "ic2.Replicator.gui.info.next");
    button(75, 82, 16, 16, 3, "ic2.Replicator.gui.info.Stop");
    button(92, 82, 16, 16, 4, "ic2.Replicator.gui.info.single");
    button(109, 82, 16, 16, 5, "ic2.Replicator.gui.info.repeat");
    addElement(
        LiveLabel.create(
            this,
            49,
            36,
            112,
            16,
            LiveText.of((com.google.common.base.Supplier<String>) this::status),
            () ->
                menu.base.getMode()
                        == ic2ce.neoforge.processing.fabrication.PatternReplicator.Mode.STOPPED
                    ? 0xebea20
                    : 0x20ebbe,
            false,
            4,
            0,
            false,
            true));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }

  private boolean hasPattern() {
    return menu.base.pattern != null && !menu.base.pattern.isEmpty();
  }

  private void button(int x, int y, int width, int height, int event, String tooltip) {
    addElement(
        new AtlasButton(this, x, y, width, height, createEventSender(event)).withTooltip(tooltip));
  }

  private String status() {
    var owner = menu.base;
    if (owner.getMode() == ic2ce.neoforge.processing.fabrication.PatternReplicator.Mode.STOPPED)
      return LocalizedText.translate("ic2.Replicator.gui.info.Waiting");
    double fraction = owner.patternUu > 0 ? owner.uuProcessed / owner.patternUu : 0;
    int percent =
        Double.isFinite(fraction) ? (int) Math.round(Math.clamp(fraction, 0, 1) * 100) : 0;
    return "UU: "
        + percent
        + "% "
        + LocalizedText.translate(
            owner.getMode() == ic2ce.neoforge.processing.fabrication.PatternReplicator.Mode.SINGLE
                ? "ic2.Replicator.gui.info.single"
                : "ic2.Replicator.gui.info.repeat");
  }
}
