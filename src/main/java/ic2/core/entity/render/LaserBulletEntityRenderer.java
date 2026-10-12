package ic2.core.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ic2.core.IC2;
import ic2.core.entity.LaserBulletEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class LaserBulletEntityRenderer extends EntityRenderer<LaserBulletEntity, LaserBulletEntityRenderer.State> {
   private static final Identifier TEXTURE = IC2.getIdentifier("textures/models/laser.png");

   public static class State extends EntityRenderState {
      public float yRot;
      public float xRot;
   }

   public LaserBulletEntityRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   @Override
   public State createRenderState() {
      return new State();
   }

   @Override
   public void extractRenderState(LaserBulletEntity entity, State state, float partialTicks) {
      super.extractRenderState(entity, state, partialTicks);
      state.yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F;
      state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
   }

   @Override
   public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
      poseStack.pushPose();
      poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(state.yRot)));
      poseStack.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(state.xRot)));
      poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(45.0F)));
      poseStack.scale(0.05625F, 0.05625F, 0.05625F);
      poseStack.translate(-4.0, 0.0, 0.0);
      int light = state.lightCoords;
      submitNodeCollector.submitCustomGeometry(
         poseStack,
         net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(TEXTURE),
         (pose, vertexConsumer) -> {
            Matrix4f matrix4f = pose.pose();
            Matrix3f matrix3f = pose.normal();
            this.vertex(matrix4f, matrix3f, vertexConsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, light);
            this.vertex(matrix4f, matrix3f, vertexConsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, light);
            this.vertex(matrix4f, matrix3f, vertexConsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, light);
            this.vertex(matrix4f, matrix3f, vertexConsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, light);
            this.vertex(matrix4f, matrix3f, vertexConsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, light);
            this.vertex(matrix4f, matrix3f, vertexConsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, light);
            this.vertex(matrix4f, matrix3f, vertexConsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, light);
            this.vertex(matrix4f, matrix3f, vertexConsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, light);
         }
      );

      for (int u = 0; u < 4; u++) {
         poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(90.0F)));
         submitNodeCollector.submitCustomGeometry(
            poseStack,
            net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(TEXTURE),
            (pose, vertexConsumer) -> {
               Matrix4f matrix4f = pose.pose();
               Matrix3f matrix3f = pose.normal();
               this.vertex(matrix4f, matrix3f, vertexConsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, light);
               this.vertex(matrix4f, matrix3f, vertexConsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, light);
               this.vertex(matrix4f, matrix3f, vertexConsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, light);
               this.vertex(matrix4f, matrix3f, vertexConsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, light);
            }
         );
      }

      poseStack.popPose();
      super.submit(state, poseStack, submitNodeCollector, camera);
   }

   public void vertex(
      Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer,
      int x, int y, int z, float u, float v, int normalX, int normalZ, int normalY, int light
   ) {
      vertexConsumer.addVertex(positionMatrix, x, y, z)
         .setColor(255, 255, 255, 255)
         .setUv(u, v)
         .setLight(light)
         .setNormal(normalX, normalY, normalZ);
   }
}
