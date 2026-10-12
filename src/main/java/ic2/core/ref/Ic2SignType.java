package ic2.core.ref;

import ic2.core.IC2;
import net.minecraft.world.level.block.state.properties.WoodType;

/** 26.1.2: WoodType is a record; custom types use the public constructor (oak sound set). */
public final class Ic2SignType {
   public static final WoodType RUBBER = IC2.envProxy.registerSignType("rubber");

   private Ic2SignType() {}
}
