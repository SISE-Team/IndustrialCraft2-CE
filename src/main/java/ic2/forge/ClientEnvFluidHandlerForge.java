package ic2.forge;

import ic2.core.fluid.EnvFluidHandler;
import ic2.core.ref.Ic2Fluids;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public class ClientEnvFluidHandlerForge extends EnvFluidHandlerForge {
   private static final Map<Fluid, Identifier> STILL_SPRITES = new HashMap<>();
   private static final Map<Fluid, Identifier> FLOWING_SPRITES = new HashMap<>();
   private static final Map<Fluid, Integer> COLORS = new HashMap<>();
   private static boolean initialized = false;

   /**
    * Built lazily on first use: building in a static initializer caused a circular
    * class-init (Ic2Fluids -> FluidHandler -> this class -> Ic2Fluids mid-init).
    */
   private static synchronized void ensureInitialized() {
      if (initialized) {
         return;
      }
      for (Field field : Ic2Fluids.class.getDeclaredFields()) {
         if (!Modifier.isStatic(field.getModifiers()) || field.getType() != EnvFluidHandler.FluidRefs.class) {
            continue;
         }
         try {
            EnvFluidHandler.FluidRefs refs = (EnvFluidHandler.FluidRefs)field.get(null);
            if (refs == null) {
               continue;
            }
            if (refs.still != null) {
               STILL_SPRITES.put(refs.still, refs.stillSpriteId);
               FLOWING_SPRITES.put(refs.still, refs.flowingSpriteId != null ? refs.flowingSpriteId : refs.stillSpriteId);
               COLORS.put(refs.still, refs.color);
            }
            if (refs.flowing != null && refs.flowingSpriteId != null) {
               STILL_SPRITES.put(refs.flowing, refs.stillSpriteId);
               FLOWING_SPRITES.put(refs.flowing, refs.flowingSpriteId);
               COLORS.put(refs.flowing, refs.color);
            }
         } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
         }
      }
      initialized = true;
   }

   public static void registerModels(net.neoforged.neoforge.client.event.RegisterFluidModelsEvent event) {
      ensureInitialized();
      for (Map.Entry<Fluid, Identifier> entry : STILL_SPRITES.entrySet()) {
         Fluid fluid = entry.getKey();
         event.register(new net.minecraft.client.renderer.block.FluidModel.Unbaked(
            new net.minecraft.client.resources.model.sprite.Material(entry.getValue(), true),
            new net.minecraft.client.resources.model.sprite.Material(FLOWING_SPRITES.get(fluid), true),
            null, net.neoforged.neoforge.client.fluid.FluidTintSources.constant(COLORS.get(fluid))
         ), fluid);
      }
   }

   @Override
   public Identifier getStillSpriteId(Fluid fluid) {
      ensureInitialized();
      Identifier sprite = STILL_SPRITES.get(fluid);
      return sprite != null ? sprite : vanillaModel(fluid).stillMaterial().sprite().contents().name();
   }

   @Override
   public Identifier getFlowingSpriteId(Fluid fluid) {
      ensureInitialized();
      Identifier sprite = FLOWING_SPRITES.get(fluid);
      return sprite != null ? sprite : vanillaModel(fluid).flowingMaterial().sprite().contents().name();
   }

   @Override
   public int getColor(Fluid fluid) {
      ensureInitialized();
      Integer color = COLORS.get(fluid);
      if (color != null) return color;
      var tint = vanillaModel(fluid).fluidTintSource();
      return tint != null ? tint.color(fluid.defaultFluidState()) : -1;
   }

   private static net.minecraft.client.renderer.block.FluidModel vanillaModel(Fluid fluid) {
      return net.minecraft.client.Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
   }
}
