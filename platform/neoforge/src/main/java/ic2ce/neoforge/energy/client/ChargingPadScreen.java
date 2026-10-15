package ic2ce.neoforge.energy.client;

import ic2ce.engine.text.SiFormat;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.energy.menu.*;
import ic2ce.neoforge.menu.document.LiveText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;

/**
 * Wireless actor charging retains its own stored/capacity display and signal mode; it never invents
 * a bank output port.
 */
public class ChargingPadScreen extends MachineScreen<ChargingPadMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guichargepadblock.png");

  public ChargingPadScreen(ChargingPadMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 161);
    addElement(ElectricalGauge.asBar(this, 79, 38, menu.base));
    var redstone = new ItemStack(Items.REDSTONE);
    addElement(
        new NativeButton(this, 152, 4, 20, 20, createEventSender(0))
            .withIcon(() -> redstone)
            .withTooltip(menu.base::getRedstoneMode));
    addElement(
        LiveLabel.create(
            this, 79, 25, LiveText.ofTranslated("ic2.EUStorage.gui.info.level"), 0x404040, false));
    addElement(
        LiveLabel.create(
            this,
            110,
            35,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        SiFormat.format(
                            Math.min(menu.base.energy.getEnergy(), menu.base.energy.getCapacity()),
                            6)),
            0x404040,
            false));
    addElement(
        LiveLabel.create(
            this,
            110,
            45,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () -> "/" + SiFormat.format(menu.base.energy.getCapacity(), 6)),
            0x404040,
            false));
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
