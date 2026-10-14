package ic2ce.neoforge.network;

/** World-thread post-write validation for an explicitly client-modifiable native field. */
public interface ClientFieldValidator {
  void acceptClientField(String field);
}
