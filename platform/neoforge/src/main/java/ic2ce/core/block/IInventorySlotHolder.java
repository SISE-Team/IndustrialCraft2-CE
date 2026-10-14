package ic2ce.core.block;

import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.block.NativeBlockEntity;
import net.minecraft.world.Container;

public interface IInventorySlotHolder<P extends NativeBlockEntity & Container> {
   P getParent();

   InventorySlot getInventorySlot(String var1);

   void addInventorySlot(InventorySlot var1);

   int getBaseIndex(InventorySlot var1);
}
