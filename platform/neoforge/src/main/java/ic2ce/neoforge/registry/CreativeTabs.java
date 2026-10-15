package ic2ce.neoforge.registry;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.runtime.CategoryIcon;
import ic2ce.neoforge.runtime.CreativeCategory;
import ic2ce.neoforge.runtime.EnvProxy;
import net.minecraft.resources.Identifier;

/** Project creative tabs, staged in this declaration order. */
public final class CreativeTabs {
  public static final Identifier GENERAL = tab("general", CreativeCategory.GENERAL);
  public static final Identifier GENERATORS_AND_WIRING =
      tab("generators_and_wiring", CreativeCategory.GENERATORS_AND_WIRING);
  public static final Identifier REACTOR = tab("reactor", CreativeCategory.REACTOR);
  public static final Identifier MACHINES = tab("machines", CreativeCategory.MACHINES);
  public static final Identifier TOOLS_AND_UTILITIES =
      tab("tools_and_utilities", CreativeCategory.TOOLS_AND_UTILITIES);
  public static final Identifier COMBAT = tab("combat", CreativeCategory.COMBAT);
  public static final Identifier FARMING = tab("farming", CreativeCategory.FARMING);
  public static final Identifier MATERIALS = tab("materials", CreativeCategory.MATERIALS);

  private CreativeTabs() {}

  public static void init() {}

  private static Identifier tab(String name, CreativeCategory type) {
    return EnvProxy.get().createItemGroup(ProjectIds.id(name), new CategoryIcon(type));
  }
}
