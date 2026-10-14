package ic2ce.core.model;

import ic2ce.neoforge.world.surface.StateVariants;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Native model projection owns returned quad lists and shares canonical state text with world
 * persistence.
 */
public final class ModelUtil {
  private ModelUtil() {}

  public static String getVariant(BlockState state) {
    return StateVariants.getVariantString(state);
  }

  public static @Nullable List<BakedQuad> getQuads(
      BlockState state, @Nullable Direction side, RandomSource random) {
    var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    if (model == null) return null;
    var parts = new ArrayList<BlockStateModelPart>();
    model.collectParts(random, parts);
    if (parts.isEmpty()) return null;
    var result = new ArrayList<BakedQuad>();
    for (var part : parts) result.addAll(part.getQuads(side));
    return List.copyOf(result);
  }
}
