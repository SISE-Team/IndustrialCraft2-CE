package ic2ce.neoforge.vehicle.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

/**
 * Native hull motion uses the project's texture; its separate water mask clips the occupied
 * surface.
 */
public final class HullRenderer extends AbstractBoatRenderer {
  private final BoatModel hull;
  private final Model.Simple waterMask;

  public HullRenderer(EntityRendererProvider.Context context, Identifier texture, boolean cargo) {
    super(context, texture);
    hull =
        new BoatModel(context.bakeLayer(cargo ? ModelLayers.OAK_CHEST_BOAT : ModelLayers.OAK_BOAT));
    waterMask =
        new Model.Simple(
            context.bakeLayer(ModelLayers.BOAT_WATER_PATCH), ignored -> RenderTypes.waterMask());
  }

  @Override
  protected EntityModel<BoatRenderState> model() {
    return hull;
  }

  @Override
  protected void submitTypeAdditions(
      BoatRenderState state,
      com.mojang.blaze3d.vertex.PoseStack pose,
      SubmitNodeCollector collector,
      int light) {
    if (!state.isUnderWater)
      collector.submitModel(
          waterMask,
          Unit.INSTANCE,
          pose,
          texture,
          light,
          OverlayTexture.NO_OVERLAY,
          state.outlineColor,
          null);
  }
}
