package ic2ce.neoforge.explosion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2ce.api.entity.block.ExplosiveEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/** Native explosive submission owns finite fuse scaling and always restores its pose. */
public final class ExplosiveBlockRenderer extends EntityRenderer<ExplosiveEntity, TntRenderState> {
  private final net.minecraft.client.renderer.block.BlockModelResolver resolver;
  private final BlockDisplayContext display = BlockDisplayContext.create();

  public ExplosiveBlockRenderer(EntityRendererProvider.Context context) {
    super(context);
    shadowRadius = .5f;
    resolver = context.getBlockModelResolver();
  }

  @Override
  public TntRenderState createRenderState() {
    return new TntRenderState();
  }

  @Override
  public void extractRenderState(ExplosiveEntity entity, TntRenderState state, float partialTick) {
    super.extractRenderState(entity, state, partialTick);
    state.fuseRemainingInTicks =
        entity.getFuse() - (Float.isFinite(partialTick) ? partialTick : 0) + 1;
    resolver.update(
        state.blockState,
        entity.renderBlockState == null
            ? net.minecraft.world.level.block.Blocks.TNT.defaultBlockState()
            : entity.renderBlockState,
        display);
  }

  static float fuseScale(float ticks) {
    if (!Float.isFinite(ticks) || ticks >= 10) return 1;
    float amount = Math.clamp(1 - ticks / 10, 0, 1);
    float square = amount * amount;
    return 1 + .3f * square * square;
  }

  @Override
  public void submit(
      TntRenderState state,
      PoseStack pose,
      SubmitNodeCollector collector,
      CameraRenderState camera) {
    pose.pushPose();
    try {
      pose.translate(0, .5, 0);
      float scale = fuseScale(state.fuseRemainingInTicks);
      pose.scale(scale, scale, scale);
      pose.translate(-.5, -.5, -.5);
      TntMinecartRenderer.submitWhiteSolidBlock(
          state.blockState,
          pose,
          collector,
          state.lightCoords,
          Math.floorDiv((int) Math.max(0, state.fuseRemainingInTicks), 5) % 2 == 0,
          state.outlineColor);
    } finally {
      pose.popPose();
    }
    super.submit(state, pose, collector, camera);
  }
}
