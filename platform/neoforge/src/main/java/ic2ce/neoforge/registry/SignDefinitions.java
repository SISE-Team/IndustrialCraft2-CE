package ic2ce.neoforge.registry;


import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.world.level.block.state.properties.WoodType;

/** 26.1.2: WoodType is a record; custom types use the public constructor (oak sound set). */
public final class SignDefinitions {
   public static final WoodType RUBBER = ProjectRuntime.envProxy.registerSignType("rubber");

   private SignDefinitions() {}
}
