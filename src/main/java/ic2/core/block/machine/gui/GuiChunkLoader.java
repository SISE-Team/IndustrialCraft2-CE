package ic2.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.gui.EnergyGauge;
import ic2.core.util.LogCategory;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.MapColor;

public class GuiChunkLoader extends Ic2Gui<ContainerChunkLoader> {
   private static final Identifier background = Identifier.fromNamespaceAndPath("ic2", "textures/gui/guichunkloader.png");

   public GuiChunkLoader(ContainerChunkLoader container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 250);
      this.addElement(EnergyGauge.asBolt(this, 12, 125, container.base));
   }

   @Override
   protected Identifier getTexture() {
      return background;
   }

   @Override
   protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      ChunkPos mainChunk = new ChunkPos(((ContainerChunkLoader)this.menu).base.getBlockPos().getX() >> 4, ((ContainerChunkLoader)this.menu).base.getBlockPos().getZ() >> 4);
      LongSet loadedChunks = ((ContainerChunkLoader)this.menu).base.getLoadedChunks();
      int amountLoadedChunks = 0;

      for (int i = -4; i <= 4; i++) {
         for (int j = -4; j <= 4; j++) {
            ChunkPos currentChunk = new ChunkPos(mainChunk.x() + i, mainChunk.z() + j);
            int xpos = -this.leftPos + 89 + 16 * i;
            int ypos = -this.topPos + 80 + 16 * j;
            this.drawChunkAt(graphics, xpos, ypos, currentChunk);
            if (loadedChunks.contains(currentChunk.pack())) {
               this.drawColoredRect(graphics, xpos, ypos, 16, 16, 805371648);
               amountLoadedChunks++;
            } else {
               this.drawColoredRect(graphics, xpos, ypos, 16, 16, 822018048);
            }
         }
      }

      this.drawTrimmedString(graphics, 8, 58, amountLoadedChunks + " / " + ((ContainerChunkLoader)this.menu).base.getMaxChunks(), 15, 4210752);
      super.drawForegroundLayer(graphics, mouseX, mouseY);
   }

   private void drawChunkAt(GuiGraphicsExtractor graphics, int x, int y, ChunkPos chunkPos) {
      Level world = ((ContainerChunkLoader)this.menu).base.getLevel();
      LevelChunk chunk = world.getChunk(chunkPos.x(), chunkPos.z());
      MutableBlockPos worldPos = new MutableBlockPos();

      for (int cx = 0; cx < 16; cx++) {
         for (int cz = 0; cz < 16; cz++) {
            worldPos.set(chunkPos.x() << 4 | cx, chunk.getHeight(Types.WORLD_SURFACE, cx, cz), chunkPos.z() << 4 | cz);
            BlockState state = chunk.getBlockState(worldPos);
            if (state.isAir()) {
               worldPos.move(Direction.DOWN);
               state = chunk.getBlockState(worldPos);
            }

            this.drawColoredRect(graphics, x + cx, y + cz, 1, 1, this.getColor(state, world, worldPos));
         }
      }
   }

   private int getColor(BlockState state, Level world, BlockPos pos) {
      MapColor color = state.getMapColor(world, pos);
      if (color == null) {
         IC2.log.error(LogCategory.General, "BlockState " + state + " does not have a MapColor set. Please report to the mod author of that mod.");
         return 0;
      } else {
         return color.col | 0xFF000000;
      }
   }

   @Override
   public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
      double mouseX = event.x();
      double mouseY = event.y();
      int mouseButton = event.button();
      if (mouseButton == 0) {
         ChunkPos mainChunk = new ChunkPos(((ContainerChunkLoader)this.menu).base.getBlockPos().getX() >> 4, ((ContainerChunkLoader)this.menu).base.getBlockPos().getZ() >> 4);

         for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
               if (mouseX - this.leftPos > 89 + 16 * dx
                  && mouseX - this.leftPos <= 89 + 16 * dx + 16
                  && mouseY - this.topPos > 80 + 16 * dy
                  && mouseY - this.topPos <= 80 + 16 * dy + 16) {
                  this.changeChunk(new ChunkPos(mainChunk.x() + dx, mainChunk.z() + dy));
                  return true;
               }
            }
         }
      }

      return super.mouseClicked(event, doubleClick);
   }

   private void changeChunk(ChunkPos chunk) {
      ChunkPos mainChunk = new ChunkPos(((ContainerChunkLoader)this.menu).base.getBlockPos().getX() >> 4, ((ContainerChunkLoader)this.menu).base.getBlockPos().getZ() >> 4);
      IC2.network
         .get(false)
         .initiateClientTileEntityEvent(
            ((ContainerChunkLoader)this.menu).base, chunk.x() - mainChunk.x() + 8 & 15 | (chunk.z() - mainChunk.z() + 8 & 15) << 4
         );
   }
}
