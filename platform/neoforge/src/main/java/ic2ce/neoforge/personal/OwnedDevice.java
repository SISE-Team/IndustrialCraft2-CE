package ic2ce.neoforge.personal;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.Container;

public interface OwnedDevice {
   boolean permitsAccess(GameProfile var1);

   Container getPrivilegedInventory(GameProfile var1);

   GameProfile getOwner();

   void setOwner(GameProfile var1);
}
