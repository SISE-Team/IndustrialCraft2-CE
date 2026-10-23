package ic2ce.neoforge.personal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.personal.PrivateChest;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** The baked case stays stationary; its door swings outward around a vertical left hinge. */
public final class PrivateChestRenderer
    implements BlockEntityRenderer<PrivateChest, PrivateChestRenderer.State> {
  private static final Identifier TEXTURE = ProjectIds.id("textures/models/newsafe.png");
  private final ModelPart door = createDoor();

  public static final class State extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public float openness;
  }

  public PrivateChestRenderer(BlockEntityRendererProvider.Context context) {}

  static ModelPart createDoor() {
    var mesh = new MeshDefinition();
    // Use the original IC2 safe atlas directly; no generated door texture is required.
    mesh.getRoot().addOrReplaceChild(
        "door", CubeListBuilder.create().texOffs(64, 0).addBox(0, 0, 0, 24, 28, 2), PartPose.ZERO);
    return LayerDefinition.create(mesh, 128, 128).bakeRoot();
  }

  static void poseDoor(PoseStack pose, Direction facing, float openness) {
    float yaw = switch (facing) {
      case EAST -> -90;
      case SOUTH -> 180;
      case WEST -> 90;
      default -> 0;
    };
    pose.translate(.5, 0, .5);
    pose.mulPose(Axis.YP.rotationDegrees(yaw));
    pose.translate(-.5, 0, -.5);
    pose.translate(2 / 16f, 1 / 16f, 1 / 16f);
    float fraction = Float.isFinite(openness) ? Math.clamp(openness, 0, 1) : 0;
    float remaining = 1 - fraction;
    pose.mulPose(Axis.YP.rotationDegrees(100 * (1 - remaining * remaining * remaining)));
    pose.scale(.5F, .5F, .5F);
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(
      PrivateChest chest, State state, float fraction, Vec3 camera,
      @Nullable CrumblingOverlay damage) {
    BlockEntityRenderer.super.extractRenderState(chest, state, fraction, camera, damage);
    state.facing = chest.getFacing();
    state.openness = chest.getLidAngle(fraction);
    if (chest.getLevel() != null) {
      state.lightCoords = LevelRenderer.getLightCoords(
          chest.getLevel(), chest.getBlockPos().relative(state.facing));
    }
  }

  @Override
  public void submit(State state, PoseStack pose, SubmitNodeCollector output, CameraRenderState camera) {
    pose.pushPose();
    try {
      poseDoor(pose, state.facing, state.openness);
      output.submitModelPart(door, pose, RenderTypes.entitySolid(TEXTURE), state.lightCoords,
          OverlayTexture.NO_OVERLAY, null, false, false, -1, state.breakProgress, 0);
    } finally {
      pose.popPose();
    }
  }
}
