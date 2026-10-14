package ic2ce.neoforge.menu.document;

import java.util.*;
import net.minecraft.network.chat.Component;

/**
 * Actual native field/method chains compile once and remain live without sharing resolved values
 * between owners.
 */
public final class TextContracts {
  public static final class Data {
    public int value = 3;
    public static int shared = 7;
    public Data nested;

    public int getValue() {
      return value;
    }
  }

  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native text: " + label);
  }

  public static Result run() {
    return new TextContracts().exercise();
  }

  private Result exercise() {
    var first = new Data();
    var second = new Data();
    second.value = 11;
    first.nested = second;
    var fields = LiveText.parse("%base.value% %base.getValue()% %base.nested.value%", Data.class);
    check(
        fields.get(first, Map.of()).equals("3 3 11"),
        "actual native field, no-arg method and nested public path resolve");
    first.value = 5;
    check(
        fields.get(first, Map.of()).equals("5 5 11")
            && fields.get(second, Map.of()).equals("ERROR"),
        "compiled member handles follow actual owner changes and tolerate missing nested values");
    var constant = LiveText.parse("%base.shared%", Data.class);
    check(
        constant.get(null, Map.of()).equals("7"),
        "declared class resolves a static native value without a world owner");
    Data.shared = 9;
    check(
        constant.get(null, Map.of()).equals("9"),
        "mutable static native fields are observed live after compilation");
    Data.shared = 7;
    var component = LiveText.of(Component.literal("Already resolved % name"));
    check(
        component.get(first, Map.of()).equals("Already resolved % name"),
        "resolved native Component text remains literal and is not interpreted as a translation"
            + " format");
    var named = LiveText.parse("[%name%]", Data.class);
    check(
        named.get(first, Map.of("name", component)).equals("[Already resolved % name]"),
        "caller-provided native text tokens remain owned live values");
    check(
        LiveText.parse("%base.missing%", Data.class).getOptional(first, Map.of()) == null
            && LiveText.parse("%base.missing%", Data.class).get(first, Map.of()).equals("ERROR"),
        "unknown native member has explicit optional and required text behavior");
    check(
        LiveText.parse("broken%", Data.class).get(first, Map.of()).startsWith("ERROR:"),
        "malformed template cannot crash a native GUI render");
    return new Result(checks, List.copyOf(failures));
  }
}
