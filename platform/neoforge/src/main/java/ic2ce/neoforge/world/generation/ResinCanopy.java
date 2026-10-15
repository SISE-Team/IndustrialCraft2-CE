package ic2ce.neoforge.world.generation;

import com.mojang.serialization.Codec;
import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.runtime.EnvProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.*;

/**
 * Resin trees have a tall open cross canopy, height-dependent side arms and an independent thin
 * crown.
 */
public final class ResinCanopy extends FoliagePlacer {
  public static final ResinCanopy INSTANCE = new ResinCanopy();
  public static final Codec<ResinCanopy> CODEC =
      com.mojang.serialization.MapCodec.unitCodec(INSTANCE);
  public static final FoliagePlacerType<?> TYPE =
      EnvProxy.get().registerFoliagePlacer(
          ProjectIds.id("rubber_tree"), CODEC);

  private ResinCanopy() {
    super(ConstantInt.of(2), ConstantInt.of(0));
  }

  public static void init() {}

  @Override
  protected FoliagePlacerType<?> type() {
    return TYPE;
  }

  @Override
  protected void createFoliage(
      WorldGenLevel world,
      FoliageSetter leaves,
      RandomSource random,
      TreeConfiguration configuration,
      int trunkHeight,
      FoliageAttachment attachment,
      int crownHeight,
      int radius,
      int offset) {
    int stemHeight = Math.clamp(trunkHeight, 1, 64), spread = Math.clamp(radius, 0, 8);
    var center = attachment.pos();
    var cursor = new BlockPos.MutableBlockPos();
    int lowest = stemHeight < 4 ? 0 : stemHeight < 7 ? 2 : 3;
    for (int layer = lowest; layer < stemHeight; layer++) {
      int thinning = layer + 4 - stemHeight;
      for (int x = -spread; x <= spread; x++)
        for (int z = -spread; z <= spread; z++) {
          int ax = Math.abs(x), az = Math.abs(z);
          boolean core = ax <= 1 && az <= 1;
          boolean arm =
              !core && (ax <= 1 || az <= 1) && (thinning <= 1 || random.nextInt(thinning) == 0);
          if (core || arm) {
            cursor.setWithOffset(center, x, layer - stemHeight, z);
            tryPlaceLeaf(world, leaves, random, configuration, cursor);
          }
        }
    }
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++)
        if (x == 0 || z == 0) {
          cursor.setWithOffset(center, x, 0, z);
          tryPlaceLeaf(world, leaves, random, configuration, cursor);
        }
    for (int layer = 1; layer <= Math.clamp(crownHeight, 0, 16); layer++) {
      cursor.setWithOffset(center, 0, layer, 0);
      tryPlaceLeaf(world, leaves, random, configuration, cursor);
    }
  }

  @Override
  public int foliageHeight(RandomSource random, int trunkHeight, TreeConfiguration configuration) {
    return 1 + Math.clamp(trunkHeight, 0, 64) / 4 + random.nextInt(2);
  }

  @Override
  protected boolean shouldSkipLocation(
      RandomSource random, int x, int y, int z, int radius, boolean giantTrunk) {
    return false;
  }
}
