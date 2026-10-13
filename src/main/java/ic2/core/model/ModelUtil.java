package ic2.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 26.1.2 block model helpers. The old BakedModel/BlockModelShaper API is gone;
 * quads are collected through the render-state model parts instead.
 */
public class ModelUtil {
   public static String getVariant(BlockState state) {
      StringBuilder sb = new StringBuilder();
      for (net.minecraft.world.level.block.state.properties.Property.Value<?> value : state.getValues().toList()) {
         if (sb.length() > 0) {
            sb.append(',');
         }

         sb.append(value.property().getName()).append('=').append(((net.minecraft.world.level.block.state.properties.Property)value.property()).getName(value.value()));
      }

      return sb.toString();
   }

   @Nullable
   public static List<BakedQuad> getQuads(BlockState state, @Nullable Direction side, RandomSource rand) {
      Minecraft mc = Minecraft.getInstance();
      List<BlockStateModelPart> parts = new ArrayList<>();
      mc.getModelManager().getBlockStateModelSet().get(state).collectParts(rand, parts);
      if (parts.isEmpty()) {
         return null;
      } else {
         List<BakedQuad> quads = new ArrayList<>();
         for (BlockStateModelPart part : parts) {
            quads.addAll(part.getQuads(side));
         }

         return quads;
      }
   }
}
