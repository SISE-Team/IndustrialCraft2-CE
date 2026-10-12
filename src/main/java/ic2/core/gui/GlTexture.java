package ic2.core.gui;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.util.profiling.ProfilerFiller;
import org.lwjgl.opengl.GL11;

public class GlTexture implements Closeable {
   private static final Map<Identifier, GlTexture> textures = new HashMap<>();
   private final Identifier loc;
   protected int textureId;
   protected int width;
   protected int height;
   protected int canvasWidth;
   protected int canvasHeight;

   public static void init() {
      ResourceManager manager = Minecraft.getInstance().getResourceManager();
      if (manager instanceof ReloadableResourceManager) {
         ((ReloadableResourceManager)manager)
            .registerReloadListener(
               (net.minecraft.server.packs.resources.ResourceManagerReloadListener)managerx -> {
                  for (GlTexture texture : GlTexture.textures.values()) {
                     if (texture != null) {
                        texture.close();
                     }
                  }

                  GlTexture.textures.clear();
               }
            );
      } else {
         IC2.log.warn(LogCategory.General, "The resource manager {} is not reloadable.", manager);
      }
   }

   public static GlTexture get(Identifier identifier) {
      GlTexture ret = textures.get(identifier);
      return ret != null ? ret : add(identifier, new GlTexture(identifier));
   }

   public static GlTexture add(Identifier identifier, GlTexture texture) {
      try {
         texture.load(Minecraft.getInstance().getResourceManager());
      } catch (IOException var3) {
         IC2.log.warn(LogCategory.General, "Can't load texture %s", identifier);
         texture.close();
         texture = null;
      }

      textures.put(identifier, texture);
      return texture;
   }

   public GlTexture(Identifier loc) {
      this.loc = loc;
   }

   protected void load(ResourceManager manager) throws IOException {
      Resource resource = manager.getResourceOrThrow(this.loc);

      try (InputStream is = resource.open()) {
         this.load(ImageIO.read(is));
      }
   }

   protected void load(BufferedImage img) {
      this.width = img.getWidth();
      this.height = img.getHeight();
      this.canvasWidth = Integer.highestOneBit((this.width - 1) * 2);
      this.canvasHeight = Integer.highestOneBit((this.height - 1) * 2);
      // 26.1.2: raw GL texture upload removed; GlTexture now only carries dimensions
      // for UV math (GuiGraphicsExtractor.blit binds textures itself).
      this.textureId = 1;
   }

   @Override
   public void close() {
      this.textureId = 0;
   }

   public void bind() {
      // no-op in 26.1.2 (kept for API compatibility)
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getCanvasWidth() {
      return this.canvasWidth;
   }

   public int getCanvasHeight() {
      return this.canvasHeight;
   }
}
