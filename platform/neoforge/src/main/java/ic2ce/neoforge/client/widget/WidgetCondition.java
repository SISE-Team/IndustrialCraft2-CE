package ic2ce.neoforge.client.widget;

import java.util.Objects;

/**
 * Native widget conditions short-circuit a fixed owned predicate plan without constructing streams
 * per frame.
 */
@FunctionalInterface
public interface WidgetCondition {
  boolean isEnabled();

  final class EnableHandlers {
    private EnableHandlers() {}

    private static WidgetCondition[] snapshot(WidgetCondition[] handlers) {
      var owned = handlers.clone();
      for (var handler : owned) Objects.requireNonNull(handler);
      return owned;
    }

    public static WidgetCondition and(WidgetCondition... handlers) {
      var owned = snapshot(handlers);
      return () -> {
        for (var handler : owned) if (!handler.isEnabled()) return false;
        return true;
      };
    }

    public static WidgetCondition or(WidgetCondition... handlers) {
      var owned = snapshot(handlers);
      return () -> {
        for (var handler : owned) if (handler.isEnabled()) return true;
        return false;
      };
    }

    public static WidgetCondition nand(WidgetCondition... handlers) {
      return not(and(handlers));
    }

    public static WidgetCondition nor(WidgetCondition... handlers) {
      return not(or(handlers));
    }

    public static WidgetCondition not(WidgetCondition handler) {
      Objects.requireNonNull(handler);
      return () -> !handler.isEnabled();
    }
  }
}
