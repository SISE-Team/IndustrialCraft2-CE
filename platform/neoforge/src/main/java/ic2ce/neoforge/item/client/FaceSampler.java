package ic2ce.neoforge.item.client;

import ic2ce.neoforge.model.ModelUtil;
import ic2ce.core.proxy.SideProxyClient;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.item.tool.SurfaceImprinter;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.*;

/**
 * Physical client face geometry is sampled without putting rendering dependencies in common
 * execution paths.
 */
@OnlyIn(Dist.CLIENT)
public final class FaceSampler {
  private FaceSampler() {}

  public static boolean sample(
      ItemStack stack, Player owner, Level world, BlockPos pos, Direction side) {
    var state = world.getBlockState(pos);
    var info = renderInfo(state, side);
    if (info == null) return false;
    var variant = ModelUtil.getVariant(state);
    var colors = new int[info.tints.length];
    var mc = net.minecraft.client.Minecraft.getInstance();
    for (int index = 0; index < colors.length; index++) {
      var source =
          info.tints[index] < 0
              ? null
              : mc.getBlockColors().getTintSource(state, info.tints[index]);
      colors[index] =
          source == null
              ? -1
              : source.colorInWorld(
                  state, (net.minecraft.client.renderer.block.BlockAndTintGetter) world, pos);
    }
    var data = StackTag.get(stack);
    if (SurfaceImprinter.getState(data) == state
        && variant.equals(SurfaceImprinter.getVariant(data))
        && SurfaceImprinter.getSide(data) == side
        && Arrays.equals(SurfaceImprinter.getColorMultipliers(data), colors)) return false;
    ProjectRuntime.network
        .get(false)
        .sendPlayerItemData(
            owner,
            owner.getOffhandItem() == stack ? 40 : owner.getInventory().getSelectedSlot(),
            state.getBlock(),
            variant,
            side.ordinal(),
            colors,
            pos.immutable());
    return true;
  }

  public static SurfaceImprinter.ObscuredRenderInfo renderInfo(BlockState state, Direction side) {
    if (state == null || state.isAir() || side == null) return null;
    var quads = ModelUtil.getQuads(state, side, RandomSource.create(42));
    if (quads == null || quads.isEmpty() || quads.size() > 64) return null;
    var uvs = new float[quads.size() * 4];
    var tints = new int[quads.size()];
    var sprites = new net.minecraft.client.renderer.texture.TextureAtlasSprite[quads.size()];
    int accepted = 0;
    for (var quad : quads) {
      if (quad.materialInfo().layer().translucent()) return null;
      var data = SideProxyClient.envProxy.getQuadData(quad);
      var p = data.positions();
      if (p.length != 12 || quad.direction() != side) continue;
      int origin = -1;
      float edge = (side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0);
      for (int vertex = 0; vertex < 4; vertex++) {
        int offset = vertex * 3;
        float axis =
            switch (side.getAxis()) {
              case X -> p[offset];
              case Y -> p[offset + 1];
              case Z -> p[offset + 2];
            };
        if (Math.abs(axis - edge) > 1e-5) {
          origin = -1;
          break;
        }
        int a = (side.getAxis() == Direction.Axis.X ? 1 : 0),
            b = (side.getAxis() == Direction.Axis.Z ? 1 : 2);
        if (Math.abs(p[offset + a]) < 1e-5 && Math.abs(p[offset + b]) < 1e-5) origin = vertex;
      }
      if (origin < 0) continue;
      float[] in = data.uvs();
      int opposite = (origin + 2) & 3;
      int offset = accepted * 4;
      uvs[offset] = in[origin * 2];
      uvs[offset + 1] = in[origin * 2 + 1];
      uvs[offset + 2] = in[opposite * 2];
      uvs[offset + 3] = in[opposite * 2 + 1];
      tints[accepted] = data.tint();
      sprites[accepted] = data.sprite();
      accepted++;
    }
    return accepted == 0
        ? null
        : new SurfaceImprinter.ObscuredRenderInfo(
            Arrays.copyOf(uvs, accepted * 4),
            Arrays.copyOf(tints, accepted),
            Arrays.copyOf(sprites, accepted));
  }
}
