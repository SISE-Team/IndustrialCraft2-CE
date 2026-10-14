package ic2ce.neoforge.model;

import ic2ce.neoforge.world.block.ConstructionWall;
import ic2ce.neoforge.world.block.ImprintedWall;
import ic2ce.neoforge.world.surface.SurfaceMap;
import java.util.*;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.quad.BakedColors;

/** An owned model generation projects each captured material onto the corresponding wall face. */
public record ObscuredWallModel(BlockStateModel fallback, Map<BlockState, BlockStateModel> models)
    implements BlockStateModel {
  private static final Direction[] FACES = Direction.values();

  public ObscuredWallModel {
    Objects.requireNonNull(fallback);
    models = Map.copyOf(models);
  }

  @Override
  public void collectParts(RandomSource random, List<BlockStateModelPart> result) {
    fallback.collectParts(random, result);
  }

  @Override
  public Material.Baked particleMaterial() {
    return fallback.particleMaterial();
  }

  @Override
  public int materialFlags() {
    return fallback.materialFlags();
  }

  private BlockStateModel base(ImprintedWall.WallRenderState state) {
    return models.getOrDefault(ConstructionWall.get(state.color).defaultBlockState(), fallback);
  }

  @Override
  public Object createGeometryKey(
      BlockAndTintGetter world, BlockPos position, BlockState state, RandomSource random) {
    var data = world.getModelData(position).get(ImprintedWall.MODEL_STATE);
    if (data == null) return fallback.createGeometryKey(world, position, state, random);
    var base = base(data);
    var baseKey = base.createGeometryKey(world, position, state, random);
    if (baseKey == null) return null;
    var keys = new ArrayList<Object>(7);
    keys.add(baseKey);
    if (data.obscurations != null)
      for (var face : data.obscurations)
        if (face != null) {
          var reference = models.get(face.state);
          if (reference != null) {
            var key = reference.createGeometryKey(world, position, face.state, random);
            if (key == null) return null;
            keys.add(key);
          }
        }
    return new GeometryKey(
        new ImprintedWall.WallRenderState(data.color, data.obscurations), List.copyOf(keys));
  }

  private record GeometryKey(ImprintedWall.WallRenderState surface, List<Object> nativeKeys) {}

  @Override
  public void collectParts(
      BlockAndTintGetter world,
      BlockPos position,
      BlockState state,
      RandomSource random,
      List<BlockStateModelPart> result) {
    var data = world.getModelData(position).get(ImprintedWall.MODEL_STATE);
    if (data == null) {
      fallback.collectParts(world, position, state, random, result);
      return;
    }
    var base = base(data);
    var parts = new ArrayList<BlockStateModelPart>();
    base.collectParts(world, position, state, random, parts);
    var faces = new EnumMap<Direction, List<BakedQuad>>(Direction.class);
    int flags = 0;
    for (var face : FACES) {
      var printed = data.obscurations == null ? null : data.obscurations[face.ordinal()];
      var refs = references(printed, random);
      int[] tintIndices =
          refs.stream().mapToInt(q -> q.materialInfo().tintIndex()).distinct().sorted().toArray();
      var quads = new ArrayList<BakedQuad>();
      for (var part : parts)
        for (var quad : part.getQuads(face)) {
          if (refs.isEmpty()) {
            quads.add(quad);
            continue;
          }
          for (var reference : refs) {
            int index = Arrays.binarySearch(tintIndices, reference.materialInfo().tintIndex());
            int color =
                printed.colorMultipliers[Math.clamp(index, 0, printed.colorMultipliers.length - 1)];
            quads.add(project(quad, reference, face, color));
          }
        }
      for (var quad : quads) flags |= quad.materialInfo().flags();
      faces.put(face, List.copyOf(quads));
    }
    result.add(new FacePart(Map.copyOf(faces), base.particleMaterial(), flags));
  }

  private List<BakedQuad> references(SurfaceMap.ObscurationData data, RandomSource random) {
    if (data == null) return List.of();
    var model = models.get(data.state);
    if (model == null) return List.of();
    var parts = new ArrayList<BlockStateModelPart>();
    model.collectParts(random, parts);
    var result = new ArrayList<BakedQuad>();
    for (var part : parts) result.addAll(part.getQuads(data.side));
    return result;
  }

  private static BakedQuad project(BakedQuad cube, BakedQuad reference, Direction face, int tint) {
    var source = reference.materialInfo();
    var material =
        new BakedQuad.MaterialInfo(
            source.sprite(),
            source.layer(),
            source.itemRenderType(),
            -1,
            source.shade(),
            source.lightEmission(),
            source.ambientOcclusion());
    var colors = reference.bakedColors();
    var baked =
        BakedColors.of(
            multiply(colors.color(0), tint),
            multiply(colors.color(1), tint),
            multiply(colors.color(2), tint),
            multiply(colors.color(3), tint));
    return new BakedQuad(
        cube.position0(),
        cube.position1(),
        cube.position2(),
        cube.position3(),
        reference.packedUV0(),
        reference.packedUV1(),
        reference.packedUV2(),
        reference.packedUV3(),
        face,
        material,
        cube.bakedNormals(),
        baked);
  }

  private record FacePart(
      Map<Direction, List<BakedQuad>> faces, Material.Baked particleMaterial, int materialFlags)
      implements BlockStateModelPart {
    @Override
    public List<BakedQuad> getQuads(Direction face) {
      return face == null ? List.of() : faces.getOrDefault(face, List.of());
    }

    @Override
    public boolean useAmbientOcclusion() {
      return true;
    }
  }

  @Override
  public int materialFlags(BlockAndTintGetter world, BlockPos position, BlockState state) {
    var data = world.getModelData(position).get(ImprintedWall.MODEL_STATE);
    if (data == null) return fallback.materialFlags(world, position, state);
    int flags = base(data).materialFlags();
    if (data.obscurations != null)
      for (var face : data.obscurations)
        if (face != null) {
          var reference = models.get(face.state);
          if (reference != null) flags |= reference.materialFlags();
        }
    return flags;
  }

  static int multiply(int base, int tint) {
    int red = ((base >>> 16) & 255) * ((tint >>> 16) & 255) / 255;
    int green = ((base >>> 8) & 255) * ((tint >>> 8) & 255) / 255;
    int blue = (base & 255) * (tint & 255) / 255;
    return (base & 0xff000000) | (red << 16) | (green << 8) | blue;
  }
}
