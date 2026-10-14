package ic2ce.neoforge.client.widget;

import com.google.common.base.Predicate;
import ic2ce.neoforge.client.screen.MachineScreen;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;

/**
 * Native text editing owns Unicode cursor movement, selection, clipboard shortcuts and render
 * coordinates.
 */
public class TextInput extends ScreenWidget<TextInput> implements KeyboardFocus {
  private final EditBox editor;
  private WidgetCondition editable;
  private ITextBoxWatcher watcher;
  private Predicate<String> validator = value -> true;
  private int maximum = 32;
  private String observed;

  public TextInput(MachineScreen<?> gui, int x, int y, int width, int height) {
    this(gui, x, y, width, height, "", true);
  }

  public TextInput(MachineScreen<?> gui, int x, int y, int width, int height, String text) {
    this(gui, x, y, width, height, text, true);
  }

  public TextInput(MachineScreen<?> gui, int x, int y, int width, int height, boolean border) {
    this(gui, x, y, width, height, "", border);
  }

  public TextInput(
      MachineScreen<?> gui, int x, int y, int width, int height, String text, boolean border) {
    super(gui, x, y, width, height);
    Objects.requireNonNull(text);
    editor =
        new EditBox(
            Minecraft.getInstance().font,
            x,
            y,
            width,
            height,
            Component.translatable("ic2.gui.text_input"));
    editor.setBordered(border);
    maximum = Math.max(maximum, text.length());
    editor.setMaxLength(maximum);
    editor.setValue(text);
    observed = editor.getValue();
    editor.setResponder(
        value -> {
          String previous = observed;
          observed = value;
          if (watcher != null && !Objects.equals(previous, value))
            watcher.onChanged(previous, value);
        });
    editor.setFilter(value -> validator.apply(value));
  }

  public TextInput withTextEnableHandler(WidgetCondition handler) {
    editable = handler;
    return this;
  }

  public TextInput withTextValidator(Predicate<String> filter) {
    validator = Objects.requireNonNull(filter);
    return this;
  }

  public TextInput withTextWatcher(ITextBoxWatcher watcher) {
    this.watcher = watcher;
    return this;
  }

  public boolean willDraw() {
    return editable == null || editable.isEnabled();
  }

  public void setFocused(boolean focused) {
    editor.setFocused(focused);
  }

  public boolean isFocused() {
    return editor.isFocused();
  }

  public void setMaxTextLength(int length) {
    if (length < 0) throw new IllegalArgumentException("Negative text limit");
    maximum = length;
    editor.setMaxLength(length);
  }

  public String getText() {
    return editor.getValue();
  }

  public boolean setText(String text) {
    return setText(text, true);
  }

  public boolean setText(String value, boolean forceLength) {
    Objects.requireNonNull(value);
    if (!forceLength && value.length() > maximum) return false;
    String accepted = value.length() > maximum ? value.substring(0, maximum) : value;
    if (!accepted.isEmpty() && Character.isHighSurrogate(accepted.charAt(accepted.length() - 1)))
      accepted = accepted.substring(0, accepted.length() - 1);
    if (!validator.apply(accepted)) return false;
    editor.setValue(accepted);
    return editor.getValue().equals(accepted);
  }

  @Override
  public void drawBackground(GuiGraphicsExtractor graphics, int mx, int my) {
    editor.setX(gui.getX() + x);
    editor.setY(gui.getY() + y);
    editor.setEditable(willDraw());
    editor.extractWidgetRenderState(graphics, mx + gui.getX(), my + gui.getY(), 0);
  }

  @Override
  public boolean onMouseClick(int mx, int my, PointerButton button, boolean onThis) {
    editor.setFocused(onThis);
    if (!onThis) return false;
    editor.setX(gui.getX() + x);
    editor.setY(gui.getY() + y);
    if (button == PointerButton.left)
      editor.onClick(
          new MouseButtonEvent(mx + gui.getX(), my + gui.getY(), new MouseButtonInfo(button.id, 0)),
          false);
    return true;
  }

  @Override
  public boolean onKeyPressed(KeyEvent event) {
    editor.setEditable(willDraw());
    return editor.keyPressed(event);
  }

  @Override
  public boolean onCharacterTyped(CharacterEvent event) {
    editor.setEditable(willDraw());
    return editor.charTyped(event);
  }

  @Override
  public boolean onKeyTyped(char character, int key) {
    if (character != '\0') return onCharacterTyped(new CharacterEvent(character));
    int modifiers =
        (Minecraft.getInstance().hasControlDown() ? 2 : 0)
            | (Minecraft.getInstance().hasShiftDown() ? 1 : 0)
            | (Minecraft.getInstance().hasAltDown() ? 4 : 0);
    return onKeyPressed(new KeyEvent(key, 0, modifiers));
  }

  public void writeText(String value) {
    editor.insertText(Objects.requireNonNull(value));
  }

  public void deleteWords(int words) {
    editor.deleteWords(words);
  }

  public void deleteFromCursor(int characters) {
    editor.deleteChars(characters);
  }

  public String getSelectedText() {
    return editor.getHighlighted();
  }

  protected void setCursorPositionStart() {
    editor.moveCursorToStart(false);
  }

  protected void setCursorPositionEnd() {
    editor.moveCursorToEnd(false);
  }

  protected void moveCursorBy(int amount) {
    editor.moveCursor(amount, false);
  }

  protected void setCursorPosition(int position) {
    editor.moveCursorTo(position, false);
  }

  protected void setSelectionPos(int position) {
    editor.setHighlightPos(position);
  }

  protected int getNthWordFromCursor(int words) {
    return editor.getWordPosition(words);
  }

  protected int getNthWordFromPos(int words, int position) {
    return getNthWordFromPosWS(words, position, true);
  }

  protected int getNthWordFromPosWS(int words, int position, boolean skipWhitespace) {
    String text = getText();
    position = Math.clamp(position, 0, text.length());
    long remaining = Math.abs((long) words);
    while (remaining-- > 0
        && position >= (words < 0 ? 1 : 0)
        && position < (words < 0 ? text.length() + 1 : text.length())) {
      if (words < 0) {
        if (skipWhitespace)
          while (position > 0 && Character.isWhitespace(text.charAt(position - 1))) position--;
        while (position > 0 && !Character.isWhitespace(text.charAt(position - 1))) position--;
      } else {
        while (position < text.length() && !Character.isWhitespace(text.charAt(position)))
          position++;
        if (skipWhitespace)
          while (position < text.length() && Character.isWhitespace(text.charAt(position)))
            position++;
      }
      if (position == 0 || position == text.length()) break;
    }
    return position;
  }

  @FunctionalInterface
  public interface ITextBoxWatcher {
    void onChanged(String previous, String current);
  }
}
