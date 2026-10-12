package ic2.core;

import ic2.core.IC2;
import com.mojang.blaze3d.platform.InputConstants;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.upgrade.UpgradeRegistry;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IKeyboardDependent;
import ic2.core.gui.MouseButton;
import ic2.core.gui.ScrollDirection;
import ic2.core.util.StackUtil;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class Ic2Gui<T extends ContainerBase<? extends Container>> extends AbstractContainerScreen<T> {
   public static final int textHeight = 8;
   protected static Runnable closeHandler;
   private boolean fixKeyEvents = false;
   private final Set<GuiElement.ImplementedMethod> elementMethods = EnumSet.noneOf(GuiElement.ImplementedMethod.class);
   private final Queue<Ic2Gui.Tooltip> queuedTooltips = new ArrayDeque<>();
   protected final List<GuiElement<?>> elements = new ArrayList<>();
   /** Current graphics context, set at extract entry; the legacy drawing helpers use it. */
   protected GuiGraphicsExtractor guiGraphics;
   /** Last texture bound via bindTexture, used by the blit-based drawing helpers. */
   protected Identifier boundTexture;

   public Ic2Gui(T container, Inventory playerInventory, Component title) {
      this(container, playerInventory, title, 176, 166);
   }

   public Ic2Gui(T container, Inventory playerInventory, Component title, int ySize) {
      this(container, playerInventory, title, 176, ySize);
   }

   public Ic2Gui(T container, Inventory playerInventory, Component title, int xSize, int ySize) {
      super(container, playerInventory, title, xSize, ySize);
   }

   public final T getContainer() {
      return (T)this.menu;
   }

   public final int getX() {
      return this.leftPos;
   }

   public final int getY() {
      return this.topPos;
   }

   public final Slot getFocusedSlot() {
      return this.hoveredSlot;
   }

   @Override
   public void init() {
      super.init();

      for (GuiElement<?> element : this.elements) {
         if (element instanceof IKeyboardDependent) {
            this.fixKeyEvents = true;
            break;
         }
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.guiGraphics = graphics;
      super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
   }

   @Override
   public void containerTick() {
      super.containerTick();
      if (this.elementMethods.contains(GuiElement.ImplementedMethod.tick)) {
         for (GuiElement<?> element : this.elements) {
            if (element.isEnabled()) {
               element.tick();
            }
         }
      }
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      this.guiGraphics = graphics;
      super.extractBackground(graphics, mouseX, mouseY, delta);
      mouseX -= this.leftPos;
      mouseY -= this.topPos;
      this.drawBackgroundAndTitle(graphics, delta, mouseX, mouseY);
      if (((ContainerBase)this.menu).base instanceof IUpgradableBlock) {
         bindTexture(Identifier.fromNamespaceAndPath("ic2", "textures/gui/infobutton.png"));
         this.drawTexturedRect(graphics, 3.0, 3.0, 10.0, 10.0, 0.0, 0.0);
      }

      if (this.elementMethods.contains(GuiElement.ImplementedMethod.drawBackground)) {
         for (GuiElement<?> element : this.elements) {
            if (element.isEnabled()) {
               element.drawBackground(graphics, mouseX, mouseY);
            }
         }
      }
   }

   protected void drawBackgroundAndTitle(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
      this.bindTexture();
      graphics.blit(RenderPipelines.GUI_TEXTURED, this.boundTexture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
      this.drawXCenteredString(graphics, this.imageWidth / 2, 6, this.title, 4210752, false);
   }

   @Override
   protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      this.guiGraphics = graphics;
      this.drawForegroundLayer(graphics, mouseX - this.leftPos, mouseY - this.topPos);
      this.flushTooltips(graphics);
   }

   protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      if (((ContainerBase)this.menu).base instanceof IUpgradableBlock) {
         this.handleUpgradeTooltip(mouseX, mouseY);
      }

      for (GuiElement<?> element : this.elements) {
         if (element.isEnabled()) {
            element.drawForeground(graphics, mouseX, mouseY);
         }
      }
   }

   private void handleUpgradeTooltip(int mouseX, int mouseY) {
      int areaSize = 12;
      if (mouseX >= 0 && mouseX <= 12 && mouseY >= 0 && mouseY <= 12) {
         List<Component> text = new ArrayList<>();
         text.add(Component.translatable("ic2.generic.text.upgrade"));

         for (ItemStack stack : getCompatibleUpgrades((IUpgradableBlock)((ContainerBase)this.menu).base)) {
            text.add(stack.getHoverName().copy().withStyle(net.minecraft.ChatFormatting.GRAY));
         }

         this.drawTooltip(mouseX, mouseY, text);
      }
   }

   private static List<ItemStack> getCompatibleUpgrades(IUpgradableBlock block) {
      List<ItemStack> ret = new ArrayList<>();
      Set<UpgradableProperty> properties = block.getUpgradableProperties();

      for (ItemStack stack : UpgradeRegistry.getUpgrades()) {
         IUpgradeItem item = (IUpgradeItem)stack.getItem();
         if (item.isSuitableFor(stack, properties)) {
            ret.add(stack);
         }
      }

      return ret;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta, double verticalDelta) {
      if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseScroll)) {
         ScrollDirection direction;
         if (verticalDelta != 0.0) {
            direction = verticalDelta < 0.0 ? ScrollDirection.down : ScrollDirection.up;
         } else {
            direction = ScrollDirection.stopped;
         }

         for (GuiElement<?> element : this.elements) {
            int localX = (int)mouseX - this.leftPos;
            int localY = (int)mouseY - this.topPos;
            if (element.isEnabled() && element.contains(localX, localY)) {
               element.onMouseScroll(localX, localY, direction);
            }
         }
      }

      return super.mouseScrolled(mouseX, mouseY, scrollDelta, verticalDelta);
   }

   @Override
   public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
      double mouseX = event.x();
      double mouseY = event.y();
      int mouseButton = event.button();
      if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseClick)) {
         MouseButton button = MouseButton.get(mouseButton);
         if (button != null) {
            boolean handled = false;
            mouseX -= this.leftPos;
            mouseY -= this.topPos;

            for (GuiElement<?> element : this.elements) {
               if (element.isEnabled()) {
                  handled |= element.onMouseClick((int)mouseX, (int)mouseY, button, element.contains((int)mouseX, (int)mouseY));
               }
            }

            if (handled) {
               return true;
            }

            mouseX += this.leftPos;
            mouseY += this.topPos;
         }
      }

      return super.mouseClicked(event, doubleClick);
   }

   @Override
   public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double deltaX, double deltaY) {
      double mouseX = event.x();
      double mouseY = event.y();
      int clickedMouseButton = event.button();
      if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseDrag)) {
         MouseButton button = MouseButton.get(clickedMouseButton);
         if (button != null) {
            boolean handled = false;
            mouseX -= this.leftPos;
            mouseY -= this.topPos;

            for (GuiElement<?> element : this.elements) {
               if (element.isEnabled()) {
                  handled |= element.onMouseDrag((int)mouseX, (int)mouseY, button, element.contains((int)mouseX, (int)mouseY));
               }
            }

            if (handled) {
               return true;
            }

            mouseX += this.leftPos;
            mouseY += this.topPos;
         }
      }

      return super.mouseDragged(event, deltaX, deltaY);
   }

   @Override
   public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
      double mouseX = event.x();
      double mouseY = event.y();
      int state = event.button();
      if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseRelease)) {
         MouseButton button = MouseButton.get(state);
         if (button != null) {
            boolean handled = false;
            mouseX -= this.leftPos;
            mouseY -= this.topPos;

            for (GuiElement<?> element : this.elements) {
               if (element.isEnabled()) {
                  handled |= element.onMouseRelease((int)mouseX, (int)mouseY, button, element.contains((int)mouseX, (int)mouseY));
               }
            }

            if (handled) {
               return true;
            }

            mouseX += this.leftPos;
            mouseY += this.topPos;
         }
      }

      return super.mouseReleased(event);
   }

   @Override
   public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
      if (this.elementMethods.contains(GuiElement.ImplementedMethod.onKeyTyped)) {
         char typedChar = new String(Character.toChars(event.codepoint())).isEmpty() ? '\0' : new String(Character.toChars(event.codepoint())).charAt(0);
         int keyCode = event.codepoint();
         boolean handled = false;

         for (GuiElement<?> element : this.elements) {
            if (element.isEnabled()) {
               handled |= element.onKeyTyped(typedChar, keyCode);
            }
         }

         if (handled) {
            return true;
         }
      }

      return super.charTyped(event);
   }

   @Override
   public void removed() {
      super.removed();
      if (this.fixKeyEvents) {
      }

      if (closeHandler != null) {
         closeHandler.run();
      }
   }

   public void drawTexturedRect(GuiGraphicsExtractor graphics, double x, double y, double width, double height, double texX, double texY) {
      this.drawTexturedRect(graphics, x, y, width, height, texX, texY, false);
   }

   public void drawTexturedRect(GuiGraphicsExtractor graphics, double x, double y, double width, double height, double texX, double texY, boolean mirrorX) {
      this.drawTexturedRect(graphics, x, y, width, height, texX / 256.0, texY / 256.0, (texX + width) / 256.0, (texY + height) / 256.0, mirrorX);
   }

   public void drawTexturedRect(
      GuiGraphicsExtractor graphics, double x, double y, double width, double height, double uS, double vS, double uE, double vE, boolean mirrorX
   ) {
      x += this.leftPos;
      y += this.topPos;
      double xE = x + width;
      double yE = y + height;
      if (mirrorX) {
         double tmp = uS;
         uS = uE;
         uE = tmp;
      }

      if (this.boundTexture == null) {
         return;
      }
      graphics.blit(
         this.boundTexture,
         (int)Math.round(x),
         (int)Math.round(y),
         (int)Math.round(xE),
         (int)Math.round(yE),
         (float)uS,
         (float)uE,
         (float)vS,
         (float)vE
      );
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
      boolean fixRight,
      boolean fixBottom
   ) {
      if (sprite == null) {
         sprite = new TextureAtlasSprite(MissingTextureAtlasSprite.getLocation(), MissingTextureAtlasSprite.create(), 16, 16, 0, 0, 0) {};
      }

      x += this.leftPos;
      y += this.topPos;
      scale *= 16.0;
      double spriteUS = sprite.getU0();
      double spriteVS = sprite.getV0();
      double spriteWidth = sprite.getU1() - spriteUS;
      double spriteHeight = sprite.getV1() - spriteVS;
      double xS = x;

      while (xS < x + width) {
         double maxWidth;
         double uS;
         if (xS == x && fixRight && (maxWidth = width % scale) > 0.0) {
            uS = spriteUS + spriteWidth * (1.0 - maxWidth / scale);
         } else {
            maxWidth = scale;
            uS = spriteUS;
         }

         double xE = Math.min(xS + maxWidth, x + width);
         double uE = uS + (xE - xS) / scale * spriteWidth;
         double yS = y;

         while (yS < y + height) {
            double maxHeight;
            double vS;
            if (yS == y && fixBottom && (maxHeight = height % scale) > 0.0) {
               vS = spriteVS + spriteHeight * (1.0 - maxHeight / scale);
            } else {
               maxHeight = scale;
               vS = spriteVS;
            }

            double yE = Math.min(yS + maxHeight, y + height);
            double vE = vS + (yE - yS) / scale * spriteHeight;
            int atlasWidth = Math.round(sprite.contents().width() / (float)spriteWidth);
            int atlasHeight = Math.round(sprite.contents().height() / (float)spriteHeight);
            graphics.blit(
               RenderPipelines.GUI_TEXTURED, sprite.atlasLocation(),
               (int)Math.round(xS), (int)Math.round(yS),
               (float)(uS * atlasWidth), (float)(vS * atlasHeight),
               (int)Math.round(xE) - (int)Math.round(xS),
               (int)Math.round(yE) - (int)Math.round(yS),
               (int)Math.round((uE - uS) * atlasWidth), (int)Math.round((vE - vS) * atlasHeight),
               atlasWidth, atlasHeight, color
            );
            yS += maxHeight;
         }

         xS += maxWidth;
      }
   }

   public void drawItem(int x, int y, ItemStack stack) {
      this.guiGraphics.item(stack, this.leftPos + x, this.topPos + y);
   }

   public void drawItemStack(int x, int y, ItemStack stack) {
      this.guiGraphics.item(this.minecraft.player, stack, this.leftPos + x, this.topPos + y, 0);
      this.guiGraphics.itemDecorations(this.font, stack, this.leftPos + x, this.topPos + y);
   }

   public void drawColoredRect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
      x += this.leftPos;
      y += this.topPos;
      int xE = x + width;
      int yE = y + height;
      graphics.fill(x, y, xE, yE, color);
   }

   public int drawString(GuiGraphicsExtractor graphics, int x, int y, String text, int color) {
      graphics.text(this.font, text, x, y, opaqueLegacyColor(color), false);
      return x + this.getStringWidth(text);
   }

   public int drawString(GuiGraphicsExtractor graphics, int x, int y, String text, int color, boolean shadow) {
      graphics.text(this.font, text, this.leftPos + x, this.topPos + y, opaqueLegacyColor(color), shadow);
      return x + this.getStringWidth(text);
   }

   public void drawTrimmedString(GuiGraphicsExtractor graphics, int x, int y, String text, int maxWidth, int color) {
      for (net.minecraft.util.FormattedCharSequence line : this.font.split(Component.literal(text), maxWidth)) {
         graphics.text(this.font, line, x, y, opaqueLegacyColor(color), false);
         y += 10;
      }
   }

   public void drawXCenteredString(GuiGraphicsExtractor graphics, int x, int y, String text, int color, boolean shadow) {
      this.drawCenteredString(graphics, x, y, text, color, shadow, true, false);
   }

   public void drawXCenteredString(GuiGraphicsExtractor graphics, int x, int y, Component text, int color, boolean shadow) {
      this.drawCenteredString(graphics, x, y, text, color, shadow, true, false);
   }

   public void drawXYCenteredString(GuiGraphicsExtractor graphics, int x, int y, String text, int color, boolean shadow) {
      this.drawCenteredString(graphics, x, y, text, color, shadow, true, true);
   }

   public void drawXYCenteredString(GuiGraphicsExtractor graphics, int x, int y, Component text, int color, boolean shadow) {
      this.drawCenteredString(graphics, x, y, text, color, shadow, true, true);
   }

   public void drawCenteredString(GuiGraphicsExtractor graphics, int x, int y, String text, int color, boolean shadow, boolean centerX, boolean centerY) {
      if (centerX) {
         x -= this.getStringWidth(text) / 2;
      }

      if (centerY) {
         y -= 4;
      }

      graphics.text(this.font, text, this.leftPos + x, this.topPos + y, opaqueLegacyColor(color), shadow);
   }

   public void drawCenteredString(GuiGraphicsExtractor graphics, int x, int y, Component text, int color, boolean shadow, boolean centerX, boolean centerY) {
      if (centerX) {
         x -= this.getStringWidth(text) / 2;
      }

      if (centerY) {
         y -= 4;
      }

      graphics.text(this.font, text, this.leftPos + x, this.topPos + y, opaqueLegacyColor(color), shadow);
   }

   private static int opaqueLegacyColor(int color) {
      return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
   }

   public int getStringWidth(String text) {
      return this.font.width(text);
   }

   public int getStringWidth(Component text) {
      return this.font.width(text);
   }

   public String trimStringToWidth(String text, int width) {
      return this.font.plainSubstrByWidth(text, width, false);
   }

   public String trimStringToWidthReverse(String text, int width) {
      return this.font.plainSubstrByWidth(text, width, true);
   }

   public void drawTooltip(int x, int y, List<Component> text) {
      this.queuedTooltips.add(new Ic2Gui.Tooltip(text, x, y));
   }

   public void drawTooltip(GuiGraphicsExtractor graphics, int x, int y, ItemStack stack) {
      assert !StackUtil.isEmpty(stack);

      graphics.setTooltipForNextFrame(this.font, stack, this.leftPos + x, this.topPos + y);
   }

   protected void flushTooltips(GuiGraphicsExtractor graphics) {
      for (Ic2Gui.Tooltip tooltip : this.queuedTooltips) {
         graphics.setTooltipForNextFrame(this.font, tooltip.text, Optional.empty(), ItemStack.EMPTY,
            this.leftPos + tooltip.x, this.topPos + tooltip.y);
      }

      this.queuedTooltips.clear();
   }

   protected void addElement(GuiElement<?> element) {
      this.elements.add(element);
      this.elementMethods.addAll(element.getImplementedMethods());
   }

   /** Legacy 7-arg blit helper bound to the last bindTexture() id (256x256 atlases). */
   protected void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
      if (this.boundTexture != null) {
         graphics.blit(RenderPipelines.GUI_TEXTURED, this.boundTexture, x, y, u, v, width, height, 256, 256);
      }
   }

   protected final void bindTexture() {
      this.bindTexture(this.getTexture());
   }

   /** Tracks the texture used by the blit-based drawing helpers (per render-state). */
   public void bindTexture(Identifier id) {
      this.boundTexture = id;
   }

   protected IClickHandler createEventSender(int event) {
      if (((ContainerBase)this.menu).base instanceof BlockEntity) {
         return new IClickHandler() {
            @Override
            public void onClick(MouseButton button) {
               IC2.network.get(false).initiateClientTileEntityEvent((BlockEntity)((ContainerBase)Ic2Gui.this.menu).base, event);
            }
         };
      } else {
         throw new IllegalArgumentException("not applicable for " + ((ContainerBase)this.menu).base);
      }
   }

   protected abstract Identifier getTexture();

   private static class Tooltip {
      final int x;
      final int y;
      final List<Component> text;

      Tooltip(List<Component> text, int x, int y) {
         this.text = text;
         this.x = x;
         this.y = y;
      }
   }
}
