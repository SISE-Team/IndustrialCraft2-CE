package ic2ce.neoforge.cable;

/**
 * Material data defines electrical ratings; geometry validates the actual supported insulation
 * depth.
 */
public enum ConductorMaterial {
  copper(1, 1, 0.25F, 0.2, 128),
  glass(0, 0, 0.25F, 0.025, 8192),
  gold(2, 1, 0.1875F, 0.4, 512),
  iron(3, 1, 0.375F, 0.8, 2048),
  tin(1, 1, 0.25F, 0.2, 32),
  detector(0, Integer.MAX_VALUE, 0.5F, 0.5, 8192),
  splitter(0, Integer.MAX_VALUE, 0.5F, 0.5, 8192);
  public static final float insulationThickness = .0625f;
  public static final ConductorMaterial[] values = values();
  public final int maxInsulation, minColoredInsulation, capacity;
  public final float thickness;
  public final double loss;

  ConductorMaterial(int maximum, int colorDepth, float width, double loss, int capacity) {
    if (maximum < 0
        || colorDepth < 0
        || !Float.isFinite(width)
        || width <= 0
        || !Double.isFinite(loss)
        || loss < 0
        || capacity <= 0) throw new IllegalArgumentException("Invalid conductor physical ratings");
    maxInsulation = maximum;
    minColoredInsulation = colorDepth;
    thickness = width;
    this.loss = loss;
    this.capacity = capacity;
  }

  public float getThickness(int depth) {
    if (depth < 0 || depth > maxInsulation)
      throw new IllegalArgumentException("Unsupported insulation depth: " + depth);
    return Math.fma(.125f, depth, thickness);
  }
}
