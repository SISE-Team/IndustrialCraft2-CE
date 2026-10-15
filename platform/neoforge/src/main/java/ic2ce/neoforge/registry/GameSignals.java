package ic2ce.neoforge.registry;


import ic2ce.neoforge.runtime.EnvProxy;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameSignals {
   public static final GameEvent TOOL_USE = register("tool_use");
   public static final GameEvent GENERATOR_ACTIVATE = register("generator_activate");
   public static final GameEvent GENERATOR_DEACTIVATE = register("generator_deactivate");
   public static final GameEvent MACHINE_ACTIVATE = register("machine_activate");
   public static final GameEvent MACHINE_DEACTIVATE = register("machine_deactivate");

   private static GameEvent register(String id) {
      return register(id, 16);
   }

   private static GameEvent register(String id, int range) {
      return EnvProxy.get().registerGameEvent(id, range);
   }

   public static void init() {
   }
}
