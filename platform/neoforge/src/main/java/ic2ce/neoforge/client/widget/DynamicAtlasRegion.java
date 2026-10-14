package ic2ce.neoforge.client.widget;

/** A dynamic origin with validated fixed extent; overflow cannot wrap UV coordinates. */
public abstract class DynamicAtlasRegion implements AtlasRegion {
  private final int width, height;

  public DynamicAtlasRegion(int size) {
    this(size, size);
  }

  public DynamicAtlasRegion(int width, int height) {
    if (width < 0 || height < 0) throw new IllegalArgumentException("Negative overlay extent");
    this.width = width;
    this.height = height;
  }

  public final int getUE() {
    return Math.addExact(getUS(), width);
  }

  public final int getVE() {
    return Math.addExact(getVS(), height);
  }
}
