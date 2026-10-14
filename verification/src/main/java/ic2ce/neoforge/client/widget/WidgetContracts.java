package ic2ce.neoforge.client.widget;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.LiveText;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

/** Runs on an actual initialized native client, with its real font, clipboard and menu. */
public final class WidgetContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native widget: " + label);
  }

  public static Result run(MachineScreen<?> original) {
    return new WidgetContracts().exercise(original);
  }

  private static final class RecordingScreen extends MachineScreen<OwnedMenu<? extends Container>> {
    int drawnX, drawnY;
    String drawnText;

    RecordingScreen(OwnedMenu<? extends Container> menu, Inventory inventory) {
      super(menu, inventory, Component.literal("Probe"));
    }

    @Override
    protected Identifier getTexture() {
      return ScreenWidget.commonTexture;
    }

    @Override
    public int drawString(
        GuiGraphicsExtractor graphics, int x, int y, String text, int color, boolean shadow) {
      drawnX = x;
      drawnY = y;
      drawnText = text;
      return x + getStringWidth(text);
    }
  }

  private static class Probe extends ScreenWidget<Probe> {
    Probe(MachineScreen<?> gui, int x, int y, int w, int h) {
      super(gui, x, y, w, h);
    }

    public void tick() {}

    public void drawBackground(String unrelated) {}
  }

  private static final class SkippedProbe extends Probe {
    SkippedProbe(MachineScreen<?> gui) {
      super(gui, 0, 0, 1, 1);
    }

    @Override
    @SkippedMethod
    public void tick() {}
  }

  private Result exercise(MachineScreen<?> original) {
    var gui =
        new RecordingScreen(original.getContainer(), Minecraft.getInstance().player.getInventory());
    var hit = new Probe(gui, 10, 20, 4, 5);
    check(
        hit.contains(10, 20)
            && hit.contains(13, 24)
            && !hit.contains(14, 24)
            && !hit.contains(13, 25),
        "physical native widget edges admit one adjacent owner only");
    check(
        !new Probe(gui, Integer.MAX_VALUE - 1, 0, 4, 1).contains(Integer.MIN_VALUE, 0),
        "widget bounds cannot overflow into the opposite screen edge");
    var methods = hit.getImplementedMethods();
    check(
        methods.contains(ScreenWidget.ImplementedMethod.tick)
            && !methods.contains(ScreenWidget.ImplementedMethod.drawBackground),
        "dispatch follows actual event signatures, not unrelated overload names");
    boolean immutable = false;
    try {
      methods.clear();
    } catch (UnsupportedOperationException expected) {
      immutable = true;
    }
    check(
        immutable && new SkippedProbe(gui).getImplementedMethods().isEmpty(),
        "ClassValue dispatch snapshots are immutable and honor nearest skipped overrides");
    int[] calls = {0};
    WidgetCondition[] plan = {
      () -> false,
      () -> {
        calls[0]++;
        return true;
      }
    };
    var and = WidgetCondition.EnableHandlers.and(plan);
    plan[0] = () -> true;
    check(
        !and.isEnabled() && calls[0] == 0,
        "owned enable plan rejects external mutation and short-circuits before side effects");
    check(
        WidgetCondition.EnableHandlers.and().isEnabled()
            && !WidgetCondition.EnableHandlers.or().isEnabled()
            && !WidgetCondition.EnableHandlers.nand().isEnabled()
            && WidgetCondition.EnableHandlers.nor().isEnabled(),
        "empty boolean plans retain conjunction/disjunction identities");
    String[] label = {"1"};
    var text =
        LiveLabel.createRightAligned(
            gui,
            100,
            20,
            -1,
            -1,
            LiveText.of((com.google.common.base.Supplier<String>) () -> label[0]),
            0xffffff,
            false,
            0,
            0,
            false,
            false);
    text.drawBackground(null, 0, 0);
    check(
        gui.drawnX + gui.getStringWidth(gui.drawnText) == 100,
        "right aligned actual font has a fixed endpoint");
    label[0] = "1234567890";
    text.drawBackground(null, 0, 0);
    check(
        gui.drawnX + gui.getStringWidth(gui.drawnText) == 100
            && text.width == gui.getStringWidth(label[0]),
        "changed live native label updates alignment and its actual hover width");
    var edits = new ArrayList<String>();
    var field =
        new TextInput(gui, 10, 30, 120, 18, "12")
            .withTextWatcher((oldValue, value) -> edits.add(oldValue + ">" + value));
    field.setMaxTextLength(4);
    check(
        field.setText("123456", true)
            && field.getText().equals("1234")
            && edits.equals(List.of("12>1234")),
        "text watcher owns the accepted native truncated value");
    field.setText("1234");
    check(edits.size() == 1, "unchanged native value does not emit another network callback");
    field.withTextValidator(value -> value.chars().allMatch(Character::isDigit));
    check(
        !field.setText("x") && field.getText().equals("1234"),
        "native rejected filter leaves text and callbacks untouched");
    field.withTextValidator(value -> true);
    field.setMaxTextLength(20);
    field.setText("A😀B");
    field.setFocused(true);
    field.setCursorPositionEnd();
    check(
        field.onKeyPressed(new KeyEvent(org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE, 0, 0))
            && field.getText().equals("A😀"),
        "native physical backspace deletes one actual Unicode character");
    field.onKeyPressed(new KeyEvent(org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE, 0, 0));
    check(
        field.getText().equals("A")
            && field.onCharacterTyped(new CharacterEvent(0x1f600))
            && field.getText().equals("A😀"),
        "native supplementary character input and cursor removal preserve valid surrogate pairs");
    field.onKeyPressed(new KeyEvent(org.lwjgl.glfw.GLFW.GLFW_KEY_A, 0, 2));
    check(
        field.getSelectedText().equals("A😀"),
        "native control-A keyboard event selects the actual field content");
    field.setFocused(false);
    check(
        !field.onCharacterTyped(new CharacterEvent('Z')) && field.getText().equals("A😀"),
        "unfocused field cannot consume another widget's character input");
    var atlas = new ImageDimensions(ScreenWidget.commonTexture);
    atlas.load(
        new java.awt.image.BufferedImage(37, 19, java.awt.image.BufferedImage.TYPE_INT_ARGB));
    check(
        atlas.getWidth() == 37 && atlas.getCanvasWidth() == 37 && atlas.getCanvasHeight() == 19,
        "non-power-of-two native images retain their actual UV extents");
    var choices = new PointerButton[] {PointerButton.right, PointerButton.left};
    var cycle = new EnumCursor<>(0, 0, 10, 6, 10, false, choices, PointerButton.right);
    choices[0] = PointerButton.left;
    cycle.onClick(PointerButton.left);
    check(
        cycle.getCurrentValue() == PointerButton.left && cycle.getUS() == 10 && cycle.getUE() == 20,
        "ordered enum cursor owns caller choices and shifts both actual endpoints");
    cycle.onClick(PointerButton.left);
    check(
        cycle.getCurrentValue() == PointerButton.right,
        "forward cycle wraps to its exact first choice");
    cycle.onClick(PointerButton.right);
    check(
        cycle.getCurrentValue() == PointerButton.left,
        "backward cycle wraps without integer overflow");
    boolean emptyCycle = false;
    try {
      new EnumCursor<>(new PointerButton[0]);
    } catch (IllegalArgumentException expected) {
      emptyCycle = true;
    }
    check(emptyCycle, "empty option cursor rejects before accessing element zero");
    var transitions = new ArrayList<String>();
    class Row implements SelectableRows.ISelectableListItem {
      final String name;

      Row(String name) {
        this.name = name;
      }

      public void draw(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mx, int my) {}

      public void onSelected() {
        transitions.add("+" + name);
      }

      public void onDeselected() {
        transitions.add("-" + name);
      }
    }
    var a = new Row("a");
    var b = new Row("b");
    var callerRows = new ArrayList<SelectableRows.ISelectableListItem>(List.of(a, b));
    var selection = new SelectableRows(gui, 10, 20, 40, 25, callerRows);
    callerRows.clear();
    selection.setSelected(a).setSelected(a).setSelected(b).clearSelected().clearSelected();
    check(
        transitions.equals(List.of("+a", "-a", "+b", "-b")),
        "real native list owns its rows and emits one pair of selected/deselected transitions");
    selection.setSelected(a).removeItem(a);
    check(
        selection.currentSelected == null && transitions.getLast().equals("-a"),
        "removing a selected native row clears actual ownership");
    boolean badSelection = false;
    try {
      selection.setSelected(a);
    } catch (IllegalArgumentException expected) {
      badSelection = true;
    }
    check(badSelection, "removed row cannot be selected again through a stale GUI reference");
    String variant = "ic2ce_widget_probe";
    var style = new InventoryGrid.SlotStyle(18, 18);
    InventoryGrid.SlotStyle.registerVarient(variant, style);
    boolean duplicate = false;
    try {
      InventoryGrid.SlotStyle.registerVarient(variant, new InventoryGrid.SlotStyle(20, 20));
    } catch (IllegalArgumentException expected) {
      duplicate = true;
    }
    check(
        duplicate && InventoryGrid.SlotStyle.get(variant) == style,
        "duplicate native style registration preserves the previously published variant");
    boolean invalidGrid = false;
    try {
      new InventoryGrid(gui, 0, 0, 1, 1, style, 0, -18);
    } catch (IllegalArgumentException expected) {
      invalidGrid = true;
    }
    check(invalidGrid, "non-positive visual stride cannot trigger an unbounded native render loop");
    return new Result(checks, List.copyOf(failures));
  }
}
