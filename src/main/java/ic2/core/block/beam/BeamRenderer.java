package ic2.core.block.beam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BeamRenderer extends EntityRenderer<ParticleEntity, BeamRenderer.BeamState> {
   private static final Identifier texture = Identifier.fromNamespaceAndPath("ic2", "textures/models/beam.png");

   public static class BeamState extends EntityRenderState {
   }

   public BeamRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   @Override
   public BeamState createRenderState() {
      return new BeamState();
   }

   @Override
   public void submit(BeamState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
      float u1 = 0.0F;
      float u2 = 1.0F;
      float v1 = 0.0F;
      float v2 = 1.0F;
      float scale = 0.1F;
      int light = state.lightCoords;
      poseStack.pushPose();
      poseStack.mulPose(camera.orientation);
      submitNodeCollector.submitCustomGeometry(
         poseStack,
         net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentCullItemTarget(texture),
         (pose, vertexConsumer) -> this.drawQuad(pose, vertexConsumer, scale, u1, u2, v1, v2, light)
      );
      poseStack.popPose();
      super.submit(state, poseStack, submitNodeCollector, camera);
   }

   private void drawQuad(Pose entry, VertexConsumer vertexConsumer, float scale, float u1, float u2, float v1, float v2, int light) {
      Matrix4f positionMatrix = entry.pose();
      Matrix3f normalMatrix = entry.normal();

      for (int i = 0; i < 4; i++) {
         float x;
         float u;
         if (i < 2) {
            x = -scale;
            u = u1;
         } else {
            x = scale;
            u = u2;
         }

         float y;
         float v;
         if (i != 0 && i != 3) {
            y = scale;
            v = v2;
         } else {
            y = -scale;
            v = v1;
         }

         vertexConsumer.addVertex(positionMatrix, x, y, 0.0F)
            .setColor(255, 255, 255, 255)
            .setUv(u, v)
            .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(0.0F, 1.0F, 0.0F);
      }
   }
}
