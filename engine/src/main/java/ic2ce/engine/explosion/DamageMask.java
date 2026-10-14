package ic2ce.engine.explosion;

/**
 * Sparse vertical layers, 32 two-bit damage flags per long, with monotone vaporization dominance.
 */
public final class DamageMask {
  @FunctionalInterface
  public interface CellConsumer {
    void accept(int x, int y, int z, boolean vaporized);
  }

  private final int minX, minY, minZ, width, depth, height, cells;
  private final long[][] layers;

  public DamageMask(int minX, int minY, int minZ, int width, int height, int depth) {
    if (width < 1 || height < 1 || depth < 1)
      throw new IllegalArgumentException("Empty damage region");
    this.minX = minX;
    this.minY = minY;
    this.minZ = minZ;
    this.width = width;
    this.depth = depth;
    this.height = height;
    cells = Math.multiplyExact(width, depth);
    layers = new long[height][];
  }

  public boolean contains(int x, int y, int z) {
    return (long) x - minX >= 0
        && (long) x - minX < width
        && (long) y - minY >= 0
        && (long) y - minY < height
        && (long) z - minZ >= 0
        && (long) z - minZ < depth;
  }

  public boolean mark(int x, int y, int z, boolean vaporized) {
    if (!contains(x, y, z)) return false;
    int row = y - minY, cell = (z - minZ) * width + x - minX;
    long[] layer = layers[row];
    if (layer == null) layers[row] = layer = new long[(int) (((long) cells + 31) / 32)];
    int word = cell >>> 5, shift = (cell & 31) * 2;
    long before = layer[word], after = before | ((vaporized ? 3L : 1L) << shift);
    layer[word] = after;
    return after != before;
  }

  public void forEach(CellConsumer consumer) {
    java.util.Objects.requireNonNull(consumer);
    for (int row = 0; row < height; row++) {
      long[] layer = layers[row];
      if (layer == null) continue;
      for (int word = 0; word < layer.length; word++) {
        long bits = layer[word];
        while (bits != 0) {
          int shift = Long.numberOfTrailingZeros(bits) & ~1;
          int cell = (word << 5) + (shift >>> 1);
          consumer.accept(
              minX + cell % width, minY + row, minZ + cell / width, ((bits >>> shift) & 3) == 3);
          bits &= ~(3L << shift);
        }
      }
    }
  }

  public void clear() {
    java.util.Arrays.fill(layers, null);
  }

  public long allocatedBytes() {
    long bytes = 0;
    for (var layer : layers) if (layer != null) bytes += (long) layer.length * Long.BYTES;
    return bytes;
  }
}
