package ic2ce.neoforge.registry;


import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.Registries;

public class EntityGroups {
   public static final TagKey<EntityType<?>> SCAFFOLD_CLIMBABLE = create("scaffold_climbable");

   public static void init() {
   }

   private static TagKey<EntityType<?>> create(String name) {
      return TagKey.create(Registries.ENTITY_TYPE, ProjectRuntime.getIdentifier(name));
   }

   private static TagKey<EntityType<?>> create(String fabricName, String forgeName) {
      Identifier id = Identifier.parse(forgeName);
      return TagKey.create(Registries.ENTITY_TYPE, id);
   }
}
