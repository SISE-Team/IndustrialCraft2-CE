package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.neoforge.crafting.IndustrialWorkbench;
import ic2ce.neoforge.processing.ProcessingBlocks;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * A placed workbench's actual button, neighbor menu, JEI overlay and return RPC run through the
 * native client.
 */
public final class WorkbenchNavigationProbe {
  private record State(BlockPos source, BlockPos target, BlockPos menu, int containerId) {}

  private static volatile State state;
  private static volatile boolean finished;
  private static int serverTicks;
  private static final List<String> failures = new ArrayList<>();
  private static int checks;

  public static boolean serverTick(ServerPlayer actor, IndustrialWorkbench source) {
    if (state == null) {
      var target = source.getBlockPos().above();
      actor
          .level()
          .setBlockAndUpdate(target, ProcessingBlocks.ELECTRIC_FURNACE.defaultBlockState());
      state =
          new State(
              source.getBlockPos().immutable(),
              target.immutable(),
              source.getBlockPos().immutable(),
              actor.containerMenu.containerId);
    }
    var actual =
        actor.containerMenu instanceof ic2ce.neoforge.menu.OwnedMenu<?> menu
                && menu.base instanceof net.minecraft.world.level.block.entity.BlockEntity entity
            ? entity.getBlockPos().immutable()
            : null;
    var previous = state;
    state =
        new State(previous.source(), previous.target(), actual, actor.containerMenu.containerId);
    if (++serverTicks > 400 && !finished) {
      fail("actual workbench navigation timed out at client stage " + Client.stage);
      finish();
    }
    if (finished) {
      actor.level().removeBlock(state.target(), false);
      return true;
    }
    return false;
  }

  public static List<String> failures() {
    return List.copyOf(failures);
  }

  private static void fail(String text) {
    synchronized (failures) {
      failures.add(text);
    }
  }

  private static void check(boolean passed, String text) {
    checks++;
    if (!passed) fail(text);
  }

  private static void finish() {
    finished = true;
    try {
      Files.writeString(
          Path.of("ic2-workbench-navigation.json"),
          new GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  Map.of(
                      "complete",
                      failures.isEmpty(),
                      "finished",
                      true,
                      "checks",
                      checks,
                      "failures",
                      List.copyOf(failures),
                      "clientStage",
                      Client.stage,
                      "serverMenu",
                      String.valueOf(state.menu()),
                      "clientScreen",
                      Client.lastScreen)));
    } catch (Exception error) {
      throw new IllegalStateException(error);
    }
  }

  public static final class Client {
    private static int stage, age;
    private static String lastScreen = "unobserved";
    private static net.minecraft.world.inventory.AbstractContainerMenu neighborMenu;

    public static void tick() {
      var snapshot = state;
      if (snapshot == null || finished) return;
      var client = net.minecraft.client.Minecraft.getInstance();
      if (client.level == null || client.player == null) return;
      try {
        lastScreen = client.screen == null ? "none" : client.screen.getClass().getName();
        age++;
        switch (stage) {
          case 0 -> {
            if (!(client.screen instanceof ic2ce.neoforge.crafting.client.WorkbenchScreen source)
                || client.level.getBlockEntity(snapshot.target()) == null) return;
            boolean clicked =
                source.mouseClicked(
                    new net.minecraft.client.input.MouseButtonEvent(
                        source.getLeftPos() + 181,
                        source.getTopPos() + 11,
                        new net.minecraft.client.input.MouseButtonInfo(0, 0)),
                    false);
            check(clicked, "actual neighbor button rejected the native mouse action");
            stage = 1;
            age = 0;
          }
          case 1 -> {
            if (!atTarget(client.screen, snapshot.target())
                || !snapshot.target().equals(snapshot.menu())
                || age < 5) return;
            check(true, "actual adjacent machine menu opened");
            neighborMenu = client.player.containerMenu;
            ic2ce.neoforge.client.widget.RecipeOverlay.jeiRecipeListOpener
                .apply(new String[] {"ore_washing_plant"})
                .onClick(ic2ce.neoforge.client.widget.PointerButton.left);
            stage = 2;
            age = 0;
          }
          case 2 -> {
            if (age < 5) return;
            check(
                ic2ce.neoforge.crafting.client.WorkbenchScreen.jeiScreenRecipesGuiCheck.test(
                    client.screen),
                "neighbor recipe action did not retain the real JEI overlay");
            check(
                client.player.containerMenu == neighborMenu
                    && snapshot.target().equals(snapshot.menu()),
                "opening JEI prematurely returned to the workbench or changed the actual physical"
                    + " menu");
            client.screen.onClose();
            stage = 3;
            age = 0;
          }
          case 3 -> {
            if (age < 5) return;
            check(
                atTarget(client.screen, snapshot.target())
                    && client.player.containerMenu == neighborMenu,
                "closing actual JEI did not return to the adjacent physical machine");
            if (client.screen != null) client.screen.onClose();
            stage = 4;
            age = 0;
          }
          case 4 -> {
            if (!(client.screen instanceof ic2ce.neoforge.crafting.client.WorkbenchScreen source)
                || !source.getMenu().base.getBlockPos().equals(snapshot.source())
                || !snapshot.source().equals(snapshot.menu())
                || age < 5) return;
            check(
                client.player.containerMenu.containerId == snapshot.containerId(),
                "closing the neighbor did not reopen a synchronized real workbench menu");
            stage = 5;
            finish();
          }
          default -> {}
        }
      } catch (Throwable error) {
        fail("actual workbench navigation failed: " + error);
        finish();
      }
    }

    private static boolean atTarget(
        net.minecraft.client.gui.screens.Screen screen, BlockPos position) {
      return screen instanceof ic2ce.neoforge.client.screen.MachineScreen<?> machine
          && machine.getMenu().base
              instanceof net.minecraft.world.level.block.entity.BlockEntity entity
          && entity.getBlockPos().equals(position);
    }
  }
}
