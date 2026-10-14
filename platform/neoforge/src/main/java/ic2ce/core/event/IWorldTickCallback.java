package ic2ce.core.event;

import net.minecraft.world.level.Level;

public interface IWorldTickCallback {
   void onTick(Level var1);
}
