package ic2ce.neoforge.menu.document;

/** Live named predicates for a screen document, sampled again on each projection. */
@FunctionalInterface
public interface ScreenConditions {
  boolean getGuiState(String condition);
}
