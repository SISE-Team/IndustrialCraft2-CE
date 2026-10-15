package ic2ce.neoforge.client.screen;

import ic2ce.api.upgrade.*;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.runtime.SideServices;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Native screen extraction owns one local coordinate system, one input consumer and bounded queued
 * tooltip snapshots.
 */
public abstract class MachineScreen<T extends OwnedMenu<? extends Container>>
    extends AbstractContainerScreen<T> {
  public static final int textHeight = 8;
  protected final List<ScreenWidget<?>> elements = new ArrayList<>();
  protected GuiGraphicsExtractor guiGraphics;
  protected Identifier boundTexture;
  private final ArrayDeque<Tooltip> tooltips = new ArrayDeque<>();

  private record Tooltip(int x, int y, List<Component> lines) {}

  public MachineScreen(T menu, Inventory inventory, Component title) {
    this(menu, inventory, title, 176, 166);
  }

  public MachineScreen(T menu, Inventory inventory, Component title, int height) {
    this(menu, inventory, title, 176, height);
  }

  public MachineScreen(T menu, Inventory inventory, Component title, int width, int height) {
    if (width <= 0 || height <= 0 || width > 4096 || height > 4096)
      throw new IllegalArgumentException("Invalid native screen dimensions");
    super(menu, inventory, title, width, height);
  }

  public final T getContainer() {
    return menu;
  }

  public final int getX() {
    return leftPos;
  }

  public final int getY() {
    return topPos;
  }

  public final Slot getFocusedSlot() {
    return hoveredSlot;
  }

  @Override
  public void init() {
    super.init();
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float fraction) {
    guiGraphics = Objects.requireNonNull(graphics);
    tooltips.clear();
    try {
      super.extractRenderState(graphics, mx, my, fraction);
    } finally {
      guiGraphics = null;
      tooltips.clear();
    }
  }

  @Override
  public void containerTick() {
    super.containerTick();
    for (var widget : elements)
      if (widget.isEnabled()
          && widget.getImplementedMethods().contains(ScreenWidget.ImplementedMethod.tick))
        widget.tick();
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float fraction) {
    guiGraphics = graphics;
    super.extractBackground(graphics, mx, my, fraction);
    int localX = mx - leftPos, localY = my - topPos;
    drawBackgroundAndTitle(graphics, fraction, localX, localY);
    if (menu.base instanceof IUpgradableBlock) {
      bindTexture(Identifier.fromNamespaceAndPath("ic2", "textures/gui/infobutton.png"));
      drawTexturedRect(graphics, 3, 3, 10, 10, 0, 0);
    }
    for (var widget : elements)
      if (widget.isEnabled()
          && widget.getImplementedMethods().contains(ScreenWidget.ImplementedMethod.drawBackground))
        widget.drawBackground(graphics, localX, localY);
  }

  protected void drawBackgroundAndTitle(
      GuiGraphicsExtractor graphics, float fraction, int mx, int my) {
    bindTexture();
    if (boundTexture != null)
      graphics.blit(
          RenderPipelines.GUI_TEXTURED,
          boundTexture,
          leftPos,
          topPos,
          0,
          0,
          imageWidth,
          imageHeight,
          256,
          256);
    drawXCenteredString(graphics, imageWidth / 2, 6, title, 0x404040, false);
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor graphics, int mx, int my) {
    guiGraphics = graphics;
    graphics.pose().pushMatrix();
    graphics.pose().translate(-leftPos, -topPos);
    try {
      drawForegroundLayer(graphics, mx - leftPos, my - topPos);
      flushTooltips(graphics);
    } finally {
      graphics.pose().popMatrix();
    }
  }

  protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mx, int my) {
    if (menu.base instanceof IUpgradableBlock upgradable
        && mx >= 0
        && my >= 0
        && mx < 12
        && my < 12) {
      var lines = new ArrayList<Component>();
      lines.add(Component.translatable("ic2.generic.text.upgrade"));
      for (var stack : UpgradeRegistry.getUpgrades())
        if (stack.getItem() instanceof IUpgradeItem upgrade
            && upgrade.isSuitableFor(stack, upgradable.getUpgradableProperties()))
          lines.add(stack.getHoverName().copy().withStyle(net.minecraft.ChatFormatting.GRAY));
      drawTooltip(mx, my, lines);
    }
    for (var widget : elements) if (widget.isEnabled()) widget.drawForeground(graphics, mx, my);
  }

  @Override
  public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
    if (vertical == 0) return super.mouseScrolled(mx, my, horizontal, vertical);
    int x = (int) mx - leftPos, y = (int) my - topPos;
    for (int index = elements.size() - 1; index >= 0; index--) {
      var widget = elements.get(index);
      if (widget.isEnabled()
          && widget.contains(x, y)
          && widget
              .getImplementedMethods()
              .contains(ScreenWidget.ImplementedMethod.onMouseScroll)) {
        widget.onMouseScroll(x, y, vertical < 0 ? ContentScroll.down : ContentScroll.up);
        return true;
      }
    }
    return super.mouseScrolled(mx, my, horizontal, vertical);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    var button = PointerButton.get(event.button());
    if (button == null) return super.mouseClicked(event, doubleClick);
    int x = (int) event.x() - leftPos, y = (int) event.y() - topPos;
    for (var widget : elements)
      if (widget instanceof TextInput input && (!widget.isEnabled() || !widget.contains(x, y)))
        input.setFocused(false);
    for (int index = elements.size() - 1; index >= 0; index--) {
      var widget = elements.get(index);
      if (widget.isEnabled() && widget.onMouseClick(x, y, button, widget.contains(x, y)))
        return true;
    }
    return super.mouseClicked(event, doubleClick);
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
    var button = PointerButton.get(event.button());
    if (button != null) {
      int x = (int) event.x() - leftPos, y = (int) event.y() - topPos;
      for (int index = elements.size() - 1; index >= 0; index--) {
        var widget = elements.get(index);
        if (widget.isEnabled() && widget.onMouseDrag(x, y, button, widget.contains(x, y)))
          return true;
      }
    }
    return super.mouseDragged(event, dx, dy);
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    var button = PointerButton.get(event.button());
    if (button != null) {
      int x = (int) event.x() - leftPos, y = (int) event.y() - topPos;
      for (int index = elements.size() - 1; index >= 0; index--) {
        var widget = elements.get(index);
        if (widget.isEnabled() && widget.onMouseRelease(x, y, button, widget.contains(x, y)))
          return true;
      }
    }
    return super.mouseReleased(event);
  }

  @Override
  public boolean charTyped(CharacterEvent event) {
    for (var widget : elements)
      if (widget.isEnabled() && widget.onCharacterTyped(event)) return true;
    return super.charTyped(event);
  }

  @Override
  public boolean keyPressed(KeyEvent event) {
    for (var widget : elements) if (widget.isEnabled() && widget.onKeyPressed(event)) return true;
    return super.keyPressed(event);
  }

  @Override
  public void removed() {
    super.removed();
    for (var widget : elements) if (widget instanceof TextInput input) input.setFocused(false);
    tooltips.clear();
    guiGraphics = null;
  }

  public void drawTexturedRect(
      GuiGraphicsExtractor graphics,
      double x,
      double y,
      double width,
      double height,
      double u,
      double v) {
    drawTexturedRect(graphics, x, y, width, height, u, v, false);
  }

  public void drawTexturedRect(
      GuiGraphicsExtractor graphics,
      double x,
      double y,
      double width,
      double height,
      double u,
      double v,
      boolean mirror) {
    drawTexturedRect(
        graphics,
        x,
        y,
        width,
        height,
        u / 256,
        v / 256,
        (u + width) / 256,
        (v + height) / 256,
        mirror);
  }

  public void drawTexturedRect(
      GuiGraphicsExtractor graphics,
      double x,
      double y,
      double width,
      double height,
      double u0,
      double v0,
      double u1,
      double v1,
      boolean mirror) {
    if (boundTexture == null
        || width <= 0
        || height <= 0
        || !Double.isFinite(x + y + width + height + u0 + v0 + u1 + v1)) return;
    graphics.blit(
        boundTexture,
        (int) Math.round(leftPos + x),
        (int) Math.round(topPos + y),
        (int) Math.round(leftPos + x + width),
        (int) Math.round(topPos + y + height),
        (float) (mirror ? u1 : u0),
        (float) (mirror ? u0 : u1),
        (float) v0,
        (float) v1);
  }

  public void drawSprite(
      GuiGraphicsExtractor graphics,
      double x,
      double y,
      double width,
      double height,
      TextureAtlasSprite sprite,
      int color,
      double scale,
      boolean alignRight,
      boolean alignBottom) {
    if (width <= 0 || height <= 0 || scale <= 0 || !Double.isFinite(x + y + width + height + scale))
      return;
    if (sprite == null) sprite = GuiElementAtlas.missing();
    double tile = scale * 16;
    if (Math.ceil(width / tile) * Math.ceil(height / tile) > 1_048_576) return;
    double u0 = sprite.getU0(),
        v0 = sprite.getV0(),
        du = sprite.getU1() - u0,
        dv = sprite.getV1() - v0;
    if (du <= 0 || dv <= 0) return;
    int atlasW = Math.round(sprite.contents().width() / (float) du),
        atlasH = Math.round(sprite.contents().height() / (float) dv);
    double firstW = alignRight && width % tile > 0 ? width % tile : tile,
        firstH = alignBottom && height % tile > 0 ? height % tile : tile;
    for (double dx = 0; dx < width; ) {
      double stepX = dx == 0 ? firstW : tile,
          w = Math.min(stepX, width - dx),
          u = u0 + (dx == 0 && alignRight ? (tile - stepX) / tile * du : 0);
      for (double dy = 0; dy < height; ) {
        double stepY = dy == 0 ? firstH : tile,
            h = Math.min(stepY, height - dy),
            v = v0 + (dy == 0 && alignBottom ? (tile - stepY) / tile * dv : 0);
        int left = (int) Math.round(leftPos + x + dx),
            top = (int) Math.round(topPos + y + dy),
            right = (int) Math.round(leftPos + x + dx + w),
            bottom = (int) Math.round(topPos + y + dy + h);
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            sprite.atlasLocation(),
            left,
            top,
            (float) (u * atlasW),
            (float) (v * atlasH),
            right - left,
            bottom - top,
            (int) Math.round(w / tile * du * atlasW),
            (int) Math.round(h / tile * dv * atlasH),
            atlasW,
            atlasH,
            color);
        dy += stepY;
      }
      dx += stepX;
    }
  }

  private static final class GuiElementAtlas {
    static TextureAtlasSprite missing() {
      return ((TextureAtlas)
              net.minecraft.client.Minecraft.getInstance()
                  .getTextureManager()
                  .getTexture(TextureAtlas.LOCATION_BLOCKS))
          .getSprite(MissingTextureAtlasSprite.getLocation());
    }
  }

  private GuiGraphicsExtractor graphics() {
    return Objects.requireNonNull(guiGraphics, "Item rendering requires an active extraction");
  }

  public void drawItem(int x, int y, ItemStack stack) {
    if (stack != null && !stack.isEmpty()) graphics().item(stack, leftPos + x, topPos + y);
  }

  public void drawItemStack(int x, int y, ItemStack stack) {
    if (stack != null && !stack.isEmpty()) {
      graphics().item(minecraft.player, stack, leftPos + x, topPos + y, 0);
      graphics().itemDecorations(font, stack, leftPos + x, topPos + y);
    }
  }

  public void drawColoredRect(
      GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
    if (width > 0 && height > 0)
      graphics.fill(leftPos + x, topPos + y, leftPos + x + width, topPos + y + height, color);
  }

  private static int textColor(int value) {
    return (value & 0xff000000) == 0 ? value | 0xff000000 : value;
  }

  public int drawString(GuiGraphicsExtractor graphics, int x, int y, String text, int color) {
    return drawString(graphics, x, y, text, color, false);
  }

  public int drawString(
      GuiGraphicsExtractor graphics, int x, int y, String text, int color, boolean shadow) {
    graphics.text(font, text, leftPos + x, topPos + y, textColor(color), shadow);
    return x + getStringWidth(text);
  }

  public void drawTrimmedString(
      GuiGraphicsExtractor graphics, int x, int y, String text, int maxWidth, int color) {
    if (maxWidth <= 0) return;
    for (var line : font.split(Component.literal(text), maxWidth)) {
      graphics.text(font, line, leftPos + x, topPos + y, textColor(color), false);
      y += 10;
    }
  }

  public void drawXCenteredString(
      GuiGraphicsExtractor graphics, int x, int y, String text, int color, boolean shadow) {
    drawCenteredString(graphics, x, y, text, color, shadow, true, false);
  }

  public void drawXCenteredString(
      GuiGraphicsExtractor graphics, int x, int y, Component text, int color, boolean shadow) {
    drawCenteredString(graphics, x, y, text, color, shadow, true, false);
  }

  public void drawXYCenteredString(
      GuiGraphicsExtractor graphics, int x, int y, String text, int color, boolean shadow) {
    drawCenteredString(graphics, x, y, text, color, shadow, true, true);
  }

  public void drawXYCenteredString(
      GuiGraphicsExtractor graphics, int x, int y, Component text, int color, boolean shadow) {
    drawCenteredString(graphics, x, y, text, color, shadow, true, true);
  }

  public void drawCenteredString(
      GuiGraphicsExtractor graphics,
      int x,
      int y,
      String text,
      int color,
      boolean shadow,
      boolean centerX,
      boolean centerY) {
    drawString(
        graphics,
        x - (centerX ? getStringWidth(text) / 2 : 0),
        y - (centerY ? 4 : 0),
        text,
        color,
        shadow);
  }

  public void drawCenteredString(
      GuiGraphicsExtractor graphics,
      int x,
      int y,
      Component text,
      int color,
      boolean shadow,
      boolean centerX,
      boolean centerY) {
    graphics.text(
        font,
        text,
        leftPos + x - (centerX ? getStringWidth(text) / 2 : 0),
        topPos + y - (centerY ? 4 : 0),
        textColor(color),
        shadow);
  }

  public int getStringWidth(String text) {
    return font.width(text);
  }

  public int getStringWidth(Component text) {
    return font.width(text);
  }

  public String trimStringToWidth(String text, int width) {
    return font.plainSubstrByWidth(text, Math.max(0, width), false);
  }

  public String trimStringToWidthReverse(String text, int width) {
    return font.plainSubstrByWidth(text, Math.max(0, width), true);
  }

  public void drawTooltip(int x, int y, List<Component> lines) {
    if (!lines.isEmpty() && tooltips.size() < 256)
      tooltips.addLast(
          new Tooltip(x, y, lines.stream().map(Component::copy).map(c -> (Component) c).toList()));
  }

  public void drawTooltip(GuiGraphicsExtractor graphics, int x, int y, ItemStack stack) {
    if (stack != null && !stack.isEmpty())
      graphics.setTooltipForNextFrame(font, stack, leftPos + x, topPos + y);
  }

  protected void flushTooltips(GuiGraphicsExtractor graphics) {
    while (!tooltips.isEmpty()) {
      var next = tooltips.removeFirst();
      graphics.setTooltipForNextFrame(
          font, next.lines, Optional.empty(), ItemStack.EMPTY, leftPos + next.x, topPos + next.y);
    }
  }

  protected void addElement(ScreenWidget<?> widget) {
    elements.add(Objects.requireNonNull(widget));
  }

  protected void blit(
      GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
    if (boundTexture != null && width > 0 && height > 0)
      graphics.blit(
          RenderPipelines.GUI_TEXTURED, boundTexture, x, y, u, v, width, height, 256, 256);
  }

  protected final void bindTexture() {
    bindTexture(getTexture());
  }

  public void bindTexture(Identifier texture) {
    boundTexture = texture;
  }

  protected ClickAction createEventSender(int event) {
    if (!(menu.base instanceof BlockEntity owner))
      throw new IllegalArgumentException("Native block event requires an actual owner");
    return ignored -> SideServices.network(false).initiateClientTileEntityEvent(owner, event);
  }

  protected abstract Identifier getTexture();
}
