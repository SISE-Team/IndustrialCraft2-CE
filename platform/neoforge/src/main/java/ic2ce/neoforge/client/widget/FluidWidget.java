package ic2ce.neoforge.client.widget;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.fluid.FluidHandler;
import ic2ce.neoforge.client.SideProxyClient;
import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Actual fluid content uses native still-sprite metadata and distinct amount/phase tooltips. */
public abstract class FluidWidget extends ScreenWidget<ReservoirSlot> {
  public static final int posU = 8,
      posV = 160,
      normalWidth = 18,
      normalHeight = 18,
      fluidOffsetX = 1,
      fluidOffsetY = 1,
      fluidNetWidth = 16,
      fluidNetHeight = 16;

  protected FluidWidget(MachineScreen<?> gui, int x, int y, int width, int height) {
    super(gui, x, y, width, height);
  }

  protected abstract Ic2FluidStack getFluidStack();

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    bindCommonTexture();
    gui.drawTexturedRect(graphics, x, y, width, height, posU, posV);
    var contents = getFluidStack();
    if (contents == null || contents.isEmpty() || contents.getFluid() == null) return;
    bindBlockTexture();
    gui.drawSprite(
        graphics,
        x + 1,
        y + 1,
        Math.max(0, width - 2),
        Math.max(0, height - 2),
        SideProxyClient.envProxy.getFluidStillSprite(contents),
        SideProxyClient.envProxy.getFluidColor(contents),
        1,
        false,
        false);
  }

  @Override
  protected List<Component> getToolTip() {
    var contents = getFluidStack();
    if (contents == null || contents.isEmpty())
      return List.of(
          Component.translatable("ic2.fluid.none"),
          Component.translatable("ic2.fluid.amount", 0),
          Component.translatable(
              "ic2.fluid.type", Component.translatable("ic2.fluid.unavailable")));
    if (contents.getFluid() == null) return List.of(Component.translatable("ic2.fluid.invalid"));
    return List.of(
        Component.literal(SideProxyClient.envProxy.getFluidName(contents)),
        Component.translatable("ic2.fluid.amount", contents.getAmountMb()),
        Component.translatable(
            "ic2.fluid.type",
            Component.translatable(
                FluidHandler.isGaseous(contents.getFluid())
                    ? "ic2.fluid.gas"
                    : "ic2.fluid.liquid")));
  }
}
