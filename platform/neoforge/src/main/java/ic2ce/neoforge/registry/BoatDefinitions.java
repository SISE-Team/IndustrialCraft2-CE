package ic2ce.neoforge.registry;


import ic2ce.api.entity.boat.BoatType;

public class BoatDefinitions {
   public static final BoatType RUBBER = BoatType.register(null, "boat_rubber");
   public static final BoatType ELECTRIC = BoatType.register(null, "boat_electric");
   public static final BoatType CARBON = BoatType.register(null, "boat_carbon");

   public static void init() {
   }
}
