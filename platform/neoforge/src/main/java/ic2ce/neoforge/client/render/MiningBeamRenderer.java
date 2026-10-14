package ic2ce.neoforge.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ic2ce.neoforge.projectile.MiningBeam;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * One deferred geometry submission owns complete entity-format vertices for a native mining beam.
 */
public final class MiningBeamRenderer extends EntityRenderer<MiningBeam, MiningBeamRenderer.State> {
  private static final Identifier TEXTURE =
      ProjectRuntime.getIdentifier("textures/models/laser.png");

  public static final class State extends EntityRenderState {
    float yaw, pitch;
  }

  public MiningBeamRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(MiningBeam entity, State state, float partialTick) {
    super.extractRenderState(entity, state, partialTick);
    state.yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()) - 90;
    state.pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
  }

  @Override
  public void submit(
      State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
    poses.pushPose();
    poses.mulPose(Axis.YP.rotationDegrees(state.yaw));
    poses.mulPose(Axis.ZP.rotationDegrees(state.pitch));
    poses.mulPose(Axis.XP.rotationDegrees(45));
    poses.scale(.05625f, .05625f, .05625f);
    poses.translate(-4, 0, 0);
    int light = state.lightCoords;
    collector.submitCustomGeometry(
        poses,
        RenderTypes.entityCutout(TEXTURE),
        (pose, vertices) -> {
          // Both sides of the tip retain the original material's two cap regions.
          quad(
              pose, vertices, -7, -2, -2, -7, -2, 2, -7, 2, 2, -7, 2, -2, 0, .15625f, .15625f,
              .3125f, -1, 0, 0, light);
          quad(
              pose, vertices, -7, 2, -2, -7, 2, 2, -7, -2, 2, -7, -2, -2, 0, .15625f, .15625f,
              .3125f, 1, 0, 0, light);
          // Four windings expose the two perpendicular shaft planes to back-face culling.
          quad(
              pose, vertices, -8, -2, 0, 8, -2, 0, 8, 2, 0, -8, 2, 0, 0, 0, .5f, .15625f, 0, 0, 1,
              light);
          quad(
              pose, vertices, -8, 2, 0, 8, 2, 0, 8, -2, 0, -8, -2, 0, 0, 0, .5f, .15625f, 0, 0, -1,
              light);
          quad(
              pose, vertices, -8, 0, -2, 8, 0, -2, 8, 0, 2, -8, 0, 2, 0, 0, .5f, .15625f, 0, -1, 0,
              light);
          quad(
              pose, vertices, -8, 0, 2, 8, 0, 2, 8, 0, -2, -8, 0, -2, 0, 0, .5f, .15625f, 0, 1, 0,
              light);
        });
    poses.popPose();
    super.submit(state, poses, collector, camera);
  }

  private static void quad(
      PoseStack.Pose pose,
      VertexConsumer output,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float u1,
      float v1,
      float u2,
      float v2,
      float nx,
      float ny,
      float nz,
      int light) {
    vertex(pose, output, x1, y1, z1, u1, v1, nx, ny, nz, light);
    vertex(pose, output, x2, y2, z2, u2, v1, nx, ny, nz, light);
    vertex(pose, output, x3, y3, z3, u2, v2, nx, ny, nz, light);
    vertex(pose, output, x4, y4, z4, u1, v2, nx, ny, nz, light);
  }

  private static void vertex(
      PoseStack.Pose pose,
      VertexConsumer output,
      float x,
      float y,
      float z,
      float u,
      float v,
      float nx,
      float ny,
      float nz,
      int light) {
    output
        .addVertex(pose.pose(), x, y, z)
        .setColor(-1)
        .setUv(u, v)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(light)
        .setNormal(pose, nx, ny, nz);
  }
}
