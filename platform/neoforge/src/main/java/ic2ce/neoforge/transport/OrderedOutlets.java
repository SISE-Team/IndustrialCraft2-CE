package ic2ce.neoforge.transport;



import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;

public interface OrderedOutlets extends Container {
   Direction getFacing();

   List<Direction> getPriority();

   void updatePriority(boolean var1);
}
