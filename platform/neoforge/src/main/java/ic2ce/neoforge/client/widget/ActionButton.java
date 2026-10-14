package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

/** A click consumes its event once; resolved live text is never translated or formatted twice. */
public abstract class ActionButton<T extends ActionButton<T>> extends ScreenWidget<T> {
  private final ClickAction handler;
  private Supplier<String> textProvider;
  private Supplier<ItemStack> iconProvider;

  protected ActionButton(
      MachineScreen<?> gui, int x, int y, int width, int height, ClickAction handler) {
    super(gui, x, y, width, height);
    this.handler = Objects.requireNonNull(handler);
  }

  public T withText(String text) {
    Objects.requireNonNull(text);
    return withText(() -> Language.getInstance().has(text) ? LocalizedText.translate(text) : text);
  }

  @SuppressWarnings("unchecked")
  public T withText(Supplier<String> text) {
    textProvider = Objects.requireNonNull(text);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T withIcon(Supplier<ItemStack> icon) {
    iconProvider = Objects.requireNonNull(icon);
    return (T) this;
  }

  protected int getTextColor(int mx, int my) {
    return 0xdddddd;
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    if (textProvider != null) {
      String text = textProvider.get();
      if (text != null && !text.isEmpty())
        gui.drawXYCenteredString(
            graphics, x + width / 2, y + height / 2, text, getTextColor(mx, my), true);
    } else if (iconProvider != null) {
      var icon = iconProvider.get();
      if (icon != null && !icon.isEmpty())
        gui.drawItem(x + (width - 16) / 2, y + (height - 16) / 2, icon);
    }
  }

  @Override
  protected boolean onMouseClick(int mx, int my, PointerButton button) {
    handler.onClick(button);
    Minecraft.getInstance()
        .getSoundManager()
        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    return true;
  }
}
