package ic2ce.neoforge.registry;


import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.registries.Registries;

public final class FluidGroups {
   public static final TagKey<Fluid> STEAM = create("c:steam", "forge:steam");

   public static void init() {
   }

   private static TagKey<Fluid> create(String fabricName, String forgeName) {
      Identifier id = Identifier.parse(forgeName);
      return TagKey.create(Registries.FLUID, id);
   }
}
