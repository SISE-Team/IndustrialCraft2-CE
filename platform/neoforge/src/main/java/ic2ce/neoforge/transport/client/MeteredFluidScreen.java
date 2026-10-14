package ic2ce.neoforge.transport.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.transport.menu.MeteredFluidMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Decimal output adjustment and redstone mode are distinct protocol operations. */
public class MeteredFluidScreen extends MachineScreen<MeteredFluidMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guifluidregulator.png");
  private static final int[] DIGITS = {1000, 100, 10, 1};

  public MeteredFluidScreen(MeteredFluidMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 184);
    addElement(ElectricalGauge.asBolt(this, 12, 39, menu.base));
    addElement(ReservoirGauge.createNormal(this, 78, 34, menu.base.getFluidTank()));
    for (int column = 0; column < DIGITS.length; column++) {
      int delta = DIGITS[column];
      addElement(new AtlasButton(this, 102 + 10 * column, 44, 9, 9, createEventSender(delta)));
      addElement(new AtlasButton(this, 102 + 10 * column, 68, 9, 9, createEventSender(-delta)));
    }
    addElement(new AtlasButton(this, 152, 44, 9, 9, createEventSender(1001)));
    addElement(new AtlasButton(this, 152, 68, 9, 9, createEventSender(1002)));
    addElement(
        LiveLabel.create(
            this,
            105,
            57,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () -> menu.base.getoutputmb() + LocalizedText.translate("ic2.generic.text.mb")),
            2157374,
            false));
    addElement(
        LiveLabel.create(
            this,
            145,
            57,
            LiveText.of((com.google.common.base.Supplier<String>) menu.base::getmodegui),
            2157374,
            false));
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }
}
