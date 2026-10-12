package ic2.core.entity.render;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.Identifier;

public class BoatEntityRenderer extends AbstractBoatRenderer {
   private final BoatModel model;

   public BoatEntityRenderer(EntityRendererProvider.Context ctx, Identifier texture, boolean chest) {
      super(ctx, texture);
      this.model = new BoatModel(ctx.bakeLayer(chest ? ModelLayers.OAK_CHEST_BOAT : ModelLayers.OAK_BOAT));
   }

   @Override
   protected EntityModel<BoatRenderState> model() {
      return this.model;
   }
}
