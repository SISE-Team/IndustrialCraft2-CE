package ic2ce.neoforge.menu.document;

/** Values are sampled live; their units and ranges belong to the named device. */
@FunctionalInterface
public interface ScreenValues {
  double getGuiValue(String value);

  /** Availability is distinct from a present value whose numeric result is zero. */
  interface IActiveGuiValueProvider extends ScreenValues {
    boolean isGuiValueActive(String value);
  }
}
