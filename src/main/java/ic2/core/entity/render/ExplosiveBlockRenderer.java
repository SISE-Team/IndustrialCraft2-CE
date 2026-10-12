package ic2.core.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ic2.api.entity.block.ExplosiveEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;

public class ExplosiveBlockRenderer extends EntityRenderer<ExplosiveEntity, TntRenderState> {
   private final net.minecraft.client.renderer.block.BlockModelResolver blockModelResolver;
   private final BlockDisplayContext blockDisplayContext = BlockDisplayContext.create();

   public ExplosiveBlockRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.shadowRadius = 0.5F;
      this.blockModelResolver = context.getBlockModelResolver();
   }

   @Override
   public TntRenderState createRenderState() {
      return new TntRenderState();
   }

   @Override
   public void extractRenderState(ExplosiveEntity entity, TntRenderState state, float partialTicks) {
      super.extractRenderState(entity, state, partialTicks);
      state.fuseRemainingInTicks = entity.getFuse() - partialTicks + 1.0F;
      this.blockModelResolver.update(state.blockState, entity.renderBlockState, this.blockDisplayContext);
   }

   @Override
   public void submit(TntRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
      poseStack.pushPose();
      poseStack.translate(0.0, 0.5, 0.0);
      float fuse = state.fuseRemainingInTicks;
      if (fuse < 10.0F) {
         float g = 1.0F - fuse / 10.0F;
         g = Mth.clamp(g, 0.0F, 1.0F);
         g *= g;
         g *= g;
         float s = 1.0F + g * 0.3F;
         poseStack.scale(s, s, s);
      }

      poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
      poseStack.translate(-0.5, -0.5, 0.5);
      poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
      TntMinecartRenderer.submitWhiteSolidBlock(
         state.blockState, poseStack, submitNodeCollector, state.lightCoords, (int)fuse / 5 % 2 == 0, state.outlineColor
      );
      poseStack.popPose();
      super.submit(state, poseStack, submitNodeCollector, camera);
   }
}
