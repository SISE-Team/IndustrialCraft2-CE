package ic2.core.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.tile.IRotorProvider;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class KineticGeneratorRenderer<T extends BlockEntity & IRotorProvider>
   implements BlockEntityRenderer<T, KineticGeneratorRenderer.RotorRenderState> {
   private static final Int2ReferenceMap<ModelPart> rotorModels = new Int2ReferenceOpenHashMap<>();

   public static class RotorRenderState extends BlockEntityRenderState {
      public int diameter;
      public float angle;
      public Identifier texture = Identifier.withDefaultNamespace("missing");
      public Direction facing = Direction.NORTH;
      public int lightCoords;
   }

   public KineticGeneratorRenderer(Context ctx) {
   }

   @Override
   public RotorRenderState createRenderState() {
      return new RotorRenderState();
   }

   @Override
   public void extractRenderState(T be, RotorRenderState state, float partialTicks, Vec3 cameraPosition, @org.jetbrains.annotations.Nullable net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
      BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
      state.diameter = be.getRotorDiameter();
      state.angle = be.getAngle();
      state.texture = be.getRotorRenderTexture();
      state.facing = be.getFacing();
      state.lightCoords = LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos().relative(be.getFacing()));
   }

   private ModelPart getModel(int diameter) {
      ModelPart model = rotorModels.get(diameter);
      if (model == null) {
         MeshDefinition modelData = new MeshDefinition();
         PartDefinition modelPartData = modelData.getRoot();
         modelPartData.addOrReplaceChild(
            "1",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
            PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 0.0F, -0.5F, 0.0F)
         );
         modelPartData.addOrReplaceChild(
            "2",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
            PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 3.1F, 0.5F, 0.0F)
         );
         modelPartData.addOrReplaceChild(
            "3",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
            PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 4.7F, 0.0F, 0.5F)
         );
         modelPartData.addOrReplaceChild(
            "4",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
            PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 1.5F, 0.0F, -0.5F)
         );
         model = LayerDefinition.create(modelData, 32, 256).bakeRoot();
         rotorModels.put(diameter, model);
      }
      return model;
   }

   @Override
   public void submit(RotorRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
      if (state.diameter == 0) {
         return;
      }
      ModelPart model = this.getModel(state.diameter);

      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);
      switch (state.facing) {
         case NORTH:
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(-90.0F)));
            break;
         case EAST:
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(-180.0F)));
            break;
         case SOUTH:
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(-270.0F)));
            break;
         case UP:
            poseStack.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(-90.0F)));
         default:
      }

      poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(state.angle)));
      poseStack.translate(-0.2, 0.0, 0.0);
      submitNodeCollector.submitModelPart(
         model, poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.entitySolid(state.texture), state.lightCoords, OverlayTexture.NO_OVERLAY, null, false, false, -1, null, 0
      );
      poseStack.popPose();
   }
}
