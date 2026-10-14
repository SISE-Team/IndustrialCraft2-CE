package ic2ce.api.tile;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public interface IRotorProvider {
   int getRotorDiameter();

   Direction getFacing();

   float getAngle();

   Identifier getRotorRenderTexture();
}
