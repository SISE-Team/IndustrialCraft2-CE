package ic2ce.neoforge.client.widget;

/** One recognized native mouse action; the parent widget owns consumption and sound. */
@FunctionalInterface
public interface ClickAction {
  void onClick(PointerButton button);
}
