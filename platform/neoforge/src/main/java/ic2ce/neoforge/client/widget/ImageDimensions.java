package ic2ce.neoforge.client.widget;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.*;

/** Cached resource dimensions only. Native texture management owns upload and sampling. */
public class ImageDimensions implements Closeable {
  private static final Map<Identifier, Optional<ImageDimensions>> CACHE = new HashMap<>();
  private static ResourceManager registered;
  private final Identifier location;
  private int width, height;

  public ImageDimensions(Identifier location) {
    this.location = Objects.requireNonNull(location);
  }

  public static synchronized void init() {
    var resources = Minecraft.getInstance().getResourceManager();
    if (registered == resources) return;
    registered = resources;
    if (resources instanceof ReloadableResourceManager reloadable)
      reloadable.registerReloadListener(
          (ResourceManagerReloadListener)
              changed -> {
                synchronized (ImageDimensions.class) {
                  CACHE.clear();
                }
              });
  }

  public static synchronized ImageDimensions get(Identifier location) {
    Objects.requireNonNull(location);
    var known = CACHE.get(location);
    if (known != null) return known.orElse(null);
    return add(location, new ImageDimensions(location));
  }

  public static synchronized ImageDimensions add(Identifier location, ImageDimensions texture) {
    Objects.requireNonNull(location);
    Objects.requireNonNull(texture);
    try {
      texture.load(Minecraft.getInstance().getResourceManager());
      CACHE.put(location, Optional.of(texture));
      return texture;
    } catch (IOException error) {
      texture.close();
      CACHE.put(location, Optional.empty());
      return null;
    }
  }

  protected void load(ResourceManager resources) throws IOException {
    try (var input = resources.getResourceOrThrow(location).open();
        var image = NativeImage.read(input)) {
      dimensions(image.getWidth(), image.getHeight());
    }
  }

  protected void load(BufferedImage image) {
    Objects.requireNonNull(image);
    dimensions(image.getWidth(), image.getHeight());
  }

  private void dimensions(int width, int height) {
    if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid image extent");
    this.width = width;
    this.height = height;
  }

  @Override
  public void close() {
    width = height = 0;
  }

  /** Binding is performed by the screen extractor through the original resource identifier. */
  public void bind() {}

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getCanvasWidth() {
    return width;
  }

  public int getCanvasHeight() {
    return height;
  }
}
