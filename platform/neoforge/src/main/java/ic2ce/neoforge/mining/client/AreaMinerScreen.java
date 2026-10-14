package ic2ce.neoforge.mining.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.LiveText;
import ic2ce.neoforge.mining.menu.AreaMinerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Area targeting, filter mode and silk touch are distinct live miner policies. */
public class AreaMinerScreen extends MachineScreen<AreaMinerMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guiadvminer.png");

  public AreaMinerScreen(AreaMinerMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 203);
    addElement(ElectricalGauge.asBolt(this, 12, 55, menu.base));
    addElement(
        MinerControl.create(
                this, 133, 101, createEventSender(0), MinerControl.ButtonStyle.AdvMinerReset)
            .withTooltip("ic2.AdvMiner.gui.switch.reset"));
    addElement(
        MinerControl.create(
                this, 123, 27, createEventSender(1), MinerControl.ButtonStyle.AdvMinerMode)
            .withTooltip("ic2.AdvMiner.gui.switch.mode"));
    addElement(
        MinerControl.create(
                this, 129, 45, createEventSender(2), MinerControl.ButtonStyle.AdvMinerSilkTouch)
            .withTooltip(
                () ->
                    LocalizedText.translate(
                        "ic2.AdvMiner.gui.switch.silktouch", menu.base.silkTouch)));
    addElement(
        LiveLabel.create(
            this,
            40,
            30,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () ->
                        LocalizedText.translate(
                            menu.base.blacklist
                                ? "ic2.AdvMiner.gui.mode.blacklist"
                                : "ic2.AdvMiner.gui.mode.whitelist")),
            2157374,
            false));
    addElement(
        LiveLabel.create(
            this,
            28,
            104,
            LiveText.of(
                (com.google.common.base.Supplier<String>)
                    () -> {
                      var target = menu.base.getMineTarget();
                      if (target == null) return "";
                      var origin = menu.base.getBlockPos();
                      return LocalizedText.translate(
                          "ic2.AdvMiner.gui.info.minelevel",
                          target.getX() - origin.getX(),
                          target.getZ() - origin.getZ(),
                          target.getY() - origin.getY());
                    }),
            2157374,
            false));
  }

  @Override
  public Identifier getTexture() {
    return TEXTURE;
  }
}
