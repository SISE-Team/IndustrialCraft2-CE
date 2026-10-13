package ic2.forge.model;

import ic2.core.block.comp.Obscuration;
import ic2.core.block.misc.WallBlock;
import ic2.core.block.tileentity.TileEntityWall;
import java.util.*;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.quad.BakedColors;

/** Prints captured face materials onto the wall's own cube geometry. */
public record ObscuredWallModel(BlockStateModel fallback, Map<BlockState, BlockStateModel> models) implements BlockStateModel {
   @Override public void collectParts(RandomSource random,List<BlockStateModelPart> output){fallback.collectParts(random,output);}
   @Override public Material.Baked particleMaterial(){return fallback.particleMaterial();}
   @Override public int materialFlags(){return fallback.materialFlags();}
   @Override public Object createGeometryKey(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random){var data=level.getModelData(pos).get(TileEntityWall.MODEL_STATE);return data==null?fallback:data;}
   @Override public void collectParts(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> output){
      var data=level.getModelData(pos).get(TileEntityWall.MODEL_STATE);if(data==null){fallback.collectParts(random,output);return;}
      var base=models.getOrDefault(WallBlock.get(data.color).defaultBlockState(),fallback);var parts=new ArrayList<BlockStateModelPart>();base.collectParts(random,parts);
      var faces=new EnumMap<Direction,List<BakedQuad>>(Direction.class);int flags=0;
      for(Direction face:Direction.values()){
         var result=new ArrayList<BakedQuad>();var printed=data.obscurations==null?null:data.obscurations[face.ordinal()];
         var refs=new ArrayList<BakedQuad>();
         if(printed!=null){var ref=models.get(printed.state);if(ref!=null){var refParts=new ArrayList<BlockStateModelPart>();ref.collectParts(random,refParts);for(var part:refParts)refs.addAll(part.getQuads(printed.side));}}
         for(var part:parts)for(var quad:part.getQuads(face)){
            if(refs.isEmpty()){result.add(quad);continue;}
            var tints=refs.stream().mapToInt(q->q.materialInfo().tintIndex()).distinct().sorted().toArray();
            for(var ref:refs){var material=ref.materialInfo();int tint=Arrays.binarySearch(tints,material.tintIndex());int color=printed.colorMultipliers==null||printed.colorMultipliers.length==0?-1:printed.colorMultipliers[Math.min(Math.max(tint,0),printed.colorMultipliers.length-1)];
               var colored=BakedColors.of(multiply(ref.bakedColors().color(0),color),multiply(ref.bakedColors().color(1),color),multiply(ref.bakedColors().color(2),color),multiply(ref.bakedColors().color(3),color));
               var mapped=new BakedQuad.MaterialInfo(material.sprite(),material.layer(),material.itemRenderType(),-1,material.shade(),material.lightEmission(),material.ambientOcclusion());
               result.add(new BakedQuad(quad.position0(),quad.position1(),quad.position2(),quad.position3(),ref.packedUV0(),ref.packedUV1(),ref.packedUV2(),ref.packedUV3(),face,mapped,quad.bakedNormals(),colored));
            }
         }
         for(var q:result)flags|=q.materialInfo().flags();faces.put(face,List.copyOf(result));
      }
      final int allFlags=flags;var particle=base.particleMaterial();output.add(new BlockStateModelPart(){public List<BakedQuad> getQuads(Direction face){return face==null?List.of():faces.getOrDefault(face,List.of());}public boolean useAmbientOcclusion(){return true;}public Material.Baked particleMaterial(){return particle;}public int materialFlags(){return allFlags;}});
   }
   @Override public int materialFlags(BlockAndTintGetter level,BlockPos pos,BlockState state){var data=level.getModelData(pos).get(TileEntityWall.MODEL_STATE);int flags=fallback.materialFlags();if(data!=null&&data.obscurations!=null)for(var face:data.obscurations)if(face!=null&&models.get(face.state)!=null)flags|=models.get(face.state).materialFlags();return flags;}
   private static int multiply(int base,int tint){return (base&0xff000000)|(((base>>16&255)*(tint>>16&255)/255)<<16)|(((base>>8&255)*(tint>>8&255)/255)<<8)|((base&255)*(tint&255)/255);}
}
