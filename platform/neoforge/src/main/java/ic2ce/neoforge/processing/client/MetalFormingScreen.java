package ic2ce.neoforge.processing.client;


import ic2ce.neoforge.cable.CableItems;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.item.tool.ToolItems;
import ic2ce.neoforge.processing.menu.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Extrusion, rolling and cutting keep distinct mode events, cached icons and recipe categories. */
public class MetalFormingScreen extends MachineScreen<MetalFormingMenu> {
  private static final Identifier BACKGROUND =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guimetalformer.png");

  public MetalFormingScreen(MetalFormingMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 166);
    addElement(ElectricalGauge.asBolt(this, 20, 37, menu.base));
    addElement(
        SupplierGauge.create(
            this, 52, 39, menu.base::getProgress, FillGauge.GaugeStyle.ProgressMetalFormer));
    var icons =
        java.util.List.of(
            new net.minecraft.world.item.ItemStack(
                CableItems.COPPER_CABLE),
            new net.minecraft.world.item.ItemStack(
                ToolItems.FORGE_HAMMER),
            new net.minecraft.world.item.ItemStack(ToolItems.CUTTER));
    addElement(
        new NativeButton(this, 65, 53, 20, 20, createEventSender(0))
            .withIcon(
                () -> {
                  int mode = menu.base.getMode();
                  return mode >= 0 && mode < icons.size()
                      ? icons.get(mode)
                      : net.minecraft.world.item.ItemStack.EMPTY;
                })
            .withTooltip(
                () ->
                    LocalizedText.translate(
                        switch (menu.base.getMode()) {
                          case 0 -> "ic2.MetalFormer.gui.switch.Extruding";
                          case 1 -> "ic2.MetalFormer.gui.switch.Rolling";
                          case 2 -> "ic2.MetalFormer.gui.switch.Cutting";
                          default -> "ic2.generic.text.empty";
                        })));
    if (RecipeOverlay.canUse())
      for (int selected = 0; selected < 3; selected++) {
        final int mode = selected;
        addElement(
            new RecipeOverlay(this, 52, 39, 46, 9, new String[] {"metal_former" + mode})
                .withEnableHandler(() -> menu.base.getMode() == mode));
      }
  }

  @Override
  public Identifier getTexture() {
    return BACKGROUND;
  }
}
