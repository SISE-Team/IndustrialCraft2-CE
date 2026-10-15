package ic2ce.neoforge.registry;


import ic2ce.neoforge.runtime.EnvProxy;
import net.minecraft.world.level.block.state.properties.WoodType;

/** 26.1.2: WoodType is a record; custom types use the public constructor (oak sound set). */
public final class SignDefinitions {
   public static final WoodType RUBBER = EnvProxy.get().registerSignType("rubber");

   private SignDefinitions() {}
}
