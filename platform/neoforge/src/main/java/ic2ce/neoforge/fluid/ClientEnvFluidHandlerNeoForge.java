package ic2ce.neoforge.fluid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

/**
 * Fluid visuals come from the registration ledger, without reflecting the content declaration
 * class.
 */
public class ClientEnvFluidHandlerNeoForge extends EnvFluidHandlerNeoForge {
  public static void registerModels(RegisterFluidModelsEvent event) {
    for (var entry : fluidMaterials.entrySet()) {
      var definition = entry.getValue();
      event.register(
          new FluidModel.Unbaked(
              new Material(definition.stillSpriteId, true),
              new Material(
                  definition.flowingSpriteId == null
                      ? definition.stillSpriteId
                      : definition.flowingSpriteId,
                  true),
              null,
              FluidTintSources.constant(definition.color)),
          entry.getKey());
    }
  }

  @Override
  public Identifier getStillSpriteId(Fluid fluid) {
    var definition = fluidMaterials.get(fluid);
    return definition == null
        ? model(fluid).stillMaterial().sprite().contents().name()
        : definition.stillSpriteId;
  }

  @Override
  public Identifier getFlowingSpriteId(Fluid fluid) {
    var definition = fluidMaterials.get(fluid);
    return definition == null
        ? model(fluid).flowingMaterial().sprite().contents().name()
        : definition.flowingSpriteId == null
            ? definition.stillSpriteId
            : definition.flowingSpriteId;
  }

  @Override
  public int getColor(Fluid fluid) {
    var definition = fluidMaterials.get(fluid);
    if (definition != null) return definition.color;
    var tint = model(fluid).fluidTintSource();
    return tint == null ? -1 : tint.color(fluid.defaultFluidState());
  }

  private static FluidModel model(Fluid fluid) {
    return Minecraft.getInstance()
        .getModelManager()
        .getFluidStateModelSet()
        .get(fluid.defaultFluidState());
  }
}
