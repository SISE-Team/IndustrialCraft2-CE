package ic2ce.neoforge.crafting.client;

import ic2ce.core.IHasGui;
import ic2ce.core.util.StackUtil;
import ic2ce.neoforge.client.screen.MachineScreen;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.crafting.menu.WorkbenchMenu;
import ic2ce.neoforge.personal.OwnedDevice;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Neighbor navigation resolves current native identities on demand and owns a bounded
 * return-to-workbench session.
 */
public final class WorkbenchScreen extends MachineScreen<WorkbenchMenu> {
  public static Predicate<Screen> jeiScreenRecipesGuiCheck;
  private static ReturnSession returnSession;
  private static ReturnSession pendingReturn;
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/gui/guiindustrialworkbench.png");

  public WorkbenchScreen(WorkbenchMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 194, 228);
    addElement(
        new HoverRegion(this, 173, 3, 18, 108) {
          @Override
          protected boolean suppressTooltip(int x, int y) {
            return elements.stream()
                .anyMatch(widget -> widget != this && widget.isEnabled() && widget.contains(x, y));
          }
        }.withTooltip("ic2.IndustrialWorkbench.gui.adjacent"));
    for (var side : Direction.values()) {
      int row = Math.floorMod(side.get3DDataValue() + 5, 6);
      addElement(
          new AtlasButton(this, 173, 3 + 18 * row, 18, 18, button -> openNeighbor(side))
              .withEnableHandler(() -> neighbor(side) instanceof IHasGui)
              .withIcon(
                  () -> {
                    var block = neighbor(side);
                    if (block == null) return ItemStack.EMPTY;
                    return StackUtil.getPickStack(
                        block.getLevel(), block.getBlockPos(), block.getBlockState(), menu.player);
                  })
              .withTooltip(
                  () -> {
                    var block = neighbor(side);
                    if (!(block instanceof IHasGui)) return "";
                    return IHasGui.getBeName(block).getString()
                        + "\n"
                        + ChatFormatting.DARK_GRAY
                        + LocalizedText.translate(directionKey(side));
                  }));
    }
    addElement(
        new NativeButton(
                this,
                93,
                42,
                16,
                16,
                button -> ProjectRuntime.network.get(false).sendContainerEvent(menu, "clear"))
            .withTooltip("ic2.gui.clear"));
    addElement(
        ResourceImage.create(
            this, 94, 43, 14, 14, ScreenWidget.commonTexture, 256, 256, 210, 47, 224, 61));
  }

  private BlockEntity neighbor(Direction side) {
    var base = menu.base;
    return base.hasLevel() && !base.isRemoved()
        ? base.getLevel().getBlockEntity(base.getBlockPos().relative(side))
        : null;
  }

  private void openNeighbor(Direction side) {
    var target = neighbor(side);
    if (!(target instanceof IHasGui view) || target.isRemoved()) return;
    if (target instanceof OwnedDevice secured
        && !secured.permitsAccess(menu.player.getGameProfile())) {
      var owner = secured.getOwner();
      ProjectRuntime.sideProxy.messagePlayer(
          menu.player, "ic2.personal_chest.owner", owner == null ? "" : owner.name());
      return;
    }
    returnSession = new ReturnSession(menu.base, target);
    ProjectRuntime.network.get(false).requestGUI(view);
  }

  private static final class ReturnSession {
    final net.minecraft.world.level.Level world;
    final net.minecraft.core.BlockPos source, target;
    Screen watched;

    ReturnSession(ic2ce.neoforge.crafting.IndustrialWorkbench source, BlockEntity target) {
      world = source.getLevel();
      this.source = source.getBlockPos().immutable();
      this.target = target.getBlockPos().immutable();
    }
  }

  /** Opening identifies the incoming native screen before the old screen is removed. */
  public static void beforeOpening(Screen current, Screen next) {
    var session = returnSession;
    if (session == null || next == null) return;
    if (next instanceof MachineScreen<?> machine
        && machine.getMenu().base instanceof BlockEntity entity
        && entity.getLevel() == session.world
        && entity.getBlockPos().equals(session.target)) {
      session.watched = next;
      return;
    }
    if (current == session.watched
        && jeiScreenRecipesGuiCheck != null
        && jeiScreenRecipesGuiCheck.test(next)) {
      session.watched = next;
      return;
    }
    returnSession = null;
  }

  public static void onClosing(Screen current) {
    var session = returnSession;
    if (session == null) return;
    if (session.watched != current) return;
    returnSession = null;
    pendingReturn = session;
  }

  /**
   * Chunk delivery may replace the source block entity; resolve its current identity after native
   * closing has completed.
   */
  public static void afterClientTick() {
    var session = pendingReturn;
    pendingReturn = null;
    if (session == null) return;
    var client = Minecraft.getInstance();
    var world = client.level;
    if (world != null
        && client.screen == null
        && session.world == world
        && world.getBlockEntity(session.source)
            instanceof ic2ce.neoforge.crafting.IndustrialWorkbench source
        && !source.isRemoved()) ProjectRuntime.network.get(false).requestGUI(source);
  }

  public static void clearReturnSession() {
    returnSession = null;
    pendingReturn = null;
  }

  private static String directionKey(Direction side) {
    return switch (side) {
      case DOWN -> "ic2.dir.Bottom";
      case UP -> "ic2.dir.Top";
      case NORTH -> "ic2.dir.North";
      case SOUTH -> "ic2.dir.South";
      case WEST -> "ic2.dir.West";
      case EAST -> "ic2.dir.East";
    };
  }

  @Override
  protected Identifier getTexture() {
    return TEXTURE;
  }
}
