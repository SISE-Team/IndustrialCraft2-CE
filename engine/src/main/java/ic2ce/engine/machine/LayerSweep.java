package ic2ce.engine.machine;

/** Checked row-major square layers; world coordinates and chunk access remain native concerns. */
public final class LayerSweep {
  public record Cell(int x, int depth, int z) {}

  private LayerSweep() {}

  private static int width(int radius) {
    if (radius < 0 || radius > 32) throw new IllegalArgumentException("Invalid mining radius");
    return radius * 2 + 1;
  }

  public static long index(int x, int depth, int z, int radius) {
    int width = width(radius);
    if (x < -radius || x > radius || z < -radius || z > radius || depth < 0)
      throw new IllegalArgumentException("Mining cursor outside its layers");
    return (long) depth * width * width + (long) (z + radius) * width + x + radius;
  }

  public static Cell cell(long index, int radius) {
    int width = width(radius);
    if (index < 0) throw new IllegalArgumentException("Negative mining cursor");
    long area = (long) width * width, depth = index / area;
    if (depth > Integer.MAX_VALUE) throw new IllegalArgumentException("Mining depth overflows");
    int offset = (int) (index % area);
    return new Cell(offset % width - radius, (int) depth, offset / width - radius);
  }
}
