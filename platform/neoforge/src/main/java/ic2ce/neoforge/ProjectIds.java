package ic2ce.neoforge;

import net.minecraft.resources.Identifier;

/** The project namespace; registry, resource and persistent IDs keep the original "ic2" value. */
public final class ProjectIds {
  public static final String MOD_ID = "ic2";

  private ProjectIds() {}

  public static Identifier id(String name) {
    return Identifier.fromNamespaceAndPath(MOD_ID, name);
  }
}
