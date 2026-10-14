package ic2ce.core.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ic2ce.api.tile.IRotorProvider;
import java.util.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Rotor geometry belongs to the current renderer generation; fixed facing transforms reuse native
 * axis rotations.
 */
public class KineticGeneratorRenderer<T extends BlockEntity & IRotorProvider>
    implements BlockEntityRenderer<T, KineticGeneratorRenderer.RotorRenderState> {
  private final Map<Integer, ModelPart> geometry = new HashMap<>();

  public static class RotorRenderState extends BlockEntityRenderState {
    public int diameter, lightCoords;
    public float angle;
    public Identifier texture = Identifier.withDefaultNamespace("missing");
    public Direction facing = Direction.NORTH;
  }

  public KineticGeneratorRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public RotorRenderState createRenderState() {
    return new RotorRenderState();
  }

  @Override
  public void extractRenderState(
      T rotor,
      RotorRenderState state,
      float fraction,
      Vec3 camera,
      @org.jspecify.annotations.Nullable CrumblingOverlay damage) {
    BlockEntityRenderer.super.extractRenderState(rotor, state, fraction, camera, damage);
    state.diameter = Math.clamp(rotor.getRotorDiameter(), 0, 64);
    float angle = rotor.getAngle();
    state.angle = Float.isFinite(angle) ? angle % 360 : 0;
    var facing = rotor.getFacing();
    state.facing = facing == null ? Direction.NORTH : facing;
    var texture = rotor.getRotorRenderTexture();
    state.texture = texture == null ? Identifier.withDefaultNamespace("missing") : texture;
    state.lightCoords =
        rotor.getLevel() == null
            ? 0
            : LevelRenderer.getLightCoords(
                rotor.getLevel(), rotor.getBlockPos().relative(state.facing));
  }

  private ModelPart geometry(int diameter) {
    return geometry.computeIfAbsent(
        diameter,
        size -> {
          var mesh = new MeshDefinition();
          var root = mesh.getRoot();
          float[][] angles = {{0, -.5F, 0}, {3.1F, .5F, 0}, {4.7F, 0, .5F}, {1.5F, 0, -.5F}};
          for (int blade = 0; blade < angles.length; blade++) {
            var angle = angles[blade];
            root.addOrReplaceChild(
                "blade_" + blade,
                CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, -4, 1, size * 8, 8),
                PartPose.offsetAndRotation(-8, 0, 0, angle[0], angle[1], angle[2]));
          }
          return LayerDefinition.create(mesh, 32, 256).bakeRoot();
        });
  }

  @Override
  public void submit(
      RotorRenderState state,
      PoseStack pose,
      SubmitNodeCollector output,
      CameraRenderState camera) {
    if (state.diameter <= 0) return;
    pose.pushPose();
    try {
      pose.translate(.5, .5, .5);
      switch (state.facing) {
        case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(-90));
        case EAST -> pose.mulPose(Axis.YP.rotationDegrees(-180));
        case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(-270));
        case UP -> pose.mulPose(Axis.ZP.rotationDegrees(-90));
        default -> {}
      }
      pose.mulPose(Axis.XP.rotationDegrees(state.angle));
      pose.translate(-.2, 0, 0);
      output.submitModelPart(
          geometry(state.diameter),
          pose,
          net.minecraft.client.renderer.rendertype.RenderTypes.entitySolid(state.texture),
          state.lightCoords,
          OverlayTexture.NO_OVERLAY,
          null,
          false,
          false,
          -1,
          null,
          0);
    } finally {
      pose.popPose();
    }
  }
}
