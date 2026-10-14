package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.menu.document.LiveText;
import java.lang.annotation.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;

/** Native widget ownership, half-open hit bounds and immutable per-class dispatch metadata. */
public abstract class ScreenWidget<T extends ScreenWidget<T>> {
  protected static final int hoverColor = 0x80ffffff;
  public static final Identifier commonTexture =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/common.png");
  protected final MachineScreen<?> gui;
  protected int x, y, width, height;
  private WidgetCondition enableHandler;
  private Supplier<String> tooltipProvider;
  private static final ClassValue<Set<ImplementedMethod>> DISPATCH =
      new ClassValue<>() {
        @Override
        protected Set<ImplementedMethod> computeValue(Class<?> type) {
          var implemented = EnumSet.noneOf(ImplementedMethod.class);
          var resolved = new HashSet<String>();
          for (Class<?> owner = type;
              owner != null && owner != ScreenWidget.class;
              owner = owner.getSuperclass()) {
            for (var method : owner.getDeclaredMethods()) {
              if (Modifier.isStatic(method.getModifiers())
                  || Modifier.isPrivate(method.getModifiers())
                  || method.isBridge()
                  || method.isSynthetic()) continue;
              var event = ImplementedMethod.LOOKUP.get(method.getName());
              if (event == null || !event.matches(method.getParameterTypes())) continue;
              String signature = method.getName() + Arrays.toString(method.getParameterTypes());
              if (resolved.add(signature) && !method.isAnnotationPresent(SkippedMethod.class))
                implemented.add(event);
            }
          }
          return Collections.unmodifiableSet(implemented);
        }
      };

  protected ScreenWidget(MachineScreen<?> gui, int x, int y, int width, int height) {
    if (width < 0 || height < 0) throw new IllegalArgumentException("Negative widget dimensions");
    this.gui = Objects.requireNonNull(gui);
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public final boolean isEnabled() {
    return enableHandler == null || enableHandler.isEnabled();
  }

  public boolean contains(int px, int py) {
    return px >= x && py >= y && (long) px - x < width && (long) py - y < height;
  }

  @SuppressWarnings("unchecked")
  public T withEnableHandler(WidgetCondition handler) {
    enableHandler = handler;
    return (T) this;
  }

  public T withTooltip(String tooltip) {
    return withTooltip(() -> tooltip);
  }

  @SuppressWarnings("unchecked")
  public T withTooltip(Supplier<String> tooltip) {
    tooltipProvider = tooltip;
    return (T) this;
  }

  public void tick() {}

  public void drawBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

  public void drawForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (!contains(mouseX, mouseY) || suppressTooltip(mouseX, mouseY)) return;
    var lines = new ArrayList<>(getToolTip());
    String tooltip = tooltipProvider == null ? null : tooltipProvider.get();
    if (tooltip != null && !tooltip.isEmpty())
      for (String line : tooltip.split("\n", -1)) lines.add(processText(line));
    if (!lines.isEmpty()) gui.drawTooltip(mouseX, mouseY, lines);
  }

  public boolean onMouseClick(int mx, int my, PointerButton button, boolean onThis) {
    return onThis && onMouseClick(mx, my, button);
  }

  protected boolean onMouseClick(int mx, int my, PointerButton button) {
    return false;
  }

  public boolean onMouseDrag(int mx, int my, PointerButton button, boolean onThis) {
    return onThis && onMouseDrag(mx, my, button);
  }

  protected boolean onMouseDrag(int mx, int my, PointerButton button) {
    return false;
  }

  public boolean onMouseRelease(int mx, int my, PointerButton button, boolean onThis) {
    return onThis && onMouseRelease(mx, my, button);
  }

  protected boolean onMouseRelease(int mx, int my, PointerButton button) {
    return false;
  }

  public void onMouseScroll(int mx, int my, ContentScroll direction) {}

  public boolean onKeyTyped(char typedChar, int keyCode) {
    return false;
  }

  public boolean onKeyPressed(net.minecraft.client.input.KeyEvent event) {
    return onKeyTyped('\0', event.key());
  }

  public boolean onCharacterTyped(net.minecraft.client.input.CharacterEvent event) {
    return event.codepoint() <= Character.MAX_VALUE && onKeyTyped((char) event.codepoint(), 0);
  }

  protected boolean suppressTooltip(int mx, int my) {
    return false;
  }

  protected List<Component> getToolTip() {
    return new ArrayList<>();
  }

  protected static Component processText(String text) {
    return Language.getInstance().has(text)
        ? Component.translatable(text)
        : Component.literal(text);
  }

  protected final Container getBase() {
    return gui.getContainer().base;
  }

  protected final Map<String, LiveText.ITextProvider> getTokens() {
    return Map.of("name", LiveText.of(gui.getTitle()));
  }

  protected void bindTexture(Identifier texture) {
    gui.bindTexture(texture);
  }

  public void bindCommonTexture() {
    bindTexture(commonTexture);
  }

  protected void bindBlockTexture() {
    bindTexture(TextureAtlas.LOCATION_BLOCKS);
  }

  protected static TextureAtlas getBlockTextureMap() {
    return (TextureAtlas)
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
  }

  public final Set<ImplementedMethod> getImplementedMethods() {
    return DISPATCH.get(getClass());
  }

  public enum ImplementedMethod {
    tick,
    drawBackground,
    drawForeground,
    onMouseClick,
    onMouseDrag,
    onMouseRelease,
    onMouseScroll,
    onKeyTyped;
    static final Map<String, ImplementedMethod> LOOKUP;

    static {
      var index = new HashMap<String, ImplementedMethod>();
      for (var method : values()) index.put(method.name(), method);
      LOOKUP = Map.copyOf(index);
    }

    boolean matches(Class<?>[] parameters) {
      return switch (this) {
        case tick -> parameters.length == 0;
        case drawBackground, drawForeground ->
            Arrays.equals(
                parameters, new Class<?>[] {GuiGraphicsExtractor.class, int.class, int.class});
        case onMouseClick, onMouseDrag, onMouseRelease ->
            Arrays.equals(parameters, new Class<?>[] {int.class, int.class, PointerButton.class})
                || Arrays.equals(
                    parameters,
                    new Class<?>[] {int.class, int.class, PointerButton.class, boolean.class});
        case onMouseScroll ->
            Arrays.equals(parameters, new Class<?>[] {int.class, int.class, ContentScroll.class});
        case onKeyTyped -> Arrays.equals(parameters, new Class<?>[] {char.class, int.class});
      };
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  protected @interface SkippedMethod {}
}
