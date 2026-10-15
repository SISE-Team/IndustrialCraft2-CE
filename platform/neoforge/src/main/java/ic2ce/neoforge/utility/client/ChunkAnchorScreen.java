package ic2ce.neoforge.utility.client;

import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.widget.ElectricalGauge;
import ic2ce.neoforge.runtime.SideServices;
import ic2ce.neoforge.utility.menu.ChunkAnchorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Bounded native chunk-map snapshots never request missing chunks and avoid repeated voxel sampling
 * per frame.
 */
public class ChunkAnchorScreen extends MachineScreen<ChunkAnchorMenu> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guichunkloader.png");
  private static final int SIDE = 9, PIXELS = 16, LEFT = 25, TOP = 16;
  private final Thumbnail[] thumbnails = new Thumbnail[SIDE * SIDE];
  private Level thumbnailWorld;
  private long center = Long.MIN_VALUE;
  private int cursor;
  private long sampledColumns;

  private static final class Thumbnail {
    final int[] colors = new int[PIXELS * PIXELS];
    LevelChunk source;
    long sampledAt = Long.MIN_VALUE;
  }

  public ChunkAnchorScreen(ChunkAnchorMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 250);
    for (int index = 0; index < thumbnails.length; index++) thumbnails[index] = new Thumbnail();
    addElement(ElectricalGauge.asBolt(this, 12, 125, menu.base));
  }

  void updateMap(boolean eager) {
    var world = menu.base.getLevel();
    if (world == null) return;
    var origin =
        new ChunkPos(menu.base.getBlockPos().getX() >> 4, menu.base.getBlockPos().getZ() >> 4);
    if (world != thumbnailWorld || center != origin.pack()) {
      thumbnailWorld = world;
      center = origin.pack();
      cursor = 0;
      for (var tile : thumbnails) {
        tile.source = null;
        tile.sampledAt = Long.MIN_VALUE;
        java.util.Arrays.fill(tile.colors, 0xff303030);
      }
      eager = true;
    }
    int limit = eager ? thumbnails.length : 3;
    for (int step = 0; step < limit; step++) {
      int index = cursor++ % thumbnails.length;
      var tile = thumbnails[index];
      int cx = origin.x() + index / SIDE - 4, cz = origin.z() + index % SIDE - 4;
      var available = world.getChunk(cx, cz, ChunkStatus.FULL, false);
      if (!(available instanceof LevelChunk chunk)) {
        if (tile.source != null) java.util.Arrays.fill(tile.colors, 0xff303030);
        tile.source = null;
        tile.sampledAt = world.getGameTime();
        continue;
      }
      long now = world.getGameTime();
      if (tile.source == chunk
          && tile.sampledAt != Long.MIN_VALUE
          && now >= tile.sampledAt
          && now - tile.sampledAt < 20) continue;
      sample(world, chunk, tile);
      tile.source = chunk;
      tile.sampledAt = now;
    }
  }

  private void sample(Level world, LevelChunk chunk, Thumbnail tile) {
    var position = new BlockPos.MutableBlockPos();
    var origin = chunk.getPos();
    for (int x = 0; x < PIXELS; x++)
      for (int z = 0; z < PIXELS; z++) {
        position.set(
            (origin.x() << 4) + x,
            chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z),
            (origin.z() << 4) + z);
        var state = chunk.getBlockState(position);
        if (state.isAir()) {
          position.move(net.minecraft.core.Direction.DOWN);
          state = chunk.getBlockState(position);
        }
        var color = state.getMapColor(world, position);
        tile.colors[x * PIXELS + z] = color == null ? 0xff303030 : 0xff000000 | color.col;
        sampledColumns++;
      }
  }

  @Override
  public void containerTick() {
    super.containerTick();
    updateMap(false);
  }

  @Override
  protected void drawForegroundLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    if (thumbnailWorld == null) updateMap(true);
    var origin =
        new ChunkPos(menu.base.getBlockPos().getX() >> 4, menu.base.getBlockPos().getZ() >> 4);
    var selected = menu.base.getLoadedChunks();
    int count = 0;
    for (int column = 0; column < SIDE; column++)
      for (int row = 0; row < SIDE; row++) {
        int x = LEFT + PIXELS * column, y = TOP + PIXELS * row;
        var tile = thumbnails[column * SIDE + row];
        for (int px = 0; px < PIXELS; px++)
          for (int pz = 0; pz < PIXELS; pz++)
            drawColoredRect(graphics, x + px, y + pz, 1, 1, tile.colors[px * PIXELS + pz]);
        boolean active =
            selected.contains(new ChunkPos(origin.x() + column - 4, origin.z() + row - 4).pack());
        drawColoredRect(graphics, x, y, PIXELS, PIXELS, active ? 805371648 : 822018048);
        if (active) count++;
      }
    drawTrimmedString(graphics, 8, 58, count + " / " + menu.base.getMaxChunks(), 15, 0x404040);
    super.drawForegroundLayer(graphics, mouseX, mouseY);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    double x = event.x() - leftPos - LEFT, y = event.y() - topPos - TOP;
    if (event.button() == 0 && x >= 0 && y >= 0 && x < SIDE * PIXELS && y < SIDE * PIXELS) {
      int dx = (int) x / PIXELS - 4, dz = (int) y / PIXELS - 4;
      SideServices
          .network(false)
          .initiateClientTileEntityEvent(menu.base, ((dx + 8) & 15) | (((dz + 8) & 15) << 4));
      return true;
    }
    return super.mouseClicked(event, doubleClick);
  }

  long sampledColumns() {
    return sampledColumns;
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }

  @Override
  public void removed() {
    super.removed();
    thumbnailWorld = null;
    for (var tile : thumbnails) tile.source = null;
  }
}
